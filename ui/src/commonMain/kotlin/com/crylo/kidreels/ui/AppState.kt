package com.crylo.kidreels.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.crylo.kidreels.core.CheckIn
import com.crylo.kidreels.core.Feed
import com.crylo.kidreels.core.FunDays
import com.crylo.kidreels.core.TuneReel
import com.crylo.kidreels.core.isFun
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
 * Where a grown-up is inside settings. The first page lists the children and what is the
 * phone's; each child has a page of their own, and each of its rows opens a [Part].
 */
sealed interface Page {
    data object Home : Page

    data object NewChild : Page

    /** The language of these screens: the phone's, so it sits on the first page, not a child's. */
    data object AppLanguage : Page

    data class OfChild(val id: Int) : Page

    data class Part(val id: Int, val part: ChildPart) : Page
}

/** One thing about a child, on a page of its own. [PROFILE] is who they are: name, animal and age. */
enum class ChildPart { PROFILE, LIMIT, BEDTIME, TOPICS, LANGUAGES, LOOK, FUN }

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

    /** Which day of play this is, for automatic fun reels. */
    val funDays = FunDays(store)

    var settings by mutableStateOf(settingsStore.load())
    var profile by mutableStateOf(app.profiles.profile(id))

    /** Bumped when the day of play is started again, so the fun reels row reads it afresh. */
    var funRestarts by mutableIntStateOf(0)
    val feed = FeedModel(app, this)
}

/** Everything the app holds while it runs. Platform shells create one and pass it to [KidReelsApp]. */
class AppState(val host: Host) {
    val profiles = Profiles(host.store)
    private val children = mutableMapOf<Int, Child>()

    /** Any child's settings, progress and feed: settings pages show children who are not playing. */
    fun childOf(id: Int): Child = children.getOrPut(id) { Child(this, id) }

    /** The child whose feed and settings are showing. */
    var child by mutableStateOf(childOf(profiles.last))
        private set

    /** Every child on the phone, for "Who's playing?" and settings. */
    var all by mutableStateOf(profiles.all())
        private set

    val settings: Settings get() = child.settings

    /**
     * The language of every screen a grown-up reads: settings, setup, the gate, the notes
     * for grown-ups. The phone's ([Profiles.uiLang]), apart from the children's languages:
     * what a reel shows and says, and what is said to a child, stays in those.
     */
    var uiLang by mutableStateOf(profiles.uiLang)
        private set

    fun changeUiLang(lang: Lang) {
        profiles.uiLang = lang
        uiLang = lang
    }
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

    /** The settings page showing, while [screen] is [Screen.SETTINGS]. */
    var page by mutableStateOf<Page>(Page.Home)

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

    /** The last crash, kept by the platform shell for a parent to copy from settings. */
    var crashReport by mutableStateOf(profiles.device.get(CRASH))
        private set

    fun clearCrash() {
        profiles.device.put(CRASH, null)
        crashReport = null
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

    /** A grown-up's text, in [uiLang]. */
    fun text(t: Txt): String = t[uiLang]

    val checkInDue: Boolean get() = checkInDue(child)

    fun checkInDue(c: Child): Boolean = CheckIn.isDue(c.settings, host.clock.today())

    /** From the feed's gear, past the gate. */
    fun openSettings() {
        page = Page.Home
        screen = Screen.SETTINGS
    }

    fun closeSettings() {
        screen = Screen.FEED
        feed.recheck()
    }

    /** Settings' back arrow and Android's back: one page up. */
    fun pageBack() {
        page = when (val p = page) {
            is Page.Part -> Page.OfChild(p.id)
            is Page.OfChild, Page.NewChild, Page.AppLanguage -> Page.Home
            Page.Home -> return closeSettings()
        }
    }

    /** Changes [c]'s settings, the playing child's unless another is named. */
    fun update(next: Settings, c: Child = child) {
        val before = c.settings
        val today = host.clock.today()
        val s = if (next.age != before.age) next.copy(ageSetDay = today) else next
        if (s.age != before.age) c.progress.resetSkills()
        c.settings = s
        c.settingsStore.save(s)
        if (c === child) host.speaker.setSlow(s.slowSpeech)
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
     * usually share them, and the parent can change them on the child's page.
     */
    fun addChild(p: Profile, age: Int): Int {
        val s = settings.copy(onboarded = true, age = age, ageSetDay = host.clock.today())
        val id = profiles.add(p, s)
        all = profiles.all()
        select(id)
        return id
    }

    /** [c]'s day of play today, for automatic fun reels; read afresh after a "start again". */
    fun funDay(c: Child): Int {
        c.funRestarts
        return c.funDays.day(host.clock.today())
    }

    /**
     * "Start again" on a child's fun reels page: back to day 1 and 1 in 2. Today stays
     * counted if it has been played, so it is day 1, and tomorrow day 2.
     */
    fun restartFun(c: Child) {
        c.funDays.restart()
        if (c.history.playedToday()) c.funDays.played(host.clock.today())
        c.funRestarts++
    }

    fun removeChild(id: Int) {
        if (profiles.count <= 1) return
        if (child.id == id) select(profiles.ids().first { it != id })
        profiles.remove(id)
        children.remove(id)
        all = profiles.all()
        page = Page.Home
    }

    private companion object {
        const val VOICE_LATER = "voiceLater."

        /** "device.crash" in the shared store: where Android's crash keeper writes. */
        const val CRASH = "crash"
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
        screen == Screen.SETTINGS -> true.also { pageBack() }
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
    /** This round's board is finished: what the views draw. [restart] clears it. */
    var solved by mutableStateOf(false)

    /**
     * Solved in any round. Kept through [restart], so a replayed game is never a skip, and
     * a second solve is celebrated but not counted again.
     */
    var counted by mutableStateOf(false)
        private set

    /** Bumped by [restart]; the feed remounts the reel's view on it, from its opening state. */
    var round by mutableIntStateOf(0)
        private set
    var wrongTries by mutableIntStateOf(0)
    val wrong = mutableStateListOf<Int>()

    /** Drag games: which piece is in which place (piece index to target index). */
    val placed = mutableStateMapOf<Int, Int>()
    var liked by mutableStateOf(false)

    /** Bumped on every like, to send up the hearts. */
    var hearts by mutableIntStateOf(0)

    /** Bumped on every right answer, to restart the celebration. */
    var celebrate by mutableIntStateOf(0)

    /** Which of the celebrations this solve gets: they take turns, so none is rarer than another. */
    var burst by mutableIntStateOf(0)

    /** The child did something with this reel (touched it, or stayed on it): see [Feed.engaged]. */
    var engaged = false

    /** Marks the first solve. Returns false when an earlier round already counted. */
    fun count(): Boolean {
        if (counted) return false
        counted = true
        return true
    }

    /**
     * The same puzzle again, from the start. Before the first solve it is a second try, so
     * that solve is not a first-try one: a memory game restarted is a second look.
     */
    fun restart() {
        if (!counted) wrongTries++
        solved = false
        wrong.clear()
        placed.clear()
        round++
    }
}

class FeedModel(private val app: AppState, private val child: Child) {
    private val engine = Feed({ child.settings }, child.progress, funDay = { child.funDays.day(app.host.clock.today()) })

    /** Celebrations take turns across the session. */
    private var bursts = 0
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
        dropFrom(at)
        fill(at)
    }

    /** Takes out the reels from [index] on, unseen: a fun reel waiting among them is no longer waiting. */
    private fun dropFrom(index: Int) {
        if (index >= reels.size) return
        engine.dropped(reels.subList(index, reels.size).toList())
        reels.removeRange(index, reels.size)
    }

    /** The reel at [page] came on screen. */
    fun onScreen(page: Int) {
        reels.getOrNull(page)?.let { engine.shown(it) }
    }

    /**
     * The child did something with [reel]: a tap on it, or five seconds on screen (an
     * explore card can be just listened to). Only these reels move the fun slot on, and
     * they are what the weekly note counts as reels played. A reel finished well (an
     * explore reel taken in here, a puzzle on its first-try solve) may bring the next fun
     * reel straight after it.
     */
    fun engaged(reel: Reel) {
        val s = state(reel)
        if (s.engaged || reel is TimeUp) return
        s.engaged = true
        engine.engaged(reel)
        child.history.played(reel.topic)
        if (reel is TuneReel) child.history.song(reel.song.key)
        if (!reel.needsSolving) finishedWell(reel)
    }

    /**
     * Puts a due fun reel straight after [reel], the one on screen: the reels already made
     * ahead were decided before the child got here. The page below is not visible yet, so
     * nothing moves under the child's finger.
     */
    private fun finishedWell(reel: Reel) {
        if (timeUp || reels.getOrNull(current)?.id != reel.id) return
        engine.funAfter(reel)?.let { reels.add(current + 1, it) }
    }

    /** Called when the child moves off [reel]. */
    fun left(reel: Reel, forward: Boolean) {
        val s = state(reel)
        if (!reel.needsSolving || s.counted) return
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
        s.burst = bursts++
        s.celebrate++
        // A fun reel played to its end is cheered, never scored.
        if (reel.isFun) return
        if (!s.count()) return
        val outcome = if (s.wrongTries == 0) Outcome.SOLVED_FIRST_TRY else Outcome.SOLVED
        engaged(reel)
        engine.record(reel, outcome)
        child.history.record(reel.topic, outcome)
        if (outcome == Outcome.SOLVED_FIRST_TRY) finishedWell(reel)
    }

    /** The reel on screen from the start, keeping that it was solved. */
    fun restart(reel: Reel) = state(reel).restart()

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
        if (child.history.playedToday()) child.funDays.played(app.host.clock.today())
        refreshBudget()
        if (timeUp) {
            goodbyeAfter(current)
            return true
        }
        return false
    }

    /** The child finishes the reel they are on; the goodbye reel is the next one. */
    private fun goodbyeAfter(current: Int) {
        dropFrom(current + 1)
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
