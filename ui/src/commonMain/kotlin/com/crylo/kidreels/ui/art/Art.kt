package com.crylo.kidreels.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import com.crylo.kidreels.ui.common.T

/**
 * One filled shape of a picture: [fill] as ARGB, [d] as SVG path data, and the path it
 * is clipped to, if any. Made by scripts/art.py from the SVGs in art/.
 */
class ArtPart(val fill: Long, val d: String, val evenOdd: Boolean = false, val clip: String? = null)

/** A picture drawn from SVG paths, [width] × [height] in its own units. */
class Art(val width: Float, val height: Float, val parts: List<ArtPart>) {
    /** Parsed once, on first draw: path data is text until then. */
    internal val paths: List<Pair<Path, Path?>> by lazy {
        parts.map { part ->
            val path = PathParser().parsePathString(part.d).toPath().apply { if (part.evenOdd) fillType = PathFillType.EvenOdd }
            path to part.clip?.let { PathParser().parsePathString(it).toPath() }
        }
    }
}

/** [art] scaled to fit this draw area and centred in it. */
fun DrawScope.drawArt(art: Art) {
    val scale = minOf(size.width / art.width, size.height / art.height)
    translate((size.width - art.width * scale) / 2, (size.height - art.height * scale) / 2) { drawArt(art, scale) }
}

/** [art] at [scale] times its own size, its top left corner at the origin. */
fun DrawScope.drawArt(art: Art, scale: Float) {
    withTransform({ scale(scale, scale, pivot = androidx.compose.ui.geometry.Offset.Zero) }) {
        art.paths.forEachIndexed { i, (path, clip) ->
            val colour = Color(art.parts[i].fill)
            if (clip == null) drawPath(path, colour) else clipPath(clip) { drawPath(path, colour) }
        }
    }
}

/** The picture for an "art:<name>" key, or null for an emoji or an unknown name. */
fun artFor(key: String): Art? = if (key.startsWith(ART)) Arts.byName[key.removePrefix(ART)] else null

const val ART = "art:"

/**
 * A picture by its key: an emoji from the system font, or one of the app's own drawings
 * ("art:okra") where no emoji shows the thing well. Drawn the size an emoji of [size]
 * would be, so either fits the same space.
 */
@Composable
fun Picture(key: String, size: TextUnit, modifier: Modifier = Modifier) {
    val art = artFor(key)
    if (art == null) {
        T(key, modifier, size = size)
    } else {
        // Through the density, so the phone's text size grows a drawing as it grows an emoji.
        val side = with(LocalDensity.current) { (size * 1.17f).toDp() }
        Canvas(modifier.size(side)) { drawArt(art) }
    }
}
