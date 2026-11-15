package com.crylo.kidreels.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.Txt
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Plain text. The app uses foundation only (no Material), so this is its one text call. */
@Composable
fun T(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 16.sp,
    color: Color = Color.Unspecified,
    weight: FontWeight = FontWeight.Normal,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    BasicText(
        text,
        modifier,
        style = TextStyle(fontSize = size, color = color, fontWeight = weight, textAlign = align, lineHeight = lineHeight),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * The same line in every chosen language: the main one big, the others smaller under it.
 * Tapping a line says it in that line's language. Identical lines (a numeral, a word that
 * reads the same) are shown once.
 */
@Composable
fun MultiText(
    text: Txt,
    langs: List<Lang>,
    color: Color,
    modifier: Modifier = Modifier,
    size: TextUnit = 26.sp,
    say: ((Say) -> Unit)? = null,
    first: Lang = langs.first(),
) {
    val order = listOf(first) + langs.filter { it != first }
    val lines = order.map { it to text[it] }.distinctBy { it.second }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        lines.forEachIndexed { i, (lang, line) ->
            val tap = if (say != null) Modifier.tap { say(Say(line, lang)) } else Modifier
            T(
                line,
                tap.padding(horizontal = 8.dp, vertical = 2.dp),
                size = if (i == 0) size else size * 0.62f,
                color = if (i == 0) color else color.copy(alpha = 0.78f),
                weight = if (i == 0) FontWeight.ExtraBold else FontWeight.SemiBold,
                align = TextAlign.Center,
                lineHeight = if (i == 0) size * 1.15f else size * 0.75f,
            )
        }
    }
}

/** A tap with no ripple: kid-facing buttons give their own feedback. */
fun Modifier.tap(onClick: () -> Unit): Modifier = this.then(
    Modifier.pointerInput(onClick) { detectTapGestures(onTap = { onClick() }) },
)

/** Shrinks slightly while pressed, then springs back. */
@Composable
fun Modifier.pressable(onClick: () -> Unit, enabled: Boolean = true): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
}

/** A gentle up-and-down float, for pictures that should feel alive. */
@Composable
fun Modifier.floating(amount: Float = 8f, millis: Int = 1400): Modifier {
    val t = rememberInfiniteTransition()
    val y by t.animateFloat(-amount, amount, infiniteRepeatable(tween(millis), RepeatMode.Reverse))
    return this.graphicsLayer { translationY = y * density / 2f }
}

/** Shakes sideways each time [key] changes (but not on first show). */
@Composable
fun Modifier.shakeOn(key: Int): Modifier {
    val x = remember { Animatable(0f) }
    LaunchedEffect(key) {
        if (key == 0) return@LaunchedEffect
        for (v in listOf(18f, -16f, 12f, -8f, 4f, 0f)) x.animateTo(v, tween(45))
    }
    return this.graphicsLayer { translationX = x.value * density / 2f }
}

/** Pops bigger and back each time [key] changes. */
@Composable
fun Modifier.popOn(key: Int): Modifier {
    val s = remember { Animatable(1f) }
    LaunchedEffect(key) {
        if (key == 0) return@LaunchedEffect
        s.snapTo(1f)
        s.animateTo(1.25f, tween(110))
        s.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    return this.graphicsLayer { scaleX = s.value; scaleY = s.value }
}

/**
 * The parent gate's button: it only fires after being held for [millis]. Toddlers tap;
 * they rarely hold still on one spot for three seconds.
 */
@Composable
fun HoldButton(
    label: String,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    millis: Int = 3000,
    background: Color = Color(0xFF6C4CF5),
    content: Color = Color.White,
) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val holding = remember { arrayOfNulls<Job>(1) }
    Box(
        modifier
            .clip(RoundedCornerShape(28.dp))
            .background(background)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    holding[0]?.cancel()
                    holding[0] = scope.launch {
                        progress.snapTo(0f)
                        progress.animateTo(1f, tween(millis, easing = LinearEasing))
                        onDone()
                        progress.snapTo(0f)
                    }
                    tryAwaitRelease()
                    if (progress.value < 1f) {
                        holding[0]?.cancel()
                        scope.launch { progress.animateTo(0f, tween(200)) }
                    }
                })
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            if (progress.value > 0f) {
                drawRoundRectProgress(progress.value, content.copy(alpha = 0.28f))
            }
        }
        T(label, color = content, weight = FontWeight.Bold, size = 16.sp, align = TextAlign.Center)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundRectProgress(fraction: Float, color: Color) {
    drawRoundRect(
        color,
        size = size.copy(width = size.width * fraction),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2),
    )
}

/** A small ring that fills: used for the time left today. */
@Composable
fun Ring(fraction: Float, color: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier) {
        val stroke = size.minDimension * 0.18f
        drawCircle(color.copy(alpha = 0.3f), style = Stroke(stroke))
        drawArc(color, -90f, 360f * fraction.coerceIn(0f, 1f), false, style = Stroke(stroke, cap = StrokeCap.Round))
    }
}

/** A thin bar along the bottom, like a video's progress line. */
@Composable
fun ProgressLine(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(3.dp)) {
        drawLine(color.copy(alpha = 0.25f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), size.height)
        drawLine(color, Offset(0f, size.height / 2), Offset(size.width * fraction.coerceIn(0f, 1f), size.height / 2), size.height)
    }
}
