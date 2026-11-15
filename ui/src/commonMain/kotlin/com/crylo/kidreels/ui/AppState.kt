package com.crylo.kidreels.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.crylo.kidreels.core.CheckIn
import com.crylo.kidreels.core.Feed
import com.crylo.kidreels.core.Outcome
import com.crylo.kidreels.core.Progress
import com.crylo.kidreels.core.Reel
import com.crylo.kidreels.core.Settings
import com.crylo.kidreels.core.SettingsStore
import com.crylo.kidreels.core.TimeBudget
import com.crylo.kidreels.core.TimeUp
import com.crylo.kidreels.core.Txt

enum class Screen { ONBOARDING, FEED, SETTINGS }

/** Everything the app holds while it runs. Platform shells create one and pass it to [KidReelsApp]. */
class AppState(val host: Host) {
    private val settingsStore = SettingsStore(host.store)
    val progress = Progress(host.store)
    val budget = TimeBudget(host.store, host.clock)

    var settings by mutableStateOf(settingsStore.load())
        private set

    var screen by mutableStateOf(if (settings.onboarded) Screen.FEED else Screen.ONBOARDING)

    /** Set while the parent gate is up; run once it is passed. */
    var gateAction by mutableStateOf<(() -> Unit)?>(null)
        private set

    var checkInOpen by mutableStateOf(false)

    /** Whether the app is on screen. Only on-screen time counts toward the daily limit. */
    var foreground by mutableStateOf(true)

    val feed = FeedModel(this)

    init {
        host.speaker.setSlow(settings.slowSpeech)
    }

    fun text(t: Txt): String = t[settings.main]

    val checkInDue: Boolean get() = CheckIn.isDue(settings, host.clock.today())

    fun update(next: Settings) {
        val before = settings
        val today = host.clock.today()
        val s = if (next.age != before.age) next.copy(ageSetDay = today) else next
        if (s.age != before.age) progress.resetSkills()
        settings = s
        settingsStore.save(s)
        host.speaker.setSlow(s.slowSpeech)
        // Reels already made for the old age or languages would be wrong now.
        if (s.age != before.age || s.langs != before.langs) feed.dropAhead()
    }

    fun finishOnboarding(s: Settings) {
        update(s.copy(onboarded = true, ageSetDay = host.clock.today()))
        screen = Screen.FEED
    }

    fun requestGate(action: () -> Unit) {
        gateAction = action
    }

    fun gatePassed() {
        val action = gateAction
        gateAction = null
        action?.invoke()
    }

    fun gateCancelled() {
        gateAction = null
    }

    /** Android's back button. Returns false when the app should close. */
    fun onBack(): Boolean = when {
        gateAction != null -> true.also { gateAction = null }
        checkInOpen -> true.also { checkInOpen = false }
        screen == Screen.SETTINGS -> true.also { screen = Screen.FEED }
        else -> false
    }
}

/** What happened on one reel; kept outside the pager so it survives the page being recycled. */
class ReelState {
    var solved by mutableStateOf(false)
    var wrongTries by mutableIntStateOf(0)
    val wrong = mutableStateListOf<Int>()
    var liked by mutableStateOf(false)

    /** Bumped on every like, to send up the hearts. */
    var hearts by mutableIntStateOf(0)

    /** Bumped on every right answer, to restart the celebration. */
    var celebrate by mutableIntStateOf(0)
}

class FeedModel(private val app: AppState) {
    private val engine = Feed({ app.settings }, app.progress)
    private val states = mutableStateMapOf<Long, ReelState>()

    val reels = mutableStateListOf<Reel>()
    var timeUp by mutableStateOf(false)
        private set
    var remainingMillis by mutableStateOf<Long?>(null)
        private set

    /** Bumped when a puzzle is swiped past unsolved, to show the sad face. */
    var skippedPuzzle by mutableIntStateOf(0)
        private set

    fun state(reel: Reel): ReelState = states.getOrPut(reel.id) { ReelState() }

    /** Keeps two reels ready below [current], so the next swipe never waits. */
    fun fill(current: Int) {
        if (reels.isEmpty()) refreshBudget()
        if (timeUp) {
            if (reels.lastOrNull() !is TimeUp) reels += engine.timeUp()
            return
        }
        while (reels.size < current + AHEAD + 1) reels += engine.next()
    }

    /** The page on screen, kept by the feed screen. */
    var current by mutableIntStateOf(0)

    /** Throws away the reels made ahead of [current]: they were made for old settings. */
    fun dropAhead() {
        if (reels.size > current + 1) reels.removeRange(current + 1, reels.size)
        if (reels.isNotEmpty()) fill(current)
    }

    /** Called when the child moves off [reel]. */
    fun left(reel: Reel, forward: Boolean) {
        val s = state(reel)
        if (!reel.needsSolving || s.solved) return
        if (forward) {
            engine.record(reel, Outcome.SKIPPED)
            skippedPuzzle++
            app.host.play(Sfx.SAD)
        }
    }

    fun solved(reel: Reel) {
        val s = state(reel)
        if (s.solved) return
        s.solved = true
        s.celebrate++
        engine.record(reel, if (s.wrongTries == 0) Outcome.SOLVED_FIRST_TRY else Outcome.SOLVED)
    }

    fun like(reel: Reel) {
        val s = state(reel)
        s.liked = !s.liked
        if (s.liked) {
            s.hearts++
            app.progress.like(reel.topic)
            app.host.play(Sfx.POP)
        }
    }

    /** One second of feed time has passed. Returns true when the limit was just reached. */
    fun tick(millis: Long, current: Int): Boolean {
        if (timeUp) return false
        app.budget.add(millis)
        refreshBudget()
        if (timeUp) {
            // The child finishes the reel they are on; the goodbye reel is the next one.
            if (reels.size > current + 1) reels.removeRange(current + 1, reels.size)
            reels += engine.timeUp()
            return true
        }
        return false
    }

    /** A grown-up gave more time from the goodbye reel. */
    fun addTime(minutes: Int) {
        app.budget.addBonus(minutes)
        refreshBudget()
        reels.removeAll { it is TimeUp }
        fill(current)
    }

    private fun refreshBudget() {
        val left = app.budget.remainingMillis(app.settings.dailyMinutes)
        remainingMillis = left
        timeUp = left == 0L
        engine.windDown = left != null && left <= TimeBudget.WIND_DOWN_MILLIS
    }

    /** Settings may have changed the limit. */
    fun onSettingsClosed() {
        val wasUp = timeUp
        refreshBudget()
        if (wasUp && !timeUp) {
            reels.removeAll { it is TimeUp }
            fill(current)
        }
    }

    companion object {
        const val AHEAD = 2
    }
}
