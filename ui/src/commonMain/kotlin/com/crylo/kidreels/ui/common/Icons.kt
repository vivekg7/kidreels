package com.crylo.kidreels.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * The few icons the app needs, drawn in code: no icon library, nothing to license, and
 * they scale to any size. Each draws into a square canvas.
 */

enum class Icon { HEART, SPEAKER, GEAR, BACK }

@Composable
fun DrawnIcon(icon: Icon, color: Color, modifier: Modifier, filled: Boolean = false) {
    Canvas(modifier) {
        when (icon) {
            Icon.HEART -> heart(color, filled)
            Icon.SPEAKER -> speaker(color)
            Icon.GEAR -> gear(color)
            Icon.BACK -> back(color)
        }
    }
}

fun heartPath(size: Size): Path {
    val w = size.width
    val h = size.height
    return Path().apply {
        moveTo(w * 0.5f, h * 0.9f)
        cubicTo(w * 0.1f, h * 0.62f, -w * 0.02f, h * 0.3f, w * 0.25f, h * 0.14f)
        cubicTo(w * 0.38f, h * 0.06f, w * 0.48f, h * 0.16f, w * 0.5f, h * 0.26f)
        cubicTo(w * 0.52f, h * 0.16f, w * 0.62f, h * 0.06f, w * 0.75f, h * 0.14f)
        cubicTo(w * 1.02f, h * 0.3f, w * 0.9f, h * 0.62f, w * 0.5f, h * 0.9f)
        close()
    }
}

private fun DrawScope.heart(color: Color, filled: Boolean) {
    val p = heartPath(size)
    if (filled) drawPath(p, color, style = Fill)
    else drawPath(p, color, style = Stroke(size.minDimension * 0.09f, join = StrokeJoin.Round))
}

private fun DrawScope.speaker(color: Color) {
    val w = size.width
    val h = size.height
    val body = Path().apply {
        moveTo(w * 0.1f, h * 0.38f)
        lineTo(w * 0.3f, h * 0.38f)
        lineTo(w * 0.52f, h * 0.18f)
        lineTo(w * 0.52f, h * 0.82f)
        lineTo(w * 0.3f, h * 0.62f)
        lineTo(w * 0.1f, h * 0.62f)
        close()
    }
    drawPath(body, color)
    val stroke = Stroke(w * 0.08f, cap = StrokeCap.Round)
    drawArc(color, -45f, 90f, false, topLeft = Offset(w * 0.42f, h * 0.32f), size = Size(w * 0.28f, h * 0.36f), style = stroke)
    drawArc(color, -50f, 100f, false, topLeft = Offset(w * 0.4f, h * 0.18f), size = Size(w * 0.5f, h * 0.64f), style = stroke)
}

private fun DrawScope.gear(color: Color) {
    val c = center
    val outer = size.minDimension * 0.46f
    val inner = size.minDimension * 0.34f
    val teeth = 8
    val path = Path()
    for (i in 0 until teeth * 2) {
        val r = if (i % 2 == 0) outer else inner
        // Each tooth spans a little either side of its angle, giving flat tops.
        for (side in listOf(-1, 1)) {
            val a = (i + side * 0.25f) * PI.toFloat() / teeth
            val p = Offset(c.x + cos(a) * r, c.y + sin(a) * r)
            if (i == 0 && side == -1) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
        }
    }
    path.close()
    // The hole in the middle: an even-odd fill cuts it out without an offscreen layer.
    path.fillType = PathFillType.EvenOdd
    path.addOval(Rect(c, inner * 0.45f))
    drawPath(path, color)
}

private fun DrawScope.back(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(w * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val p = Path().apply {
        moveTo(w * 0.62f, h * 0.2f)
        lineTo(w * 0.32f, h * 0.5f)
        lineTo(w * 0.62f, h * 0.8f)
    }
    drawPath(p, color, style = stroke)
}
