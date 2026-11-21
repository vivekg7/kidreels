package com.crylo.kidreels.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GappuTest {
    private fun said(reel: Reel, vararg langs: Lang) = reel.intro(langs.toList()).joinToString(" ") { it.text }

    /** He goes to eat too, in whichever language the reel speaks. */
    @Test
    fun theGoodbyeReelSaysGappuIsGoingToEatToo() {
        assertTrue(Gappu.eatsToo[Lang.EN] in said(TimeUp(1), Lang.EN, Lang.HI))
        assertTrue(Gappu.eatsToo[Lang.HI] in said(TimeUp(1), Lang.HI, Lang.EN))
        assertTrue("Gappu is going to eat too." in said(TimeUp(1), Lang.EN))
        assertTrue("गप्पू भी खाना खाने जा रहा है।" in said(TimeUp(1), Lang.HI))
        // After the child's own "time to eat", so the "too" makes sense.
        val en = said(TimeUp(1), Lang.EN)
        assertTrue(en.indexOf("Time to eat") < en.indexOf("Gappu"))
    }

    @Test
    fun theGoodNightReelSaysGappuIsAsleep() {
        assertTrue("Gappu is asleep already." in said(TimeUp(1, bedtime = true), Lang.EN, Lang.HI))
        assertTrue("गप्पू सो गया है।" in said(TimeUp(1, bedtime = true), Lang.HI, Lang.EN))
        assertFalse("eat" in said(TimeUp(1, bedtime = true), Lang.EN))
    }

    /** A loved character is a hook: he never asks the child to stay or is sad that they go. */
    @Test
    fun gappuNeverAsksTheChildToStay() {
        for (lang in Lang.entries) for (bedtime in listOf(false, true)) {
            val text = said(TimeUp(1, bedtime), lang).lowercase()
            for (hook in listOf("stay", "miss you", "don't go", "one more", "sad", "मत जाओ", "याद आएगी", "रुको", "उदास")) {
                assertFalse(hook in text, "\"$hook\" in $text")
            }
        }
    }

    /** Every drawing of him is a picture with a name, and a name is not bridged. */
    @Test
    fun tappingGappuSaysHisNameWithNoBridge() {
        for (pose in GappuPose.entries) {
            val word = assertNotNull(Words.of(pose.art), "No name for ${pose.art}")
            assertEquals(listOf(Say("This is Gappu!", Lang.EN)), word.said(Lang.EN, listOf(Lang.EN, Lang.HI)))
            assertEquals(listOf(Say("यह गप्पू है!", Lang.HI)), word.said(Lang.HI, listOf(Lang.HI, Lang.EN)))
        }
        assertEquals(9, GappuPose.entries.size)
        assertEquals("art:gappu-asleep", GappuPose.ASLEEP.art)
    }
}
