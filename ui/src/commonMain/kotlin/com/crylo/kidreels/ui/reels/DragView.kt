package com.crylo.kidreels.ui.reels

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.crylo.kidreels.core.DragGame
import com.crylo.kidreels.core.DragKind
import com.crylo.kidreels.core.NumberWords
import com.crylo.kidreels.core.Phrases
import com.crylo.kidreels.core.Sprite
import com.crylo.kidreels.core.Tone
import com.crylo.kidreels.ui.ReelState
import com.crylo.kidreels.ui.Sfx
import com.crylo.kidreels.ui.common.MultiText
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.floating
import com.crylo.kidreels.ui.common.Motion
import com.crylo.kidreels.ui.common.moveOn
import com.crylo.kidreels.ui.common.popOn
import com.crylo.kidreels.ui.common.shakeOn
import com.crylo.kidreels.ui.common.tap
import com.crylo.kidreels.ui.draw.ShapeIcon
import com.crylo.kidreels.ui.draw.shapePath
import com.crylo.kidreels.ui.skin.LocalReelColors
import com.crylo.kidreels.ui.skin.reelWidth
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Drag each piece to where it belongs. The places are drawn by [DragGame.kind]; the
 * pieces wait in a tray below. A right drop stays, a wrong one slides back and is
 * explained, and the game is solved once every place is full.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DragView(game: DragGame, state: ReelState, padding: PaddingValues, onSolved: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val bounds = remember(game.id) { mutableStateMapOf<Int, Rect>() }
    val placed = state.placed
    var shakeTarget by remember(game.id) { mutableStateOf(-1 to 0) }
    val slack = with(LocalDensity.current) { 20.dp.toPx() }

    fun filled(t: Int) = placed.values.count { it == t }

    /** True if the piece stays where it was dropped. */
    fun drop(piece: Int, at: Offset): Boolean {
        if (state.solved) return false
        val t = bounds.entries.filter { it.value.inflate(slack).contains(at) }
            .minByOrNull { (it.value.center - at).getDistanceSquared() }?.key ?: return false
        if (game.fits(piece, t, filled(t))) {
            placed[piece] = t
            val said = if (game.kind == DragKind.BASKET) NumberWords.of(filled(t)) else game.pieces[piece].placed
            if (game.targets.indices.all { filled(it) == game.targets[it].capacity }) {
                env.sfx(Sfx.CORRECT)
                env.praise(game.reveal, null, first = said)
                onSolved()
            } else {
                env.sfx(Sfx.TICK)
                said?.let { env.say(it) }
            }
            return true
        }
        state.wrongTries++
        shakeTarget = t to shakeTarget.second + 1
        env.sfx(Sfx.WRONG)
        env.say(game.pieces[piece].whyNot[t] ?: Phrases.pick(Phrases.tryAgain, Random), tone = Tone.GENTLE)
        return false
    }

    fun register(t: Int) = Modifier.onGloballyPositioned { bounds[t] = it.boundsInRoot() }
    fun shakeOf(t: Int) = if (shakeTarget.first == t) shakeTarget.second else 0
    fun tapTarget(t: Int) {
        val target = game.targets[t]
        val sprite = target.sprite
        when {
            target.label != null -> env.say(target.label!!)
            sprite is Sprite.Emoji && game.kind != DragKind.BASKET -> env.name(sprite.text)
            else -> env.say(game.say)
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().padding(padding).reelWidth()) {
        val width = maxWidth
        val height = maxHeight
        val cell = if (game.kind == DragKind.MISSING_PIECE) missingCell(game, width, height) else 0.dp
        val pieceSize: Dp = when (game.kind) {
            DragKind.MISSING_PIECE -> cell.coerceAtMost((width - 12.dp * game.pieces.size) / game.pieces.size)
            DragKind.SHAPE_SORTER -> 76.dp
            DragKind.BASKET -> if (game.pieces.size > 8) 54.dp else 64.dp
            else -> 66.dp
        }
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            MultiText(game.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 24.sp, say = { env.say(it) })
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                when (game.kind) {
                    DragKind.MISSING_PIECE -> MissingPicture(game, placed.isNotEmpty() && state.solved, cell, register(0), ::tapTarget, shakeOf(0))
                    DragKind.SHAPE_SORTER -> Board {
                        game.targets.forEachIndexed { t, target ->
                            val piece = placed.entries.firstOrNull { it.value == t }?.key
                            ShapeHole(target.sprite as Sprite.Shape, piece?.let { game.pieces[it].sprite as Sprite.Shape }, register(t).shakeOn(shakeOf(t)).tap { tapTarget(t) })
                        }
                    }
                    DragKind.FEED -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        game.targets.forEachIndexed { t, target ->
                            val food = placed.entries.filter { it.value == t }.map { it.key }
                            Column(
                                register(t).shakeOn(shakeOf(t)).clip(RoundedCornerShape(24.dp)).background(colors.card)
                                    .tap { tapTarget(t) }.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                // Fed, every animal wags and hops.
                                val cheer = if (env.calm) 0 else state.celebrate
                                T((target.sprite as Sprite.Emoji).text, Modifier.popOn(food.size).moveOn(cheer, Motion.WAG, times = 2).moveOn(cheer, Motion.HOP, delayMillis = 760 + t * 120), size = 60.sp)
                                Box(Modifier.height(48.dp), contentAlignment = Alignment.Center) {
                                    // Keyed by piece, so only the one just dropped pops in.
                                    food.forEach { p -> key(p) { SpriteView(game.pieces[p].sprite, 44.dp, Modifier.popOn(1)) } }
                                }
                            }
                        }
                    }
                    DragKind.SORT -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        game.targets.forEachIndexed { t, target ->
                            Basket(game, t, placed.entries.filter { it.value == t }.map { it.key }, register(t).shakeOn(shakeOf(t)).weight(1f).tap { tapTarget(t) })
                        }
                    }
                    DragKind.BASKET -> Basket(game, 0, placed.keys.toList(), register(0).shakeOn(shakeOf(0)).fillMaxWidth(0.8f).tap { tapTarget(0) })
                }
            }
            // The tray: pieces still to place. A tap names the piece; a drag moves it.
            FlowRow(
                Modifier.fillMaxWidth().heightIn(min = pieceSize + 16.dp).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                game.pieces.forEachIndexed { i, piece ->
                    // A placed piece leaves its slot empty, so the others stay under the
                    // child's finger instead of sliding along.
                    if (i in placed) {
                        Box(Modifier.size(pieceSize))
                        return@forEachIndexed
                    }
                    key(game.id, i) {
                        // Once solved, the pieces left over go: they are not part of the answer.
                        // They fade and shrink where they are, so the tray keeps its shape, and
                        // take no more taps.
                        val left by animateFloatAsState(if (state.solved) 0f else 1f, tween(250))
                        if (state.solved) {
                            Box(Modifier.graphicsLayer { alpha = left; scaleX = 0.5f + 0.5f * left; scaleY = scaleX }) {
                                SpriteView(piece.sprite, pieceSize, card = game.kind != DragKind.SHAPE_SORTER)
                            }
                        } else Draggable(
                            onTap = {
                                when (val s = piece.sprite) {
                                    is Sprite.Emoji -> env.name(s.text)
                                    is Sprite.Shape -> piece.placed?.let { env.say(it) }
                                    is Sprite.Crop -> env.say(game.say)
                                }
                            },
                            onDrop = { drop(i, it) },
                        ) { SpriteView(piece.sprite, pieceSize, card = game.kind != DragKind.SHAPE_SORTER) }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.Center) {
                val reveal = game.reveal
                if (state.solved && reveal != null) MultiText(reveal, env.langs, colors.ink, Modifier.popOn(1), size = 17.sp, say = { env.say(it) })
                else if (placed.isEmpty() && !state.solved) T("👆", Modifier.floating(), size = 28.sp)
            }
        }
    }
}

/** The side of one square of the missing-piece picture. */
private fun missingCell(game: DragGame, width: Dp, height: Dp): Dp {
    val grid = (game.targets[0].sprite as Sprite.Crop).grid
    val side = minOf(width * 0.78f, height * 0.5f, if (grid == 2) 240.dp else 285.dp)
    return side / grid
}

/** Follows the finger; on release asks [onDrop], and slides home if the answer is no. */
@Composable
private fun Draggable(onTap: () -> Unit, onDrop: (Offset) -> Boolean, content: @Composable () -> Unit) {
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var dragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // The gesture handlers below are made once and outlive this composition, and the page is
    // first made off screen, muted: they call through to the latest drop and its voice.
    val tapped by rememberUpdatedState(onTap)
    val dropped by rememberUpdatedState(onDrop)
    Box(
        Modifier.zIndex(if (dragging) 10f else 0f)
            .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) }
            .onGloballyPositioned { coords = it }
            .graphicsLayer {
                val s = if (dragging) 1.15f else 1f
                scaleX = s
                scaleY = s
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = {
                        dragging = false
                        val center = coords?.boundsInRoot()?.center
                        if (center == null || !dropped(center)) scope.launch { offset.animateTo(Offset.Zero, spring(Spring.DampingRatioMediumBouncy)) }
                    },
                    onDragCancel = {
                        dragging = false
                        scope.launch { offset.animateTo(Offset.Zero) }
                    },
                ) { change, drag ->
                    change.consume()
                    scope.launch { offset.snapTo(offset.value + drag) }
                }
            }
            .pointerInput(Unit) { detectTapGestures { tapped() } },
    ) { content() }
}

@Composable
private fun SpriteView(sprite: Sprite, size: Dp, modifier: Modifier = Modifier, card: Boolean = false) {
    val colors = LocalReelColors.current
    val back = if (card) Modifier.clip(RoundedCornerShape(16.dp)).background(colors.card) else Modifier
    when (sprite) {
        is Sprite.Emoji -> Box(modifier.size(size).then(back), contentAlignment = Alignment.Center) {
            T(sprite.text, size = with(LocalDensity.current) { (size * 0.66f).toSp() })
        }
        is Sprite.Shape -> ShapeIcon(sprite.kind, Color(sprite.argb), size, modifier)
        is Sprite.Crop -> Crop(sprite, size, modifier)
    }
}

/** The whole picture, drawn the same way everywhere so a piece matches its gap exactly. */
@Composable
private fun WholePicture(emoji: String, side: Dp, modifier: Modifier = Modifier) {
    Box(modifier.requiredSize(side).background(Color(0xFFFFFDF7)), contentAlignment = Alignment.Center) {
        T(emoji, size = with(LocalDensity.current) { (side * 0.8f).toSp() })
    }
}

/** One square of the picture, the size of [cell], turned as the sprite says. */
@Composable
private fun Crop(crop: Sprite.Crop, cell: Dp, modifier: Modifier = Modifier) {
    val col = crop.cell % crop.grid
    val row = crop.cell / crop.grid
    Box(
        modifier.size(cell).graphicsLayer { rotationZ = crop.turns * 90f }
            .clip(RoundedCornerShape(4.dp)).clipToBounds()
            .border(1.dp, Color(0x332D2440), RoundedCornerShape(4.dp)),
    ) {
        WholePicture(crop.emoji, cell * crop.grid, Modifier.wrapContentSize(Alignment.TopStart, unbounded = true).offset(-cell * col, -cell * row))
    }
}

/** The picture with one square left empty, until the right piece fills it. */
@Composable
private fun MissingPicture(game: DragGame, done: Boolean, cell: Dp, target: Modifier, tap: (Int) -> Unit, shake: Int) {
    val hole = game.targets[0].sprite as Sprite.Crop
    val side = cell * hole.grid
    val col = hole.cell % hole.grid
    val row = hole.cell / hole.grid
    Box(
        Modifier.shakeOn(shake).popOn(if (done) 1 else 0).size(side).clip(RoundedCornerShape(18.dp))
            .border(3.dp, Color(0xFF2D2440), RoundedCornerShape(18.dp)),
    ) {
        WholePicture(hole.emoji, side, Modifier.wrapContentSize(Alignment.TopStart, unbounded = true))
        if (!done) {
            Box(
                Modifier.offset(cell * col, cell * row).then(target).size(cell).background(Color(0xFFE9E4F5)).tap { tap(0) },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize().padding(4.dp)) {
                    drawRoundRect(
                        Color(0xFF6C4CF5), style = Stroke(3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    )
                }
                T("?", color = Color(0xFF6C4CF5), size = 30.sp, weight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun Board(content: @Composable () -> Unit) {
    val colors = LocalReelColors.current
    Row(
        Modifier.clip(RoundedCornerShape(26.dp)).background(colors.card).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) { content() }
}

/** A shape-shaped hole in the board; the piece shows in it once it is in. */
@Composable
private fun ShapeHole(hole: Sprite.Shape, filled: Sprite.Shape?, modifier: Modifier) {
    Box(modifier.size(78.dp), contentAlignment = Alignment.Center) {
        if (filled != null) {
            ShapeIcon(filled.kind, Color(filled.argb), 76.dp, Modifier.popOn(1))
        } else {
            Canvas(Modifier.size(76.dp)) {
                val path = shapePath(hole.kind, size)
                drawPath(path, Color(0x552D2440))
                drawPath(path, Color(0xFF2D2440), style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))))
            }
        }
    }
}

/** A basket: its sign (a member, or 🧺) and name, and the pieces ([inside]) put in so far. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Basket(game: DragGame, t: Int, inside: List<Int>, modifier: Modifier) {
    val colors = LocalReelColors.current
    val env = LocalReelEnv.current
    val target = game.targets[t]
    Column(
        modifier.clip(RoundedCornerShape(24.dp)).background(colors.card).padding(12.dp).heightIn(min = 190.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            T((target.sprite as Sprite.Emoji).text, size = if (game.kind == DragKind.BASKET) 56.sp else 34.sp)
            if (game.kind == DragKind.BASKET && inside.isNotEmpty()) {
                T(inside.size.toString(), Modifier.popOn(inside.size), color = colors.cardInk, size = 40.sp, weight = FontWeight.Black)
            }
        }
        target.label?.let { T(it[env.main].replaceFirstChar { c -> c.uppercase() }, color = colors.cardInk, size = 16.sp, weight = FontWeight.Bold) }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Keyed by piece, so only the one just dropped pops in.
            inside.forEach { p -> key(p) { SpriteView(game.pieces[p].sprite, 36.dp, Modifier.popOn(1)) } }
        }
    }
}
