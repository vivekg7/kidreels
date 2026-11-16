package com.crylo.kidreels.ui

import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.LevelStats
import kotlin.test.Test
import kotlin.test.assertEquals

class StringsTest {
    @Test
    fun aLevelLineSaysHowManyWereSwipedPastSoALowShareIsNotReadAsWrongAnswers() {
        assertEquals(
            "Age 8 reels: 20 puzzles, 0% right first time, 19 swiped past",
            S.levelLine(8, LevelStats(tried = 20, firstTry = 0, skipped = 19))[Lang.EN],
        )
    }

    @Test
    fun theWeekSaysItsSkipsTheSameWay() {
        assertEquals("26 puzzles, 0% right first time, 25 swiped past", S.weekPuzzles(26, 0, 25)[Lang.EN])
        assertEquals("1 पहेली, 0% पहली बार में सही, 1 छोड़ दी गई", S.weekPuzzles(1, 0, 1)[Lang.HI])
    }

    @Test
    fun aLevelLineWithNothingSwipedPastLeavesItOut() {
        assertEquals(
            "Age 6 reels: 1 puzzle, 100% right first time",
            S.levelLine(6, LevelStats(tried = 1, firstTry = 1, skipped = 0))[Lang.EN],
        )
    }
}
