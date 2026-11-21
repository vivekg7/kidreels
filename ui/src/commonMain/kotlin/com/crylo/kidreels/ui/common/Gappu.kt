package com.crylo.kidreels.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.crylo.kidreels.core.GappuPose
import com.crylo.kidreels.ui.art.artFor
import com.crylo.kidreels.ui.art.drawArt

/**
 * Gappu in [pose], [height] tall (the lying-down pose is as wide as it needs). In dp, so
 * `screenScale` draws him bigger on a tablet as it does everything else. Tapping is the
 * caller's: a celebration or the sad face take no taps, so they never hold up a swipe.
 */
@Composable
fun Gappu(pose: GappuPose, height: Dp, modifier: Modifier = Modifier) {
    val art = artFor(pose.art) ?: error("No drawing for ${pose.art}: run scripts/art.py")
    Canvas(modifier.size(height * (art.width / art.height), height)) { drawArt(art) }
}
