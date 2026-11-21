package com.crylo.kidreels.ui

import androidx.compose.ui.graphics.vector.PathParser
import com.crylo.kidreels.core.GappuPose
import com.crylo.kidreels.core.Silly
import com.crylo.kidreels.core.Words
import com.crylo.kidreels.ui.art.ART
import com.crylo.kidreels.ui.art.Arts
import com.crylo.kidreels.ui.art.artFor
import kotlin.test.Test
import kotlin.test.assertTrue

class ArtTest {
    /** A word naming a drawing that does not exist would show its key as text. */
    @Test
    fun everyDrawnPictureExists() {
        val keys = Words.byTopic.values.flatten().map { it.emoji }.filter { it.startsWith(ART) } + GappuPose.entries.map { it.art } + Silly.all.map { it.art }
        assertTrue(keys.isNotEmpty())
        val missing = keys.filter { artFor(it) == null }
        assertTrue(missing.isEmpty(), "No drawing for $missing: add the SVG to art/ and run scripts/art.py")
    }

    /** Path data is text until drawn; a typo would only show as a blank on a phone. */
    @Test
    fun everyDrawingParses() {
        for ((name, art) in Arts.byName) for (part in art.parts) {
            val nodes = PathParser().parsePathString(part.d).toNodes()
            assertTrue(nodes.isNotEmpty(), "$name has an empty path")
            part.clip?.let { assertTrue(PathParser().parsePathString(it).toNodes().isNotEmpty(), "$name has an empty clip") }
        }
    }
}
