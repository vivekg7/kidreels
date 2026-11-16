package com.crylo.kidreels.ui

import com.crylo.kidreels.core.Clock
import com.crylo.kidreels.core.KeyValueStore
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.MapStore
import com.crylo.kidreels.core.Profile
import com.crylo.kidreels.core.Profiles
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.Settings
import com.crylo.kidreels.core.SettingsStore
import com.crylo.kidreels.core.TimeBudget
import com.crylo.kidreels.core.TimeUp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The whole app's state with a pretend phone: storage in memory, a clock the test moves. */
private class FakeHost : Host {
    var now = 1_000_000_000L
    var day = 100L
    var minute = 12 * 60
    val voices = mutableSetOf(Lang.EN, Lang.HI)
    var copied: String? = null

    override val store: KeyValueStore = MapStore()
    override val clock = object : Clock {
        override fun nowMillis() = now
        override fun today() = day
        override fun minuteOfDay() = minute
    }
    override val speaker = object : Speaker {
        override val hasEngine = true
        override fun ready(lang: Lang) = lang in voices
        override fun speak(lines: List<Say>) = Unit
        override fun stop() = Unit
        override fun setSlow(slow: Boolean) = Unit
    }
    override val appVersion = "test"

    override fun play(sfx: Sfx) = Unit
    override fun openVoiceSettings() = Unit
    override fun keepScreenOn(on: Boolean) = Unit
    override fun darkSystemIcons(dark: Boolean) = Unit
    override fun copyText(text: String) {
        copied = text
    }
}

class AppStateTest {
    private val host = FakeHost()

    /** Children already set up on the phone, as if from earlier runs. Returns their ids. */
    private fun children(vararg settings: Settings): List<Int> {
        val profiles = Profiles(host.store)
        SettingsStore(profiles.storeOf(1)).save(settings.first().copy(onboarded = true))
        profiles.save(Profile(1, "Aarav"))
        val rest = settings.drop(1).mapIndexed { i, s -> profiles.add(Profile(0, "Child ${i + 2}"), s.copy(onboarded = true)) }
        return listOf(1) + rest
    }

    private fun usedUp(id: Int) {
        val profiles = Profiles(host.store)
        val minutes = SettingsStore(profiles.storeOf(id)).load().dailyMinutes
        TimeBudget(profiles.storeOf(id), host.clock).add(minutes * TimeBudget.MINUTE)
    }

    @Test
    fun aNewPhoneStartsWithSetup() {
        assertEquals(Screen.ONBOARDING, AppState(host).screen)
    }

    @Test
    fun oneChildGoesStraightToTheirFeed() {
        children(Settings())
        val app = AppState(host)
        assertEquals(Screen.FEED, app.screen)
        assertEquals(1, app.child.id)
    }

    @Test
    fun twoChildrenAreAskedWhoIsPlaying() {
        val (_, meera) = children(Settings(age = 4), Settings(age = 7))
        val app = AppState(host)
        assertEquals(Screen.PICKER, app.screen)
        app.choose(meera)
        assertEquals(Screen.FEED, app.screen)
        assertEquals(7, app.settings.age)
        assertEquals("Hi, Child 2!", app.feed.takeGreeting()?.get(Lang.EN))
        // The next start opens on whoever played last.
        assertEquals(meera, AppState(host).child.id)
    }

    @Test
    fun aChildWhoseTimeIsUpNeedsAGrownUpToSwitch() {
        val (aarav, meera) = children(Settings(dailyMinutes = 10), Settings(dailyMinutes = 10))
        usedUp(aarav)
        val app = AppState(host)
        assertTrue(app.timeUp(aarav))
        assertTrue(app.needsGate(meera))
        assertFalse(app.needsGate(aarav))
        // A grown-up who opened the picker from the feed, past the gate, may pick anyone.
        app.choose(aarav)
        app.openPicker()
        assertFalse(app.needsGate(meera))
        app.choose(meera)
        assertEquals(meera, app.child.id)
    }

    @Test
    fun aLongTimeAwayAsksAgainButAShortOneDoesNot() {
        val (aarav, _) = children(Settings(), Settings())
        val app = AppState(host)
        app.choose(aarav)

        app.foreground = false
        host.now += Profiles.ASK_AGAIN_MILLIS - 60_000
        app.foreground = true
        assertEquals(Screen.FEED, app.screen)

        app.foreground = false
        host.now += Profiles.ASK_AGAIN_MILLIS
        app.foreground = true
        assertEquals(Screen.PICKER, app.screen)
        // Back returns to the child who was playing.
        assertTrue(app.onBack())
        assertEquals(Screen.FEED, app.screen)
        assertEquals(aarav, app.child.id)
    }

    @Test
    fun oneChildIsNeverAskedAgain() {
        children(Settings())
        val app = AppState(host)
        app.foreground = false
        host.now += 10 * Profiles.ASK_AGAIN_MILLIS
        app.foreground = true
        assertEquals(Screen.FEED, app.screen)
    }

    @Test
    fun backFromTheFirstPickerClosesTheApp() {
        children(Settings(), Settings())
        assertFalse(AppState(host).onBack())
    }

    @Test
    fun settingsChangeOnlyThePickedChild() {
        val (aarav, meera) = children(Settings(age = 4), Settings(age = 7))
        val app = AppState(host)
        app.choose(meera)
        app.update(app.settings.copy(dailyMinutes = 45, langs = listOf(Lang.HI)))
        app.select(aarav)
        assertEquals(20, app.settings.dailyMinutes)
        assertEquals(listOf(Lang.EN), app.settings.langs)
        app.select(meera)
        assertEquals(45, app.settings.dailyMinutes)
    }

    @Test
    fun aChildsPageChangesThatChildEvenWhenAnotherIsPlaying() {
        val (aarav, meera) = children(Settings(age = 4), Settings(age = 7))
        val app = AppState(host)
        app.choose(aarav)
        host.day += 50
        val other = app.childOf(meera)
        app.update(other.settings.copy(age = 8, bedtime = 20 * 60), other)
        assertEquals(aarav, app.child.id)
        assertEquals(4, app.settings.age)
        assertEquals(0, app.settings.bedtime)
        val saved = SettingsStore(Profiles(host.store).storeOf(meera)).load()
        assertEquals(8, saved.age)
        assertEquals(20 * 60, saved.bedtime)
        // A new age starts the check-in's count again, as it does for the playing child.
        assertEquals(host.day, saved.ageSetDay)
    }

    @Test
    fun backGoesUpOneSettingsPageAtATime() {
        val (aarav) = children(Settings())
        val app = AppState(host)
        app.openSettings()
        app.page = Page.Part(aarav, ChildPart.PROFILE)
        assertTrue(app.onBack())
        assertEquals(Page.OfChild(aarav), app.page)
        assertTrue(app.onBack())
        assertEquals(Page.Home, app.page)
        assertEquals(Screen.SETTINGS, app.screen)
        assertTrue(app.onBack())
        assertEquals(Screen.FEED, app.screen)
        // The gear always opens on the list, not on the page left last time.
        app.page = Page.OfChild(aarav)
        app.closeSettings()
        app.openSettings()
        assertEquals(Page.Home, app.page)
    }

    @Test
    fun removingAChildFromTheirPageGoesBackToTheList() {
        val (_, meera) = children(Settings(), Settings())
        val app = AppState(host)
        app.openSettings()
        app.page = Page.OfChild(meera)
        app.removeChild(meera)
        assertEquals(Page.Home, app.page)
        assertEquals(1, app.all.size)
    }

    @Test
    fun aNewChildStartsFromTheCurrentSettingsWithTheirOwnAge() {
        children(Settings(age = 4, dailyMinutes = 30, langs = listOf(Lang.HI, Lang.EN)))
        val app = AppState(host)
        app.addChild(Profile(0, "Meera"), age = 8)
        assertEquals(2, app.all.size)
        assertEquals("Meera", app.child.profile.name)
        assertEquals(8, app.settings.age)
        assertEquals(30, app.settings.dailyMinutes)
        assertEquals(listOf(Lang.HI, Lang.EN), app.settings.langs)
    }

    @Test
    fun removingThePlayingChildSwitchesToAnother() {
        val (aarav, meera) = children(Settings(), Settings())
        val app = AppState(host)
        app.choose(meera)
        app.removeChild(meera)
        assertEquals(aarav, app.child.id)
        assertEquals(1, app.all.size)
        // The last child stays.
        app.removeChild(aarav)
        assertEquals(1, app.all.size)
    }

    @Test
    fun aMissingVoiceIsToldUntilAGrownUpSaysNotNow() {
        children(Settings(langs = listOf(Lang.EN, Lang.HI)))
        val app = AppState(host)
        assertNull(app.missingVoice)
        host.voices -= Lang.HI
        assertEquals(Lang.HI, app.missingVoice)
        app.voiceLater(Lang.HI)
        assertNull(app.missingVoice)
        host.day += 6
        assertNull(app.missingVoice)
        host.day += 1
        assertEquals(Lang.HI, app.missingVoice)
        // A voice installed meanwhile ends it for good.
        host.voices += Lang.HI
        assertNull(app.missingVoice)
    }

    @Test
    fun bedtimeEndsTheFeedAndAGrownUpCanGiveTenMinutes() {
        children(Settings(dailyMinutes = 0, bedtime = 20 * 60))
        host.minute = 20 * 60 + 15
        val app = AppState(host)
        app.feed.fill(0)
        val last = app.feed.reels.last()
        assertTrue(last is TimeUp && last.bedtime)

        app.feed.addTime(10)
        assertFalse(app.feed.timeUp)
        assertTrue(app.feed.reels.none { it is TimeUp })
        host.now += 10 * TimeBudget.MINUTE
        app.feed.tick(1000, app.feed.current)
        assertTrue(app.feed.timeUp)
        assertTrue((app.feed.reels.last() as TimeUp).bedtime)
    }

    @Test
    fun theFeedTimeLimitEndsWithTheUsualGoodbye() {
        children(Settings(dailyMinutes = 10))
        val app = AppState(host)
        app.feed.fill(0)
        repeat(10 * 60) { app.feed.tick(1000, 0) }
        assertTrue(app.feed.timeUp)
        val last = app.feed.reels.last()
        assertTrue(last is TimeUp && !last.bedtime)
        // The child finishes the reel on screen; the goodbye is next.
        assertEquals(2, app.feed.reels.size)
    }

    /** The first reel in the feed that has an answer. */
    private fun puzzle(app: AppState) = app.feed.run {
        fill(5)
        reels.first { it.needsSolving }
    }

    @Test
    fun aSolvedPuzzleStartedOverIsNeitherASkipNorCountedTwice() {
        children(Settings())
        val app = AppState(host)
        val reel = puzzle(app)
        val state = app.feed.state(reel)
        app.feed.solved(reel)
        assertEquals(1, app.progress.stats(reel.level).tried)

        app.feed.restart(reel)
        assertFalse(state.solved)
        assertEquals(1, state.round)
        app.feed.solved(reel)
        // Celebrated again, as it was solved again, but counted once.
        assertEquals(2, state.celebrate)
        assertEquals(1, app.progress.stats(reel.level).tried)

        // Started over and swiped past half done: no sad face.
        app.feed.restart(reel)
        app.feed.left(reel, forward = true)
        assertEquals(0, app.feed.skippedPuzzle)
    }

    @Test
    fun startingOverBeforeTheFirstSolveIsASecondTry() {
        children(Settings())
        val app = AppState(host)
        val reel = puzzle(app)
        app.feed.restart(reel)
        app.feed.solved(reel)
        val stats = app.progress.stats(reel.level)
        assertEquals(1, stats.tried)
        assertEquals(0, stats.firstTry)
    }

    @Test
    fun startingOverIsNoWayPastTheSadFace() {
        children(Settings())
        val app = AppState(host)
        val reel = puzzle(app)
        app.feed.restart(reel)
        app.feed.left(reel, forward = true)
        assertEquals(1, app.feed.skippedPuzzle)
    }

    @Test
    fun aKeptCrashShowsUntilDeleted() {
        children(Settings())
        host.store.put("device.crash", "Kid Reels test\nboom")
        val app = AppState(host)
        assertEquals("Kid Reels test\nboom", app.crashReport)
        app.clearCrash()
        assertNull(app.crashReport)
        assertNull(host.store.get("device.crash"))
    }

    @Test
    fun tabletsFitThePhoneLayoutAndPhonesAreLeftAlone() {
        // Phones, small and large: as designed.
        assertEquals(1f, screenScale(360f, 760f))
        assertEquals(1f, screenScale(411f, 923f))
        assertEquals(1f, screenScale(450f, 980f))
        // A 10-inch tablet: bigger upright; on its side, a little smaller, to fit the height.
        assertEquals(1.45f, screenScale(800f, 1280f), 0.01f)
        assertEquals(0.91f, screenScale(1280f, 800f), 0.01f)
        // A big screen is not blown up without end.
        assertEquals(1.6f, screenScale(1600f, 2400f))
    }
}
