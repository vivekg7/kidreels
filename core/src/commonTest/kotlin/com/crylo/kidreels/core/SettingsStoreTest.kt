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
}
