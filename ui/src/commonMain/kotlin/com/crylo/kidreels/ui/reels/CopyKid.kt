package com.crylo.kidreels.ui.reels

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.crylo.kidreels.core.CopyAct
import com.crylo.kidreels.core.Diagram
import com.crylo.kidreels.core.DiagramPart
import com.crylo.kidreels.core.Diagrams
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * The child of Copy me, drawn as a puppet so it can do each move: hands to the nose, a
 * clap on the drum, fingers wiggling, a squat to the knees. It is art/drawn/body.svg (the
 * child of the body reels) redrawn in code, with joints: the picture pipeline flattens a
 * drawing into one list of shapes, which cannot bend at the elbow. Standing still it
 * matches body.svg, so the body reels' tap spots (core/Body.kt) still land on it; change
 * one drawing and change the other.
 *
 * A pose says where each hand's fingertips go, and the elbows and knees are worked out from
 * that (two-bone reach), so a new move is a few points, not a set of angles to tune.
 * Everything is in the drawing's units, 240 × 440.
 */

private val Skin = Color(0xFFD9A06F)
private val SkinDark = Color(0xFFC98E5E)
private val Shirt = Color(0xFF4F7DF3)
private val ShirtDark = Color(0xFF3D66D6)
private val ShortsCol = Color(0xFFF7A325)
private val ShortsDark = Color(0xFFE08E12)
private val HairCol = Color(0xFF33231A)
private val CheekCol = Color(0x8CE88A8A)
private val Lips = Color(0xFFC2413B)
private val TongueCol = Color(0xFFF0828E)
private val Glow = Color(0xFFFFC107)

private const val UPPER = 78f
private const val ELBOWS_OUT = 0.5f

/** A thin darker edge round arms, hands and legs, so a hand shows against the face. */
private const val EDGE = 1.6f
private const val FORE = 51f

/** Wrist to fingertip: the hand is part of the forearm for reaching. */
private const val HAND = 36f
private const val THIGH = 52f
private const val SHIN = 56f
private const val HIP_Y = 282f
private const val NECK_Y = 138f

private val SHOULDER = Offset(84f, 160f)
private val HIP = Offset(100f, HIP_Y)
private val ANKLE = Offset(101f, 390f)

/** The right side (as we look at it) is the left mirrored. */
private fun Offset.mirror() = Offset(240f - x, y)

private fun side(p: Offset, right: Boolean) = if (right) p.mirror() else p

/**
 * One frame of the child. [l] and [r] are the left and right fingertips as we see them;
 * [drop] sinks the hips for a squat and [lean] shortens the body over them, bending
 * forward; [lFoot] and [rFoot] move a foot off the ground; [tilt] turns the head;
 * [lift] raises the whole child, for a jump; [kneesOut] is how far knees bend outward
 * rather than towards us (1 for a leg lifted to the side). The rest are the face.
 */
private data class Pose(
    val l: Offset,
    val r: Offset,
    val drop: Float = 0f,
    val lean: Float = 1f,
    val lFoot: Offset = Offset.Zero,
    val rFoot: Offset = Offset.Zero,
    val tilt: Float = 0f,
    val lift: Float = 0f,
    val kneesOut: Float = 0.35f,
    val blink: Float = 0f,
    val tongue: Float = 0f,
    val puff: Float = 0f,
    val wiggle: Float = 0f,
)

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

private fun lerp(a: Offset, b: Offset, t: Float) = Offset(lerp(a.x, b.x, t), lerp(a.y, b.y, t))

private fun lerp(a: Pose, b: Pose, t: Float) = Pose(
    lerp(a.l, b.l, t), lerp(a.r, b.r, t), lerp(a.drop, b.drop, t), lerp(a.lean, b.lean, t),
    lerp(a.lFoot, b.lFoot, t), lerp(a.rFoot, b.rFoot, t), lerp(a.tilt, b.tilt, t), lerp(a.lift, b.lift, t),
    lerp(a.kneesOut, b.kneesOut, t), lerp(a.blink, b.blink, t), lerp(a.tongue, b.tongue, t),
    lerp(a.puff, b.puff, t), lerp(a.wiggle, b.wiggle, t),
)

/** Both hands to [left] and its mirror. */
private fun both(left: Offset) = left to left.mirror()

/**
 * The middle joint of a two-bone limb from [from] reaching for [to], bent outward on
 * [right]'s side by [out] (1: fully sideways; less: partly towards us, so it shows less).
 * Out of reach, the limb points straight at [to].
 */
private fun joint(from: Offset, to: Offset, a: Float, b: Float, right: Boolean, out: Float = 1f): Offset {
    val v = to - from
    val len = v.getDistance()
    val dir = if (len < 0.01f) Offset(0f, 1f) else v / len
    val d = len.coerceIn(abs(a - b) + 0.01f, a + b - 0.01f)
    val cosA = ((a * a + d * d - b * b) / (2 * a * d)).coerceIn(-1f, 1f)
    val sinA = sqrt(1 - cosA * cosA)
    val e1 = from + Offset(dir.x * cosA - dir.y * sinA, dir.x * sinA + dir.y * cosA) * a
    val e2 = from + Offset(dir.x * cosA + dir.y * sinA, -dir.x * sinA + dir.y * cosA) * a
    val bent = if ((e1.x > e2.x) == right) e1 else e2
    if (out >= 1f) return bent
    // Towards us, a bend shows as the joint staying nearer the straight line.
    val straight = from + dir * (a * cosA)
    return lerp(straight, bent, out)
}

/** Where every joint is for a [Pose]. */
private class Rig(val p: Pose) {
    val hipY = HIP_Y + p.drop

    /** A point of the upper body, standing still, to where it is now. */
    fun up(q: Offset) = Offset(q.x, hipY + (q.y - HIP_Y) * p.lean)

    val headDy = up(Offset(120f, NECK_Y)).y - NECK_Y

    fun head(q: Offset): Offset {
        val rad = p.tilt * PI.toFloat() / 180f
        val c = kotlin.math.cos(rad)
        val s = sin(rad)
        val v = q - Offset(120f, NECK_Y)
        return Offset(120f + v.x * c - v.y * s, NECK_Y + v.x * s + v.y * c + headDy)
    }

    inner class Arm(right: Boolean) {
        val shoulder = up(side(SHOULDER, right))
        val tip0 = if (right) p.r else p.l
        // Elbows bend partly towards us, as a real child's do: straight out sideways
        // they made wings of every move near the face.
        val elbow = joint(shoulder, tip0, UPPER, FORE + HAND, right, ELBOWS_OUT)
        val tip = run {
            val v = tip0 - elbow
            val len = v.getDistance().coerceAtLeast(0.01f)
            elbow + v / len * (FORE + HAND)
        }
        val wrist = elbow + (tip - elbow) * (FORE / (FORE + HAND))
        val palm = elbow + (tip - elbow) * ((FORE + 15f) / (FORE + HAND))
        val fingers = elbow + (tip - elbow) * ((FORE + 28f) / (FORE + HAND))
    }

    inner class Leg(right: Boolean) {
        val hip = Offset(side(HIP, right).x, hipY)
        val ankle = side(ANKLE, right) + (if (right) p.rFoot else p.lFoot)
        // A foot lifted high, onto the other knee, swings the knee out sideways; a stomp's
        // small lift brings it towards us, as a sideways knee there looked like a kick.
        val lifted = (if (right) p.rFoot else p.lFoot).getDistance() > 30f
        val knee = joint(hip, ankle, THIGH, SHIN, right, if (lifted) 1f else p.kneesOut)
    }

    val lArm = Arm(false)
    val rArm = Arm(true)
    val lLeg = Leg(false)
    val rLeg = Leg(true)

    /** Where a body reel's tap spot is now, for the ring around the part to copy. */
    fun spot(key: String, rest: Offset): Offset {
        val right = rest.x > 120f
        val arm = if (right) rArm else lArm
        val leg = if (right) rLeg else lLeg
        return when (key) {
            "hand", "thumb" -> arm.palm
            "finger" -> arm.fingers
            "wrist" -> arm.wrist
            "elbow" -> arm.elbow
            "arm" -> (arm.shoulder + arm.elbow) / 2f
            "knee" -> leg.knee
            "leg" -> (leg.hip + leg.knee) / 2f
            "foot", "toes", "ankle" -> rest + (leg.ankle - side(ANKLE, right))
            "hair", "eye", "nose", "mouth", "ear", "cheek", "chin", "eyebrow" -> head(rest)
            else -> up(rest)
        }
    }
}

private val REST = Pose(Offset(53f, 322f), Offset(187f, 322f))

/** A bump from 0 up to 1 and back over [width] of each [period], for blinks and pats. */
private fun bump(t: Float, period: Float, start: Float, width: Float): Float {
    val ph = ((t % period) - start) / width
    return if (ph in 0f..1f) sin(ph * PI.toFloat()) else 0f
}

/** The fingertips a squat of [drop] brings down to the knees. */
private val KNEES_DROP = 26f
private val TOES_DROP = 44f

private fun kneeAt(drop: Float) = joint(Offset(HIP.x, HIP_Y + drop), ANKLE, THIGH, SHIN, false, 0.35f)

/**
 * The pose for [act] at [t] seconds into it. [pulse] rises to 1 on each drum [beat] and
 * falls back, for claps, pats, stomps and jumps.
 */
private fun poseFor(act: CopyAct?, t: Float, pulse: Float, beat: Int): Pose {
    val tau = 2 * PI.toFloat()
    return when (act) {
        null, CopyAct.SIT_DOWN -> REST
        CopyAct.NOSE -> REST.copy(r = Offset(124f, 101f))
        CopyAct.EARS -> both(Offset(73f, 92f)).let { (l, r) -> REST.copy(l = l, r = r) }
        CopyAct.HAIR -> both(Offset(103f, 38f)).let { (l, r) -> REST.copy(l = l, r = r) }
        CopyAct.EYES -> both(Offset(97f, 97f)).let { (l, r) -> REST.copy(l = l, r = r) }
        CopyAct.CHIN -> REST.copy(r = Offset(123f, 128f))
        CopyAct.PAT_TUMMY -> {
            val pat = -7f * max(0f, sin(t * tau * 2f))
            both(Offset(107f, 212f + pat)).let { (l, r) -> REST.copy(l = l, r = r) }
        }
        CopyAct.HANDS_UP -> both(Offset(58f, 8f)).let { (l, r) -> REST.copy(l = l, r = r) }
        CopyAct.TONGUE -> REST.copy(tongue = 1f)
        CopyAct.PUFF_CHEEKS -> REST.copy(puff = 1f)
        CopyAct.SHAKE_HEAD -> REST.copy(tilt = 13f * sin(t * tau * 1.8f))
        CopyAct.BLINK -> REST.copy(blink = bump(t, 0.8f, 0.3f, 0.22f))
        CopyAct.WIGGLE -> both(Offset(56f, 106f)).let { (l, r) -> REST.copy(l = l, r = r, wiggle = 1f) }
        CopyAct.CLAP -> {
            // Apart between beats, palms together on each one.
            val x = lerp(78f, 118f, pulse)
            both(Offset(x, 196f)).let { (l, r) -> REST.copy(l = l, r = r) }
        }
        CopyAct.PAT_HEAD -> REST.copy(r = lerp(Offset(150f, 6f), Offset(134f, 34f), pulse))
        CopyAct.SHOULDERS -> both(Offset(98f, 150f)).let { (l, r) -> REST.copy(l = l, r = r) }
        CopyAct.ELBOWS -> REST.copy(l = Offset(162f, 226f), r = Offset(78f, 226f))
        CopyAct.KNEES -> {
            val k = kneeAt(KNEES_DROP)
            REST.copy(l = k + Offset(2f, -4f), r = (k + Offset(2f, -4f)).mirror(), drop = KNEES_DROP)
        }
        CopyAct.TOES -> both(Offset(90f, 404f)).let { (l, r) -> REST.copy(l = l, r = r, drop = TOES_DROP, lean = 0.8f) }
        CopyAct.JUMP -> {
            val arms = lerp(Offset(30f, 250f), Offset(30f, 120f), pulse)
            both(arms).let { (l, r) -> REST.copy(l = l, r = r, lift = 30f * pulse, drop = 8f * (1 - pulse)) }
        }
        CopyAct.STOMP -> {
            // On each beat a foot comes down; then the other one lifts, ready for the next.
            val up = Offset(0f, -22f * (1 - pulse))
            val nextRight = beat % 2 == 1
            both(Offset(80f, 262f)).let { (l, r) ->
                REST.copy(l = l, r = r, lFoot = if (beat > 0 && !nextRight) up else Offset.Zero, rFoot = if (beat > 0 && nextRight) up else Offset.Zero)
            }
        }
        CopyAct.ONE_LEG -> both(Offset(12f, 186f)).let { (l, r) -> REST.copy(l = l, r = r, rFoot = Offset(-27f, -52f)) }
        CopyAct.TURN -> both(Offset(38f, 300f)).let { (l, r) -> REST.copy(l = l, r = r) }
    }
}

private fun path(d: String): Path = PathParser().parsePathString(d).toPath()

/** The shapes that do not bend, parsed once. */
private object Shapes {
    val shorts = path("M80,228 L160,228 L164,284 L126,284 L120,262 L114,284 L76,284 Z")
    val shortsFold = path("M120,240 L120,262 L114,284 L110,284 L117,258 Z")
    val shirt = path("M82,146 C96,138 108,136 120,142 C132,136 144,138 158,146 L162,192 L158,234 L82,234 L78,192 Z")
    val collar = path("M104,140 C110,150 130,150 136,140 C130,144 110,144 104,140 Z")
    val hair = path("M74,86 C68,42 94,24 120,24 C146,24 172,42 166,86 C160,66 150,56 136,58 C124,50 106,52 96,60 C86,62 78,72 74,86 Z")
    val browL = path("M94,68 Q104,60 114,66 L113,70 Q104,65 95,72 Z")
    val browR = path("M146,68 Q136,60 126,66 L127,70 Q136,65 145,72 Z")
    val mouth = path("M105,111 Q120,127 135,111 Q120,118 105,111 Z")
    val foot = path("M90,388 L112,388 C114,398 112,412 100,414 L78,414 C70,414 70,404 78,400 C84,398 88,394 90,388 Z")
    val toes = listOf(Offset(74f, 409f) to 4f, Offset(81f, 411f) to 3.5f, Offset(87f, 412f) to 3.2f, Offset(93f, 413f) to 3f)
}

private fun DrawScope.capsule(a: Offset, b: Offset, width: Float, colour: Color) =
    drawLine(colour, a, b, strokeWidth = width, cap = StrokeCap.Round)

private fun DrawScope.ellipse(colour: Color, centre: Offset, rx: Float, ry: Float) =
    drawOval(colour, centre - Offset(rx, ry), Size(rx * 2, ry * 2))

private fun DrawScope.leg(leg: Rig.Leg, right: Boolean) {
    capsule(leg.hip, leg.knee, 25f + EDGE * 2, SkinDark)
    capsule(leg.knee, leg.ankle, 20f + EDGE * 2, SkinDark)
    capsule(leg.hip, leg.knee, 25f, Skin)
    capsule(leg.knee, leg.ankle, 20f, Skin)
    ellipse(SkinDark, leg.knee, 10f, 7.5f)
    val moved = leg.ankle - side(ANKLE, right)
    translate(moved.x, moved.y) {
        scale(if (right) -1f else 1f, 1f, pivot = Offset(120f, 0f)) {
            drawPath(Shapes.foot, Skin)
            Shapes.toes.forEach { (c, r) -> drawCircle(SkinDark, r, c) }
        }
    }
}

private fun DrawScope.arm(arm: Rig.Arm, right: Boolean, t: Float, wiggle: Float) {
    capsule(arm.shoulder, arm.elbow, 16f + EDGE * 2, SkinDark)
    capsule(arm.elbow, arm.wrist, 15f + EDGE * 2, SkinDark)
    capsule(arm.shoulder, arm.elbow, 16f, Skin)
    capsule(arm.elbow, arm.wrist, 15f, Skin)
    drawCircle(SkinDark, 6.5f, arm.elbow)
    // The hand in its own frame: wrist at the origin, fingers down +y, thumb towards the body.
    val u = (arm.wrist - arm.elbow).let { it / it.getDistance().coerceAtLeast(0.01f) }
    val deg = atan2(-u.x, u.y) * 180f / PI.toFloat()
    val sx = if (right) -1f else 1f
    withTransform({
        translate(arm.wrist.x, arm.wrist.y)
        rotate(deg, Offset.Zero)
        scale(sx, 1f, Offset.Zero)
    }) {
        val fingers = listOf(-9f to 15f, -3f to 18f, 3f to 17f, 8.5f to 13f)
        // Twice: once larger in the darker skin for the edge, then the hand over it.
        for ((grow, colour) in listOf(EDGE to SkinDark, 0f to Skin)) {
            ellipse(colour, Offset(0f, 13f), 12.5f + grow, 14.5f + grow)
            ellipse(colour, Offset(11f, 8f), 5f + grow, 9f + grow)
            fingers.forEachIndexed { i, (x, len) ->
                val angle = wiggle * 22f * sin(t * 2 * PI.toFloat() * 3f + i * 1.4f)
                rotate(angle, Offset(x, 22f)) { capsule(Offset(x, 22f), Offset(x, 22f + len), 6f + grow * 2, colour) }
            }
        }
    }
    // The sleeve last, over the top of the arm: round at the shoulder, so it turns with it.
    val sleeveEnd = arm.shoulder + (arm.elbow - arm.shoulder) * (26f / UPPER)
    capsule(arm.shoulder, sleeveEnd, 27f, Shirt)
}

private fun DrawScope.head(rig: Rig) {
    val p = rig.p
    translate(0f, rig.headDy) {
        rotate(p.tilt, Offset(120f, NECK_Y)) {
            ellipse(SkinDark, Offset(75f, 84f), 9f, 13f)
            ellipse(SkinDark, Offset(165f, 84f), 9f, 13f)
            ellipse(Skin, Offset(120f, 80f), 45f * (1 + 0.1f * p.puff), 49f)
            drawPath(Shapes.hair, HairCol)
            drawPath(Shapes.browL, HairCol)
            drawPath(Shapes.browR, HairCol)
            val open = 1f - 0.9f * p.blink
            for (x in listOf(104f, 136f)) {
                scale(1f, open, Offset(x, 84f)) {
                    ellipse(Color.White, Offset(x, 84f), 8f, 9f)
                    drawCircle(HairCol, 4.5f, Offset(if (x < 120f) x + 1 else x - 1, 85f))
                }
            }
            ellipse(SkinDark, Offset(120f, 99f), 6f, 4.5f)
            val cheek = 1f + 0.9f * p.puff
            ellipse(CheekCol, Offset(95f - 5f * p.puff, 103f), 9f * cheek, 5.5f * cheek * (1 + 0.4f * p.puff))
            ellipse(CheekCol, Offset(145f + 5f * p.puff, 103f), 9f * cheek, 5.5f * cheek * (1 + 0.4f * p.puff))
            if (p.puff > 0.5f) {
                // Cheeks full of air: the smile becomes a small round mouth.
                drawCircle(Lips, 5f, Offset(120f, 114f))
            } else {
                drawPath(Shapes.mouth, Lips)
            }
            if (p.tongue > 0.01f) ellipse(TongueCol, Offset(120f, 117f + 8f * p.tongue), 8f, 3f + 9f * p.tongue)
        }
    }
}

private fun DrawScope.child(rig: Rig, t: Float) {
    val p = rig.p
    translate(0f, -p.lift) {
        leg(rig.lLeg, false)
        leg(rig.rLeg, true)
        translate(0f, p.drop) {
            drawPath(Shapes.shorts, ShortsCol)
            drawPath(Shapes.shortsFold, ShortsDark)
        }
        withTransform({
            translate(0f, p.drop)
            scale(1f, p.lean, Offset(120f, HIP_Y))
        }) {
            drawRoundRect(SkinDark, Offset(107f, 118f), Size(26f, 26f), androidx.compose.ui.geometry.CornerRadius(6f))
            drawPath(Shapes.shirt, Shirt)
            drawPath(Shapes.collar, ShirtDark)
        }
        head(rig)
        arm(rig.lArm, false, t, p.wiggle)
        arm(rig.rArm, true, t, p.wiggle)
    }
}

/**
 * The child doing [act] (standing still for null), with [lit] ringed wherever it is now.
 * [beat] counts the drum: each new one is a clap, a pat, a stomp or a jump. Between games
 * a tapped part names itself, as on the body reels.
 */
@Composable
fun CopyKid(act: CopyAct?, beat: Int, lit: DiagramPart?, modifier: Modifier, onTap: (DiagramPart) -> Unit) {
    val env = LocalReelEnv.current
    var from by remember { mutableStateOf<CopyAct?>(null) }
    var to by remember { mutableStateOf<CopyAct?>(null) }
    val blend = remember { Animatable(1f) }
    val pulse = remember { Animatable(0f) }
    val ring = remember { Animatable(1f) }
    var t by remember { mutableFloatStateOf(0f) }
    var fromT by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(act) {
        if (act == to) return@LaunchedEffect
        from = to
        fromT = t
        to = act
        t = 0f
        blend.snapTo(0f)
        blend.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(beat) {
        if (beat == 0) return@LaunchedEffect
        pulse.snapTo(0f)
        pulse.animateTo(1f, tween(110))
        pulse.animateTo(0f, tween(420))
    }
    // The clock runs only while something moves, and not on a reel swiped away.
    LaunchedEffect(act, env.active) {
        if (act == null || !env.active) return@LaunchedEffect
        var last = withFrameMillis { it }
        while (true) {
            withFrameMillis { now ->
                val dt = (now - last) / 1000f
                t += dt
                fromT += dt
                last = now
            }
        }
    }
    LaunchedEffect(lit, env.active) {
        ring.snapTo(1f)
        if (lit == null || !env.active) return@LaunchedEffect
        repeat(3) {
            ring.animateTo(1.35f, tween(700))
            ring.animateTo(1f, tween(700))
        }
    }

    val tap by rememberUpdatedState(onTap)
    val d = Diagram.BODY
    Canvas(
        modifier.pointerInput(Unit) {
            detectTapGestures { q ->
                val s = minOf(size.width / d.width, size.height / d.height)
                val x = (q.x - (size.width - d.width * s) / 2) / s
                val y = (q.y - (size.height - d.height * s) / 2) / s
                Diagrams.at(d, x, y)?.let(tap)
            }
        },
    ) {
        val pose = lerp(poseFor(from, fromT, pulse.value, beat), poseFor(to, t, pulse.value, beat), blend.value)
        val rig = Rig(pose)
        val s = minOf(size.width / d.width, size.height / d.height)
        translate((size.width - d.width * s) / 2, (size.height - d.height * s) / 2) {
            scale(s, s, Offset.Zero) {
                child(rig, t)
                lit?.spots?.forEach { (x, y) ->
                    val at = rig.spot(lit.key, Offset(x, y)) - Offset(0f, pose.lift)
                    val r = (lit.radius + 4) * ring.value
                    drawCircle(Glow.copy(alpha = 0.25f), r, at)
                    drawCircle(Glow, r, at, style = Stroke(3.5.dp.toPx() / s))
                }
            }
        }
    }
}
