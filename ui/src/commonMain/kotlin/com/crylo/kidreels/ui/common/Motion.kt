package com.crylo.kidreels.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.crylo.kidreels.core.GappuPose
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

/** How a picture moves when it celebrates or is tapped: usable on any emoji or drawing. */
enum class Motion { HOP, SQUASH, WOBBLE, SPIN, SHAKE, WAG }

/**
 * Moves [times] times each time [key] changes (not on first show), after [delayMillis]: a
 * hop, a squash and stretch, a wobble, a spin, a shake or a wag. On top of [popOn], for
 * the things in a puzzle to come alive when it is solved and for the fun reels' toys.
 */
@Composable
fun Modifier.moveOn(key: Int, motion: Motion, delayMillis: Int = 0, times: Int = 1, millis: Int = 520): Modifier {
    val p = remember { Animatable(0f) }
    LaunchedEffect(key) {
        if (key == 0) return@LaunchedEffect
        if (delayMillis > 0) delay(delayMillis.toLong())
        p.snapTo(0f)
        p.animateTo(times.toFloat(), tween(millis * times, easing = LinearEasing))
        p.snapTo(0f)
    }
    return this.graphicsLayer {
        val t = p.value
        if (t <= 0f) return@graphicsLayer
        val f = t - floor(t)
        val arc = sin(PI * f).toFloat()
        val wave = sin(2 * PI * f).toFloat()
        when (motion) {
            Motion.HOP -> {
                translationY = -arc * 22f * density
                scaleY = 1f + 0.08f * arc
                scaleX = 1f - 0.05f * arc
            }
            Motion.SQUASH -> {
                scaleX = 1f + 0.25f * arc
                scaleY = 1f - 0.2f * arc
            }
            Motion.WOBBLE -> rotationZ = wave * 14f
            Motion.SPIN -> rotationZ = 360f * FastOutSlowInEasing.transform(f)
            Motion.SHAKE -> translationX = sin(4 * PI * f).toFloat() * 9f * density
            Motion.WAG -> {
                transformOrigin = TransformOrigin(0.5f, 1f)
                rotationZ = sin(4 * PI * f).toFloat() * 16f
            }
        }
    }
}

/**
 * The celebration after a right answer: one of the [Burst]s, each about 1.5 seconds with a
 * short fanfare of its own after the chime. They take turns ([burst] counts up, see
 * [burstFor]), so none is rarer than another: a reward that comes at random is a slot
 * machine. Gappu cheering and Gappu dancing are two of them, the same size and length as
 * the rest. In the wind-down ([calm]) it is the plain confetti and chime, nothing new. It
 * never takes a tap, so it never holds up the next swipe.
 */
@Composable
fun Celebration(trigger: Int, burst: Int, calm: Boolean, note: (Int) -> Unit) {
    if (trigger == 0) return
    if (calm) {
        Confetti(trigger)
        return
    }
    val kind = burstFor(burst)
    LaunchedEffect(trigger) {
        // After the chime, which every right answer already plays.
        delay(380)
        for (n in kind.fanfare) {
            note(n)
            delay(85)
        }
    }
    if (kind == Burst.CONFETTI) {
        Confetti(trigger)
        return
    }
    val t = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) { t.animateTo(1f, tween(BURST_MILLIS, easing = LinearEasing)) }
    if (t.value >= 1f) return
    if (kind == Burst.GAPPU_CHEER || kind == Burst.GAPPU_DANCE) {
        GappuBurst(trigger, t.value, dance = kind == Burst.GAPPU_DANCE)
        return
    }
    val seed = remember(trigger) { trigger * 31 + kind.ordinal }
    Canvas(Modifier.fillMaxSize()) {
        val r = Random(seed)
        when (kind) {
            Burst.FIREWORKS -> fireworks(t.value, r)
            Burst.BALLOONS -> balloons(t.value, r)
            Burst.RAINBOW -> rainbow(t.value)
            Burst.STARS -> stars(t.value, r)
            Burst.BUBBLES -> bubbles(t.value, r)
            Burst.STREAMERS -> streamers(t.value, r)
            else -> sparkles(t.value, r)
        }
    }
}

/**
 * The celebrations, in the order they take turns, each with its fanfare on the music box's
 * notes (semitones from middle C).
 */
enum class Burst(val fanfare: List<Int>) {
    CONFETTI(listOf(0, 4, 7, 12)),
    FIREWORKS(listOf(7, 12, 16, 19)),
    BALLOONS(listOf(0, 2, 4, 5, 7)),
    RAINBOW(listOf(0, 4, 7, 9, 12)),
    STARS(listOf(12, 7, 12, 16)),
    BUBBLES(listOf(7, 9, 11, 12, 14, 16)),

    /** Gappu pops up and hops, cheering. */
    GAPPU_CHEER(listOf(0, 7, 4, 12)),
    STREAMERS(listOf(16, 14, 12, 11, 12)),
    SPARKLES(listOf(19, 16, 12, 16, 19)),

    /** Gappu pops up and sways on one foot. */
    GAPPU_DANCE(listOf(12, 11, 12, 14, 16, 12)),
}

/** The [n]th celebration of the session: each in turn, so every one comes as often as the others. */
fun burstFor(n: Int): Burst = Burst.entries[n.mod(Burst.entries.size)]

private const val BURST_MILLIS = 1500

/**
 * Gappu cheering or dancing, low down beside the puzzle so the things in it can still be
 * seen celebrating: he pops up, hops (or sways, on one foot) twice, then fades, in the same
 * 1.5 seconds as every other burst.
 */
@Composable
private fun GappuBurst(trigger: Int, p: Float, dance: Boolean) {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.align(BiasAlignment(-0.55f, 0.62f)).graphicsLayer {
                val s = if (p < 0.12f) 0.5f + p / 0.12f * 0.5f else 1f
                scaleX = s
                scaleY = s
                transformOrigin = TransformOrigin(0.5f, 1f)
                alpha = fade(p, 0.8f)
            },
        ) {
            Gappu(
                if (dance) GappuPose.DANCE else GappuPose.CHEER, 150.dp,
                Modifier.moveOn(trigger, if (dance) Motion.WAG else Motion.HOP, delayMillis = 180, times = 2, millis = 560),
            )
        }
    }
}

private val party = listOf(
    Color(0xFFFF5252), Color(0xFFFFD740), Color(0xFF69F0AE), Color(0xFF40C4FF), Color(0xFFE040FB), Color(0xFFFF9100),
)

private fun fade(p: Float, from: Float = 0.7f) = if (p < from) 1f else (1f - (p - from) / (1f - from)).coerceIn(0f, 1f)

/** Three rockets up, then sparks falling outwards. */
private fun DrawScope.fireworks(p: Float, r: Random) {
    repeat(3) { i ->
        val x = size.width * (0.22f + 0.28f * i)
        val top = size.height * (0.22f + 0.08f * (i % 2))
        val colour = party[r.nextInt(party.size)]
        val start = i * 0.08f
        val q = ((p - start) / (1f - start)).coerceIn(0f, 1f)
        if (q < 0.3f) {
            val y = size.height - (size.height - top) * (q / 0.3f)
            drawCircle(colour, px(5), Offset(x, y))
        } else {
            val e = (q - 0.3f) / 0.7f
            repeat(18) { k ->
                val a = (2 * PI * k / 18).toFloat()
                val d = FastOutSlowInEasing.transform(e) * size.minDimension * 0.22f
                val at = Offset(x + cos(a) * d, top + sin(a) * d + e * e * 60f * density)
                drawCircle(colour.copy(alpha = 1f - e), px(4), at)
            }
        }
    }
}

/** Balloons rising from below, swaying, on strings. */
private fun DrawScope.balloons(p: Float, r: Random) {
    repeat(9) { i ->
        val colour = party[i % party.size]
        val x0 = size.width * (0.08f + 0.84f * r.nextFloat())
        val speed = 0.8f + r.nextFloat() * 0.5f
        val y = size.height * (1.15f - p * speed * 1.2f)
        val x = x0 + sin(p * 6f + i) * 14f * density
        val w = px(26)
        val h = px(32)
        drawLine(Color.White.copy(alpha = 0.7f), Offset(x, y + h / 2), Offset(x + sin(p * 9f + i) * 6f * density, y + h / 2 + px(40)), 1.5f * density)
        drawOval(colour.copy(alpha = fade(p, 0.8f)), Offset(x - w / 2, y - h / 2), Size(w, h))
        drawCircle(Color.White.copy(alpha = 0.45f * fade(p, 0.8f)), px(4), Offset(x - w / 5, y - h / 5))
    }
}

/** A rainbow that sweeps across, then fades. */
private fun DrawScope.rainbow(p: Float) {
    val bands = listOf(0xFFE53935, 0xFFFB8C00, 0xFFFDD835, 0xFF43A047, 0xFF1E88E5, 0xFF3949AB, 0xFF8E24AA)
    val sweep = 180f * FastOutSlowInEasing.transform((p / 0.5f).coerceAtMost(1f))
    val centre = Offset(size.width / 2, size.height * 0.62f)
    val stroke = px(12)
    bands.forEachIndexed { i, c ->
        val radius = size.minDimension * 0.42f - i * stroke
        drawArc(
            Color(c).copy(alpha = 0.9f * fade(p)), 180f, sweep, false,
            topLeft = centre - Offset(radius, radius), size = Size(radius * 2, radius * 2), style = Stroke(stroke),
        )
    }
}

/** Stars spinning out from the middle. */
private fun DrawScope.stars(p: Float, r: Random) {
    val origin = Offset(size.width / 2, size.height * 0.45f)
    repeat(12) { i ->
        val a = (2 * PI * i / 12).toFloat() + p * 1.5f
        val d = FastOutSlowInEasing.transform(p) * size.minDimension * (0.3f + 0.15f * r.nextFloat())
        val at = origin + Offset(cos(a) * d, sin(a) * d)
        rotate(p * 540f + i * 30f, at) { drawPath(star(at, px(14) * (0.7f + p)), party[i % party.size].copy(alpha = fade(p))) }
    }
}

/** Bubbles rising, each with a sheen. */
private fun DrawScope.bubbles(p: Float, r: Random) {
    repeat(24) { i ->
        val x = size.width * r.nextFloat() + sin(p * 7f + i) * 10f * density
        val start = r.nextFloat() * 0.3f
        val q = ((p - start) / (1f - start)).coerceIn(0f, 1f)
        val y = size.height * (1.05f - q * (0.8f + r.nextFloat() * 0.4f))
        val radius = (10 + r.nextInt(14)) * density
        val a = fade(q, 0.75f)
        drawCircle(Color(0xFF8FD3FF).copy(alpha = 0.3f * a), radius, Offset(x, y))
        drawCircle(Color.White.copy(alpha = 0.8f * a), radius, Offset(x, y), style = Stroke(1.5f * density))
        drawCircle(Color.White.copy(alpha = 0.7f * a), radius / 4, Offset(x - radius / 3, y - radius / 3))
    }
}

/** Wavy streamers falling from the top. */
private fun DrawScope.streamers(p: Float, r: Random) {
    repeat(10) { i ->
        val x0 = size.width * (0.05f + 0.9f * i / 9f)
        val fall = size.height * (p * (0.7f + r.nextFloat() * 0.4f))
        val path = Path()
        val length = px(120)
        for (k in 0..20) {
            val y = fall - length + length * k / 20f
            val x = x0 + sin(k / 3f + p * 10f + i) * 10f * density
            if (k == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, party[i % party.size].copy(alpha = fade(p)), style = Stroke(px(5)))
    }
}

/** Four-pointed sparkles across the screen, each twinkling once: never more than three flashes a second. */
private fun DrawScope.sparkles(p: Float, r: Random) {
    repeat(28) { i ->
        val at = Offset(size.width * r.nextFloat(), size.height * (0.1f + 0.7f * r.nextFloat()))
        val start = r.nextFloat() * 0.6f
        val q = ((p - start) / 0.4f).coerceIn(0f, 1f)
        val s = sin(PI * q).toFloat() * px(12)
        if (s <= 0f) return@repeat
        val c = if (i % 3 == 0) Color(0xFFFFD740) else Color.White
        val path = Path().apply {
            moveTo(at.x, at.y - s)
            quadraticTo(at.x, at.y, at.x + s, at.y)
            quadraticTo(at.x, at.y, at.x, at.y + s)
            quadraticTo(at.x, at.y, at.x - s, at.y)
            quadraticTo(at.x, at.y, at.x, at.y - s)
            close()
        }
        drawPath(path, c)
    }
}

private fun star(c: Offset, radius: Float): Path = Path().apply {
    for (k in 0 until 10) {
        val a = (-PI / 2 + PI * k / 5).toFloat()
        val rr = if (k % 2 == 0) radius else radius * 0.45f
        val pt = Offset(c.x + cos(a) * rr, c.y + sin(a) * rr)
        if (k == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
    }
    close()
}

/** [v] density-independent pixels, in a Canvas. */
private fun DrawScope.px(v: Int): Float = v * density
