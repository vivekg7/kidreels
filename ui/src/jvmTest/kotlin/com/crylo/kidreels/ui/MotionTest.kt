package com.crylo.kidreels.ui

import com.crylo.kidreels.core.Songs
import com.crylo.kidreels.ui.common.Burst
import com.crylo.kidreels.ui.common.burstFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MotionTest {
    /**
     * Celebrations take turns, Gappu's two among them: each comes exactly as often as every
     * other, so none is a rare jackpot. A reward that comes at random is a slot machine.
     */
    @Test
    fun celebrationsTakeTurnsWithGappuAsAnEqual() {
        val rounds = 7
        val seen = (0 until Burst.entries.size * rounds).map(::burstFor)
        assertTrue(Burst.GAPPU_CHEER in seen && Burst.GAPPU_DANCE in seen)
        for (b in Burst.entries) assertEquals(rounds, seen.count { it == b }, "$b")
        // Any run of as many solves as there are bursts sees each once.
        for (start in 0..20) assertEquals(Burst.entries.toSet(), (start until start + Burst.entries.size).map(::burstFor).toSet())
    }

    @Test
    fun gappuNeverCelebratesTwiceInARow() {
        val gappu = setOf(Burst.GAPPU_CHEER, Burst.GAPPU_DANCE)
        for (n in 0..100) assertTrue(burstFor(n) !in gappu || burstFor(n + 1) !in gappu, "solves $n and ${n + 1}")
        assertNotEquals(Burst.entries.first(), Burst.entries.last())
    }

    /** Every fanfare is on the music box, or the nearest note would play instead. */
    @Test
    fun everyFanfareIsOnTheMusicBox() {
        for (b in Burst.entries) for (n in b.fanfare) assertTrue(n in Songs.NOTES, "$b plays $n")
    }
}
