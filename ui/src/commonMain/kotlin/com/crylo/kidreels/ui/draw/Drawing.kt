package com.crylo.kidreels.ui.draw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.ShapeKind
import com.crylo.kidreels.ui.common.heartPath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private fun polygon(n: Int, size: Size, startDegrees: Float, radius: Float = 0.47f): List<Offset> {
    val c = Offset(size.width / 2, size.height / 2)
    val r = min(size.width, size.height) * radius
    return List(n) { i ->
        val a = (startDegrees + i * 360f / n) * PI.toFloat() / 180f
        Offset(c.x + cos(a) * r, c.y + sin(a) * r)
    }
}

private fun star(size: Size): List<Offset> {
    val c = Offset(size.width / 2, size.height * 0.53f)
    val outer = min(size.width, size.height) * 0.5f
    val inner = outer * 0.42f
    return List(10) { i ->
        val r = if (i % 2 == 0) outer else inner
        val a = (-90f + i * 36f) * PI.toFloat() / 180f
        Offset(c.x + cos(a) * r, c.y + sin(a) * r)
    }
}

private fun Path.through(points: List<Offset>) {
    points.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
    close()
}

private fun rel(size: Size, vararg xy: Float): List<Offset> =
    xy.toList().chunked(2).map { (x, y) -> Offset(size.width * x, size.height * y) }

/** The outline of [kind], fitted into [size]. */
fun shapePath(kind: ShapeKind, size: Size): Path = Path().apply {
    val w = size.width
    val h = size.height
    when (kind) {
        ShapeKind.CIRCLE -> addOval(Rect(w * 0.04f, h * 0.04f, w * 0.96f, h * 0.96f))
        ShapeKind.OVAL -> addOval(Rect(w * 0.04f, h * 0.18f, w * 0.96f, h * 0.82f))
        ShapeKind.HEART -> addPath(heartPath(size))
        ShapeKind.CRESCENT -> {
            val outer = Path().apply { addOval(Rect(w * 0.06f, h * 0.06f, w * 0.94f, h * 0.94f)) }
            val bite = Path().apply { addOval(Rect(w * 0.3f, h * 0.0f, w * 1.12f, h * 0.82f)) }
            addPath(Path.combine(PathOperation.Difference, outer, bite))
        }
        else -> through(corners(kind, size))
    }
}

/** Where the corners (or points) are, for counting them one by one. Empty for round shapes. */
fun corners(kind: ShapeKind, size: Size): List<Offset> = when (kind) {
    ShapeKind.SQUARE -> rel(size, 0.1f, 0.1f, 0.9f, 0.1f, 0.9f, 0.9f, 0.1f, 0.9f)
    ShapeKind.RECTANGLE -> rel(size, 0.03f, 0.22f, 0.97f, 0.22f, 0.97f, 0.78f, 0.03f, 0.78f)
    ShapeKind.TRIANGLE -> rel(size, 0.5f, 0.08f, 0.96f, 0.88f, 0.04f, 0.88f)
    ShapeKind.DIAMOND -> rel(size, 0.5f, 0.03f, 0.86f, 0.5f, 0.5f, 0.97f, 0.14f, 0.5f)
    ShapeKind.PENTAGON -> polygon(5, size, -90f, 0.49f).map { it + Offset(0f, size.height * 0.03f) }
    ShapeKind.HEXAGON -> polygon(6, size, 0f, 0.49f)
    ShapeKind.OCTAGON -> polygon(8, size, 22.5f, 0.5f)
    ShapeKind.STAR -> star(size)
    else -> emptyList()
}

/** The points a child counts: a star's tips rather than all ten of its corners. */
fun countedCorners(kind: ShapeKind, size: Size): List<Offset> =
    if (kind == ShapeKind.STAR) star(size).filterIndexed { i, _ -> i % 2 == 0 } else corners(kind, size)

fun DrawScope.drawShape(kind: ShapeKind, color: Color, outline: Color = Color.Black.copy(alpha = 0.18f)) {
    val path = shapePath(kind, size)
    drawPath(path, color)
    drawPath(path, outline, style = Stroke(size.minDimension * 0.025f))
}

/** A drawn shape as a composable. */
@Composable
fun ShapeIcon(kind: ShapeKind, color: Color, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) { drawShape(kind, color) }
}

private val stickHead = Color(0xFFE5484D)

/**
 * Matchsticks under a number, the way Indian classrooms count with तीलियाँ: loose sticks
 * in fives, and a tied bundle for every ten once the number passes ten.
 */
@Composable
fun Sticks(n: Int, color: Color, modifier: Modifier = Modifier, height: Dp = 34.dp) {
    if (n <= 0) return
    val bundles = if (n > 10) n / 10 else 0
    val ones = if (n > 10) n % 10 else n
    val step = height * 0.24f
    val gap = height * 0.22f
    val bundleWidth = height * 0.6f
    val width = (bundleWidth + gap) * bundles + step * ones + gap * ((ones - 1).coerceAtLeast(0) / 5)
    Canvas(modifier.size(width.coerceAtLeast(1.dp), height)) {
        val t = size.height * 0.12f
        var x = t / 2
        repeat(bundles) {
            val bw = bundleWidth.toPx()
            repeat(10) { i -> stick(x + i * (bw - t) / 9, t * 0.8f, color) }
            drawRoundRect(color.copy(red = color.red * 0.6f, green = color.green * 0.6f, blue = color.blue * 0.6f),
                topLeft = Offset(x - t / 2, size.height * 0.55f), size = Size(bw, t * 1.2f), cornerRadius = CornerRadius(t))
            x += bw + gap.toPx()
        }
        repeat(ones) { i ->
            if (i > 0 && i % 5 == 0) x += gap.toPx()
            stick(x, t, color)
            x += step.toPx()
        }
    }
}

private fun DrawScope.stick(x: Float, t: Float, color: Color) {
    drawLine(color, Offset(x, size.height * 0.22f), Offset(x, size.height - t / 2), t, StrokeCap.Round)
    drawCircle(stickHead, radius = t * 0.85f, center = Offset(x, size.height * 0.16f))
}

/** [rows] rows of [cols] dots, to show multiplication as an array. */
@Composable
fun Dots(rows: Int, cols: Int, color: Color, modifier: Modifier = Modifier, dot: Dp = 14.dp) {
    Canvas(modifier.size(dot * cols * 1.5f, dot * rows * 1.5f)) {
        val d = dot.toPx()
        for (r in 0 until rows) for (c in 0 until cols) {
            drawCircle(color, d / 2, Offset(d * 0.75f + c * d * 1.5f, d * 0.75f + r * d * 1.5f))
        }
    }
}

/** An analogue clock face with hands at [hour]:[minute]. */
fun DrawScope.drawClock(hour: Int, minute: Int, face: Color, ink: Color, accent: Color, measurer: TextMeasurer) {
    val c = center
    val r = size.minDimension / 2 * 0.96f
    drawCircle(face, r, c)
    drawCircle(ink, r, c, style = Stroke(r * 0.05f))
    for (i in 1..12) {
        val a = (i * 30 - 90) * PI.toFloat() / 180f
        val p = Offset(c.x + cos(a) * r * 0.78f, c.y + sin(a) * r * 0.78f)
        val label = measurer.measure(i.toString(), TextStyle(fontSize = (r / density * 0.2f).sp, fontWeight = FontWeight.Bold, color = ink))
        drawText(label, topLeft = p - Offset(label.size.width / 2f, label.size.height / 2f))
    }
    for (i in 0 until 60) {
        val a = (i * 6) * PI.toFloat() / 180f
        val inner = if (i % 5 == 0) 0.9f else 0.94f
        drawLine(ink.copy(alpha = 0.5f), Offset(c.x + cos(a) * r * inner, c.y + sin(a) * r * inner), Offset(c.x + cos(a) * r * 0.98f, c.y + sin(a) * r * 0.98f), r * 0.012f)
    }
    val hourAngle = ((hour % 12) + minute / 60f) * 30f
    val minuteAngle = minute * 6f
    rotate(hourAngle, c) { drawLine(ink, c, Offset(c.x, c.y - r * 0.5f), r * 0.07f, StrokeCap.Round) }
    rotate(minuteAngle, c) { drawLine(accent, c, Offset(c.x, c.y - r * 0.74f), r * 0.045f, StrokeCap.Round) }
    drawCircle(ink, r * 0.06f, c)
}
