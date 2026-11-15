package com.crylo.kidreels.ui.reels

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.AngleKind
import com.crylo.kidreels.core.AngleLab
import com.crylo.kidreels.core.CurveKind
import com.crylo.kidreels.core.CurveLab
import com.crylo.kidreels.core.Phrases
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.SolidCard
import com.crylo.kidreels.ui.ReelState
import com.crylo.kidreels.ui.Sfx
import com.crylo.kidreels.ui.common.MultiText
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.popOn
import com.crylo.kidreels.ui.common.tap
import com.crylo.kidreels.ui.draw.drawMesh
import com.crylo.kidreels.ui.draw.meshOf
import com.crylo.kidreels.ui.skin.LocalReelColors
import com.crylo.kidreels.ui.skin.reelWidth
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sinh

/** A solid that turns slowly on its own and follows a finger when dragged. */
@Composable
fun SolidView(reel: SolidCard, padding: PaddingValues) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val solid = reel.solid
    val mesh = remember(solid.kind) { meshOf(solid.kind) }
    var yaw by remember(reel.id) { mutableFloatStateOf(0.6f) }
    var pitch by remember(reel.id) { mutableFloatStateOf(0.45f) }
    var dragging by remember(reel.id) { mutableStateOf(false) }

    LaunchedEffect(env.active, dragging) {
        if (!env.active || dragging) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            yaw += (now - last) / 1e9f * 0.6f
            last = now
        }
    }

    Column(
        Modifier.fillMaxSize().padding(padding).reelWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        MultiText(solid.name, env.langs, colors.ink, size = 40.sp, say = { env.say(it) })
        // The drag area is the solid only, so swipes elsewhere still move the feed.
        Canvas(
            Modifier.fillMaxWidth(0.85f).aspectRatio(1f)
                .pointerInput(reel.id) {
                    detectDragGestures(
                        onDragStart = { dragging = true },
                        onDragEnd = { dragging = false },
                        onDragCancel = { dragging = false },
                    ) { change, drag ->
                        change.consume()
                        yaw += drag.x / size.width * 4f
                        pitch = (pitch + drag.y / size.height * 4f).coerceIn(-1.4f, 1.4f)
                    }
                }
                .pointerInput(reel.id) {
                    // The fact is said only where it is shown: faces and edges are beyond a five-year-old.
                    detectTapGestures { env.say(listOfNotNull(Say(solid.name[env.main], env.main), Say(solid.fact[env.main], env.main).takeIf { reel.showFact })) }
                },
        ) {
            drawMesh(mesh, yaw, pitch, Color(reel.argb), Color(0xFF2D2440))
        }
        if (reel.showFact) MultiText(solid.fact, env.langs, colors.ink, size = 19.sp, say = { env.say(it) })
        if (solid.examples.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                solid.examples.forEach { T(it, size = 46.sp) }
            }
        }
    }
}

/**
 * The fixed arm points right from the corner; the child drags the other arm round. The
 * angle's name and size update as it moves, and are said when the finger lifts.
 */
@Composable
fun AngleView(reel: AngleLab, state: ReelState, padding: PaddingValues, onSolved: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    var degrees by remember(reel.id) { mutableFloatStateOf(reel.startDegrees.toFloat()) }
    val shown = AngleKind.snap(degrees.roundToInt())
    val kind = AngleKind.of(degrees.roundToInt())
    val measurer = rememberTextMeasurer()
    val armColor = Color(0xFF4F7DF3)

    fun released() {
        val k = AngleKind.of(degrees.roundToInt())
        val target = reel.target
        if (target != null && !state.solved && k == target) {
            env.sfx(Sfx.CORRECT)
            env.praise(k.title, null)
            onSolved()
        } else {
            env.say(listOf(Say("$shown°", env.main), Say(k.title[env.main], env.main)))
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(padding).reelWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            MultiText(reel.prompt, env.langs, colors.ink, size = 26.sp, say = { env.say(it) })
            Canvas(
                Modifier.fillMaxWidth().aspectRatio(1f).pointerInput(reel.id) {
                    detectDragGestures(onDragEnd = { released() }) { change, _ ->
                        change.consume()
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val p = change.position - c
                        var a = (atan2(-p.y, p.x) * 180f / PI.toFloat())
                        if (a < 0) a += 360f
                        degrees = a.coerceIn(1f, 359f)
                    }
                },
            ) {
                val c = center
                val r = size.minDimension * 0.42f
                val rad = shown * PI.toFloat() / 180f
                val end = Offset(c.x + cos(rad) * r, c.y - sin(rad) * r)
                val arcR = r * 0.32f
                drawArc(
                    armColor.copy(alpha = 0.25f), 0f, -shown.toFloat(), true,
                    topLeft = c - Offset(arcR, arcR), size = Size(arcR * 2, arcR * 2),
                )
                if (kind == AngleKind.RIGHT) {
                    val s = r * 0.14f
                    drawRect(Color.White, topLeft = Offset(c.x, c.y - s), size = Size(s, s), style = Stroke(r * 0.02f))
                }
                drawLine(colors.ink, c, Offset(c.x + r, c.y), r * 0.05f, StrokeCap.Round)
                drawLine(armColor, c, end, r * 0.05f, StrokeCap.Round)
                drawCircle(armColor, r * 0.09f, end)
                drawCircle(Color.White, r * 0.04f, end)
                drawCircle(colors.ink, r * 0.04f, c)
                val mid = (shown / 2f) * PI.toFloat() / 180f
                val label = measurer.measure("$shown°", TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = colors.ink))
                val lp = Offset(c.x + cos(mid) * arcR * 1.9f, c.y - sin(mid) * arcR * 1.9f)
                drawText(label, topLeft = lp - Offset(label.size.width / 2f, label.size.height / 2f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MultiText(kind.title, env.langs, if (state.solved) colors.good else colors.ink, Modifier.popOn(kind.ordinal + 1), size = 30.sp, say = { env.say(it) })
                MultiText(kind.rule, env.langs, colors.ink, size = 17.sp)
            }
        }
    }
}

/** A curve that draws itself; the slider stretches it, and it draws again. */
@Composable
fun CurveView(reel: CurveLab, padding: PaddingValues) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val curve = reel.curve
    var param by remember(reel.id) { mutableFloatStateOf(0.45f) }
    val trace = remember(reel.id) { Animatable(0f) }
    val ball = remember(reel.id) { Animatable(0f) }
    LaunchedEffect(env.active, reel.id) {
        if (!env.active) return@LaunchedEffect
        trace.snapTo(0f)
        trace.animateTo(1f, tween(2200, easing = LinearEasing))
        if (curve.kind == CurveKind.PARABOLA) {
            while (true) {
                ball.snapTo(0f)
                ball.animateTo(1f, tween(1800, easing = LinearEasing))
            }
        }
    }
    val lineColor = Color(0xFFE5484D)
    Column(
        Modifier.fillMaxSize().padding(padding).reelWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        MultiText(curve.name, env.langs, colors.ink, size = 38.sp, say = { env.say(it) })
        val measurer = rememberTextMeasurer()
        Canvas(Modifier.fillMaxWidth().aspectRatio(1.1f)) {
            val axis = colors.ink.copy(alpha = 0.25f)
            drawLine(axis, Offset(0f, center.y), Offset(size.width, center.y), 2f)
            drawLine(axis, Offset(center.x, 0f), Offset(center.x, size.height), 2f)
            val unit = size.minDimension * 0.45f
            fun pt(x: Float, y: Float) = Offset(center.x + x * unit, center.y - y * unit)
            val stroke = Stroke(size.minDimension * 0.022f, cap = StrokeCap.Round)
            when (curve.kind) {
                CurveKind.CIRCLE -> {
                    val r = 0.35f + 0.6f * param
                    drawArc(lineColor, 0f, -360f * trace.value, false, topLeft = pt(-r, r), size = Size(2 * r * unit, 2 * r * unit), style = stroke)
                    val a = 2 * PI.toFloat() * trace.value
                    drawLine(colors.ink, pt(0f, 0f), pt(cos(a) * r, sin(a) * r), 3f)
                    drawCircle(colors.ink, 6f, pt(0f, 0f))
                }
                CurveKind.ELLIPSE -> {
                    val a = 0.95f
                    val b = a * (1f - 0.7f * param)
                    drawArc(lineColor, 0f, -360f * trace.value, false, topLeft = pt(-a, b), size = Size(2 * a * unit, 2 * b * unit), style = stroke)
                    val f = kotlin.math.sqrt(a * a - b * b)
                    drawCircle(Color(0xFFFFB300), 10f, pt(-f, 0f))
                    drawCircle(Color(0xFFFFB300), 10f, pt(f, 0f))
                }
                CurveKind.PARABOLA -> {
                    // Opening downward, like a throw: highest in the middle.
                    val k = 0.6f + 2.2f * param
                    val half = kotlin.math.sqrt(1.7f / k).coerceAtMost(1.05f)
                    val path = curvePath(160, trace.value) { t -> val x = -half + 2 * half * t; pt(x, 0.85f - k * x * x) }
                    drawPath(path, lineColor, style = stroke)
                    if (trace.value >= 1f) {
                        val x = -half + 2 * half * ball.value
                        val label = measurer.measure("🏀", TextStyle(fontSize = 26.sp))
                        drawText(label, topLeft = pt(x, 0.85f - k * x * x) - Offset(label.size.width / 2f, label.size.height / 2f))
                    }
                }
                CurveKind.HYPERBOLA -> {
                    val a = 0.2f + 0.45f * param
                    val b = 0.5f
                    val dash = PathEffect.dashPathEffect(floatArrayOf(14f, 12f))
                    drawLine(axis, pt(-1.1f, -1.1f * b / a), pt(1.1f, 1.1f * b / a), 3f, pathEffect = dash)
                    drawLine(axis, pt(-1.1f, 1.1f * b / a), pt(1.1f, -1.1f * b / a), 3f, pathEffect = dash)
                    val tMax = 2.2f
                    for (side in listOf(1f, -1f)) {
                        val path = curvePath(120, trace.value) { t ->
                            val u = -tMax + 2 * tMax * t
                            pt(side * a * cosh(u), b * sinh(u))
                        }
                        drawPath(path, lineColor, style = stroke)
                    }
                }
            }
        }
        KidSlider(param, { param = it }, colors.ink, Modifier.fillMaxWidth(0.8f))
        MultiText(curve.fact, env.langs, colors.ink, size = 19.sp, say = { env.say(it) })
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            T(curve.example, size = 40.sp)
            if (reel.showEquation) T(curve.equation, color = colors.ink, size = 22.sp, weight = FontWeight.Bold)
        }
    }
}

private fun DrawScope.curvePath(steps: Int, upTo: Float, point: (Float) -> Offset): Path {
    val path = Path()
    val last = (steps * upTo).roundToInt()
    val bounds = Rect(-size.width, -size.height, size.width * 2, size.height * 2)
    for (i in 0..last) {
        val p = point(i / steps.toFloat())
        val clamped = Offset(p.x.coerceIn(bounds.left, bounds.right), p.y.coerceIn(bounds.top, bounds.bottom))
        if (i == 0) path.moveTo(clamped.x, clamped.y) else path.lineTo(clamped.x, clamped.y)
    }
    return path
}

/** A chunky slider for small fingers. Horizontal drags only, so the feed still swipes. */
@Composable
fun KidSlider(value: Float, onChange: (Float) -> Unit, color: Color, modifier: Modifier = Modifier) {
    val env = LocalReelEnv.current
    BoxWithConstraints(modifier.height(48.dp)) {
        Canvas(
            Modifier.fillMaxSize().pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    onChange((change.position.x / size.width).coerceIn(0f, 1f))
                }
            },
        ) {
            val y = size.height / 2
            drawLine(color.copy(alpha = 0.3f), Offset(0f, y), Offset(size.width, y), size.height * 0.22f, StrokeCap.Round)
            drawLine(color, Offset(0f, y), Offset(size.width * value, y), size.height * 0.22f, StrokeCap.Round)
            drawCircle(Color.White, size.height * 0.42f, Offset(size.width * value, y))
            drawCircle(color, size.height * 0.42f, Offset(size.width * value, y), style = Stroke(size.height * 0.08f))
        }
    }
    T(Phrases.stretchIt[env.main], color = color.copy(alpha = 0.7f), size = 13.sp)
}
