package com.crylo.kidreels.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.crylo.kidreels.core.CheckIn
import com.crylo.kidreels.core.Feed
import com.crylo.kidreels.core.History
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.Outcome
import com.crylo.kidreels.core.Phrases
import com.crylo.kidreels.core.Profile
import com.crylo.kidreels.core.Profiles
import com.crylo.kidreels.core.Progress
import com.crylo.kidreels.core.Reel
import com.crylo.kidreels.core.Settings
import com.crylo.kidreels.core.SettingsStore
import com.crylo.kidreels.core.TimeBudget
import com.crylo.kidreels.core.TimeUp
import com.crylo.kidreels.core.Topic
import com.crylo.kidreels.core.Txt

enum class Screen { ONBOARDING, PICKER, FEED, SETTINGS }

/**
 * One child's settings, progress, time and feed. Kept while the app runs, so switching
 * back to a child finds the reel they left, mid-puzzle.
 */
class Child(app: AppState, val id: Int) {
    private val store = app.profiles.storeOf(id)
    val settingsStore = SettingsStore(store)
    val progress = Progress(store)
    val budget = TimeBudget(store, app.host.clock)
    val history = History(store, app.host.clock)

    var settings by mutableStateOf(settingsStore.load())
    var profile by mutableStateOf(app.profiles.profile(id))
    val feed = FeedModel(app, this)
}

/** Everything the app holds while it runs. Platform shells create one and pass it to [KidReelsApp]. */
class AppState(val host: Host) {
    val profiles = Profiles(host.store)
    private val children = mutableMapOf<Int, Child>()

    private fun childOf(id: Int): Child = children.getOrPut(id) { Child(this, id) }

    /** The child whose feed and settings are showing. */
    var child by mutableStateOf(childOf(profiles.last))
        private set

    /** Every child on the phone, for "Who's playing?" and settings. */
    var all by mutableStateOf(profiles.all())
        private set

    val settings: Settings get() = child.settings
    val progress: Progress get() = child.progress
    val budget: TimeBudget get() = child.budget
    val feed: FeedModel get() = child.feed

    var screen by mutableStateOf(
        when {
            !settings.onboarded -> Screen.ONBOARDING
            profiles.count > 1 -> Screen.PICKER
            else -> Screen.FEED
        },
    )

    /** Whether a feed has been shown this run: back from "Who's playing?" then returns to it. */
    private var played = screen == Screen.FEED

    /** "Who's playing?" opened by a grown-up from the feed: any child may be picked. */
    var pickerUnlocked by mutableStateOf(false)
        private set

    /** The child being added (0) or edited, over settings; null when neither. */
    var editing by mutableStateOf<Int?>(null)

    /** Set while the parent gate is up; run once it is passed. */
    var gateAction by mutableStateOf<(() -> Unit)?>(null)
        private set

    var checkInOpen by mutableStateOf(false)

    /** The "no voice for this language" card, opened past the gate from the feed's banner. */
    var voiceOpen by mutableStateOf<Lang?>(null)

    private var voiceSnoozes by mutableIntStateOf(0)

    /**
     * A language this child's reels speak that has no voice on the phone, for the feed to
     * tell a grown-up about: otherwise those reels are silent and nobody knows why, as
     * the same note in settings is only seen by a parent who goes looking. "Not now"
     * keeps it quiet for a week, for a phone whose engine has no such voice at all.
     */
    val missingVoice: Lang?
        get() {
            voiceSnoozes
            val speaker = host.speaker
            if (!speaker.hasEngine) return null
            val today = host.clock.today()
            return settings.langs.firstOrNull { lang ->
                val later = profiles.device.get(VOICE_LATER + lang.tag)?.toLongOrNull()
                !speaker.ready(lang) && (later == null || today - later >= VOICE_SNOOZE_DAYS)
            }
        }

    fun voiceLater(lang: Lang) {
        profiles.device.put(VOICE_LATER + lang.tag, host.clock.today().toString())
        voiceSnoozes++
        voiceOpen = null
    }

    private var visible by mutableStateOf(true)
    private var awaySince: Long? = null

    /**
     * Whether the app is on screen. Only on-screen time counts toward the daily limit.
     * Back after [Profiles.ASK_AGAIN_MILLIS] away, with more than one child, the app asks
     * who is playing: the phone may have changed hands.
     */
    var foreground: Boolean
        get() = visible
        set(on) {
            if (on == visible) return
            visible = on
            val now = host.clock.nowMillis()
            if (!on) {
                awaySince = now
                return
            }
            val away = awaySince ?: return
            awaySince = null
            // Bedtime may have come, or morning, while the app was away.
            feed.recheck()
            if (now - away >= Profiles.ASK_AGAIN_MILLIS && profiles.count > 1 && screen == Screen.FEED && gateAction == null) {
                checkInOpen = false
                voiceOpen = null
                pickerUnlocked = false
                screen = Screen.PICKER
            }
        }

    init {
        host.speaker.setSlow(settings.slowSpeech)
    }

    fun text(t: Txt): String = t[settings.main]

    val checkInDue: Boolean get() = CheckIn.isDue(settings, host.clock.today())

    fun update(next: Settings) {
        val c = child
        val before = c.settings
        val today = host.clock.today()
        val s = if (next.age != before.age) next.copy(ageSetDay = today) else next
        if (s.age != before.age) c.progress.resetSkills()
        c.settings = s
        c.settingsStore.save(s)
        host.speaker.setSlow(s.slowSpeech)
        // Reels already made for the old age or languages would be wrong now, including the
        // one on screen: a 2-year-old should not come back from settings to a times table.
        if (s.age != before.age || s.langs != before.langs) c.feed.remake()
    }

    fun finishOnboarding(s: Settings, p: Profile) {
        saveProfile(p.copy(id = child.id))
        update(s.copy(onboarded = true, ageSetDay = host.clock.today()))
        played = true
        screen = Screen.FEED
    }

    /** Makes [id] the child whose feed and settings show, without leaving the screen. */
    fun select(id: Int) {
        if (id !in profiles.ids()) return
        profiles.last = id
        val c = childOf(id)
        if (c === child) return
        child = c
        host.speaker.setSlow(c.settings.slowSpeech)
        // A new day may have started since this child last played.
        c.feed.recheck()
    }

    /** A child picked themselves at "Who's playing?". */
    fun choose(id: Int) {
        select(id)
        child.feed.greet(Phrases.hi(child.profile.label(settings.main)))
        pickerUnlocked = false
        played = true
        screen = Screen.FEED
    }

    /** From the feed, past the gate. */
    fun openPicker() {
        pickerUnlocked = true
        screen = Screen.PICKER
    }

    fun needsGate(id: Int): Boolean = !pickerUnlocked && profiles.needsGate(id, host.clock)

    fun timeUp(id: Int): Boolean = profiles.timeUp(id, host.clock)

    fun saveProfile(p: Profile) {
        profiles.save(p)
        children[p.id]?.profile = profiles.profile(p.id)
        all = profiles.all()
    }

    /**
     * A new child, starting from the current child's languages, look and limit: siblings
     * usually share them, and the parent can change them once the child is picked.
     */
    fun addChild(p: Profile, age: Int) {
        val s = settings.copy(onboarded = true, age = age, ageSetDay = host.clock.today())
        val id = profiles.add(p, s)
        all = profiles.all()
        select(id)
    }

    fun removeChild(id: Int) {
        if (profiles.count <= 1) return
        if (child.id == id) select(profiles.ids().first { it != id })
        profiles.remove(id)
        children.remove(id)
        all = profiles.all()
    }

    private companion object {
        const val VOICE_LATER = "voiceLater."
        const val VOICE_SNOOZE_DAYS = 7
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
        voiceOpen != null -> true.also { voiceOpen = null }
        editing != null -> true.also { editing = null }
        screen == Screen.SETTINGS -> true.also {
            screen = Screen.FEED
            feed.recheck()
        }
        // Back to the child who was playing: they are never the one the gate guards.
        screen == Screen.PICKER && played -> true.also {
            pickerUnlocked = false
            screen = Screen.FEED
        }
        else -> false
    }
}

/** What happened on one reel; kept outside the pager so it survives the page being recycled. */
class ReelState {
    var solved by mutableStateOf(false)
    var wrongTries by mutableIntStateOf(0)
    val wrong = mutableStateListOf<Int>()

    /** Drag games: which piece is in which place (piece index to target index). */
    val placed = mutableStateMapOf<Int, Int>()
    var liked by mutableStateOf(false)

    /** Bumped on every like, to send up the hearts. */
    var hearts by mutableIntStateOf(0)

    /** Bumped on every right answer, to restart the celebration. */
    var celebrate by mutableIntStateOf(0)
}

class FeedModel(private val app: AppState, private val child: Child) {
    private val engine = Feed({ child.settings }, child.progress)
    private val states = mutableStateMapOf<Long, ReelState>()

    val reels = mutableStateListOf<Reel>()
    var timeUp by mutableStateOf(false)
        private set
    var remainingMillis by mutableStateOf<Long?>(null)
        private set

    /** The last couple of minutes before the limit or bedtime: easy reels and an "almost done" chip. */
    var windingDown by mutableStateOf(false)
        private set

    /** Why [timeUp]: past bedtime rather than out of minutes. */
    private var bedtime = false

    /**
     * The topic a parent picked to show alone, for as long as the app runs: it is not
     * saved, so the next start is the usual mix. Changing it remakes the reel on screen.
     */
    var focus by mutableStateOf<Topic?>(null)
        private set

    fun pick(topic: Topic?) {
        if (topic == focus) return
        focus = topic
        engine.focus = topic
        remake()
    }

    private var greeting: Txt? = null

    /** Said before the next reel: "Hi, Meera!" once a child has picked themselves. */
    fun greet(hello: Txt) {
        greeting = hello
    }

    fun takeGreeting(): Txt? = greeting.also { greeting = null }

    /** Bumped when a puzzle is swiped past unsolved, to show the sad face. */
    var skippedPuzzle by mutableIntStateOf(0)
        private set

    fun state(reel: Reel): ReelState = states.getOrPut(reel.id) { ReelState() }

    /** Keeps two reels ready below [current], so the next swipe never waits. */
    fun fill(current: Int) {
        if (reels.isEmpty()) refreshBudget()
        if (timeUp) {
            if (reels.lastOrNull() !is TimeUp) reels += engine.timeUp(bedtime)
            return
        }
        while (reels.size < current + AHEAD + 1) reels += engine.next()
    }

    /** The page on screen, kept by the feed screen. */
    var current by mutableIntStateOf(0)

    /**
     * Replaces the reel on screen and the ones made ahead of it: they were made for old
     * settings. Reels behind stay, so swiping back still finds what the child saw.
     */
    fun remake() {
        if (reels.isEmpty()) return
        val at = current.coerceAtMost(reels.lastIndex)
        reels.removeRange(at, reels.size)
        fill(at)
    }

    /** Called when the child moves off [reel]. */
    fun left(reel: Reel, forward: Boolean) {
        val s = state(reel)
        if (!reel.needsSolving || s.solved) return
        if (forward) {
            engine.record(reel, Outcome.SKIPPED)
            child.history.record(reel.topic, Outcome.SKIPPED)
            skippedPuzzle++
            app.host.play(Sfx.SAD)
        }
    }

    fun solved(reel: Reel) {
        val s = state(reel)
        if (s.solved) return
        s.solved = true
        s.celebrate++
        val outcome = if (s.wrongTries == 0) Outcome.SOLVED_FIRST_TRY else Outcome.SOLVED
        engine.record(reel, outcome)
        child.history.record(reel.topic, outcome)
    }

    fun like(reel: Reel) {
        val s = state(reel)
        s.liked = !s.liked
        if (s.liked) {
            s.hearts++
            child.progress.like(reel.topic)
            child.history.like(reel.topic)
            app.host.play(Sfx.POP)
        }
    }

    /** One second of feed time has passed. Returns true when the limit was just reached. */
    fun tick(millis: Long, current: Int): Boolean {
        if (timeUp) return false
        child.budget.add(millis)
        child.history.addTime(millis)
        refreshBudget()
        if (timeUp) {
            goodbyeAfter(current)
            return true
        }
        return false
    }

    /** The child finishes the reel they are on; the goodbye reel is the next one. */
    private fun goodbyeAfter(current: Int) {
        if (reels.size > current + 1) reels.removeRange(current + 1, reels.size)
        reels += engine.timeUp(bedtime)
    }

    /** A grown-up gave more time from the goodbye reel: past bedtime, before bed; otherwise on the limit. */
    fun addTime(minutes: Int) {
        if (bedtime) child.budget.addBedtimeMinutes(minutes)
        if (remainingMillis == 0L) child.budget.addBonus(minutes)
        refreshBudget()
        reels.removeAll { it is TimeUp }
        fill(current)
    }

    private fun refreshBudget() {
        val s = child.settings
        val left = child.budget.remainingMillis(s.dailyMinutes)
        remainingMillis = left
        bedtime = child.budget.isBedtime(s.bedtime)
        timeUp = left == 0L || bedtime
        val toBed = child.budget.minutesToBedtime(s.bedtime)
        windingDown = !timeUp &&
            ((left != null && left <= TimeBudget.WIND_DOWN_MILLIS) || (toBed != null && toBed * TimeBudget.MINUTE <= TimeBudget.WIND_DOWN_MILLIS))
        engine.windDown = windingDown
    }

    /** Settings may have changed the limit or bedtime, or the clock may have passed one. */
    fun recheck() {
        val wasUp = timeUp
        refreshBudget()
        if (wasUp && !timeUp) {
            reels.removeAll { it is TimeUp }
            fill(current)
        } else if (!wasUp && timeUp && reels.isNotEmpty()) {
            // A bedtime set for a time already past, or reached while the app was away.
            goodbyeAfter(current)
        }
    }

    companion object {
        const val AHEAD = 2
    }
}
