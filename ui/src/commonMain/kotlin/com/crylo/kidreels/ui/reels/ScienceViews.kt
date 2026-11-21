package com.crylo.kidreels.ui.reels

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Lab
import com.crylo.kidreels.core.LabReel
import com.crylo.kidreels.core.LabTarget
import com.crylo.kidreels.core.Plant
import com.crylo.kidreels.core.PlanetCard
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.Science
import com.crylo.kidreels.core.Stage
import com.crylo.kidreels.core.Tone
import com.crylo.kidreels.core.Txt
import com.crylo.kidreels.core.txt
import com.crylo.kidreels.ui.Sfx
import com.crylo.kidreels.ui.art.Arts
import com.crylo.kidreels.ui.art.drawArt
import com.crylo.kidreels.ui.common.Confetti
import com.crylo.kidreels.ui.common.MultiText
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.floating
import com.crylo.kidreels.ui.common.popOn
import com.crylo.kidreels.ui.common.tap
import com.crylo.kidreels.ui.skin.LocalReelColors
import com.crylo.kidreels.ui.skin.reelWidth
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin

private val SunYellow = Color(0xFFFFC107)
private val WaterBlue = Color(0xFF29B6F6)
private val Oxygen = Color(0xFF66BB6A)
private val Ring = Color(0xFFFFFFFF)

/** Where things are in a lab's scene, in pixels: each target's centre and how far a tap may be from it. */
private class Scene(val targets: Map<LabTarget, List<Pair<Offset, Float>>>, val labels: List<Offset>)

/**
 * A science animation stepped through by taps: tap the sun, and its light reaches the
 * leaves; tap the cloud, and water rises through the plant. Only the next thing to tap
 * does anything, ringed so it can be found, and every step keeps moving once started, so
 * the whole process is on screen at the end.
 */
@Composable
fun LabView(reel: LabReel, padding: PaddingValues) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val lab = reel.lab
    var step by remember(reel.id) { mutableIntStateOf(0) }
    val done = step == lab.steps.size
    val t by rememberInfiniteTransition().animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing)))
    val pulse by rememberInfiniteTransition().animateFloat(1f, 1.3f, infiniteRepeatable(tween(650), RepeatMode.Reverse))
    // The newest step fades in (the cloud grows, it greys), so each tap visibly adds one thing.
    val grow = remember(reel.id) { Animatable(1f) }
    LaunchedEffect(step) {
        if (step > 0) {
            grow.snapTo(0f)
            grow.animateTo(1f, tween(400))
        }
    }

    fun tapped(target: LabTarget?) {
        val now = lab.steps.getOrNull(step) ?: return
        if (target != now.target) {
            env.say(txt("Tap ${now.name[com.crylo.kidreels.core.Lang.EN]}!", "${now.name[com.crylo.kidreels.core.Lang.HI]} को छुओ!"))
            return
        }
        step++
        val next = lab.steps.getOrNull(step)
        env.sfx(if (next == null) Sfx.CORRECT else Sfx.TICK)
        val lines = mutableListOf(Say(reel.said(now)[env.main], env.main))
        if (next != null) {
            lines += Say(txt("Now tap ${next.name[com.crylo.kidreels.core.Lang.EN]}.", "अब ${next.name[com.crylo.kidreels.core.Lang.HI]} को छुओ।")[env.main], env.main)
        } else {
            lines += Say(lab.outro[env.main], env.main, Tone.CHEER)
        }
        env.say(lines)
    }
    val onTap by rememberUpdatedState(::tapped)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(padding).reelWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            MultiText(reel.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 26.sp, say = { env.say(it) })
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(20.dp))) {
                val density = LocalDensity.current
                val w = with(density) { maxWidth.toPx() }
                val h = with(density) { maxHeight.toPx() }
                val scene = remember(w, h, lab) { if (lab == Lab.PHOTOSYNTHESIS) photoScene(w, h) else waterScene(w, h) }
                Canvas(
                    Modifier.fillMaxSize().pointerInput(scene) {
                        detectTapGestures { p ->
                            // The step's own target first: the sky's area takes in the cloud that forms in it.
                            fun inside(t: LabTarget) = scene.targets.getValue(t).any { (c, r) -> hypot(p.x - c.x, p.y - c.y) <= r }
                            val wanted = lab.steps.getOrNull(step)?.target
                            onTap(if (wanted != null && inside(wanted)) wanted else scene.targets.keys.firstOrNull(::inside))
                        }
                    },
                ) {
                    if (lab == Lab.PHOTOSYNTHESIS) drawPhoto(step, t, grow.value) else drawWater(step, t, grow.value)
                    lab.steps.getOrNull(step)?.let { now ->
                        scene.targets.getValue(now.target).forEach { (c, r) ->
                            drawCircle(Ring, r * 0.85f * pulse, c, style = Stroke(3.dp.toPx()))
                        }
                    }
                }
                // Each finished step's word, by what it names.
                lab.steps.take(step).forEachIndexed { i, s ->
                    val at = scene.labels[i]
                    Box(Modifier.offset(with(density) { at.x.toDp() } - 60.dp, with(density) { at.y.toDp() } - 14.dp).size(120.dp, 28.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.92f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            T(reel.label(s)[env.main], Modifier.popOn(1), color = Color(0xFF2D2440), size = 13.sp, weight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
                if (done && lab == Lab.PHOTOSYNTHESIS) {
                    val at = scene.labels.last()
                    val word = if (reel.words) txt("oxygen", "ऑक्सीजन") else txt("fresh air", "ताज़ी हवा")
                    Box(Modifier.offset(with(density) { at.x.toDp() } - 60.dp, with(density) { at.y.toDp() } - 14.dp).size(120.dp, 28.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.92f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            T(word[env.main], Modifier.popOn(1), color = Color(0xFF2E7D32), size = 13.sp, weight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
                val now = lab.steps.getOrNull(step)
                val line: Txt = if (now == null) lab.outro else txt("Tap ${now.name[com.crylo.kidreels.core.Lang.EN]}", "${now.name[com.crylo.kidreels.core.Lang.HI]} को छुओ")
                MultiText(line, env.langs, colors.ink, size = 18.sp, say = { env.say(it) })
            }
        }
        Confetti(if (done) 1 else 0)
    }
}

// ── How plants make food ──

/** The plant fills the lower part of the scene; these map its drawing's units onto the scene. */
private fun plantScale(w: Float, h: Float) = minOf(w / 240f, h * 0.8f / 360f)

private fun plantOrigin(w: Float, h: Float) = plantScale(w, h).let { s -> Offset((w - 240f * s) / 2, h - 360f * s) }

private fun plantPoint(w: Float, h: Float, x: Float, y: Float) = plantOrigin(w, h) + Offset(x * plantScale(w, h), y * plantScale(w, h))

private val leafSpots get() = Plant.parts.first { it.key == "leaf" }.spots

private fun photoScene(w: Float, h: Float): Scene {
    val r = minOf(w, h) * 0.11f
    val leaves = leafSpots.map { (x, y) -> plantPoint(w, h, x, y) to r * 0.8f }
    return Scene(
        mapOf(
            LabTarget.SUN to listOf(Offset(w * 0.16f, h * 0.12f) to r),
            LabTarget.CLOUD to listOf(Offset(w * 0.8f, h * 0.11f) to r * 1.2f),
            LabTarget.AIR to listOf(Offset(w * 0.12f, h * 0.45f) to r),
            LabTarget.LEAF to leaves,
        ),
        labels = listOf(
            Offset(w * 0.34f, h * 0.22f),
            plantPoint(w, h, 150f, 250f),
            Offset(w * 0.16f, h * 0.55f),
            plantPoint(w, h, 165f, 225f),
            Offset(w * 0.72f, h * 0.32f),
        ),
    )
}

/** [fresh] (0 to 1) is how far the newest step has faded in. */
private fun DrawScope.drawPhoto(step: Int, t: Float, fresh: Float) {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(Color(0xFFB3E5FC), Color(0xFFE1F5FE))))
    val s = plantScale(w, h)
    val o = plantOrigin(w, h)
    // The ground runs the width of the scene; the plant's own drawing only covers its width.
    drawRect(Color(0xFF8D6E63), Offset(0f, o.y + 262f * s), Size(w, h))
    drawRect(Color(0xFF7CB342), Offset(0f, o.y + 256f * s), Size(w, 10f * s))
    translate(o.x, o.y) { drawArt(Arts.plant, s) }
    val sun = Offset(w * 0.16f, h * 0.12f)
    val cloud = Offset(w * 0.8f, h * 0.11f)
    val air = Offset(w * 0.12f, h * 0.45f)
    val r = minOf(w, h) * 0.11f
    val leaves = leafSpots.map { (x, y) -> plantPoint(w, h, x, y) }
    fun p(x: Float, y: Float) = plantPoint(w, h, x, y)
    /** How far step [n] is in: the newest one is still fading in. */
    fun shown(n: Int) = if (n == step) fresh else 1f

    // The sun, with short rays all round.
    for (i in 0 until 12) {
        val a = i * PI.toFloat() / 6
        val d = Offset(kotlin.math.cos(a), kotlin.math.sin(a))
        drawLine(SunYellow, sun + d * (r * 0.75f), sun + d * (r * 1.05f), 4.dp.toPx(), StrokeCap.Round)
    }
    drawCircle(SunYellow, r * 0.62f, sun)
    drawCloud(cloud, r, Color.White)
    // The air: three curls of wind.
    for (i in -1..1) {
        val y = air.y + i * r * 0.35f
        val path = Path().apply {
            moveTo(air.x - r * 0.7f, y)
            cubicTo(air.x - r * 0.2f, y - r * 0.25f, air.x + r * 0.2f, y + r * 0.25f, air.x + r * 0.7f, y)
        }
        drawPath(path, Color(0xFF78909C), style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
    }

    if (step >= 1) {
        // Sunlight reaching each leaf, the dashes running towards it.
        val dash = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), -t * 64f)
        leaves.forEach { drawLine(SunYellow.copy(alpha = shown(1)), sun, it, 3.dp.toPx(), StrokeCap.Round, pathEffect = dash) }
    }
    if (step >= 2) {
        // Rain into the soil, then water up the roots and the stem to the leaves.
        for (i in 0 until 7) {
            val x = cloud.x + (i - 3) * r * 0.22f
            val y = cloud.y + r * 0.5f + ((t + i / 7f) % 1f) * (p(0f, 262f).y - cloud.y - r * 0.5f)
            drawLine(WaterBlue.copy(alpha = shown(2)), Offset(x, y), Offset(x, y + 10.dp.toPx()), 3.dp.toPx(), StrokeCap.Round)
        }
        val route = listOf(p(120f, 340f), p(120f, 262f), p(120f, 200f))
        for (leaf in leaves) for (i in 0 until 3) {
            drawCircle(WaterBlue.copy(alpha = shown(2)), 4.dp.toPx(), along(route + leaf, (t + i / 3f) % 1f))
        }
    }
    if (step >= 3) {
        // Air drifting into the leaves.
        for (leaf in leaves.take(2)) for (i in 0 until 3) {
            val f = (t + i / 3f) % 1f
            drawCircle(Color(0xFF90A4AE).copy(alpha = shown(3)), 5.dp.toPx(), air + (leaf - air) * f)
        }
    }
    if (step >= 4) {
        // Food made in the leaves going down the stem, and oxygen bubbling out into the air.
        val down = listOf(p(120f, 180f), p(120f, 262f), p(120f, 320f))
        for (i in 0 until 4) {
            val c = along(down, (t + i / 4f) % 1f)
            drawRect(Color.White.copy(alpha = shown(4)), c - Offset(4.dp.toPx(), 4.dp.toPx()), Size(8.dp.toPx(), 8.dp.toPx()))
        }
        for (leaf in leaves) for (i in 0 until 3) {
            val f = (t + i / 3f) % 1f
            val c = Offset(leaf.x + sin(f * 6f + i) * 10.dp.toPx(), leaf.y - f * h * 0.3f)
            drawCircle(Oxygen.copy(alpha = (1f - f) * shown(4)), 6.dp.toPx(), c, style = Stroke(2.5.dp.toPx()))
        }
    }
}

/** A point [f] (0 to 1) of the way along a path through [points]. */
private fun along(points: List<Offset>, f: Float): Offset {
    val legs = points.zipWithNext()
    val lengths = legs.map { (a, b) -> (b - a).getDistance() }
    var d = f * lengths.sum()
    for ((i, leg) in legs.withIndex()) {
        if (d <= lengths[i]) return leg.first + (leg.second - leg.first) * (d / lengths[i]).coerceIn(0f, 1f)
        d -= lengths[i]
    }
    return points.last()
}

private fun DrawScope.drawCloud(c: Offset, r: Float, colour: Color) {
    drawCircle(colour, r * 0.45f, c + Offset(-r * 0.45f, r * 0.1f))
    drawCircle(colour, r * 0.6f, c + Offset(0f, -r * 0.1f))
    drawCircle(colour, r * 0.45f, c + Offset(r * 0.5f, r * 0.12f))
    drawRect(colour, c + Offset(-r * 0.45f, 0f), Size(r * 0.95f, r * 0.55f))
}

// ── The water cycle ──

private fun waterScene(w: Float, h: Float): Scene {
    val r = minOf(w, h) * 0.11f
    return Scene(
        mapOf(
            LabTarget.SUN to listOf(Offset(w * 0.15f, h * 0.12f) to r),
            LabTarget.SKY to listOf(Offset(w * 0.5f, h * 0.24f) to r * 1.6f),
            LabTarget.CLOUD to listOf(Offset(w * 0.66f, h * 0.2f) to r * 1.2f),
            LabTarget.RIVER to listOf(Offset(w * 0.66f, h * 0.6f) to r),
        ),
        labels = listOf(
            Offset(w * 0.27f, h * 0.52f),
            Offset(w * 0.66f, h * 0.06f),
            Offset(w * 0.86f, h * 0.36f),
            Offset(w * 0.5f, h * 0.8f),
        ),
    )
}

private fun riverPath(w: Float, h: Float) = listOf(
    Offset(w * 0.8f, h * 0.42f), Offset(w * 0.74f, h * 0.52f), Offset(w * 0.66f, h * 0.6f),
    Offset(w * 0.6f, h * 0.68f), Offset(w * 0.5f, h * 0.73f),
)

/** [fresh] (0 to 1) is how far the newest step has faded in. */
private fun DrawScope.drawWater(step: Int, t: Float, fresh: Float) {
    val w = size.width
    val h = size.height
    val r = minOf(w, h) * 0.11f
    drawRect(Brush.verticalGradient(listOf(Color(0xFF81D4FA), Color(0xFFE1F5FE))))
    // Land with a mountain on the right, the sea on the left.
    val land = Path().apply {
        moveTo(w * 0.45f, h * 0.72f)
        lineTo(w * 0.62f, h * 0.55f)
        lineTo(w * 0.82f, h * 0.32f)
        lineTo(w, h * 0.5f)
        lineTo(w, h)
        lineTo(w * 0.45f, h)
        close()
    }
    drawPath(land, Color(0xFF8D6E63))
    drawPath(Path().apply { moveTo(w * 0.76f, h * 0.39f); lineTo(w * 0.82f, h * 0.32f); lineTo(w * 0.88f, h * 0.39f); close() }, Color.White)
    drawRect(Color(0xFF7CB342), Offset(w * 0.45f, h * 0.9f), Size(w * 0.55f, h * 0.1f))
    drawRect(Color(0xFF1E88E5), Offset(0f, h * 0.72f), Size(w * 0.55f, h * 0.28f))
    val river = riverPath(w, h)
    drawPath(Path().apply { moveTo(river[0].x, river[0].y); river.drop(1).forEach { lineTo(it.x, it.y) } }, Color(0xFF42A5F5), style = Stroke(9.dp.toPx(), cap = StrokeCap.Round))
    val sun = Offset(w * 0.15f, h * 0.12f)
    for (i in 0 until 12) {
        val a = i * PI.toFloat() / 6
        val d = Offset(kotlin.math.cos(a), kotlin.math.sin(a))
        drawLine(SunYellow, sun + d * (r * 0.75f), sun + d * (r * 1.05f), 4.dp.toPx(), StrokeCap.Round)
    }
    drawCircle(SunYellow, r * 0.62f, sun)
    /** How far step [n] is in: the newest one is still fading in. */
    fun shown(n: Int) = if (n == step) fresh else 1f

    if (step >= 1) {
        // Vapour rising off the sea in wavy wisps.
        for (i in 0 until 5) {
            val f = (t + i / 5f) % 1f
            val x = w * (0.08f + i * 0.09f)
            val y = h * 0.7f - f * h * 0.42f
            val path = Path().apply {
                moveTo(x, y)
                for (k in 1..4) lineTo(x + sin(k * 1.6f + f * 6f) * 6.dp.toPx(), y - k * 6.dp.toPx())
            }
            drawPath(path, Color.White.copy(alpha = (1f - f) * shown(1)), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        }
    }
    val cloud = Offset(w * 0.66f, h * 0.2f)
    // The cloud grows in (solid: a see-through cloud shows its overlapping puffs), then
    // greys as it fills with rain.
    if (step >= 2) drawCloud(cloud, r * 1.1f * shown(2), lerp(Color.White, Color(0xFF90A4AE), if (step >= 3) shown(3) else 0f))
    if (step >= 3) {
        for (i in 0 until 8) {
            val x = cloud.x + (i - 3.5f) * r * 0.22f
            val y = cloud.y + r * 0.55f + ((t + i / 8f) % 1f) * h * 0.2f
            drawLine(WaterBlue.copy(alpha = shown(3)), Offset(x, y), Offset(x - 3.dp.toPx(), y + 12.dp.toPx()), 3.dp.toPx(), StrokeCap.Round)
        }
    }
    if (step >= 4) {
        for (i in 0 until 5) drawCircle(Color.White.copy(alpha = shown(4)), 4.dp.toPx(), along(river, (t + i / 5f) % 1f))
    }
}

// ── Float or sink, magnets ──

/** A tank of water; once answered, the thing drops in and bobs at the top or settles on the bottom. */
@Composable
fun TankStage(stage: Stage.Tank, solved: Boolean) {
    val drop by animateFloatAsState(if (solved) 1f else 0f, tween(1100))
    val bob by rememberInfiniteTransition().animateFloat(-1f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse))
    BoxWithConstraints(Modifier.fillMaxWidth().height(230.dp), contentAlignment = Alignment.TopCenter) {
        val tankW = minOf(maxWidth * 0.75f, 280.dp)
        Box(Modifier.size(tankW, 230.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val top = size.height * 0.38f
                drawRoundRect(Color(0xFF4FC3F7).copy(alpha = 0.85f), Offset(0f, top), Size(size.width, size.height - top), androidx.compose.ui.geometry.CornerRadius(18f))
                drawRoundRect(Color.White, Offset(0f, size.height * 0.2f), Size(size.width, size.height * 0.8f), androidx.compose.ui.geometry.CornerRadius(18f), style = Stroke(4.dp.toPx()))
                drawLine(Color.White.copy(alpha = 0.8f), Offset(8f, top), Offset(size.width - 8f, top), 2.dp.toPx())
            }
            // From above the tank to the waterline, then on down to the bottom if it sinks.
            val start = 0.dp
            val surface = 230.dp * 0.38f - 36.dp
            val bottom = 230.dp - 70.dp
            val y = if (stage.floats) start + (surface - start) * drop + (if (solved) 3.dp * bob else 0.dp)
            else if (drop < 0.4f) start + (surface - start) * (drop / 0.4f) else surface + (bottom - surface) * ((drop - 0.4f) / 0.6f)
            Box(Modifier.align(Alignment.TopCenter).offset(y = y)) { Pic(stage.emoji, 50.sp) }
        }
    }
}

/** A magnet above the thing; once answered, the thing jumps up to it, or only wobbles. */
@Composable
fun MagnetStage(stage: Stage.Magnet, solved: Boolean) {
    val pull by animateFloatAsState(if (solved) 1f else 0f, tween(if (stage.sticks) 450 else 900))
    Box(Modifier.fillMaxWidth().height(230.dp), contentAlignment = Alignment.TopCenter) {
        // The emoji's horseshoe opens to the right; a quarter turn points it at the thing below.
        T("🧲", Modifier.graphicsLayer { rotationZ = 90f }, size = 64.sp)
        val y = if (stage.sticks) 160.dp - 82.dp * pull else 160.dp - 10.dp * sin(pull * PI.toFloat() * 3).coerceAtLeast(0f)
        Box(Modifier.offset(y = y)) { Pic(stage.emoji, 48.sp) }
    }
}

// ── The solar system ──

/** A planet (or the Sun, or the Moon) as a shaded ball, Saturn with its rings; tap to give it a spin. */
@Composable
fun PlanetView(reel: PlanetCard, padding: PaddingValues) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val p = reel.planet
    var spins by remember(reel.id) { mutableIntStateOf(0) }
    val turn by animateFloatAsState(spins * 360f, tween(1200))
    Column(
        Modifier.fillMaxSize().padding(padding).reelWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        MultiText(p.name, env.langs, colors.ink, size = 40.sp, say = { env.say(it) })
        p.order?.let { MultiText(it, env.langs, colors.ink, size = 20.sp, say = { line -> env.say(line) }) }
        Box(
            Modifier.size(260.dp).floating(4f, 2200).tap { spins++; env.sfx(Sfx.POP); env.say(Science.planetSay(p)) },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize().graphicsLayer { rotationZ = turn }) {
                // Saturn is drawn smaller, so its rings fit the card as well as the planet does.
                val r = size.minDimension / 2 * (0.35f + 0.6f * p.size) * (if (p.rings) 0.62f else 1f)
                val c = center
                if (p.rings) drawOval(Color(0xFFD7C08A), c - Offset(r * 1.9f, r * 0.42f), Size(r * 3.8f, r * 0.84f), style = Stroke(r * 0.18f))
                drawCircle(Brush.radialGradient(p.colours.map { Color(it) }, c - Offset(r * 0.35f, r * 0.35f), r * 1.5f), r, c)
                if (p.key == "earth") {
                    drawCircle(Color(0xFF66BB6A), r * 0.32f, c + Offset(-r * 0.25f, -r * 0.2f))
                    drawCircle(Color(0xFF66BB6A), r * 0.24f, c + Offset(r * 0.35f, r * 0.3f))
                }
                if (p.key == "jupiter") drawCircle(Color(0xFFD84315), r * 0.14f, c + Offset(r * 0.3f, r * 0.25f))
                if (p.rings) {
                    // The front half of the rings, over the planet.
                    drawArc(Color(0xFFD7C08A), 0f, 180f, false, c - Offset(r * 1.9f, r * 0.42f), Size(r * 3.8f, r * 0.84f), style = Stroke(r * 0.18f))
                }
            }
        }
        MultiText(p.fact, env.langs, colors.ink, size = 20.sp, say = { env.say(it) })
    }
}
