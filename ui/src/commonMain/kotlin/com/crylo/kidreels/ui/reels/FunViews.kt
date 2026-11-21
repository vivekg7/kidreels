package com.crylo.kidreels.ui.reels

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.PI
import com.crylo.kidreels.core.Bubble
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.BubbleGame
import com.crylo.kidreels.core.BubblePop
import com.crylo.kidreels.core.Bubbles
import com.crylo.kidreels.core.CopyMe
import com.crylo.kidreels.core.CopyMotion
import com.crylo.kidreels.core.CopyMoves
import com.crylo.kidreels.core.Diagram
import com.crylo.kidreels.core.Diagrams
import com.crylo.kidreels.core.NumberWords
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.Silly
import com.crylo.kidreels.core.SillyAnimalReel
import com.crylo.kidreels.core.SillyPart
import com.crylo.kidreels.core.Songs
import com.crylo.kidreels.core.Tone
import com.crylo.kidreels.core.TuneReel
import com.crylo.kidreels.core.Txt
import com.crylo.kidreels.ui.Sfx
import com.crylo.kidreels.ui.art.drawArt
import com.crylo.kidreels.ui.art.artFor
import com.crylo.kidreels.core.SillyZone
import com.crylo.kidreels.ui.common.Motion
import com.crylo.kidreels.ui.common.MultiText
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.floating
import com.crylo.kidreels.ui.common.moveOn
import com.crylo.kidreels.ui.common.popOn
import com.crylo.kidreels.ui.common.pressable
import com.crylo.kidreels.ui.common.shakeOn
import com.crylo.kidreels.ui.common.tap
import com.crylo.kidreels.ui.skin.LocalReelColors
import com.crylo.kidreels.ui.skin.reelWidth
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

/*
 * The fun reels' views. Each is a toy the child makes happen: nothing moves for long on
 * its own, and each ends by itself or stays as funny on the hundredth tap as on the first.
 * They call onDone when they end (a song played through, the last bubble popped), which
 * cheers without scoring anything.
 */

/**
 * Tap its nose and it sneezes, its tummy and it giggles, its feet and it hops, its ears and
 * it flaps them. The animal is one drawing, so the whole of it moves; where each part is
 * comes from its [com.crylo.kidreels.core.SillyShape].
 */
@Composable
fun SillyAnimalView(reel: SillyAnimalReel, padding: PaddingValues) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val animal = reel.animal
    var sneeze by remember(reel.id) { mutableIntStateOf(0) }
    var giggle by remember(reel.id) { mutableIntStateOf(0) }
    var hop by remember(reel.id) { mutableIntStateOf(0) }
    var flap by remember(reel.id) { mutableIntStateOf(0) }
    var look by remember(reel.id) { mutableIntStateOf(0) }
    var hello by remember(reel.id) { mutableIntStateOf(0) }
    var named by remember(reel.id) { mutableStateOf<SillyPart?>(null) }

    fun silly(part: SillyPart) {
        val lines = mutableListOf(Say(part.does[env.main], env.main, animal.voice))
        if (reel.namesParts) lines += Say(Silly.thatsThe(animal, part)[env.main], env.main)
        named = part
        env.say(lines)
    }

    fun tapped(zone: SillyZone) {
        when (zone) {
            SillyZone.NOSE -> { sneeze++; env.sfx(Sfx.SNEEZE); silly(SillyPart.NOSE) }
            SillyZone.TUMMY -> { giggle++; env.sfx(Sfx.POP); silly(SillyPart.TUMMY) }
            SillyZone.FEET -> { hop++; env.sfx(Sfx.BOING); silly(SillyPart.FEET) }
            SillyZone.EARS -> { flap++; env.sfx(Sfx.TICK); silly(SillyPart.EARS) }
            SillyZone.HEAD -> { hello++; env.say(Say(animal.sound[env.main], env.main, animal.voice)) }
            // Anywhere else, it looks round to see what that was.
            SillyZone.AIR -> { look++; env.sfx(Sfx.TICK) }
        }
    }

    // The tap handler outlives a composition, and the page was first composed off screen,
    // silent: it must reach the reel's current voice.
    val onZone by rememberUpdatedState<(SillyZone) -> Unit> { tapped(it) }
    val art = remember(animal) { artFor(animal.art) ?: error("No drawing for ${animal.art}: run scripts/art.py") }
    Column(Modifier.fillMaxSize().padding(padding).reelWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        MultiText(reel.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 26.sp, say = { env.say(it) })
        BoxWithConstraints(
            // Taps beside the animal are "air"; those on it are caught by the drawing first.
            Modifier.weight(1f).fillMaxWidth().pointerInput(reel.id) { detectTapGestures { onZone(SillyZone.AIR) } },
            contentAlignment = Alignment.Center,
        ) {
            val ratio = art.width / art.height
            val tall = minOf(maxHeight * 0.92f, maxWidth * 0.9f / ratio)
            Box(
                Modifier.size(tall * ratio, tall)
                    .moveOn(hop, Motion.HOP, times = 2)
                    .moveOn(giggle, Motion.WOBBLE, times = 3, millis = 260)
                    .moveOn(sneeze, Motion.SQUASH, millis = 380)
                    .moveOn(sneeze, Motion.SHAKE, delayMillis = 380, millis = 300)
                    .moveOn(flap, Motion.WAG, times = 2, millis = 360)
                    .moveOn(look, Motion.WOBBLE, millis = 900)
                    .popOn(hello)
                    .pointerInput(reel.id) { detectTapGestures { p -> onZone(Silly.zone(animal, p.x / size.width, p.y / size.height)) } },
            ) {
                Canvas(Modifier.fillMaxSize()) { drawArt(art) }
                if (sneeze > 0) Sneeze(sneeze, Offset(animal.shape.centre, animal.shape.noseY), Modifier.fillMaxSize())
            }
        }
        Box(Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
            val part = named
            if (part != null && reel.namesParts) {
                MultiText(Silly.thatsThe(animal, part), env.langs, colors.ink, Modifier.popOn(sneeze + giggle + hop + flap), size = 20.sp, say = { env.say(it) })
            } else {
                T("👆", Modifier.floating(), size = 32.sp)
            }
        }
    }
}

/** A spray of drops from the nose at [nose] (fractions of the picture), for a moment. */
@Composable
private fun Sneeze(trigger: Int, nose: Offset, modifier: Modifier) {
    val t = remember(trigger) { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(trigger) {
        delay(380)
        t.animateTo(1f, tween(500))
    }
    if (t.value <= 0f || t.value >= 1f) return
    Canvas(modifier) {
        val origin = Offset(size.width * nose.x, size.height * nose.y)
        repeat(9) { i ->
            val a = (-0.6f + 1.2f * i / 8f)
            val d = t.value * size.width * 0.4f
            drawCircle(Color(0xFF8FD3FF).copy(alpha = 1f - t.value), size.width * 0.022f, origin + Offset(sin(a) * d, d * 0.9f))
        }
    }
}

/**
 * Bubbles float up, each with something inside; popping one says the count and plays the
 * next note up a scale, so popping makes a tune. A number-in-order or word game holds its
 * bubbles in place, bobbing, so the next one can be found.
 */
@Composable
fun BubbleView(reel: BubblePop, padding: PaddingValues, onDone: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val popped = remember(reel.id) { mutableStateMapOf<Int, Popped>() }
    val counted = popped.keys.count { reel.bubbles[it].wanted }
    val done = counted >= reel.toPop
    var doneAt by remember(reel.id) { mutableLongStateOf(Long.MAX_VALUE) }
    var shake by remember(reel.id) { mutableIntStateOf(0) }
    var shaking by remember(reel.id) { mutableIntStateOf(-1) }
    var clock by remember(reel.id) { mutableLongStateOf(0L) }
    val rising = reel.game == BubbleGame.COUNT || reel.game == BubbleGame.COLOUR
    val period = if (reel.slow) 10_000L else 6_500L
    val gap = period / 5
    val scale = remember { Songs.NOTES.drop(3) }
    val (lanes, spots) = remember(reel.id) {
        val r = Random(reel.id)
        reel.bubbles.indices.map { 0.08f + 0.84f * r.nextFloat() } to staticSpots(reel.bubbles.size, r)
    }

    // Time only runs on screen: off-screen pages stay still.
    LaunchedEffect(reel.id, env.active) {
        if (!env.active) return@LaunchedEffect
        val start = withFrameMillis { it } - clock
        while (true) withFrameMillis { clock = it - start }
    }

    /**
     * Where bubble [i] is at [t], as fractions of the room (see [staticSpots]), and how much
     * of it shows: a rising bubble fades in from below and out at the top, instead of
     * appearing at the edge of the room. Null when it is not there.
     */
    fun place(i: Int, t: Long): Popped? {
        if (!rising) return Popped(t, spots[i].first, spots[i].second + 0.015f * sin(t / 600f + i * 1.7f), 1f)
        val spawn = i * gap
        if (t < spawn) return null
        val loops = floor((t - spawn).toDouble() / period).toLong()
        // No new bubbles once it is over: those on their way up float off.
        if (spawn + loops * period > doneAt) return null
        val q = ((t - spawn) % period).toFloat() / period
        val y = 1.1f - q * 1.35f
        val shown = minOf((1.12f - y) / 0.22f, (y + 0.25f) / 0.2f).coerceIn(0f, 1f)
        return Popped(t, (lanes[i] + 0.03f * sin(t / 700f + i)).coerceIn(0f, 1f), y, shown)
    }

    fun pop(i: Int) {
        // Read afresh: two taps can land before the screen catches up.
        val counted = popped.keys.count { reel.bubbles[it].wanted }
        if (i in popped || counted >= reel.toPop) return
        val b = reel.bubbles[i]
        val next = when (reel.game) {
            BubbleGame.NUMBERS -> b.inside == (counted + 1).toString()
            BubbleGame.WORD -> b.inside == reel.word!!.first[counted].toString()
            else -> b.wanted
        }
        if (!next) {
            shaking = i
            shake++
            env.say(Bubbles.notThis(reel, counted), reel.voice, Tone.GENTLE)
            return
        }
        popped[i] = place(i, clock) ?: return
        val n = counted + 1
        env.sfx(Sfx.BUBBLE)
        env.note(scale[(n - 1) % scale.size])
        if (n >= reel.toPop) {
            doneAt = clock
            env.say(Bubbles.done(reel), reel.voice, Tone.CHEER)
            onDone()
        } else {
            env.say(
                when (reel.game) {
                    BubbleGame.WORD -> Txt.same(b.inside)
                    BubbleGame.NUMBERS -> NumberWords.of(n)
                    else -> Bubbles.counted(n)
                },
                reel.voice,
            )
        }
    }

    Column(Modifier.fillMaxSize().padding(padding).reelWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        MultiText(reel.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 26.sp, say = { env.say(it) }, first = reel.voice ?: env.main)
        reel.word?.let { (word, picture) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Pic(picture, 40.sp, voice = reel.voice)
                // Each letter as it is popped jumps into its place.
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    word.forEachIndexed { at, letter ->
                        T(
                            if (at < counted) letter.toString() else "_",
                            Modifier.popOn(if (at == counted - 1) counted else 0),
                            color = colors.ink, size = 30.sp, weight = FontWeight.Black,
                        )
                    }
                }
            }
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
            val bubble = if (reel.bubbles.size > 12) 74.dp else 86.dp
            fun at(p: Popped) = DpOffset((maxWidth - bubble) * p.x, (maxHeight - bubble) * p.y)
            reel.bubbles.forEachIndexed { i, b ->
                val pop = popped[i]
                // Popped: it swells for a moment, then bursts (drawn below).
                val swell = pop?.let { (clock - it.at).toFloat() / SWELL_MILLIS }
                if (swell != null && swell >= 1f) return@forEachIndexed
                val p = pop ?: place(i, clock) ?: return@forEachIndexed
                if (pop == null && !rising && done) return@forEachIndexed
                val o = at(p)
                Box(
                    Modifier.offset(o.x, o.y)
                        .size(bubble)
                        .graphicsLayer {
                            alpha = p.shown
                            val s = 1f + 0.22f * (swell ?: 0f)
                            scaleX = s
                            scaleY = s
                        }
                        .shakeOn(if (shaking == i) shake else 0)
                        .clip(CircleShape)
                        .then(if (pop == null) Modifier.tap { pop(i) } else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    BubbleBall(Color(b.argb))
                    BubbleInside(b, bubble)
                }
            }
            // The bursts: a ring and a spray of drops, and what was inside floats up and away.
            popped.forEach { (i, pop) ->
                val age = clock - pop.at
                if (age < SWELL_MILLIS || age > SWELL_MILLIS + FLOAT_MILLIS) return@forEach
                val o = at(pop)
                val q = (age - SWELL_MILLIS).toFloat() / FLOAT_MILLIS
                Box(
                    Modifier.offset(o.x, o.y).size(bubble).graphicsLayer {
                        translationY = -q * bubble.toPx() * 0.9f
                        alpha = 1f - q * q
                        scaleX = 1f + 0.35f * q
                        scaleY = 1f + 0.35f * q
                    },
                    contentAlignment = Alignment.Center,
                ) { BubbleInside(reel.bubbles[i], bubble) }
            }
            Canvas(Modifier.fillMaxSize()) {
                val r0 = bubble.toPx() / 2
                popped.forEach { (i, pop) ->
                    val q = (clock - pop.at - SWELL_MILLIS).toFloat() / BURST_MILLIS
                    if (q < 0f || q >= 1f) return@forEach
                    val o = at(pop)
                    burst(Offset(o.x.toPx() + r0, o.y.toPx() + r0), r0 * 1.22f, Color(reel.bubbles[i].argb), q, Random(reel.id * 31 + i))
                }
            }
            if (done) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    MultiText(Bubbles.done(reel), env.langs, colors.ink, Modifier.popOn(1), size = 30.sp, say = { env.say(it) }, first = reel.voice ?: env.main)
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
            T("${counted} / ${reel.toPop}", Modifier.popOn(counted), color = colors.ink.copy(alpha = 0.8f), size = 18.sp, weight = FontWeight.Bold)
        }
    }
}

/** A popped bubble: when ([at], on the reel's clock) and where it was, and how much of it showed. */
private data class Popped(val at: Long, val x: Float, val y: Float, val shown: Float)

/** How long a popped bubble swells before it bursts; then the burst, and what was inside floating off. */
private const val SWELL_MILLIS = 70L
private const val BURST_MILLIS = 420L
private const val FLOAT_MILLIS = 650L

/**
 * A bubble bursting, [q] of the way through: a thin ring flies out and fades, and drops of
 * its film spray out and fall a little.
 */
private fun DrawScope.burst(centre: Offset, r: Float, colour: Color, q: Float, random: Random) {
    val ease = 1f - (1f - q) * (1f - q)
    // A flash of the film where the bubble was, gone in the first quarter.
    if (q < 0.25f) drawCircle(colour.copy(alpha = 0.5f * (1f - q * 4f)), r * (1f + 0.15f * q), centre)
    drawCircle(Color.White.copy(alpha = 0.85f * (1f - q)), r * (1f + 0.5f * ease), centre, style = Stroke(r * 0.09f * (1f - q) + 1f))
    repeat(12) { k ->
        val a = (k / 12f + random.nextFloat() * 0.05f) * 2 * PI.toFloat()
        val d = r * (0.8f + (0.6f + 0.35f * random.nextFloat()) * ease)
        val drop = centre + Offset(cos(a) * d, sin(a) * d + r * 0.6f * q * q)
        drawCircle((if (k % 3 == 0) Color.White else colour).copy(alpha = 1f - q), r * (0.13f - 0.07f * q), drop)
    }
}

/** What a bubble holds: a big numeral or letter, or a picture. */
@Composable
private fun BubbleInside(b: Bubble, bubble: Dp) {
    val glyph = b.inside.length == 1 && b.inside[0].isLetterOrDigit()
    T(b.inside, color = Color.White, size = if (glyph) 34.sp else (bubble.value * 0.42f).sp, weight = FontWeight.Black)
}

/**
 * Places for bubbles that stay put, as fractions of the room left once a bubble is taken
 * off (0 is the left or top edge, 1 the right or bottom): a loose grid, jittered a little.
 */
private fun staticSpots(n: Int, r: Random): List<Pair<Float, Float>> {
    val cols = if (n <= 6) 3 else 4
    val rows = (n + cols - 1) / cols
    return (0 until n).map { i ->
        val x = (i % cols) / (cols - 1f) + (r.nextFloat() - 0.5f) * 0.06f
        val y = if (rows > 1) 0.05f + 0.85f * (i / cols) / (rows - 1f) else 0.4f
        x.coerceIn(0f, 1f) to (y + (r.nextFloat() - 0.5f) * 0.04f).coerceIn(0f, 1f)
    }
}

/** A soap bubble: a tinted ball with a rim and a sheen. */
@Composable
private fun BubbleBall(colour: Color) {
    Canvas(Modifier.fillMaxSize()) {
        val r = size.minDimension / 2
        drawCircle(colour.copy(alpha = 0.75f), r)
        drawCircle(Color.White.copy(alpha = 0.7f), r - 1.5f * density, style = Stroke(2f * density))
        drawCircle(Color.White.copy(alpha = 0.55f), r * 0.22f, center + Offset(-r * 0.4f, -r * 0.4f))
    }
}

/**
 * Tap the tune: every tap anywhere plays the next note, as on a music box, and the word for
 * that note lights up with a ball bouncing over it. At the end of a line its picture does
 * its thing, and the next line is said before it is played. The song ends by itself.
 */
@Composable
fun TuneView(reel: TuneReel, padding: PaddingValues, onDone: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val song = reel.song
    val voice = song.lang
    val flat = remember(song) { song.lines.flatMapIndexed { l, line -> line.notes.indices.map { l to it } } }
    var played by remember(reel.id) { mutableIntStateOf(0) }
    var lineDone by remember(reel.id) { mutableIntStateOf(0) }
    var lineAt by remember(reel.id) { mutableIntStateOf(0) }
    val finished = played >= flat.size
    val playing = if (finished) song.lines.lastIndex else flat[played].first
    // A line just played stays on screen while its picture does its thing, then the next
    // one comes in; notes tapped meanwhile already belong to the next line.
    LaunchedEffect(lineDone) {
        if (lineDone == 0) return@LaunchedEffect
        delay(LINE_HOLD_MILLIS)
        lineAt = if (played >= flat.size) song.lines.lastIndex else flat[played].first
    }
    val line = song.lines[lineAt]
    val syllableAt = if (finished || lineAt != playing) line.notes.size else flat[played].second

    fun next() {
        // The state itself, not the value read at the last frame: fast taps come between frames.
        if (played >= flat.size) return
        val (l, s) = flat[played]
        env.note(song.lines[l].notes[s])
        played++
        if (s == song.lines[l].notes.lastIndex) {
            lineDone++
            if (played >= flat.size) {
                env.say(Songs.played(song), voice, Tone.CHEER)
                onDone()
            } else {
                env.say(Say(song.lines[l + 1].words, voice))
            }
        }
    }

    Column(
        Modifier.fillMaxSize().padding(padding).reelWidth().tap { next() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MultiText(song.title, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 24.sp, first = voice)
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Crossfade(lineAt, animationSpec = tween(350)) { l ->
                T(song.lines[l].picture, Modifier.floating().moveOn(lineDone, Motion.HOP).moveOn(lineDone, Motion.SPIN, delayMillis = 200), size = 110.sp)
            }
        }
        // The line, a word lit for each note: the next one to play, with the ball over it.
        AnimatedContent(
            lineAt,
            transitionSpec = { (slideInVertically(tween(300)) { it / 2 } + fadeIn(tween(300))) togetherWith fadeOut(tween(200)) },
        ) { at -> LineWords(song.lines[at].syllables, if (at == lineAt) syllableAt else song.lines[at].notes.size, finished) }
        Box(Modifier.fillMaxWidth().height(90.dp), contentAlignment = Alignment.Center) {
            if (finished) {
                MultiText(Songs.played(song), env.langs, colors.ink, Modifier.popOn(1), size = 20.sp, first = voice, say = { env.say(it) })
            } else if (played == 0) {
                T("👆", Modifier.floating(), size = 32.sp)
            } else {
                T("🎵 ${played} / ${flat.size}", color = colors.ink.copy(alpha = 0.7f), size = 15.sp, weight = FontWeight.Bold)
            }
        }
    }
}

/** How long a line just played stays on screen, for its picture to hop and spin. */
private const val LINE_HOLD_MILLIS = 800L

/** A line of the song, a word for each note: those [syllableAt] sung, the next lit with the ball over it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LineWords(syllables: List<String>, syllableAt: Int, finished: Boolean) {
    val colors = LocalReelColors.current
    FlowRow(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        syllables.forEachIndexed { i, syllable ->
            val lit = !finished && i == syllableAt
            val sung = finished || i < syllableAt
            val joined = syllable.endsWith("-")
            Column(Modifier.padding(end = if (joined) 0.dp else 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(14.dp), contentAlignment = Alignment.Center) { if (lit) Ball(colors.good) }
                T(
                    syllable.removeSuffix("-"),
                    Modifier.clip(RoundedCornerShape(8.dp)).background(if (lit) colors.good.copy(alpha = 0.3f) else Color.Transparent).padding(horizontal = 2.dp),
                    color = if (sung || lit) colors.ink else colors.ink.copy(alpha = 0.55f),
                    size = 28.sp,
                    weight = if (lit) FontWeight.Black else FontWeight.Bold,
                )
            }
        }
    }
}

/** The ball bouncing over the word to play. */
@Composable
private fun Ball(colour: Color) {
    val y by rememberInfiniteTransition().animateFloat(0f, -6f, infiniteRepeatable(tween(260), RepeatMode.Reverse))
    Box(Modifier.size(12.dp).graphicsLayer { translationY = y * density }.clip(CircleShape).background(colour))
}

/** Where Copy me is: waiting for Go, doing the moves, or done. */
private enum class CopyPhase { READY, PLAYING, DONE }

/** Roughly how long a line takes to say, so the next move waits for it: the engine does not say when it is done. */
private fun speakingMillis(text: String): Long = 700L + text.length * 70L

/**
 * "Copy me!": the drawn child does each move with the part ringed, and the moves go on
 * without waiting for a tap, so the child can look away from the screen and copy.
 */
@Composable
fun CopyMeView(reel: CopyMe, padding: PaddingValues, onDone: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    var phase by remember(reel.id) { mutableStateOf(CopyPhase.READY) }
    var at by remember(reel.id) { mutableIntStateOf(-1) }
    var beat by remember(reel.id) { mutableIntStateOf(0) }
    var lit by remember(reel.id) { mutableStateOf<String?>(null) }
    var bounce by remember(reel.id) { mutableIntStateOf(0) }
    var wobble by remember(reel.id) { mutableIntStateOf(0) }
    var shake by remember(reel.id) { mutableIntStateOf(0) }
    var hop by remember(reel.id) { mutableIntStateOf(0) }
    var spin by remember(reel.id) { mutableIntStateOf(0) }
    var caught by remember(reel.id) { mutableStateOf(false) }
    val done by rememberUpdatedState(onDone)

    // Swiped away and back: start again from Go, as a game half seen is no game.
    LaunchedEffect(env.active) { if (!env.active && phase == CopyPhase.PLAYING) phase = CopyPhase.READY }
    LaunchedEffect(phase, env.active) {
        if (phase != CopyPhase.PLAYING || !env.active) return@LaunchedEffect
        for ((i, step) in reel.steps.withIndex()) {
            at = i
            beat = 0
            caught = false
            val doIt = !reel.simon || step.simonSays
            lit = step.move.part.takeIf { doIt }
            val said = CopyMoves.said(step, reel.simon)[env.main]
            env.say(Say(said, env.main))
            delay(speakingMillis(said))
            if (doIt && step.move.beats > 0) {
                for (b in 1..step.move.beats) {
                    beat = b
                    bounce++
                    env.sfx(Sfx.DRUM)
                    env.say(NumberWords.of(b), tone = Tone.CHEER)
                    delay(850)
                }
                delay(500)
            } else if (doIt) {
                when (step.move.motion) {
                    CopyMotion.WOBBLE -> wobble++
                    CopyMotion.SHAKE -> shake++
                    CopyMotion.HOP -> hop++
                    CopyMotion.SPIN -> spin++
                    CopyMotion.BOUNCE -> bounce++
                    CopyMotion.STILL -> Unit
                }
                delay(2600)
            } else {
                // Simon did not say: the drawn child stays still, and so should the child.
                delay(2200)
                caught = true
                // The drawn child giggles at the catch.
                wobble++
                env.say(CopyMoves.gotcha, tone = Tone.CHEER)
                delay(speakingMillis(CopyMoves.gotcha[env.main]))
            }
        }
        lit = null
        at = -1
        phase = CopyPhase.DONE
        env.say(CopyMoves.done, tone = Tone.CHEER)
        done()
    }

    Column(Modifier.fillMaxSize().padding(padding).reelWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        MultiText(reel.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 28.sp, say = { env.say(it) })
        val part = lit?.let { key -> Diagram.BODY.parts.firstOrNull { it.key == key } }
        Box(Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            DiagramFigure(
                Diagram.BODY, part, null,
                Modifier.fillMaxSize()
                    .moveOn(bounce, Motion.SQUASH, millis = 300)
                    .moveOn(wobble, Motion.WOBBLE, times = 2)
                    .moveOn(shake, Motion.SHAKE, times = 2)
                    .moveOn(hop, Motion.HOP, times = 2)
                    .moveOn(spin, Motion.SPIN, millis = 900),
            ) { tapped ->
                // Between games, a tapped part says its name, as on the body reels.
                if (phase != CopyPhase.PLAYING) env.say(Diagrams.thisIs(Diagram.BODY, tapped))
            }
            if (beat > 0) {
                T(beat.toString(), Modifier.align(Alignment.TopEnd).padding(12.dp).popOn(beat), color = colors.ink, size = 64.sp, weight = FontWeight.Black)
            }
        }
        Box(Modifier.fillMaxWidth().height(110.dp), contentAlignment = Alignment.Center) {
            when (phase) {
                CopyPhase.READY -> GoButton(CopyMoves.go[env.main]) { phase = CopyPhase.PLAYING }
                CopyPhase.PLAYING -> reel.steps.getOrNull(at)?.let { step ->
                    // Each new move, and "Gotcha!", pops in, so a child looking up sees it changed.
                    MultiText(
                        if (caught) CopyMoves.gotcha else CopyMoves.said(step, reel.simon), env.langs, colors.ink,
                        Modifier.popOn(at * 2 + (if (caught) 2 else 1)), size = 22.sp, say = { env.say(it) },
                    )
                }
                CopyPhase.DONE -> Column(Modifier.popOn(1), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MultiText(CopyMoves.done, env.langs, colors.ink, size = 22.sp, say = { env.say(it) })
                    GoButton(CopyMoves.again[env.main]) { phase = CopyPhase.PLAYING }
                }
            }
        }
    }
}

@Composable
private fun GoButton(label: String, onClick: () -> Unit) {
    val colors = LocalReelColors.current
    Box(
        Modifier.clip(RoundedCornerShape(32.dp)).background(colors.good).pressable(onClick).padding(horizontal = 40.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) { T(label, color = Color.White, size = 26.sp, weight = FontWeight.Black) }
}
