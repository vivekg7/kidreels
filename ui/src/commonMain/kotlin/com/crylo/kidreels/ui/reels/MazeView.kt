package com.crylo.kidreels.ui.reels

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Maze
import com.crylo.kidreels.core.MazeGame
import com.crylo.kidreels.core.Mazes
import com.crylo.kidreels.ui.ReelState
import com.crylo.kidreels.ui.Sfx
import com.crylo.kidreels.ui.common.MultiText
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.floating
import com.crylo.kidreels.ui.common.Motion
import com.crylo.kidreels.ui.common.moveOn
import com.crylo.kidreels.ui.common.popOn
import com.crylo.kidreels.ui.draw.Ink
import com.crylo.kidreels.ui.draw.Paper
import com.crylo.kidreels.ui.skin.LocalReelColors
import com.crylo.kidreels.ui.skin.reelWidth
import kotlin.math.abs

private val Trail = Color(0xFFFFB300)

/**
 * A maze on paper. The runner is dragged along the paths (a drag that starts on it, so
 * a drag anywhere else still turns the page) or moved by tapping the square next to it,
 * for hands too small to drag. Going back along the trail rubs it out. Dead ends are
 * part of a maze, not mistakes, so nothing here counts as a wrong try.
 */
@Composable
fun MazeView(game: MazeGame, state: ReelState, padding: PaddingValues, onSolved: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val maze = game.maze
    val trail = remember(game.id) { mutableStateListOf(if (state.solved) game.goal else game.start) }
    val at = trail.last()

    fun moveTo(next: Int) {
        if (state.solved) return
        if (trail.size >= 2 && trail[trail.size - 2] == next) trail.removeAt(trail.lastIndex) else trail += next
        if (next == game.goal) {
            env.sfx(Sfx.CORRECT)
            env.praise(Mazes.reached(game.runner), null)
            onSolved()
        }
    }

    // The gesture handlers below outlive this composition (they are keyed on the maze, not
    // on every frame), so they call through to the latest moveTo and its voice.
    val move by rememberUpdatedState(::moveTo)
    val nameRunner by rememberUpdatedState { env.name(game.runner.emoji) }

    /** The side of [from] that leads to [to], if they are neighbours. */
    fun sideTo(from: Int, to: Int): Int? = Maze.SIDES.firstOrNull { side ->
        val r = from / maze.cols
        val c = from % maze.cols
        val (nr, nc) = when (side) { Maze.N -> r - 1 to c; Maze.S -> r + 1 to c; Maze.E -> r to c + 1; else -> r to c - 1 }
        nr in 0 until maze.rows && nc in 0 until maze.cols && maze.index(nr, nc) == to
    }

    Column(Modifier.fillMaxSize().padding(padding).reelWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        MultiText(game.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 26.sp, say = { env.say(it) })
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            val cell = minOf((maxWidth - 24.dp) / maze.cols, (maxHeight - 24.dp) / maze.rows, 72.dp)
            val cellPx = with(androidx.compose.ui.platform.LocalDensity.current) { cell.toPx() }
            Box(
                Modifier.shadow(8.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(Paper).padding(8.dp),
            ) {
                Box(
                    Modifier.size(cell * maze.cols, cell * maze.rows).pointerInput(game.id, cellPx) {
                        detectTapGestures { p ->
                            val tapped = maze.index((p.y / cellPx).toInt().coerceIn(0, maze.rows - 1), (p.x / cellPx).toInt().coerceIn(0, maze.cols - 1))
                            val here = trail.last()
                            val side = sideTo(here, tapped)
                            if (side != null && maze.isOpen(here, side)) move(tapped) else nameRunner()
                        }
                    },
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val w = cellPx
                        fun centre(i: Int) = Offset((i % maze.cols + 0.5f) * w, (i / maze.cols + 0.5f) * w)
                        if (trail.size > 1) {
                            val path = Path().apply {
                                moveTo(centre(trail[0]).x, centre(trail[0]).y)
                                trail.drop(1).forEach { lineTo(centre(it).x, centre(it).y) }
                            }
                            drawPath(path, Trail.copy(alpha = 0.7f), style = Stroke(w * 0.28f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                        val wall = maxOf(3f, w * 0.09f)
                        for (i in maze.open.indices) {
                            val x = (i % maze.cols) * w
                            val y = (i / maze.cols) * w
                            if (!maze.isOpen(i, Maze.N)) drawLine(Ink, Offset(x, y), Offset(x + w, y), wall, StrokeCap.Round)
                            if (!maze.isOpen(i, Maze.W)) drawLine(Ink, Offset(x, y), Offset(x, y + w), wall, StrokeCap.Round)
                            if (i % maze.cols == maze.cols - 1 && !maze.isOpen(i, Maze.E)) drawLine(Ink, Offset(x + w, y), Offset(x + w, y + w), wall, StrokeCap.Round)
                            if (i / maze.cols == maze.rows - 1 && !maze.isOpen(i, Maze.S)) drawLine(Ink, Offset(x, y + w), Offset(x + w, y + w), wall, StrokeCap.Round)
                        }
                    }
                    val emoji = (cell.value * 0.62f).sp
                    Box(Modifier.offset(cell * (game.goal % maze.cols), cell * (game.goal / maze.cols)).size(cell), contentAlignment = Alignment.Center) {
                        T(game.runner.goal, if (state.solved) Modifier else Modifier.floating(3f, 1200), size = emoji)
                    }
                    val x by animateDpAsState(cell * (at % maze.cols), tween(90))
                    val y by animateDpAsState(cell * (at / maze.cols), tween(90))
                    Box(
                        Modifier.offset(x, y).size(cell).pointerInput(game.id, cellPx) {
                            // The finger's lead over the runner; a step is taken once it is
                            // half a square ahead, towards an open side.
                            var lead = Offset.Zero
                            detectDragGestures(onDragStart = { lead = Offset.Zero }) { change, drag ->
                                change.consume()
                                lead += drag
                                while (true) {
                                    val side = when {
                                        abs(lead.x) >= abs(lead.y) && lead.x > cellPx * 0.55f -> Maze.E
                                        abs(lead.x) >= abs(lead.y) && lead.x < -cellPx * 0.55f -> Maze.W
                                        abs(lead.y) > abs(lead.x) && lead.y > cellPx * 0.55f -> Maze.S
                                        abs(lead.y) > abs(lead.x) && lead.y < -cellPx * 0.55f -> Maze.N
                                        else -> break
                                    }
                                    val next = maze.step(trail.last(), side)
                                    if (next == null || state.solved) {
                                        // Against a wall: the runner stays, and so does the finger's lead, capped.
                                        lead = Offset(lead.x.coerceIn(-cellPx * 0.6f, cellPx * 0.6f), lead.y.coerceIn(-cellPx * 0.6f, cellPx * 0.6f))
                                        break
                                    }
                                    move(next)
                                    lead -= when (side) { Maze.E -> Offset(cellPx, 0f); Maze.W -> Offset(-cellPx, 0f); Maze.S -> Offset(0f, cellPx); else -> Offset(0f, -cellPx) }
                                }
                            }
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        // At the goal, the runner dances.
                        val cheer = if (env.calm) 0 else state.celebrate
                        T(game.runner.emoji, Modifier.popOn(if (state.solved) 1 else 0).moveOn(cheer, Motion.HOP, times = 2).moveOn(cheer, Motion.SPIN, delayMillis = 1040), size = emoji)
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
            if (state.solved) MultiText(Mazes.reached(game.runner), env.langs, colors.ink, Modifier.popOn(1), size = 20.sp, say = { env.say(it) })
            else if (trail.size == 1) T("👆", Modifier.floating(), size = 32.sp)
        }
    }
}
