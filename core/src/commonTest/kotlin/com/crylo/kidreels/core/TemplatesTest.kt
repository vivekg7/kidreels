package com.crylo.kidreels.core

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TemplatesTest {
    private val cursor = Progress(MapStore())

    /** Every template, at every age and difficulty it claims, many times over. */
    private fun everyReel(langs: List<Lang> = Lang.entries, seeds: Int = 60): Sequence<Pair<Template, Reel>> = sequence {
        for (t in Templates.all) for (level in t.ages) for (d in 1..5) for (seed in 0 until seeds) {
            val gen = Gen(level, d, Random(seed * 31 + level * 7 + d), langs, 1, cursor)
            val reel = try {
                t.make(gen)
            } catch (e: Exception) {
                fail("${t.topic} level $level difficulty $d seed $seed threw $e")
            }
            yield(t to reel)
        }
    }

    @Test
    fun everyTemplateMakesValidReels() {
        for ((t, reel) in everyReel()) {
            assertEquals(t.topic, reel.topic)
            assertTrue(reel.level in t.ages)
            if (reel is Quiz) {
                val expected = when (reel.topic) {
                    Topic.COMPARE -> 2
                    Topic.SUDOKU -> 4
                    else -> reel.level.coerceIn(2, 4)
                }
                assertEquals(expected, reel.choices.size, "${reel.topic} at ${reel.level}: ${reel.choices}")
                reel.choices.filterIsInstance<Choice.Number>().forEach { assertTrue(it.n >= 0, "Negative choice in $reel") }
            }
        }
    }

    @Test
    fun sumsHaveTheRightAnswer() {
        for ((_, reel) in everyReel()) {
            if (reel !is Quiz) continue
            val stage = reel.stage as? Stage.Sum ?: continue
            val expected = when (stage.op) {
                Op.ADD -> stage.a + stage.b
                Op.SUB -> stage.a - stage.b
                Op.MUL -> stage.a * stage.b
            }
            assertTrue(expected >= 0, "Negative result in $reel")
            assertEquals(expected, (reel.choices[reel.answer] as Choice.Number).n, "$reel")
        }
    }

    @Test
    fun countingAnswerIsTheCount() {
        for ((_, reel) in everyReel()) {
            if (reel !is Quiz || reel.stage !is Stage.Count) continue
            assertEquals(reel.stage.n, (reel.choices[reel.answer] as Choice.Number).n)
        }
    }

    @Test
    fun sudokuAnswerFillsTheGrid() {
        for ((_, reel) in everyReel()) {
            if (reel !is Quiz) continue
            val grid = reel.stage as? Stage.Grid ?: continue
            assertTrue(Sudoku.isValid(grid.cells, grid.size))
            val answer = (reel.choices[reel.answer] as Choice.Token).text
            assertEquals(grid.symbols[grid.cells[grid.blank]], answer)
        }
    }

    @Test
    fun patternAnswerContinuesTheRow() {
        for ((_, reel) in everyReel()) {
            if (reel !is Quiz || reel.topic != Topic.PATTERN) continue
            val row = reel.stage as Stage.Row
            assertEquals(row.tokens[row.blank], (reel.choices[reel.answer] as Choice.Token).text)
            assertTrue(row.tokens.size <= 8, "Row too long to fit: ${row.tokens}")
        }
    }

    @Test
    fun sequenceAnswerIsTheMissingTerm() {
        for ((_, reel) in everyReel()) {
            if (reel !is Quiz || reel.topic != Topic.SEQUENCE) continue
            val row = reel.stage as Stage.Row
            assertEquals(row.tokens[row.blank].toInt(), (reel.choices[reel.answer] as Choice.Number).n)
        }
    }

    @Test
    fun clockAnswerIsTheTimeShown() {
        for ((_, reel) in everyReel()) {
            if (reel !is Quiz) continue
            val clock = reel.stage as? Stage.Clock ?: continue
            assertEquals(Choice.Time(clock.hour, clock.minute), reel.choices[reel.answer])
            assertTrue(clock.hour in 1..12 && clock.minute in 0..59)
        }
    }

    @Test
    fun onlyChosenLanguagesAppear() {
        for ((_, reel) in everyReel(langs = listOf(Lang.EN), seeds = 10)) {
            if (reel is LetterCard) assertEquals(Lang.EN, reel.lang)
            if (reel.voice != null) assertEquals(Lang.EN, reel.voice)
        }
    }

    @Test
    fun angleChallengeNeverStartsSolved() {
        for ((_, reel) in everyReel()) {
            if (reel is AngleLab && reel.target != null) assertTrue(AngleKind.of(reel.startDegrees) != reel.target)
        }
    }
}
