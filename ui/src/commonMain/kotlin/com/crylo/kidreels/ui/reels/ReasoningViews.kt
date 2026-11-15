package com.crylo.kidreels.ui.reels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Choice
import com.crylo.kidreels.core.Figure
import com.crylo.kidreels.core.Layout
import com.crylo.kidreels.core.Stage
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.popOn
import com.crylo.kidreels.ui.draw.Accent
import com.crylo.kidreels.ui.draw.FigureCard
import com.crylo.kidreels.ui.draw.Ink
import com.crylo.kidreels.ui.draw.Paper
import com.crylo.kidreels.ui.draw.drawCutout
import com.crylo.kidreels.ui.draw.drawFigure
import com.crylo.kidreels.ui.draw.drawHoles
import com.crylo.kidreels.ui.draw.drawLines
import com.crylo.kidreels.ui.draw.drawPiece
import com.crylo.kidreels.ui.draw.drawShapeLines
import com.crylo.kidreels.ui.draw.path
import com.crylo.kidreels.ui.skin.LocalReelColors

/** The board's colour in the cut-out puzzle, and the pieces'. */
private val Board = Color(0xFF7C5CF8)

/** The figures of a reasoning puzzle; [answer] fills the "?" once solved. */
@Composable
fun FiguresStage(stage: Stage.Figures, answer: Figure?) {
    val colors = LocalReelColors.current
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val w = maxWidth
        val h = maxHeight
        fun slot(f: Figure?, size: Dp) = @Composable {
            if (f == null && answer != null) FigureCard(answer, size, Modifier.popOn(1), highlight = true) else FigureCard(f, size, highlight = f == null)
        }
        when (stage.layout) {
            Layout.ONE -> slot(stage.figures[0], minOf(w * 0.62f, h, 230.dp))()
            Layout.MIRROR -> {
                val size = minOf(w * 0.55f, h, 220.dp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    slot(stage.figures[0], size)()
                    // The mirror: a silvery strip standing on the figure's right.
                    Box(
                        Modifier.width(16.dp).height(size * 1.1f).clip(RoundedCornerShape(6.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF9FB8D0), Color(0xFFE8F1FA), Color(0xFF9FB8D0))))
                            .border(1.dp, Ink.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                    )
                    Box(Modifier.size(size * 0.35f))
                }
            }
            Layout.ROW -> {
                val n = stage.figures.size
                val size = minOf((w - 6.dp * (n - 1)) / n, h, 84.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { stage.figures.forEach { slot(it, size)() } }
            }
            Layout.ANALOGY -> {
                val size = minOf((w - 80.dp) / 4, h, 84.dp)
                val (a, b, c, d) = stage.figures
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    slot(a, size)()
                    T("→", color = colors.ink, size = 22.sp, weight = FontWeight.Black)
                    slot(b, size)()
                    T("  ::  ", color = colors.ink, size = 20.sp, weight = FontWeight.Black)
                    slot(c, size)()
                    T("→", color = colors.ink, size = 22.sp, weight = FontWeight.Black)
                    slot(d, size)()
                }
            }
            Layout.GRID -> {
                // One square pattern in four quarters, drawn as one tile with thin lines between.
                val side = minOf(w * 0.7f, h, 250.dp)
                Column(Modifier.clip(RoundedCornerShape(14.dp)).background(Paper).border(2.dp, Ink, RoundedCornerShape(14.dp))) {
                    stage.figures.chunked(2).forEach { row ->
                        Row {
                            row.forEach { f ->
                                Box(Modifier.size(side / 2).border(0.5.dp, Ink.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                    val shown = f ?: answer
                                    if (shown != null) {
                                        Canvas(Modifier.fillMaxSize().padding(8.dp).then(if (f == null) Modifier.popOn(1) else Modifier)) { drawFigure(shown, if (f == null) Accent else Ink) }
                                    } else {
                                        Box(Modifier.fillMaxSize().padding(6.dp).background(Accent.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                            T("?", color = Accent, size = 40.sp, weight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** The square with a piece cut out; once solved the gap is filled in. */
@Composable
fun CutoutStage(stage: Stage.Cutout, solved: Boolean) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth * 0.62f, maxHeight, 240.dp)
        Canvas(Modifier.size(side).popOn(if (solved) 1 else 0)) {
            drawCutout(stage.board, stage.piece, Board)
            if (solved) {
                val unit = size.minDimension / stage.board
                drawPath(stage.piece.path(unit), Board.copy(alpha = 0.75f))
            }
        }
    }
}

/** The busy lines; once solved the hidden shape lights up in them. */
@Composable
fun HiddenStage(stage: Stage.Hidden, solved: Boolean) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth * 0.65f, maxHeight, 250.dp)
        Box(Modifier.size(side).clip(RoundedCornerShape(14.dp)).background(Paper).padding(10.dp)) {
            Canvas(Modifier.fillMaxSize()) { drawLines(stage.lines, stage.size, found = stage.answer.takeIf { solved }) }
        }
    }
}

/** First the paper and how it folds, then the folded paper with its holes. */
@Composable
fun FoldStage(stage: Stage.Fold) {
    val colors = LocalReelColors.current
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val side = minOf((maxWidth - 50.dp) / 2, maxHeight, 160.dp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Canvas(Modifier.size(side)) {
                drawHoles(emptySet(), showGrid = false)
                val dash = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
                val s = size.minDimension
                if (stage.vertical) {
                    drawLine(Accent, Offset(s / 2, 0f), Offset(s / 2, s), dash.width, pathEffect = dash.pathEffect)
                    arrow(Offset(s * 0.18f, s / 2), Offset(s * 0.82f, s / 2))
                }
                if (stage.twice || !stage.vertical) {
                    drawLine(Accent, Offset(0f, s / 2), Offset(s, s / 2), dash.width, pathEffect = dash.pathEffect)
                    arrow(Offset(s * (if (stage.twice) 0.75f else 0.5f), s * 0.18f), Offset(s * (if (stage.twice) 0.75f else 0.5f), s * 0.82f))
                }
            }
            T("→", color = colors.ink, size = 26.sp, weight = FontWeight.Black)
            Canvas(Modifier.size(side)) {
                val cell = size.minDimension / 4
                // The part of the square that is gone (folded under) is a faint outline.
                drawRect(Color.White.copy(alpha = 0.7f), size = Size(cell * 4, cell * 4), style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))))
                val left = if (stage.vertical) 2 else 0
                val top = if (stage.twice || !stage.vertical) 2 else 0
                val tl = Offset(left * cell, top * cell)
                val sz = Size((4 - left) * cell, (4 - top) * cell)
                drawRect(Paper, tl, sz)
                drawRect(Ink, tl, sz, style = Stroke(2.dp.toPx()))
                stage.holes.forEach { drawCircle(Ink, cell * 0.28f, Offset((it.col + 0.5f) * cell, (it.row + 0.5f) * cell)) }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.arrow(from: Offset, to: Offset) {
    val w = 3.dp.toPx()
    drawLine(Accent, from, to, w)
    val d = (to - from) / (to - from).getDistance()
    val n = Offset(-d.y, d.x)
    val head = 10.dp.toPx()
    drawLine(Accent, to, to - d * head + n * head * 0.6f, w)
    drawLine(Accent, to, to - d * head - n * head * 0.6f, w)
}

/** A reasoning choice, drawn square in the middle of its paper answer card. */
@Composable
fun ReasoningChoice(choice: Choice, size: Dp) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(4.dp)) {
            when (choice) {
                is Choice.Fig -> drawFigure(choice.figure)
                is Choice.Cut -> drawPiece(choice.piece, this.size.minDimension / 4.2f, Board)
                is Choice.Shapes -> drawShapeLines(choice.lines, this.size.minDimension / 3.4f)
                is Choice.Holes -> drawHoles(choice.holes)
                else -> Unit
            }
        }
    }
}
