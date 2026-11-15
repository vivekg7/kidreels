package com.crylo.kidreels.ui.draw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Figure
import com.crylo.kidreels.core.Hole
import com.crylo.kidreels.core.Lines
import com.crylo.kidreels.core.Mark
import com.crylo.kidreels.core.MarkKind
import com.crylo.kidreels.core.Polygon
import com.crylo.kidreels.ui.common.T

/*
 * The reasoning puzzles' figures. Black on paper white in every look, like the clock and
 * sudoku: they are read by small differences, so contrast beats matching the skin.
 */

val Paper = Color(0xFFFFFDF7)
val Ink = Color(0xFF2D2440)
val Accent = Color(0xFF6C4CF5)

/** [figure] filling this square, on a 3 × 3 grid. */
fun DrawScope.drawFigure(figure: Figure, ink: Color = Ink) {
    val cell = size.minDimension / 3f
    figure.marks.forEach { drawMark(it, Offset((it.x + 1.5f) * cell, (it.y + 1.5f) * cell), cell * 0.34f, ink) }
}

private fun DrawScope.drawMark(m: Mark, c: Offset, r: Float, ink: Color) {
    val line = Stroke(r * 0.22f, cap = StrokeCap.Round)
    rotate(m.dir.toFloat(), c) {
        when (m.kind) {
            MarkKind.DOT -> drawCircle(ink, r * 0.75f, c)
            MarkKind.RING -> drawCircle(ink, r * 0.8f, c, style = line)
            MarkKind.SQUARE -> drawRect(ink, Offset(c.x - r * 0.75f, c.y - r * 0.75f), Size(r * 1.5f, r * 1.5f))
            MarkKind.BOX -> drawRect(ink, Offset(c.x - r * 0.75f, c.y - r * 0.75f), Size(r * 1.5f, r * 1.5f), style = line)
            MarkKind.CROSS -> {
                drawLine(ink, Offset(c.x - r, c.y), Offset(c.x + r, c.y), line.width, StrokeCap.Round)
                drawLine(ink, Offset(c.x, c.y - r), Offset(c.x, c.y + r), line.width, StrokeCap.Round)
            }
            MarkKind.BAR -> drawLine(ink, Offset(c.x - r * 1.1f, c.y), Offset(c.x + r * 1.1f, c.y), line.width * 1.4f, StrokeCap.Round)
            // Points along its direction: to the right when dir is 0.
            MarkKind.TRIANGLE -> drawPath(
                Path().apply {
                    moveTo(c.x + r, c.y)
                    lineTo(c.x - r * 0.8f, c.y - r * 0.9f)
                    lineTo(c.x - r * 0.8f, c.y + r * 0.9f)
                    close()
                },
                ink,
            )
            MarkKind.ARROW -> {
                drawLine(ink, Offset(c.x - r * 1.1f, c.y), Offset(c.x + r * 1.1f, c.y), line.width, StrokeCap.Round)
                drawLine(ink, Offset(c.x + r * 1.1f, c.y), Offset(c.x + r * 0.45f, c.y - r * 0.6f), line.width, StrokeCap.Round)
                drawLine(ink, Offset(c.x + r * 1.1f, c.y), Offset(c.x + r * 0.45f, c.y + r * 0.6f), line.width, StrokeCap.Round)
            }
            // A circle with the half towards its direction filled.
            MarkKind.HALF -> {
                drawCircle(ink, r * 0.8f, c, style = line)
                drawArc(ink, -90f, 180f, true, Offset(c.x - r * 0.8f, c.y - r * 0.8f), Size(r * 1.6f, r * 1.6f))
            }
        }
    }
}

/** A paper card with a figure on it, or a "?" for the one to find. */
@Composable
fun FigureCard(figure: Figure?, size: Dp, modifier: Modifier = Modifier, highlight: Boolean = false) {
    val shape = RoundedCornerShape(size * 0.12f)
    Box(
        modifier.size(size).clip(shape).background(Paper).border(if (highlight) 3.dp else 1.5.dp, if (highlight) Accent else Ink.copy(alpha = 0.35f), shape),
        contentAlignment = Alignment.Center,
    ) {
        if (figure != null) {
            Canvas(Modifier.fillMaxSize().padding(size * 0.06f)) { drawFigure(figure) }
        } else {
            T("?", color = Accent, size = (size.value * 0.42f).sp, weight = FontWeight.Black)
        }
    }
}

/** The polygon's outline as a path, at [unit] per lattice step, moved by [origin]. */
fun Polygon.path(unit: Float, origin: Offset = Offset.Zero): Path = Path().apply {
    points.forEachIndexed { i, p ->
        val x = origin.x + p.x * unit
        val y = origin.y + p.y * unit
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

/** A square board of [board] steps with [piece] cut out: the gap shows dashed. */
fun DrawScope.drawCutout(board: Int, piece: Polygon, fill: Color) {
    val unit = size.minDimension / board
    drawRect(fill, size = Size(unit * board, unit * board))
    val hole = piece.path(unit)
    drawPath(hole, Paper)
    drawPath(hole, Ink, style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))))
    drawRect(Ink, size = Size(unit * board, unit * board), style = Stroke(2.dp.toPx()))
}

/** A piece on its own, moved to the top left and drawn at [unit] per step, centred. */
fun DrawScope.drawPiece(piece: Polygon, unit: Float, fill: Color) {
    val mx = piece.points.minOf { it.x }
    val my = piece.points.minOf { it.y }
    val w = (piece.points.maxOf { it.x } - mx) * unit
    val h = (piece.points.maxOf { it.y } - my) * unit
    val origin = Offset((size.width - w) / 2 - mx * unit, (size.height - h) / 2 - my * unit)
    val path = piece.path(unit, origin)
    drawPath(path, fill)
    drawPath(path, Ink, style = Stroke(2.dp.toPx()))
}

/** Lines on a lattice of [steps] × [steps], filling the square; [found] drawn over in colour. */
fun DrawScope.drawLines(lines: Lines, steps: Int, ink: Color = Ink, found: Lines? = null, foundColor: Color = Accent) {
    // Inset by a line's width, so lines along the edge are not cut in half.
    val inset = 4.dp.toPx()
    val unit = (size.minDimension - 2 * inset) / steps
    fun at(x: Int, y: Int) = Offset(inset + x * unit, inset + y * unit)
    lines.edges.forEach { drawLine(ink, at(it.a.x, it.a.y), at(it.b.x, it.b.y), 3.dp.toPx(), StrokeCap.Round) }
    found?.edges?.forEach { drawLine(foundColor, at(it.a.x, it.a.y), at(it.b.x, it.b.y), 6.dp.toPx(), StrokeCap.Round) }
}

/** A shape to find, centred in this square at [unit] per step. */
fun DrawScope.drawShapeLines(lines: Lines, unit: Float, ink: Color = Ink) {
    val w = lines.width * unit
    val h = lines.height * unit
    val ox = (size.width - w) / 2
    val oy = (size.height - h) / 2
    lines.atOrigin().edges.forEach {
        drawLine(ink, Offset(ox + it.a.x * unit, oy + it.a.y * unit), Offset(ox + it.b.x * unit, oy + it.b.y * unit), 3.dp.toPx(), StrokeCap.Round)
    }
}

/** An open square of paper, 4 × 4 cells, with holes punched in it. */
fun DrawScope.drawHoles(holes: Set<Hole>, showGrid: Boolean = true) {
    val cell = size.minDimension / 4
    drawRect(Paper, size = Size(cell * 4, cell * 4))
    if (showGrid) for (i in 1..3) {
        drawLine(Ink.copy(alpha = 0.12f), Offset(i * cell, 0f), Offset(i * cell, cell * 4), 1.dp.toPx())
        drawLine(Ink.copy(alpha = 0.12f), Offset(0f, i * cell), Offset(cell * 4, i * cell), 1.dp.toPx())
    }
    holes.forEach { drawCircle(Ink, cell * 0.28f, Offset((it.col + 0.5f) * cell, (it.row + 0.5f) * cell)) }
    drawRect(Ink, size = Size(cell * 4, cell * 4), style = Stroke(2.dp.toPx()))
}
