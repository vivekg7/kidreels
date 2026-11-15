package com.crylo.kidreels.core

/*
 * The abstract figures of the reasoning puzzles (the kind the Navodaya entrance test
 * uses). Everything sits on a small grid, so turning and flipping are exact and two
 * figures can be compared for "the same": no floating point, no "nearly equal".
 * Screen coordinates: x to the right, y downwards; a direction is in degrees, 0 pointing
 * right and 90 pointing down.
 */

enum class MarkKind(
    /** Turning by this many degrees leaves the mark looking the same (360: only a full turn). */
    val symmetry: Int,
) {
    DOT(1), RING(1), SQUARE(90), BOX(90), CROSS(90), BAR(180), TRIANGLE(360), ARROW(360), HALF(360),
}

/** One mark in a figure's 3 × 3 grid: [x] and [y] are −1, 0 or 1. */
data class Mark(val kind: MarkKind, val x: Int, val y: Int, val dir: Int = 0) {
    /** The same mark written one way only, so equal-looking marks compare equal. */
    fun normal(): Mark = copy(dir = if (kind.symmetry == 1) 0 else ((dir % kind.symmetry) + kind.symmetry) % kind.symmetry)
}

/** The eight places round the edge of the grid, clockwise from the top left. */
private val ring = listOf(-1 to -1, 0 to -1, 1 to -1, 1 to 0, 1 to 1, 0 to 1, -1 to 1, -1 to 0)

data class Figure(val marks: List<Mark>) {
    /** What the figure looks like, whatever order its marks were listed in. */
    val look: Set<Mark> get() = marks.map { it.normal() }.toSet()

    fun same(other: Figure): Boolean = look == other.look

    /** Turned clockwise by [quarters] quarter turns about the centre. */
    fun turned(quarters: Int): Figure {
        var f = this
        repeat(((quarters % 4) + 4) % 4) { f = Figure(f.marks.map { Mark(it.kind, -it.y, it.x, it.dir + 90) }) }
        return f
    }

    /** As in a mirror stood to its side: left and right swap. */
    fun mirrored(): Figure = Figure(marks.map { Mark(it.kind, -it.x, it.y, 180 - it.dir) })

    /** Upside down, as in water below it: top and bottom swap. */
    fun flipped(): Figure = Figure(marks.map { Mark(it.kind, it.x, -it.y, -it.dir) })

    /**
     * Each mark on the edge moves [steps] places clockwise round it (times its speed), and
     * if [turn], turns 45° for each place; a mark in the middle only turns. The figure
     * series' "what comes next".
     */
    fun stepped(steps: Int, speeds: List<Int> = marks.map { 1 }, turn: Boolean = true): Figure = Figure(
        marks.mapIndexed { i, m ->
            val s = steps * speeds[i]
            val dir = if (turn) m.dir + 45 * s else m.dir
            val at = ring.indexOf(m.x to m.y)
            if (at < 0) m.copy(dir = dir) else ring[((at + s) % 8 + 8) % 8].let { (x, y) -> Mark(m.kind, x, y, dir) }
        },
    )
}

/** True if no two of [figures] look the same. */
fun allDifferent(figures: List<Figure>): Boolean = figures.map { it.look }.toSet().size == figures.size

/** A point on the lattice the cut-out pieces and hidden-figure lines are drawn on. */
data class Pt(val x: Int, val y: Int)

/** A flat piece cut from a square board, as its corners in order. */
data class Polygon(val points: List<Pt>) {
    /** Moved so its top-left is at 0, 0: two pieces of the same shape and way round compare equal. */
    val shape: Set<Pt>
        get() {
            val mx = points.minOf { it.x }
            val my = points.minOf { it.y }
            return points.map { Pt(it.x - mx, it.y - my) }.toSet()
        }

    /** Turned clockwise by quarter turns within a board [size] wide. */
    fun turned(quarters: Int, size: Int): Polygon {
        var p = points
        repeat(((quarters % 4) + 4) % 4) { p = p.map { Pt(size - it.y, it.x) } }
        return Polygon(p)
    }

    fun mirrored(size: Int): Polygon = Polygon(points.map { Pt(size - it.x, it.y) }.reversed())
}

/** One straight line between two neighbouring lattice points (sideways, up or diagonal). */
data class Edge(val a: Pt, val b: Pt) {
    companion object {
        /** The same edge whichever end is named first. */
        fun of(a: Pt, b: Pt): Edge = if (a.x < b.x || (a.x == b.x && a.y < b.y)) Edge(a, b) else Edge(b, a)
    }
}

/** Lines on the lattice: a shape to find, or the busy figure it hides in. */
data class Lines(val edges: Set<Edge>) {
    fun moved(dx: Int, dy: Int) = Lines(edges.map { Edge(Pt(it.a.x + dx, it.a.y + dy), Pt(it.b.x + dx, it.b.y + dy)) }.toSet())

    val width: Int get() = edges.maxOf { maxOf(it.a.x, it.b.x) } - edges.minOf { minOf(it.a.x, it.b.x) }
    val height: Int get() = edges.maxOf { maxOf(it.a.y, it.b.y) } - edges.minOf { minOf(it.a.y, it.b.y) }

    /** Moved so its top-left is at 0, 0. */
    fun atOrigin(): Lines = moved(-edges.minOf { minOf(it.a.x, it.b.x) }, -edges.minOf { minOf(it.a.y, it.b.y) })

    /** Whether [shape], the same size and way round, appears anywhere in these lines. */
    fun hides(shape: Lines, size: Int): Boolean {
        val s = shape.atOrigin()
        return (0..size - s.width).any { dx -> (0..size - s.height).any { dy -> edges.containsAll(s.moved(dx, dy).edges) } }
    }
}

/** A square of paper's holes, as cells of a 4 × 4 grid: [col] and [row] are 0 to 3. */
data class Hole(val col: Int, val row: Int)
