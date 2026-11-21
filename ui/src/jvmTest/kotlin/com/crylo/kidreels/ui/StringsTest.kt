package com.crylo.kidreels.ui

import com.crylo.kidreels.core.FunMode
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.Songs
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

    @Test
    fun theFunReelsRowSaysWhereAutomaticHasGotTo() {
        assertEquals("Automatic · day 9 of 21, now 1 in 3", S.funLine(FunMode.AUTO, 9, 21, 3)[Lang.EN])
        assertEquals("अपने-आप · 21 में से दिन 9, अभी 3 में 1", S.funLine(FunMode.AUTO, 9, 21, 3)[Lang.HI])
        assertEquals("Automatic · settled at 1 in 6", S.funLine(FunMode.AUTO, 30, 21, 6)[Lang.EN])
        assertEquals("Off", S.funLine(FunMode.OFF, 9, 21, null)[Lang.EN])
    }

    @Test
    fun theWeeklyNoteSaysHowMuchWasFun() {
        assertEquals(
            "112 reels, 31 of them fun (songs and toys). Automatic: day 9 of 21, now 1 in 3.",
            S.weekFun(112, 31)[Lang.EN] + " " + S.funNote(FunMode.AUTO, 9, 21, 3)[Lang.EN],
        )
        assertEquals("Their favourite song this week: Twinkle Twinkle Little Star. Sing it with them!", S.favouriteSong(Songs.twinkle.title)[Lang.EN])
    }
}
