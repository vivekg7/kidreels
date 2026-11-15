package com.crylo.kidreels.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val confettiColours = listOf(
    Color(0xFFFF5252), Color(0xFFFFD740), Color(0xFF69F0AE), Color(0xFF40C4FF), Color(0xFFE040FB), Color(0xFFFF9100),
)

private class Bit(val angle: Float, val speed: Float, val spin: Float, val colour: Color, val w: Float, val h: Float)

/** A burst of confetti from the middle of the screen each time [trigger] goes up. */
@Composable
fun Confetti(trigger: Int, modifier: Modifier = Modifier.fillMaxSize()) {
    if (trigger == 0) return
    val t = remember(trigger) { Animatable(0f) }
    val bits = remember(trigger) {
        val r = Random(trigger)
        List(70) {
            Bit(
                angle = (r.nextFloat() * 2 * PI).toFloat(),
                speed = 0.35f + r.nextFloat() * 0.75f,
                spin = r.nextFloat() * 720f - 360f,
                colour = confettiColours[r.nextInt(confettiColours.size)],
                w = 6f + r.nextFloat() * 8f,
                h = 10f + r.nextFloat() * 10f,
            )
        }
    }
    LaunchedEffect(trigger) { t.animateTo(1f, tween(1700, easing = LinearEasing)) }
    if (t.value >= 1f) return
    Canvas(modifier) {
        val p = t.value
        val origin = Offset(size.width / 2, size.height * 0.45f)
        val reach = size.minDimension * 0.75f
        for (b in bits) {
            // Out fast, then fall: a little physics is what makes confetti look like confetti.
            val out = FastOutSlowInEasing.transform(p) * b.speed * reach
            val fall = p * p * size.height * 0.45f
            val pos = origin + Offset(cos(b.angle) * out, sin(b.angle) * out + fall)
            rotate(b.spin * p, pos) {
                drawRect(
                    b.colour.copy(alpha = 1f - p * p),
                    topLeft = pos - Offset(b.w * density / 4, b.h * density / 4),
                    size = Size(b.w * density / 2, b.h * density / 2),
                )
            }
        }
    }
}

/** Hearts float up from the like button. */
@Composable
fun HeartBurst(trigger: Int, modifier: Modifier = Modifier) {
    if (trigger == 0) return
    val t = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) { t.animateTo(1f, tween(1100)) }
    if (t.value >= 1f) return
    Box(modifier) {
        repeat(5) { i ->
            val drift = (i - 2) * 18f
            val p = (t.value * 1.2f - i * 0.05f).coerceIn(0f, 1f)
            T(
                "❤️",
                Modifier.graphicsLayer {
                    translationY = -p * 220f * density / 2
                    translationX = drift * p * density / 2
                    alpha = 1f - p
                    scaleX = 0.8f + p * 0.6f
                    scaleY = 0.8f + p * 0.6f
                },
                size = 26.sp,
            )
        }
    }
}

/** The sad face when a puzzle is swiped past unsolved. Gentle: a moment, then it goes. */
@Composable
fun SadBubble(trigger: Int, text: String, modifier: Modifier = Modifier) {
    if (trigger == 0) return
    val t = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) { t.animateTo(1f, tween(1800, easing = LinearEasing)) }
    if (t.value >= 1f) return
    val p = t.value
    val alpha = when {
        p < 0.12f -> p / 0.12f
        p > 0.8f -> (1f - p) / 0.2f
        else -> 1f
    }
    Column(
        modifier
            .graphicsLayer {
                this.alpha = alpha
                val s = if (p < 0.12f) 0.6f + p / 0.12f * 0.4f else 1f
                scaleX = s
                scaleY = s
                rotationZ = sin(p * 18f) * 6f * (1f - p)
            }
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xEE2B2440))
            .padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        T("🥺", size = 56.sp)
        T(text, color = Color.White, size = 16.sp, weight = FontWeight.Bold, align = TextAlign.Center)
    }
}

/** A bouncing "swipe up" once a reel is done, so the child knows what to do next. */
@Composable
fun SwipeHint(text: String, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition()
    val y by t.animateFloat(0f, -14f, infiniteRepeatable(tween(520), RepeatMode.Reverse))
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) { a.animateTo(1f, tween(400)) }
    // On its own dark pill: it can land over a reel's text, and must stay readable there.
    Row(
        modifier.graphicsLayer { translationY = y * density / 2; alpha = a.value }
            .clip(RoundedCornerShape(50)).background(Color(0xCC2B2440))
            .padding(start = 10.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        T("☝️", size = 24.sp)
        T(text, Modifier.padding(start = 6.dp), color = Color.White, size = 15.sp, weight = FontWeight.Bold)
    }
}

/** Small "z"s rising, for the sleeping moon on the goodbye reel. */
@Composable
fun Snooze(color: Color, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition()
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)))
    Box(modifier) {
        repeat(3) { i ->
            val q = (p + i / 3f) % 1f
            T(
                "z",
                Modifier.graphicsLayer {
                    translationX = (q * 50f + i * 6f) * density / 2
                    translationY = -q * 90f * density / 2
                    alpha = 1f - q
                    scaleX = 0.7f + q
                    scaleY = 0.7f + q
                },
                color = color,
                size = 22.sp,
                weight = FontWeight.Bold,
            )
        }
    }
}
