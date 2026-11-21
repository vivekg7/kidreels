package com.crylo.kidreels.core

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsStoreTest {
    private val kv = MapStore()
    private val store = SettingsStore(kv)

    @Test
    fun aCustomLimitAndBedtimeAreKept() {
        store.save(Settings(dailyMinutes = 95, bedtime = 22 * 60 + 40))
        val s = store.load()
        assertEquals(95, s.dailyMinutes)
        assertEquals(22 * 60 + 40, s.bedtime)
    }

    @Test
    fun aLimitPastSixHoursIsSixHours() {
        kv.put("dailyMinutes", "900")
        assertEquals(Settings.MAX_DAILY_MINUTES, store.load().dailyMinutes)
        // No limit stays no limit.
        kv.put("dailyMinutes", "0")
        assertEquals(0, store.load().dailyMinutes)
    }

    @Test
    fun aBedtimeOutsideTheEveningIsOff() {
        kv.put("bedtime", (10 * 60).toString())
        assertEquals(0, store.load().bedtime)
        kv.put("bedtime", (16 * 60).toString())
        assertEquals(16 * 60, store.load().bedtime)
        kv.put("bedtime", (24 * 60).toString())
        assertEquals(0, store.load().bedtime)
    }

    @Test
    fun theFunSettingsAreKept() {
        val s = Settings(funMode = FunMode.FIXED, funWeeks = 6, funEvery = 5, jumping = true)
        store.save(s)
        assertEquals(s, store.load())
    }

    /** A child set up before fun reels existed has none of their keys: automatic over three weeks, no jumping. */
    @Test
    fun aChildFromBeforeFunReelsStartsOnAutomatic() {
        kv.put("age", "5")
        val s = store.load()
        assertEquals(FunMode.AUTO, s.funMode)
        assertEquals(FunShare.DEFAULT_WEEKS, s.funWeeks)
        assertEquals(FunShare.DEFAULT_EVERY, s.funEvery)
        assertEquals(false, s.jumping)
    }

    @Test
    fun aFunValueThatIsNotOfferedIsTheDefault() {
        kv.put("funMode", "LOTS")
        kv.put("funWeeks", "5")
        kv.put("funEvery", "1")
        val s = store.load()
        assertEquals(FunMode.AUTO, s.funMode)
        assertEquals(FunShare.DEFAULT_WEEKS, s.funWeeks)
        assertEquals(FunShare.DEFAULT_EVERY, s.funEvery)
    }

    @Test
    fun daysOfPlayCountOncePerDayAndStartAgain() {
        val days = FunDays(kv)
        assertEquals(1, days.day(100), "a new child is on day 1")
        days.played(100)
        days.played(100)
        assertEquals(1, days.day(100))
        // Not played for a week: the next day played is day 2, not day 9.
        assertEquals(2, days.day(107))
        days.played(107)
        days.played(108)
        assertEquals(3, days.day(108))
        days.restart()
        assertEquals(1, days.day(108))
        days.played(108)
        assertEquals(1, days.day(108))
        assertEquals(2, days.day(109))
    }
}
