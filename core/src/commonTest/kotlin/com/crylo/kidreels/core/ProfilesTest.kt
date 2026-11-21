package com.crylo.kidreels.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfilesTest {
    private val clock = object : Clock {
        override fun nowMillis() = 0L
        override fun today() = 100L
        override fun minuteOfDay() = 12 * 60
    }

    @Test
    fun anUpdateFromOneChildKeepsEverything() {
        // What version 1.1 left behind: one child's data at the top level.
        val kv = MapStore()
        val old = Settings(true, listOf(Lang.HI, Lang.EN), 7, Look.CALM, 45, 1, 12345, true)
        SettingsStore(kv).save(old)
        Progress(kv).like(Topic.ANIMALS)
        TimeBudget(kv, clock).add(5 * TimeBudget.MINUTE)
        val before = kv.keys().size

        val profiles = Profiles(kv)
        assertEquals(listOf(1), profiles.ids())
        assertEquals(1, profiles.last)
        val one = profiles.storeOf(1)
        assertEquals(old, SettingsStore(one).load())
        assertEquals(1, Progress(one).likes(Topic.ANIMALS))
        assertEquals(40 * TimeBudget.MINUTE, TimeBudget(one, clock).remainingMillis(45))
        // Nothing left at the top level but the phone's own keys.
        assertTrue(kv.keys().all { it.startsWith("device.") || it.startsWith("p1.") })
        assertEquals(before, one.keys().size)
        // Opening again moves nothing twice.
        assertEquals(old, SettingsStore(Profiles(kv).storeOf(1)).load())
    }

    @Test
    fun childrenKeepTheirOwnSettingsProgressAndTime() {
        val profiles = Profiles(MapStore())
        SettingsStore(profiles.storeOf(1)).save(Settings(onboarded = true, age = 3, dailyMinutes = 20))
        val two = profiles.add(Profile(0, "Meera", Avatar.PANDA, 3), Settings(onboarded = true, age = 8, dailyMinutes = 45))
        assertEquals(listOf(1, two), profiles.ids())
        assertEquals("Meera", profiles.profile(two).name)
        assertEquals(Avatar.PANDA, profiles.profile(two).avatar)
        assertEquals(3, profiles.profile(two).colour)
        assertEquals(3, SettingsStore(profiles.storeOf(1)).load().age)
        assertEquals(8, SettingsStore(profiles.storeOf(two)).load().age)

        Progress(profiles.storeOf(1)).like(Topic.LETTERS)
        assertEquals(0, Progress(profiles.storeOf(two)).likes(Topic.LETTERS))

        TimeBudget(profiles.storeOf(1), clock).add(20 * TimeBudget.MINUTE)
        assertTrue(profiles.timeUp(1, clock))
        assertFalse(profiles.timeUp(two, clock))
        assertEquals(45 * TimeBudget.MINUTE, TimeBudget(profiles.storeOf(two), clock).remainingMillis(45))
    }

    @Test
    fun aChildWhoseTimeIsUpCannotSwitchToASiblingAlone() {
        val profiles = Profiles(MapStore())
        SettingsStore(profiles.storeOf(1)).save(Settings(onboarded = true, dailyMinutes = 10))
        val two = profiles.add(Profile(0), Settings(onboarded = true, dailyMinutes = 10))
        profiles.last = 1
        assertFalse(profiles.needsGate(two, clock))

        TimeBudget(profiles.storeOf(1), clock).add(10 * TimeBudget.MINUTE)
        assertTrue(profiles.needsGate(two, clock))
        // Their own profile is still theirs to open: it only shows the goodbye reel.
        assertFalse(profiles.needsGate(1, clock))
        // A grown-up's extra minutes lift it.
        TimeBudget(profiles.storeOf(1), clock).addBonus(10)
        assertFalse(profiles.needsGate(two, clock))
    }

    @Test
    fun removingAChildWipesTheirDataButNeverTheLastChild() {
        val kv = MapStore()
        val profiles = Profiles(kv)
        assertFalse(profiles.remove(1))
        val two = profiles.add(Profile(0, "Aarav"), Settings(onboarded = true))
        Progress(profiles.storeOf(two)).like(Topic.SHAPES)
        profiles.last = two

        assertTrue(profiles.remove(two))
        assertEquals(listOf(1), profiles.ids())
        assertEquals(1, profiles.last)
        assertTrue(kv.keys().none { it.startsWith("p$two.") })
        // A child added later gets a fresh start, not the removed one's leftovers.
        val three = profiles.add(Profile(0), Settings(onboarded = true))
        assertEquals(0, Progress(profiles.storeOf(three)).likes(Topic.SHAPES))
        assertFalse(profiles.remove(99))
    }

    @Test
    fun aChildWithNoNameIsCalledByTheirAnimal() {
        assertEquals("Fox", Profile(1, " ", Avatar.FOX).label(Lang.EN))
        assertEquals("लोमड़ी", Profile(1, "", Avatar.FOX).label(Lang.HI))
        assertEquals("Aarav", Profile(1, "Aarav", Avatar.FOX).label(Lang.HI))
        // Every avatar has a name to fall back on.
        Avatar.entries.forEach { it.word }
    }

    @Test
    fun theAppsLanguageIsThePhonesAndEnglishUntilChanged() {
        val kv = MapStore()
        val profiles = Profiles(kv)
        // A child whose reels are in Hindi does not make the grown-up's screens Hindi.
        SettingsStore(profiles.storeOf(1)).save(Settings(onboarded = true, langs = listOf(Lang.HI)))
        assertEquals(Lang.EN, profiles.uiLang)

        profiles.uiLang = Lang.HI
        assertEquals("hi", kv.get("device.uiLang"))
        assertEquals(Lang.HI, Profiles(kv).uiLang)
        // Phone-wide: no child's keys hold it, and removing a child keeps it.
        val two = profiles.add(Profile(0), Settings(onboarded = true))
        profiles.remove(two)
        assertEquals(Lang.HI, Profiles(kv).uiLang)
        assertEquals(listOf(Lang.HI), SettingsStore(profiles.storeOf(1)).load().langs)

        // A language this version does not know reads as English.
        kv.put("device.uiLang", "xx")
        assertEquals(Lang.EN, Profiles(kv).uiLang)
    }

    @Test
    fun namesAreTrimmedAndKeptShort() {
        val profiles = Profiles(MapStore())
        profiles.save(Profile(1, "  " + "x".repeat(40) + "  "))
        assertEquals("x".repeat(Profile.MAX_NAME), profiles.profile(1).name)
    }
}
