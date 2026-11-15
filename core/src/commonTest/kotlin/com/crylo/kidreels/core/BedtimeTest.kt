package com.crylo.kidreels.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BedtimeTest {
    private class TestClock(var minute: Int, var now: Long = 0L) : Clock {
        override fun nowMillis() = now
        override fun today() = 100L
        override fun minuteOfDay() = minute
    }

    private val eight = 20 * 60

    @Test
    fun nightRunsFromBedtimeToMorning() {
        assertFalse(Bedtime.isNight(eight, eight - 1))
        assertTrue(Bedtime.isNight(eight, eight))
        assertTrue(Bedtime.isNight(eight, 23 * 60 + 59))
        assertTrue(Bedtime.isNight(eight, 0))
        assertTrue(Bedtime.isNight(eight, Bedtime.MORNING - 1))
        assertFalse(Bedtime.isNight(eight, Bedtime.MORNING))
        // Off means never, even at 3 am.
        assertFalse(Bedtime.isNight(0, 3 * 60))
    }

    @Test
    fun aGrownUpCanGiveTenMinutesBeforeBed() {
        val clock = TestClock(eight + 30)
        val budget = TimeBudget(MapStore(), clock)
        assertTrue(budget.isBedtime(eight))
        budget.addBedtimeMinutes(10)
        assertFalse(budget.isBedtime(eight))
        clock.now += 9 * TimeBudget.MINUTE
        assertFalse(budget.isBedtime(eight))
        clock.now += TimeBudget.MINUTE
        assertTrue(budget.isBedtime(eight))
        // Asked again later, the ten minutes start from now, not from the last grant.
        clock.now += 60 * TimeBudget.MINUTE
        budget.addBedtimeMinutes(10)
        assertFalse(budget.isBedtime(eight))
    }

    @Test
    fun theLastMinutesBeforeBedAreCounted() {
        val clock = TestClock(eight - 10)
        val budget = TimeBudget(MapStore(), clock)
        assertEquals(10, budget.minutesToBedtime(eight))
        clock.minute = eight + 60
        assertNull(budget.minutesToBedtime(eight))
        clock.minute = 3 * 60
        assertNull(budget.minutesToBedtime(eight))
        assertNull(budget.minutesToBedtime(0))
        // Ten more minutes count down too, so the last of them are calm.
        budget.addBedtimeMinutes(10)
        assertEquals(10, budget.minutesToBedtime(eight))
        clock.now += 9 * TimeBudget.MINUTE
        assertEquals(1, budget.minutesToBedtime(eight))
    }

    @Test
    fun bedtimeIsSavedAndPutsAChildToSleepAtThePicker() {
        val clock = TestClock(eight + 5)
        val profiles = Profiles(MapStore())
        SettingsStore(profiles.storeOf(1)).save(Settings(onboarded = true, dailyMinutes = 0, bedtime = eight))
        val two = profiles.add(Profile(0), Settings(onboarded = true, dailyMinutes = 0))
        assertEquals(eight, SettingsStore(profiles.storeOf(1)).load().bedtime)
        assertEquals(0, SettingsStore(profiles.storeOf(two)).load().bedtime)

        profiles.last = 1
        assertTrue(profiles.timeUp(1, clock))
        assertFalse(profiles.timeUp(two, clock))
        // Asleep, so a sibling's profile needs a grown-up, as when the minutes run out.
        assertTrue(profiles.needsGate(two, clock))
        clock.minute = Bedtime.MORNING
        assertFalse(profiles.timeUp(1, clock))
    }
}
