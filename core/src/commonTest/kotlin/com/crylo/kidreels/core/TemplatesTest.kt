package com.crylo.kidreels.core

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class TemplatesTest {
    private val cursor = Progress(MapStore())

    /** Every template, at every age and difficulty it claims, many times over. */
    private fun everyReel(langs: List<Lang> = Lang.entries, seeds: Int = 60): Sequence<Pair<Template, Reel>> = sequence {
        for (t in Templates.all) for (level in t.ages) for (d in 1..5) for (seed in 0 until seeds) {
            val usable = t.usable(langs)
            if (usable.isEmpty()) continue
            val gen = Gen(level, d, Random(seed * 31 + level * 7 + d), usable, 1, cursor)
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
                    Topic.EVEN_ODD -> if (reel.level <= 7) 2 else 4
                    Topic.FLOAT_SINK, Topic.MAGNETS -> 2
                    Topic.STATES -> 3
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
            if (reel !is Quiz || reel.topic != Topic.COUNTING || reel.stage !is Stage.Count) continue
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

    /** Every age has enough topics for the feed's "a topic waits four reels" rule, and a mix of both kinds. */
    @Test
    fun everyAgeHasAFullMix() {
        for (age in Settings.AGES) {
            val here = Templates.all.filter { age in it.ages }
            assertTrue(here.size >= 8, "Only ${here.size} topics at age $age")
            assertTrue(here.any { it.solving } && here.any { !it.solving }, "Age $age lacks puzzles or explore reels")
        }
    }

    @Test
    fun evenOddAndPrimeAnswersAreRight() {
        for ((_, reel) in everyReel()) {
            if (reel !is Quiz) continue
            when (reel.topic) {
                Topic.EVEN_ODD -> {
                    val stage = reel.stage
                    if (stage is Stage.Pairs) {
                        assertEquals(stage.n % 2, reel.answer, "$reel")
                    } else {
                        val numbers = reel.choices.map { (it as Choice.Number).n }
                        val right = numbers[reel.answer] % 2
                        assertTrue(numbers.filterIndexed { i, _ -> i != reel.answer }.all { it % 2 != right }, "two answers in $numbers")
                    }
                }
                Topic.PRIMES -> {
                    val numbers = reel.choices.map { (it as Choice.Number).n }
                    assertEquals(listOf(numbers[reel.answer]), numbers.filter(Numbers::isPrime), "$numbers")
                }
                else -> Unit
            }
        }
        assertEquals(25, Numbers.primes.count { it <= 100 })
        assertEquals(listOf(2, 3, 5, 7), SieveLab(1, 11, 1, 100).steps)
    }

    @Test
    fun memoryGamesAreFair() {
        for ((_, reel) in everyReel()) {
            when (reel) {
                is CupGame -> {
                    assertTrue(reel.answer in 0 until reel.cups && reel.swaps.isNotEmpty(), "$reel")
                    if (reel.cups > 2) assertTrue(reel.swaps.zipWithNext().none { (a, b) -> a == b }, "a swap undone: $reel")
                }
                is PairsGame -> {
                    assertTrue(reel.cards.groupingBy { it }.eachCount().values.all { it == 2 }, "not all pairs: $reel")
                    assertEquals(0, reel.cards.size % reel.columns, "ragged grid: $reel")
                }
                is OrderGame -> {
                    assertTrue(reel.sequence.all { it in 0 until reel.pads }, "$reel")
                    assertTrue(reel.sequence.zipWithNext().none { (a, b) -> a == b }, "a pad twice running: $reel")
                }
                is Quiz -> {
                    val stage = reel.stage as? Stage.Memory ?: continue
                    val right = (reel.choices[reel.answer] as Choice.Token).text
                    assertEquals(stage.items[stage.missing], right)
                    // No other option was on screen, so only memory can tell them apart.
                    assertTrue(reel.choices.filterIndexed { i, _ -> i != reel.answer }.none { (it as Choice.Token).text in stage.items }, "$reel")
                }
                else -> Unit
            }
        }
    }

    /** A perfect maze: every cell reachable, no loops, walls that agree from both sides. */
    @Test
    fun mazesHaveOneWayThrough() {
        for ((_, reel) in everyReel(seeds = 20)) {
            if (reel !is MazeGame) continue
            val m = reel.maze
            val d = m.distances(reel.start)
            assertTrue(d.all { it >= 0 }, "unreachable cells: $reel")
            val passages = m.open.sumOf { it.countOneBits() } / 2
            assertEquals(m.rows * m.cols - 1, passages, "a loop or a gap")
            for (cell in m.open.indices) for (side in Maze.SIDES) m.step(cell, side)?.let { next ->
                assertTrue(next in m.open.indices, "a door out of the maze at $cell")
                assertTrue(Maze.SIDES.any { m.step(next, it) == cell }, "a one-way wall at $cell")
            }
            assertEquals(d.max(), d[reel.goal], "the goal is not the far end")
        }
    }

    @Test
    fun grammarAnswersAreRight() {
        for ((_, reel) in everyReel(seeds = 30)) {
            if (reel !is Quiz) continue
            val right = reel.choices[reel.answer]
            when (reel.topic) {
                Topic.OPPOSITES -> {
                    val a = (reel.stage as Stage.Picture).name!!.name
                    assertEquals(Choice.Word(Grammar.opposites.first { it.a == a }.b), right)
                }
                Topic.PLURALS -> {
                    val lang = reel.voice!!
                    val p = (if (lang == Lang.EN) Grammar.pluralsEn else Grammar.pluralsHi).first { it.emoji == (reel.stage as Stage.Count).emoji }
                    assertEquals(p.many, (right as Choice.Word).text[lang])
                }
                Topic.GENDER -> {
                    val lang = reel.voice!!
                    val male = (reel.stage as Stage.Picture).name!!.name[lang]
                    val list = if (lang == Lang.EN) Grammar.gendersEn else Grammar.gendersHi
                    assertEquals(list.first { it.male == male }.female, (right as Choice.Word).text[lang])
                }
                Topic.RHYMES -> {
                    val word = (reel.stage as Stage.Picture).name!!.name[Lang.EN]
                    val group = Grammar.rhymes.first { g -> g.words.any { it.first == word } }.words.map { it.first }
                    val labels = reel.choices.map { (it as Choice.Token).label!![Lang.EN] }
                    assertEquals(listOf(labels[reel.answer]), labels.filter { it in group }, "only one option may rhyme with $word")
                }
                Topic.MATRAS -> {
                    val row = reel.stage as Stage.Row
                    assertEquals(row.tokens[row.blank], (right as Choice.Token).text)
                }
                else -> Unit
            }
        }
        // Every plural has three different wrong guesses, none of them the answer or another
        // word from the lists (लड़कियाँ is not a wrong लड़का, it is girls).
        for (list in listOf(Grammar.pluralsEn, Grammar.pluralsHi)) {
            val real = list.flatMap { listOf(it.one, it.many) }.toSet()
            for (p in list) {
                assertEquals(3, p.wrong.distinct().size, p.one)
                assertTrue(p.many !in p.wrong, p.one)
                assertTrue(p.wrong.none { it != p.one && it in real }, "${p.wrong} holds a real word")
            }
        }
    }

    @Test
    fun numberCardsSpellTheirNumber() {
        assertEquals(listOf("O", "N", "E"), Numbers.spelling(1))
        assertEquals("TWENTY", Numbers.spelling(20).joinToString(""))
        assertEquals("3 times 4 is 12", Numbers.tableLine(3, 4)[Lang.EN])
        assertEquals("3 चौके 12", Numbers.tableLine(3, 4)[Lang.HI])
        // Every number a card can show has its own picture, and none is used twice.
        assertEquals(Numbers.MAX, Numbers.things.map { it.emoji }.distinct().size)
        for ((_, reel) in everyReel()) {
            if (reel is NumberCard) assertTrue(reel.n in 1..Numbers.MAX, "$reel")
            if (reel is SquareCard) assertTrue(reel.n >= 2, "$reel")
        }
    }

    @Test
    fun angleChallengeNeverStartsSolved() {
        for ((_, reel) in everyReel()) {
            if (reel is AngleLab && reel.target != null) assertTrue(AngleKind.of(reel.startDegrees) != reel.target)
        }
    }

    @Test
    fun dragGamesCanBeFinishedAndExplainEveryWrongDrop() {
        for ((_, reel) in everyReel(seeds = 30)) {
            if (reel !is DragGame) continue
            reel.pieces.forEach { p ->
                p.whyNot.forEachIndexed { t, why -> assertEquals(p.target == t, why == null, "${reel.kind}: $p at $t") }
                // Every picture in a drag game can be tapped for its name too.
                (p.sprite as? Sprite.Emoji)?.let { assertTrue(Words.of(it.text) != null, "no name for ${it.text}") }
            }
            if (reel.kind == DragKind.MISSING_PIECE) {
                val right = reel.pieces.filter { it.target == 0 }
                assertEquals(1, right.size)
                assertEquals(reel.targets[0].sprite, right[0].sprite)
                assertEquals(reel.pieces.size, reel.pieces.map { it.sprite }.toSet().size, "two pieces look the same")
            }
            if (reel.kind == DragKind.BASKET) assertTrue(reel.pieces.size > reel.targets[0].capacity, "nothing to count out")
        }
    }

    /** The right answer really is right, and no wrong option looks the same as it. */
    @Test
    fun reasoningAnswersAreRightAndOptionsDiffer() {
        for ((_, reel) in everyReel(seeds = 30)) {
            if (reel !is Quiz) continue
            val right = reel.choices[reel.answer]
            when (val stage = reel.stage) {
                is Stage.Figures -> if (stage.layout == Layout.MIRROR) {
                    assertTrue((right as Choice.Fig).figure.same(stage.figures[0]!!.mirrored()), "mirror")
                }
                is Stage.Hidden -> {
                    assertTrue(stage.lines.hides((right as Choice.Shapes).lines, stage.size), "the answer is not hidden")
                    reel.choices.forEachIndexed { i, c -> if (i != reel.answer) assertFalse(stage.lines.hides((c as Choice.Shapes).lines, stage.size), "a wrong shape is hidden too") }
                }
                is Stage.Cutout -> {
                    val turns = (0..3).map { stage.piece.turned(it, stage.board).shape }
                    assertTrue((right as Choice.Cut).piece.shape in turns, "the answer does not fit")
                }
                is Stage.Fold -> {
                    val holes = (right as Choice.Holes).holes
                    assertTrue(holes.containsAll(stage.holes))
                    if (stage.vertical) assertTrue(holes.all { Hole(3 - it.col, it.row) in holes }, "not mirrored across the fold")
                    if (stage.twice || !stage.vertical) assertTrue(holes.all { Hole(it.col, 3 - it.row) in holes }, "not mirrored across the fold")
                }
                else -> Unit
            }
            val figs = reel.choices.filterIsInstance<Choice.Fig>()
            if (figs.isNotEmpty() && reel.topic != Topic.ODD_FIGURE) assertTrue(allDifferent(figs.map { it.figure }), "${reel.topic}: two options look the same")
            if (reel.topic == Topic.ODD_FIGURE) {
                val odd = (right as Choice.Fig).figure
                assertTrue(figs.filter { it != right }.none { it.figure.same(odd) }, "the odd one is not different")
            }
            val cuts = reel.choices.filterIsInstance<Choice.Cut>()
            assertEquals(cuts.size, cuts.map { it.piece.shape }.toSet().size, "two pieces look the same")
        }
    }

    @Test
    fun figuresTurnAndFlipExactly() {
        val f = Figure(listOf(Mark(MarkKind.ARROW, 1, -1, 0), Mark(MarkKind.DOT, -1, 0)))
        assertTrue(f.turned(4).same(f))
        assertTrue(f.mirrored().mirrored().same(f))
        assertTrue(f.mirrored().flipped().same(f.turned(2)), "a mirror and a flip make a half turn")
        // An arrow pointing right at the top right, turned a quarter, points down at the bottom right.
        assertEquals(setOf(Mark(MarkKind.ARROW, 1, 1, 90), Mark(MarkKind.DOT, 0, -1)), f.turned(1).look)
        assertTrue(f.stepped(8).same(f), "eight steps go all the way round")
    }
}
