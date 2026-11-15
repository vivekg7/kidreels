package com.crylo.kidreels.ui.reels

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.CupGame
import com.crylo.kidreels.core.Memory
import com.crylo.kidreels.core.OrderGame
import com.crylo.kidreels.core.PairsGame
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.Stage
import com.crylo.kidreels.ui.ReelState
import com.crylo.kidreels.ui.Sfx
import com.crylo.kidreels.ui.common.MultiText
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.floating
import com.crylo.kidreels.ui.common.popOn
import com.crylo.kidreels.ui.common.shakeOn
import com.crylo.kidreels.ui.common.tap
import com.crylo.kidreels.ui.skin.LocalReelColors
import com.crylo.kidreels.ui.skin.reelWidth
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The memory channel's pink, for card backs. */
private val MemoryPink = Color(0xFFE0629A)
private val CupRed = Color(0xFFE5484D)
private val CupDark = Color(0xFFB4262B)

private enum class Phase { SHOW, MOVE, ANSWER, DONE }

/**
 * The cup shuffle. The prize is shown under its cup, the cups come down and swap places
 * (each swap an arc, one cup over and one under, so a child can follow it), then the
 * child taps a cup. A wrong cup lifts to show it is empty.
 */
@Composable
fun CupView(game: CupGame, state: ReelState, padding: PaddingValues, onSolved: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val scope = rememberCoroutineScope()
    var phase by remember(game.id) { mutableStateOf(if (state.solved) Phase.DONE else Phase.SHOW) }
    // Where each cup stands, in slots from the left; the prize stays with cup game.start.
    val xs = remember(game.id) { List(game.cups) { Animatable(it.toFloat()) } }
    val arcs = remember(game.id) { List(game.cups) { Animatable(0f) } }
    val lifts = remember(game.id) { List(game.cups) { Animatable(if (it == game.start) 1f else 0f) } }
    var shake by remember(game.id) { mutableIntStateOf(0) }
    var shaking by remember(game.id) { mutableIntStateOf(-1) }

    LaunchedEffect(game.id, env.active) {
        if (!env.active || phase == Phase.ANSWER || phase == Phase.DONE) return@LaunchedEffect
        // Swiped away mid-shuffle: start again from the top.
        phase = Phase.SHOW
        xs.forEachIndexed { i, x -> x.snapTo(i.toFloat()) }
        lifts.forEachIndexed { i, l -> l.snapTo(if (i == game.start) 1f else 0f) }
        delay(2600)
        lifts[game.start].animateTo(0f, tween(350))
        delay(500)
        phase = Phase.MOVE
        for ((a, b) in game.swaps) {
            val ca = xs.indices.first { xs[it].targetValue.roundToInt() == a }
            val cb = xs.indices.first { xs[it].targetValue.roundToInt() == b }
            val spec = tween<Float>(game.swapMillis, easing = LinearEasing)
            launch { xs[ca].animateTo(b.toFloat(), spec) }
            launch { xs[cb].animateTo(a.toFloat(), spec) }
            launch { arcs[ca].snapTo(0f); arcs[ca].animateTo(1f, spec) }
            arcs[cb].snapTo(0f)
            arcs[cb].animateTo(-1f, spec)
            arcs[ca].snapTo(0f)
            arcs[cb].snapTo(0f)
            delay(80)
        }
        phase = Phase.ANSWER
        env.say(Memory.cupsPick(game.prize))
    }

    fun tapped(cup: Int) {
        if (phase != Phase.ANSWER || cup in state.wrong) return
        if (cup == game.start) {
            phase = Phase.DONE
            scope.launch { lifts[cup].animateTo(1f, tween(300)) }
            env.sfx(Sfx.CORRECT)
            env.praise(Memory.cupsFound(game.prize), null)
            onSolved()
        } else {
            state.wrong += cup
            state.wrongTries++
            shaking = cup
            shake++
            env.sfx(Sfx.WRONG)
            env.say(Memory.cupsEmpty(game.prize), tone = com.crylo.kidreels.core.Tone.GENTLE)
            scope.launch {
                lifts[cup].animateTo(1f, tween(250))
                delay(700)
                lifts[cup].animateTo(0f, tween(250))
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(padding).reelWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        MultiText(game.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 28.sp, say = { env.say(it) })
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val slot = maxWidth / game.cups
            val cupW = minOf(slot * 0.82f, 120.dp)
            val cupH = cupW * 1.1f
            val ground = maxHeight * 0.62f
            val prizeSize = (cupW.value * 0.62f).sp
            for (c in 0 until game.cups) {
                val x = slot * (xs[c].value + 0.5f) - cupW / 2
                // One cup goes over in a high arc, the other slips under in a low one.
                val arc = sin(PI * kotlin.math.abs(arcs[c].value)).toFloat() * (if (arcs[c].value >= 0) -cupH.value * 0.45f else cupH.value * 0.12f)
                if (c == game.start && lifts[c].value > 0.05f) {
                    Box(Modifier.offset(x, ground - cupW * 0.62f).size(cupW, cupW * 0.62f), contentAlignment = Alignment.Center) {
                        T(game.prize.emoji, Modifier.popOn(if (phase == Phase.DONE) 1 else 0), size = prizeSize)
                    }
                }
                Canvas(
                    Modifier.offset(x, ground - cupH - cupH * 0.75f * lifts[c].value + arc.dp).size(cupW, cupH)
                        .shakeOn(if (shaking == c) shake else 0)
                        .tap { tapped(c) },
                ) { drawCup() }
            }
            // The table the cups stand on.
            Box(Modifier.offset(0.dp, ground).fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(colors.ink.copy(alpha = 0.25f)))
        }
        Box(Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
            when (phase) {
                Phase.SHOW -> T("👀", Modifier.floating(), size = 36.sp)
                Phase.ANSWER -> T("👆", Modifier.floating(), size = 36.sp)
                Phase.DONE -> MultiText(Memory.cupsFound(game.prize), env.langs, colors.ink, size = 20.sp, say = { env.say(it) })
                else -> Unit
            }
        }
    }
}

/** An upturned cup: wide at the table, narrow at the top, with a rim and a shine. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCup() {
    val w = size.width
    val h = size.height
    val body = Path().apply {
        moveTo(w * 0.2f, h * 0.06f)
        lineTo(w * 0.8f, h * 0.06f)
        lineTo(w * 0.96f, h * 0.9f)
        lineTo(w * 0.04f, h * 0.9f)
        close()
    }
    drawPath(body, CupRed)
    drawRoundRect(CupDark, Offset(w * 0.18f, 0f), Size(w * 0.64f, h * 0.1f), androidx.compose.ui.geometry.CornerRadius(h * 0.05f))
    drawRoundRect(CupDark, Offset(0f, h * 0.86f), Size(w, h * 0.14f), androidx.compose.ui.geometry.CornerRadius(h * 0.06f))
    drawLine(Color.White.copy(alpha = 0.45f), Offset(w * 0.32f, h * 0.2f), Offset(w * 0.24f, h * 0.76f), w * 0.07f)
}

/**
 * Find the pairs: cards face down, two turned at a time. A turned card says what it is,
 * so the name helps the memory. A miss counts as a slip only when the partner of one of
 * the two had been seen before: missing a card never seen is luck, not memory.
 */
@Composable
fun PairsView(game: PairsGame, state: ReelState, padding: PaddingValues, onSolved: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val scope = rememberCoroutineScope()
    val up = remember(game.id) { mutableStateListOf<Int>() }
    val matched = remember(game.id) { mutableStateListOf<Int>() }
    val seen = remember(game.id) { mutableStateListOf<Int>() }
    var peeking by remember(game.id) { mutableStateOf(game.peekMillis > 0 && !state.solved) }

    LaunchedEffect(game.id, env.active) {
        if (!env.active || !peeking) return@LaunchedEffect
        delay(game.peekMillis + 1500)
        peeking = false
    }

    fun tapped(i: Int) {
        if (peeking || state.solved || up.size >= 2 || i in up || i in matched) return
        up += i
        val card = game.cards[i]
        env.say(Say(card.one[env.main], env.main))
        if (up.size < 2) {
            seen += i
            return
        }
        val (a, b) = up[0] to up[1]
        if (game.cards[a] == game.cards[b]) {
            matched += a
            matched += b
            up.clear()
            seen += i
            if (matched.size == game.cards.size) {
                env.sfx(Sfx.CORRECT)
                env.praise(Memory.pairsDone, null)
                onSolved()
            } else {
                env.sfx(Sfx.TICK)
            }
        } else {
            val partnerSeen = listOf(a, b).any { k -> seen.any { it != k && game.cards[it] == game.cards[k] } }
            if (partnerSeen) state.wrongTries++
            seen += i
            scope.launch {
                delay(1000)
                up.clear()
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(padding).reelWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        MultiText(game.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 28.sp, say = { env.say(it) })
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            val rows = game.cards.size / game.columns
            val gap = 10.dp
            val card = minOf((maxWidth - gap * (game.columns + 1)) / game.columns, (maxHeight - gap * (rows + 1)) / rows, 120.dp)
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                for (r in 0 until rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        for (c in 0 until game.columns) {
                            val i = r * game.columns + c
                            Card(game.cards[i].emoji, card, faceUp = peeking || i in up || i in matched || state.solved, matched = i in matched) { tapped(i) }
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
            if (peeking) MultiText(Memory.pairsPeek, env.langs, colors.ink, size = 20.sp, say = { env.say(it) })
            else if (state.solved) MultiText(Memory.pairsDone, env.langs, colors.ink, size = 20.sp, say = { env.say(it) })
        }
    }
}

@Composable
private fun Card(emoji: String, size: Dp, faceUp: Boolean, matched: Boolean, onTap: () -> Unit) {
    val turn by animateFloatAsState(if (faceUp) 180f else 0f, tween(280))
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.size(size).graphicsLayer { rotationY = turn; cameraDistance = 12f * density }
            .clip(shape).background(if (turn > 90f) Color.White else MemoryPink)
            // A white edge on the back, so pink cards stand out on the pink look too.
            .border(3.dp, if (matched) Color(0xFF2E9E5B) else if (turn > 90f) Color.Transparent else Color.White.copy(alpha = 0.85f), shape)
            .tap(onTap),
        contentAlignment = Alignment.Center,
    ) {
        if (turn > 90f) {
            T(emoji, Modifier.graphicsLayer { rotationY = 180f }, size = (size.value * 0.5f).sp)
        } else {
            T("?", color = Color.White.copy(alpha = 0.85f), size = (size.value * 0.42f).sp, weight = FontWeight.Black)
        }
    }
}

/**
 * Copy the order: the pads light up one after another, then the child taps them in the
 * same order. A wrong pad shows the order again, from the start.
 */
@Composable
fun OrderView(game: OrderGame, state: ReelState, padding: PaddingValues, onSolved: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val scope = rememberCoroutineScope()
    var phase by remember(game.id) { mutableStateOf(if (state.solved) Phase.DONE else Phase.SHOW) }
    var lit by remember(game.id) { mutableIntStateOf(-1) }
    var done by remember(game.id) { mutableIntStateOf(0) }
    var round by remember(game.id) { mutableIntStateOf(0) }
    var shake by remember(game.id) { mutableIntStateOf(0) }

    LaunchedEffect(game.id, env.active, round) {
        if (!env.active || phase == Phase.DONE) return@LaunchedEffect
        phase = Phase.SHOW
        done = 0
        // Time for the spoken instructions first; a replay starts sooner.
        delay(if (round == 0) 3200 else 1200)
        for (p in game.sequence) {
            lit = p
            env.sfx(Sfx.TICK)
            delay(game.stepMillis.toLong())
            lit = -1
            delay(game.stepMillis * 35L / 100)
        }
        phase = Phase.ANSWER
        env.say(Memory.orderYourTurn)
    }

    fun tapped(p: Int) {
        if (phase != Phase.ANSWER) return
        scope.launch {
            lit = p
            delay(220)
            if (lit == p) lit = -1
        }
        if (p == game.sequence[done]) {
            done++
            if (done == game.sequence.size) {
                phase = Phase.DONE
                env.sfx(Sfx.CORRECT)
                env.praise(Memory.orderDone(game.sequence.size), null)
                onSolved()
            } else {
                env.sfx(Sfx.TICK)
            }
        } else {
            state.wrongTries++
            shake++
            env.sfx(Sfx.WRONG)
            env.say(Memory.orderAgain, tone = com.crylo.kidreels.core.Tone.GENTLE)
            phase = Phase.SHOW
            round++
        }
    }

    Column(Modifier.fillMaxSize().padding(padding).reelWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        MultiText(game.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 28.sp, say = { env.say(it) })
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            val pad = minOf((maxWidth - 40.dp) / 2, (maxHeight - 40.dp) / 2, 150.dp)
            Column(Modifier.shakeOn(shake), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                (0 until game.pads).chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { p ->
                            val on = lit == p
                            val glow by animateFloatAsState(if (on) 1f else 0f, tween(90))
                            Box(
                                Modifier.size(pad).graphicsLayer { scaleX = 1f + glow * 0.08f; scaleY = scaleX }
                                    .clip(RoundedCornerShape(28.dp))
                                    // Solid and darker until lit: see-through pads took on the look's colour,
                                    // so yellow showed orange on the pink look.
                                    .background(lerp(Color(Memory.padColours[p].argb), Color.Black, 0.35f * (1f - glow)))
                                    .border(if (on) 5.dp else 0.dp, Color.White, RoundedCornerShape(28.dp))
                                    .tap { tapped(p) },
                            )
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
            when (phase) {
                Phase.SHOW -> T("👀", Modifier.floating(), size = 36.sp)
                else -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    game.sequence.indices.forEach { i ->
                        Box(Modifier.size(14.dp).clip(CircleShape).background(colors.ink.copy(alpha = if (i < done || phase == Phase.DONE) 1f else 0.25f)))
                    }
                }
            }
        }
    }
}

/**
 * "What is missing?": the things for a few seconds, then the same without one. Until the
 * look is over the answers stay hidden: one of them was on screen, which would give it away.
 */
@Composable
fun MemoryStage(stage: Stage.Memory, quizId: Long, looking: Boolean, solved: Boolean, onLooked: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val left = remember(quizId) { Animatable(1f) }
    LaunchedEffect(quizId, env.active) {
        if (!env.active || !looking) return@LaunchedEffect
        left.snapTo(1f)
        left.animateTo(0f, tween(stage.lookMillis.toInt(), easing = LinearEasing))
        onLooked()
    }
    val shown: List<String?> = when {
        looking || solved -> stage.items
        stage.reshuffle -> stage.items.filterIndexed { i, _ -> i != stage.missing }.shuffled(Random(quizId))
        else -> stage.items.mapIndexed { i, e -> if (i == stage.missing) null else e }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        shown.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { e ->
                    Box(Modifier.size(88.dp).clip(RoundedCornerShape(20.dp)).background(colors.card), contentAlignment = Alignment.Center) {
                        if (e == null) {
                            T("?", color = colors.cardInk, size = 44.sp, weight = FontWeight.Black)
                        } else {
                            val back = solved && e == stage.items[stage.missing]
                            Pic(e, 54.sp, if (back) Modifier.popOn(1) else Modifier)
                        }
                    }
                }
            }
        }
        if (looking) {
            Box(Modifier.fillMaxWidth(0.6f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(colors.ink.copy(alpha = 0.2f))) {
                Box(Modifier.fillMaxWidth(left.value).height(8.dp).clip(RoundedCornerShape(4.dp)).background(colors.ink))
            }
        }
    }
}
