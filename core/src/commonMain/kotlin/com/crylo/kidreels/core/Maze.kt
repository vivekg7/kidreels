package com.crylo.kidreels.core

import kotlin.random.Random

/**
 * A maze of [rows] × [cols] cells. [open] holds, for each cell (row by row), which sides
 * have no wall: [N], [E], [S], [W] as bits. Made by a random depth-first walk, so there is
 * exactly one way between any two cells: no loops, and every cell can be reached.
 */
data class Maze(val rows: Int, val cols: Int, val open: List<Int>) {
    fun index(r: Int, c: Int) = r * cols + c

    fun isOpen(cell: Int, side: Int): Boolean = open[cell] and side != 0

    /** The cell through [side] of [cell], if there is no wall in the way. */
    fun step(cell: Int, side: Int): Int? {
        if (!isOpen(cell, side)) return null
        val r = cell / cols
        val c = cell % cols
        return when (side) {
            N -> index(r - 1, c)
            S -> index(r + 1, c)
            E -> index(r, c + 1)
            else -> index(r, c - 1)
        }
    }

    /** How many steps from [from] to every cell. */
    fun distances(from: Int): List<Int> {
        val d = MutableList(rows * cols) { -1 }
        d[from] = 0
        val queue = ArrayDeque(listOf(from))
        while (queue.isNotEmpty()) {
            val at = queue.removeFirst()
            for (side in SIDES) step(at, side)?.let { if (d[it] < 0) { d[it] = d[at] + 1; queue += it } }
        }
        return d
    }

    companion object {
        const val N = 1
        const val E = 2
        const val S = 4
        const val W = 8
        val SIDES = listOf(N, E, S, W)

        private fun opposite(side: Int) = when (side) { N -> S; S -> N; E -> W; else -> E }

        fun make(rows: Int, cols: Int, random: Random): Maze {
            val open = IntArray(rows * cols)
            val seen = BooleanArray(rows * cols)
            val stack = ArrayDeque(listOf(0))
            seen[0] = true
            while (stack.isNotEmpty()) {
                val at = stack.last()
                val r = at / cols
                val c = at % cols
                val next = SIDES.mapNotNull { side ->
                    val (nr, nc) = when (side) { N -> r - 1 to c; S -> r + 1 to c; E -> r to c + 1; else -> r to c - 1 }
                    if (nr in 0 until rows && nc in 0 until cols && !seen[nr * cols + nc]) side to nr * cols + nc else null
                }
                if (next.isEmpty()) {
                    stack.removeLast()
                } else {
                    val (side, cell) = next[random.nextInt(next.size)]
                    open[at] = open[at] or side
                    open[cell] = open[cell] or opposite(side)
                    seen[cell] = true
                    stack += cell
                }
            }
            return Maze(rows, cols, open.toList())
        }
    }
}

/**
 * Who runs the maze and what they are after. [whom] is the runner's Hindi name as the
 * object of a sentence ("चूहे को"), [feminine] the goal's Hindi gender ("मिल गई").
 */
data class Runner(val emoji: String, val name: Txt, val whom: String, val goal: String, val goalName: Txt, val feminine: Boolean = false)

/** Help the runner through the maze from [start] to [goal]: drag it, or tap the next cell. */
data class MazeGame(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val maze: Maze,
    val start: Int,
    val goal: Int,
    val runner: Runner,
) : Reel {
    override val topic get() = Topic.MAZE
    override val needsSolving get() = true
    override val prompt: Txt get() = Mazes.help(runner)
    override val voice: Lang? get() = null
}

object Mazes {
    val runners = listOf(
        Runner("🐭", txt("mouse", "चूहा"), "चूहे", "🧀", txt("cheese", "पनीर")),
        Runner("🐶", txt("dog", "कुत्ता"), "कुत्ते", "🦴", txt("bone", "हड्डी"), feminine = true),
        Runner("🐰", txt("rabbit", "खरगोश"), "खरगोश", "🥕", txt("carrot", "गाजर"), feminine = true),
        Runner("🐝", txt("bee", "मधुमक्खी"), "मधुमक्खी", "🌻", txt("flower", "फूल")),
        Runner("🐵", txt("monkey", "बंदर"), "बंदर", "🍌", txt("banana", "केला")),
        Runner("🐼", txt("panda", "पांडा"), "पांडा", "🎋", txt("bamboo", "बाँस")),
        Runner("🐧", txt("penguin", "पेंगुइन"), "पेंगुइन", "🐟", txt("fish", "मछली"), feminine = true),
    )

    fun help(r: Runner) = txt(
        "Help the ${r.name[Lang.EN]} reach the ${r.goalName[Lang.EN]}!",
        "${r.whom} को ${r.goalName[Lang.HI]} तक पहुँचाओ!",
    )

    fun say(r: Runner) = help(r) + txt(
        "Drag the ${r.name[Lang.EN]} along the path, or tap the next square.",
        "${r.whom} को रास्ते पर खिसकाओ, या अगले खाने को छुओ।",
    )

    fun reached(r: Runner) = txt(
        "The ${r.name[Lang.EN]} found the ${r.goalName[Lang.EN]}!",
        "${r.whom} को ${r.goalName[Lang.HI]} मिल ${if (r.feminine) "गई" else "गया"}!",
    )

    /** Rows and columns: a few cells for a toddler, a long winding one at 12. */
    private fun size(g: Gen): Pair<Int, Int> {
        val (rows, cols) = when (g.level) {
            3 -> 3 to 3
            4 -> 4 to 4
            5 -> 5 to 5
            6 -> 6 to 5
            7 -> 7 to 6
            8 -> 8 to 6
            9 -> 8 to 7
            10 -> 9 to 7
            11 -> 10 to 8
            else -> 11 to 8
        }
        return (if (g.difficulty >= 4) rows + 1 else rows) to cols
    }

    fun make(g: Gen): MazeGame {
        val (rows, cols) = size(g)
        val maze = Maze.make(rows, cols, g.random)
        // The goal is the cell furthest from the start along the paths, so the whole maze is used.
        val far = maze.distances(0)
        val goal = far.indices.maxBy { far[it] }
        return MazeGame(g.id, g.level, g.difficulty, maze, 0, goal, g.pick(runners))
    }
}
