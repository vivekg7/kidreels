package com.crylo.kidreels.core

import kotlin.random.Random

/**
 * Full, valid sudoku grids of size 4 (2×2 boxes), 6 (2×3) or 9 (3×3), shuffled so no two
 * look alike. A solved grid with one cell blanked has exactly one answer, so nothing
 * needs a solver: the blank's value is the answer.
 */
object Sudoku {
    fun boxes(size: Int): Pair<Int, Int> = when (size) {
        4 -> 2 to 2
        6 -> 2 to 3
        9 -> 3 to 3
        else -> error("Unsupported sudoku size $size")
    }

    /** Values 0 until size, row by row. */
    fun solved(size: Int, random: Random): List<Int> {
        val (boxRows, boxCols) = boxes(size)
        // The classic pattern: each row is the previous one shifted, with a bigger shift
        // at box boundaries. It is valid for any box shape.
        fun pattern(r: Int, c: Int) = (boxCols * (r % boxRows) + r / boxRows + c) % size

        // Shuffles that keep a grid valid: rows within a band and bands themselves,
        // columns within a stack and stacks themselves, and relabelling the values.
        fun shuffledGroups(groupSize: Int, groups: Int): List<Int> =
            (0 until groups).shuffled(random).flatMap { g -> (0 until groupSize).shuffled(random).map { g * groupSize + it } }

        val rows = shuffledGroups(boxRows, size / boxRows)
        val cols = shuffledGroups(boxCols, size / boxCols)
        val labels = (0 until size).shuffled(random)
        return List(size * size) { i -> labels[pattern(rows[i / size], cols[i % size])] }
    }

    fun isValid(cells: List<Int>, size: Int): Boolean {
        val (boxRows, boxCols) = boxes(size)
        val all = (0 until size).toSet()
        val rowsOk = (0 until size).all { r -> (0 until size).map { cells[r * size + it] }.toSet() == all }
        val colsOk = (0 until size).all { c -> (0 until size).map { cells[it * size + c] }.toSet() == all }
        val boxesOk = (0 until size step boxRows).all { r0 ->
            (0 until size step boxCols).all { c0 ->
                (0 until boxRows).flatMap { dr -> (0 until boxCols).map { dc -> cells[(r0 + dr) * size + c0 + dc] } }.toSet() == all
            }
        }
        return rowsOk && colsOk && boxesOk
    }
}
