package com.crylo.kidreels.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HistoryTest {
    private class DayClock(var day: Long) : Clock {
        override fun nowMillis() = 0L
        override fun today() = day
        override fun minuteOfDay() = 12 * 60
    }

    private fun History.solve(topic: Topic, first: Int, later: Int = 0, skipped: Int = 0) {
        repeat(first) { record(topic, Outcome.SOLVED_FIRST_TRY) }
        repeat(later) { record(topic, Outcome.SOLVED) }
        repeat(skipped) { record(topic, Outcome.SKIPPED) }
    }

    @Test
    fun aWeekAddsUpItsDays() {
        val clock = DayClock(100)
        val history = History(MapStore(), clock)
        history.addTime(12 * TimeBudget.MINUTE)
        history.solve(Topic.COUNTING, first = 5)
        clock.day = 101
        history.addTime(20 * TimeBudget.MINUTE)
        history.solve(Topic.COUNTING, first = 3, later = 1)
        history.solve(Topic.MIRROR, first = 1, later = 2, skipped = 2)
        history.like(Topic.ANIMALS)
        history.like(Topic.ANIMALS)
        history.like(Topic.SHAPES)
        // Opened by accident for a few seconds: not a day played.
        clock.day = 102
        history.addTime(10_000)

        val week = history.week()
        assertEquals(32, week.minutes)
        assertEquals(2, week.days)
        assertEquals(14, week.tried)
        assertEquals(9, week.firstTry)
        assertEquals(64, week.firstTryPercent)
        assertEquals(listOf(Topic.COUNTING), week.strong)
        assertEquals(listOf(Topic.MIRROR), week.hard)
        assertEquals(listOf(Topic.ANIMALS, Topic.SHAPES), week.liked)
    }

    @Test
    fun aFewTriesAreNotAVerdict() {
        val history = History(MapStore(), DayClock(100))
        history.solve(Topic.CLOCK, first = 0, skipped = History.MIN_TRIES - 1)
        history.solve(Topic.SHAPES, first = History.MIN_TRIES - 1)
        val week = history.week()
        assertTrue(week.hard.isEmpty())
        assertTrue(week.strong.isEmpty())
    }

    @Test
    fun onlyTheLastSevenDaysCountAndAFortnightIsKept() {
        val kv = MapStore()
        val clock = DayClock(100)
        val history = History(kv, clock)
        history.addTime(30 * TimeBudget.MINUTE)
        clock.day = 107
        history.addTime(5 * TimeBudget.MINUTE)
        assertEquals(5, history.week().minutes)
        assertTrue(kv.keys().any { it.startsWith("day.100.") })
        clock.day = 100L + History.KEEP_DAYS
        history.addTime(TimeBudget.MINUTE)
        assertTrue(kv.keys().none { it.startsWith("day.100.") })
        assertTrue(kv.keys().any { it.startsWith("day.107.") })
    }
}
