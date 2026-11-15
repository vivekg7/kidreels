package com.crylo.kidreels.ui.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.crylo.kidreels.core.SolidKind
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * A tiny 3D renderer, enough for the six school solids: a mesh of flat faces, rotated,
 * projected with perspective and flat-shaded. Every solid is convex, so hiding the faces
 * that point away is all the depth handling it needs.
 */

data class V3(val x: Float, val y: Float, val z: Float) {
    operator fun minus(o: V3) = V3(x - o.x, y - o.y, z - o.z)
    operator fun plus(o: V3) = V3(x + o.x, y + o.y, z + o.z)
    operator fun times(s: Float) = V3(x * s, y * s, z * s)
    infix fun dot(o: V3) = x * o.x + y * o.y + z * o.z
    infix fun cross(o: V3) = V3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)
    fun norm(): V3 = sqrt(this dot this).let { if (it == 0f) this else this * (1 / it) }
}

/** [edges]: draw face outlines. Polyhedra show their edges; smooth solids do not. */
class Mesh(val vertices: List<V3>, val faces: List<IntArray>, val edges: Boolean)

private fun ring(n: Int, y: Float, r: Float): List<V3> = List(n) { i ->
    val a = i * 2 * PI.toFloat() / n
    V3(cos(a) * r, y, sin(a) * r)
}

private fun box(x: Float, y: Float, z: Float): Mesh {
    val v = listOf(
        V3(-x, -y, -z), V3(x, -y, -z), V3(x, y, -z), V3(-x, y, -z),
        V3(-x, -y, z), V3(x, -y, z), V3(x, y, z), V3(-x, y, z),
    )
    val f = listOf(intArrayOf(0, 1, 2, 3), intArrayOf(4, 5, 6, 7), intArrayOf(0, 1, 5, 4), intArrayOf(2, 3, 7, 6), intArrayOf(1, 2, 6, 5), intArrayOf(0, 3, 7, 4))
    return Mesh(v, f, edges = true)
}

fun meshOf(kind: SolidKind): Mesh = when (kind) {
    SolidKind.CUBE -> box(0.8f, 0.8f, 0.8f)
    SolidKind.CUBOID -> box(1.15f, 0.62f, 0.62f)
    SolidKind.PYRAMID -> {
        val v = listOf(V3(-0.9f, -0.7f, -0.9f), V3(0.9f, -0.7f, -0.9f), V3(0.9f, -0.7f, 0.9f), V3(-0.9f, -0.7f, 0.9f), V3(0f, 1.0f, 0f))
        Mesh(v, listOf(intArrayOf(0, 1, 2, 3), intArrayOf(0, 1, 4), intArrayOf(1, 2, 4), intArrayOf(2, 3, 4), intArrayOf(3, 0, 4)), edges = true)
    }
    SolidKind.CYLINDER -> {
        val n = 32
        val v = ring(n, -0.85f, 0.75f) + ring(n, 0.85f, 0.75f)
        val sides = List(n) { i -> intArrayOf(i, (i + 1) % n, n + (i + 1) % n, n + i) }
        Mesh(v, sides + listOf(IntArray(n) { it }, IntArray(n) { n + it }), edges = false)
    }
    SolidKind.CONE -> {
        val n = 32
        val v = ring(n, -0.75f, 0.85f) + V3(0f, 1.05f, 0f)
        val sides = List(n) { i -> intArrayOf(i, (i + 1) % n, n) }
        Mesh(v, sides + listOf(IntArray(n) { it }), edges = false)
    }
    SolidKind.SPHERE -> {
        val lat = 12
        val lon = 20
        val v = mutableListOf<V3>()
        for (i in 0..lat) {
            val t = PI.toFloat() * i / lat
            for (j in 0 until lon) {
                val p = 2 * PI.toFloat() * j / lon
                v += V3(sin(t) * cos(p), cos(t), sin(t) * sin(p)) * 0.95f
            }
        }
        val f = mutableListOf<IntArray>()
        for (i in 0 until lat) for (j in 0 until lon) {
            val a = i * lon + j
            val b = i * lon + (j + 1) % lon
            f += intArrayOf(a, b, b + lon, a + lon)
        }
        Mesh(v, f, edges = false)
    }
}

private val light = V3(-0.45f, 0.65f, 1f).norm()

/** Draws [mesh] turned by [yaw] and [pitch] (radians), filling the canvas. */
fun DrawScope.drawMesh(mesh: Mesh, yaw: Float, pitch: Float, color: Color, edge: Color) {
    val cy = cos(yaw)
    val sy = sin(yaw)
    val cp = cos(pitch)
    val sp = sin(pitch)
    val turned = mesh.vertices.map { v ->
        val x = v.x * cy + v.z * sy
        val z = -v.x * sy + v.z * cy
        val y = v.y * cp - z * sp
        V3(x, y, v.y * sp + z * cp)
    }
    val camera = 5f
    val scale = size.minDimension * 0.36f * camera / 4f
    fun project(v: V3): Offset {
        val k = camera / (camera - v.z)
        return Offset(center.x + v.x * k * scale, center.y - v.y * k * scale)
    }
    val eye = V3(0f, 0f, camera)
    val visible = mesh.faces.mapNotNull { face ->
        val pts = face.map { turned[it] }
        val mid = pts.reduce { a, b -> a + b } * (1f / pts.size)
        // Winding varies between the meshes above; for a convex solid around the origin
        // the outward normal is the one pointing away from the centre.
        // The sphere's polar faces repeat a vertex, so fall back to the other corners.
        val c = ((pts[1] - pts[0]) cross (pts[2] - pts[0])).let { if ((it dot it) < 1e-9f) (pts[2] - pts[1]) cross (pts[3 % pts.size] - pts[1]) else it }
        var n = if ((c dot c) < 1e-9f) mid.norm() else c.norm()
        if (n dot mid < 0) n = n * -1f
        if (n dot (eye - mid) <= 0f) null else Triple(pts, mid.z, n)
    }.sortedBy { it.second }
    for ((pts, _, n) in visible) {
        val shade = 0.42f + 0.58f * max(0f, n dot light)
        val fill = Color(color.red * shade, color.green * shade, color.blue * shade, color.alpha)
        val path = Path().apply {
            pts.map(::project).forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
            close()
        }
        drawPath(path, fill)
        // A hairline in the face colour hides the seams between smooth faces.
        drawPath(path, if (mesh.edges) edge else fill, style = Stroke(if (mesh.edges) size.minDimension * 0.012f else 1.2f, join = StrokeJoin.Round))
    }
}
