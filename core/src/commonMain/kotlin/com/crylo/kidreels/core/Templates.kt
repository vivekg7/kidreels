package com.crylo.kidreels.core

import kotlin.math.roundToInt
import kotlin.random.Random

/** Hands out positions in an ordered list (the alphabet), remembered across sessions. */
fun interface Cursor {
    fun next(key: String, size: Int): Int
}

/** Everything a template needs to make one reel. */
class Gen(
    val level: Int,
    val difficulty: Int,
    val random: Random,
    val langs: List<Lang>,
    val id: Long,
    private val cursor: Cursor,
    /** The child's age, which picks the words; [level] picks the content. */
    val age: Int = level,
    /** A parent picked this topic alone: things that can go in order do (count 1, 2, 3…). */
    val ordered: Boolean = false,
) {
    /** [t] in words for this child: see [Txt.forAge]. */
    fun words(t: Txt): Txt = t.forAge(age)

    /** Two-year-olds pick between 2, three-year-olds between 3, everyone else 4. */
    val optionCount: Int get() = (level).coerceIn(2, 4)

    fun <T> pick(list: List<T>): T = list[random.nextInt(list.size)]

    fun chance(p: Double): Boolean = random.nextDouble() < p

    fun inOrder(key: String, size: Int): Int = cursor.next(key, size)

    /** A number in [lo, hi], reaching further toward [hi] for harder reels. */
    fun upTo(lo: Int, hi: Int): Int {
        val span = hi - lo
        val top = lo + (span * (0.4 + 0.15 * difficulty)).roundToInt().coerceAtMost(span)
        return random.nextInt(lo, top + 1)
    }

    fun between(lo: Int, hi: Int): Int = random.nextInt(lo, hi + 1)
}

/**
 * One kind of reel. [ages] says which levels it can be made for; the feed picks the level
 * and difficulty, the template turns them into concrete numbers, letters or shapes.
 */
class Template(
    val topic: Topic,
    val ages: IntRange,
    val solving: Boolean,
    val weight: Double = 1.0,
    /**
     * For a topic about one language (English rhymes, Hindi मात्राएँ): made only when one
     * of these is chosen, and handed only those. Null for a topic in every language.
     */
    val langs: Set<Lang>? = null,
    val make: (Gen) -> Reel,
) {
    /** Whether this topic can be made for a child with [chosen] languages. */
    fun fits(chosen: List<Lang>): Boolean = langs == null || chosen.any { it in langs }

    /** The chosen languages this topic can use. */
    fun usable(chosen: List<Lang>): List<Lang> = if (langs == null) chosen else chosen.filter { it in langs }
}

/**
 * The letter's word as a picture name, so tapping the picture says "This is a ball" with
 * the word the quiz is about, not another name for the same emoji.
 */
internal fun Letter.asWord(): Word {
    val emoji = (picture as Picture.Emoji).text
    val usual = Words.of(emoji)
    return Word(emoji, txt(meaning[Lang.EN].lowercase(), meaning[Lang.HI]), plural = usual?.plural ?: false, pluralHi = usual?.pluralHi ?: false)
}

/** Puts [answer] among [others] at a random place. */
internal fun <T> Gen.shuffleIn(answer: T, others: List<T>): Pair<List<T>, Int> {
    val all = (others + answer).shuffled(random)
    return all to all.indexOf(answer)
}

/**
 * Wrong answers that are close to the right one (a child who is off by one learns more
 * from that than from a wild guess), never negative, never repeated.
 */
internal fun Gen.numberChoices(answer: Int, spread: List<Int>, sticks: Boolean, min: Int = 0, count: Int = optionCount): Pair<List<Choice>, Int> {
    val wrong = LinkedHashSet<Int>()
    for (d in spread.flatMap { listOf(it, -it) }.shuffled(random)) {
        val v = answer + d
        if (v >= min && v != answer) wrong += v
        if (wrong.size == count - 1) break
    }
    var k = 1
    while (wrong.size < count - 1) {
        val v = answer + k++
        if (v != answer) wrong += v
    }
    val (values, index) = shuffleIn(answer, wrong.toList())
    return values.map { Choice.Number(it, sticks) } to index
}

/** For each of [values], why it is not the one at [answer]; see [Quiz.whyNot]. */
internal fun <T> whyNot(values: List<T>, answer: Int, why: (T) -> Txt): List<Txt?> =
    values.mapIndexed { i, v -> if (i == answer) null else why(v) }

private val List<Choice>.numbers: List<Int> get() = map { (it as Choice.Number).n }

object Templates {
    val all: List<Template> = listOf(
        Template(Topic.COUNT_ALONG, 2..4, solving = false) { g ->
            val n = when {
                g.ordered -> g.inOrder("count", when (g.level) { 2 -> 5; 3 -> 7; else -> 10 }) + 1
                g.level == 2 -> g.upTo(1, 5)
                g.level == 3 -> g.upTo(3, 7)
                else -> g.upTo(5, 10)
            }
            CountAlong(g.id, g.level, g.difficulty, g.pick(Countables), n)
        },

        // 1 to 5 for a three-year-old, to 10 at 4, to 20 from 5: about what nursery and
        // kindergarten teach. A picked topic goes 1, 2, 3… to the age's top.
        Template(Topic.NUMBERS, 3..6, solving = false) { g ->
            val top = when (g.level) { 3 -> 5; 4 -> 10; else -> Numbers.MAX }
            val n = if (g.ordered) g.inOrder("numbers", top) + 1 else g.upTo(1, top)
            NumberCard(g.id, g.level, g.difficulty, n)
        },

        Template(Topic.COUNTING, 2..5, solving = true) { g ->
            val n = when (g.level) {
                2 -> g.upTo(1, 3)
                3 -> g.upTo(1, 5)
                4 -> g.upTo(2, 10)
                else -> g.upTo(5, 15)
            }
            val (choices, answer) = g.numberChoices(n, listOf(1, 2, 3), sticks = n <= 10 && g.level <= 4, min = 1)
            val thing = g.pick(Countables)
            Quiz(
                g.id, Topic.COUNTING, g.level, g.difficulty, Phrases.howMany(thing),
                Stage.Count(thing.emoji, n, scattered = g.level >= 4 && g.difficulty >= 3),
                choices, answer, reveal = Phrases.thereAre(n, thing), say = Phrases.howManyOnPhone(thing),
                whyNot = whyNot(choices.numbers, answer) { Phrases.notCount(it, n, thing) },
            )
        },

        Template(Topic.ADDITION, 4..12, solving = true) { g -> addition(g) },
        Template(Topic.SUBTRACTION, 5..12, solving = true) { g -> subtraction(g) },
        Template(Topic.MULTIPLICATION, 7..12, solving = true) { g -> multiplication(g) },
        Template(Topic.COMPARE, 3..7, solving = true) { g -> compare(g) },
        // The tables a class is on: 2, 5 and 10 first, then up to 10, then past it.
        Template(Topic.TABLES, 6..12, solving = false) { g ->
            val tables = when (g.level) {
                6 -> listOf(2, 5, 10)
                7 -> (2..10).toList()
                8 -> (2..12).toList()
                9 -> (2..15).toList()
                else -> (11..20).toList()
            }
            val table = if (g.ordered) tables[g.inOrder("tables", tables.size)] else tables[g.upTo(0, tables.lastIndex)]
            TableCard(g.id, g.level, g.difficulty, table)
        },
        Template(Topic.SQUARES, 8..12, solving = false) { g ->
            val top = when (g.level) { 8 -> 6; 9 -> 10; 10 -> 12; else -> 15 }
            val n = if (g.ordered) g.inOrder("squares", top - 1) + 2 else g.upTo(2, top)
            SquareCard(g.id, g.level, g.difficulty, n, showPower = g.level >= 9)
        },

        Template(Topic.EVEN_ODD, 6..9, solving = true) { g -> NumberKinds.evenOdd(g) },
        Template(Topic.PRIMES, 10..12, solving = true) { g -> NumberKinds.prime(g) },
        Template(Topic.SIEVE, 10..12, solving = false) { g -> SieveLab(g.id, g.level, g.difficulty, top = if (g.level == 10) 50 else 100) },
        Template(Topic.SEQUENCE, 6..12, solving = true) { g -> sequence(g) },
        Template(Topic.CLOCK, 5..9, solving = true) { g -> clock(g) },

        Template(Topic.LETTERS, 2..6, solving = false, weight = 1.5) { g ->
            val lang = g.pick(g.langs)
            val letters = Alphabets.of(lang)
            LetterCard(g.id, g.level, g.difficulty, lang, letters[g.inOrder("letters.${lang.tag}", letters.size)])
        },

        Template(Topic.FIRST_LETTER, 4..7, solving = true) { g ->
            val lang = g.pick(g.langs)
            val alphabet = Alphabets.of(lang)
            val target = g.pick(alphabet.filter { !it.like && it.picture is Picture.Emoji })
            val others = alphabet.map { it.glyph }.distinct().filter { it != target.glyph }.shuffled(g.random).take(g.optionCount - 1)
            val (glyphs, answer) = g.shuffleIn(target.glyph, others)
            Quiz(
                g.id, Topic.FIRST_LETTER, g.level, g.difficulty, Phrases.startsWith(target),
                Stage.Picture((target.picture as Picture.Emoji).text, target.asWord()),
                glyphs.map { Choice.Token(it) }, answer,
                reveal = Txt.same(target.phrase), say = Phrases.startsWithSay(target), voice = lang,
                revealSaid = target.spoken(), whyNot = whyNot(glyphs, answer) { Phrases.notLetter(it, target) },
            )
        },

        Template(Topic.SHAPES, 2..6, solving = false) { g ->
            val shapes = Shapes.upTo(g.level)
            ShapeCard(g.id, g.level, g.difficulty, shapes[g.inOrder("shapes", shapes.size)].forAge(g.age), g.pick(Colours.bright))
        },

        Template(Topic.SHAPE_QUIZ, 3..7, solving = true) { g -> shapeQuiz(g) },

        Template(Topic.SOLIDS, 5..12, solving = false) { g ->
            val solids = Solids.upTo(g.level)
            SolidCard(g.id, g.level, g.difficulty, solids[g.inOrder("solids", solids.size)].forAge(g.age), g.pick(Colours.bright), showFact = g.level >= 6)
        },

        Template(Topic.ANGLES, 7..12, solving = false) { g ->
            val kinds = if (g.level >= 9) AngleKind.entries else listOf(AngleKind.ACUTE, AngleKind.RIGHT, AngleKind.OBTUSE)
            val target = if (g.level >= 8 && g.chance(0.6)) g.pick(kinds) else null
            var start: Int
            do start = g.between(25, 155) while (target != null && AngleKind.of(start) == target)
            AngleLab(g.id, g.level, g.difficulty, start, target)
        },

        Template(Topic.CURVES, 8..12, solving = false) { g ->
            val curves = Curves.upTo(g.level)
            CurveLab(g.id, g.level, g.difficulty, curves[g.inOrder("curves", curves.size)], showEquation = g.level >= 10 || (g.level == 9 && g.difficulty >= 3))
        },

        Template(Topic.PATTERN, 3..6, solving = true) { g -> pattern(g) },
        Template(Topic.ODD_ONE_OUT, 3..12, solving = true) { g -> oddOneOut(g) },
        Template(Topic.SUDOKU, 5..12, solving = true) { g -> sudoku(g) },

        Template(Topic.FEEDING, 2..4, solving = true) { g -> DragGames.feed(g) },
        Template(Topic.SHAPE_SORTER, 2..5, solving = true) { g -> DragGames.shapeSorter(g) },
        Template(Topic.BASKET, 3..6, solving = true) { g -> DragGames.basket(g) },
        Template(Topic.SORTING, 4..8, solving = true) { g -> DragGames.sort(g) },
        Template(Topic.MAZE, 3..12, solving = true) { g -> Mazes.make(g) },
        Template(Topic.MISSING_PIECE, 3..8, solving = true) { g -> DragGames.missingPiece(g) },

        // Reasoning with figures: nine small topics, so each weighs half a reel in the mix.
        // Memory: four small games, lighter than a topic each so they season the mix.
        Template(Topic.CUPS, 3..12, solving = true, weight = 0.75) { g -> Memory.cups(g) },
        Template(Topic.PAIRS, 3..12, solving = true, weight = 0.75) { g -> Memory.pairs(g) },
        Template(Topic.COPY_ORDER, 4..12, solving = true, weight = 0.75) { g -> Memory.order(g) },
        Template(Topic.WHATS_MISSING, 3..12, solving = true, weight = 0.75) { g -> Memory.missing(g) },

        Template(Topic.SAME_FIGURE, 4..8, solving = true, weight = 0.5) { g -> Reasoning.sameFigure(g) },
        Template(Topic.ODD_FIGURE, 4..12, solving = true, weight = 0.5) { g -> Reasoning.oddFigure(g) },
        Template(Topic.COMPLETE_PATTERN, 5..12, solving = true, weight = 0.5) { g -> Reasoning.completePattern(g) },
        Template(Topic.FIGURE_SERIES, 6..12, solving = true, weight = 0.5) { g -> Reasoning.figureSeries(g) },
        Template(Topic.MIRROR, 6..12, solving = true, weight = 0.5) { g -> Reasoning.mirror(g) },
        Template(Topic.ANALOGY, 7..12, solving = true, weight = 0.5) { g -> Reasoning.analogy(g) },
        Template(Topic.FIT_PIECE, 7..12, solving = true, weight = 0.5) { g -> Reasoning.fitPiece(g) },
        Template(Topic.HIDDEN_FIGURE, 8..12, solving = true, weight = 0.5) { g -> Reasoning.hidden(g) },
        Template(Topic.PAPER_FOLD, 9..12, solving = true, weight = 0.5) { g -> Reasoning.paperFold(g) },

        Template(Topic.SIZES, 2..3, solving = true) { g ->
            val thing = g.pick(Countables)
            val scales = if (g.level == 2) listOf(0.45f, 1f) else listOf(0.4f, 0.7f, 1f)
            val big = g.chance(0.5)
            val target = if (big) scales.max() else scales.min()
            val (shuffled, answer) = g.shuffleIn(target, scales - target)
            val prompt = Phrases.whichSize(thing, big, ofThree = scales.size == 3)
            Quiz(
                g.id, Topic.SIZES, g.level, g.difficulty, prompt, Stage.None, shuffled.map { Choice.Sized(thing.emoji, it) }, answer,
                say = prompt + Phrases.tapIt,
                whyNot = whyNot(shuffled, answer) { Phrases.notSize(thing, big, scales.size == 3, scales.indexOf(it)) },
            )
        },

        Template(Topic.COLOURS, 2..4, solving = true) { g ->
            val pool = when (g.level) {
                2 -> Colours.basic
                3 -> Colours.all.take(8)
                else -> Colours.all
            }
            val target = g.pick(pool)
            val (colours, answer) = g.shuffleIn(target, (pool - target).shuffled(g.random).take(g.optionCount - 1))
            Quiz(
                g.id, Topic.COLOURS, g.level, g.difficulty, Phrases.whichColour(target), Stage.None,
                colours.map { Choice.Colour(it.argb) }, answer, reveal = Phrases.yesColour(target), say = Phrases.whichColourSay(target),
                whyNot = whyNot(colours, answer) { Phrases.notColour(it, target) },
            )
        },

        Template(Topic.ANIMAL_SOUNDS, 2..4, solving = true) { g ->
            val target = g.pick(AnimalSounds.all)
            val (animals, answer) = g.shuffleIn(target, (AnimalSounds.all - target).shuffled(g.random).take(g.optionCount - 1))
            Quiz(
                g.id, Topic.ANIMAL_SOUNDS, g.level, g.difficulty, Phrases.whoSays(target), Stage.None,
                animals.map { Choice.Token(it.emoji) }, answer, reveal = target.reveal, say = Phrases.whoSaysSay(target),
                whyNot = whyNot(animals, answer) { Phrases.notAnimal(it, target) },
            )
        },

        Template(Topic.HABITATS, 4..6, solving = true) { g ->
            val dweller = g.pick(Habitats.all).forAge(g.age)
            val (homes, answer) = g.shuffleIn(dweller.home, (Home.entries - dweller.home).shuffled(g.random).take(g.optionCount - 1))
            Quiz(
                g.id, Topic.HABITATS, g.level, g.difficulty, dweller.question, Stage.Picture(dweller.emoji),
                homes.map { Choice.Token(it.emoji, it.label) }, answer, reveal = dweller.reveal, say = dweller.question + Phrases.tapItsHome,
                whyNot = whyNot(homes, answer) { Phrases.notHome(dweller, it) },
            )
        },
        // The whole child, drawn: parts emoji cannot show, where they really are.
        Template(Topic.BODY_PARTS, 2..6, solving = false, weight = 0.6) { g -> Diagrams.card(g, Topic.BODY_PARTS, Diagram.BODY) },
        Template(Topic.BODY_TAP, 3..8, solving = true) { g -> Diagrams.tap(g, Topic.BODY_TAP, Diagram.BODY) },
        // Science.
        Template(Topic.PLANT_PARTS, 4..8, solving = false, weight = 0.6) { g -> Diagrams.card(g, Topic.PLANT_PARTS, Diagram.PLANT) },
        Template(Topic.PLANT_TAP, 5..9, solving = true) { g -> Diagrams.tap(g, Topic.PLANT_TAP, Diagram.PLANT) },
        Template(Topic.PHOTOSYNTHESIS, 7..12, solving = false, weight = 0.6) { g -> Science.lab(g, Topic.PHOTOSYNTHESIS, Lab.PHOTOSYNTHESIS) },
        Template(Topic.WATER_CYCLE, 6..12, solving = false, weight = 0.6) { g -> Science.lab(g, Topic.WATER_CYCLE, Lab.WATER_CYCLE) },
        Template(Topic.FLOAT_SINK, 4..8, solving = true) { g -> Science.floatSink(g) },
        Template(Topic.MAGNETS, 6..10, solving = true) { g -> Science.magnet(g) },
        Template(Topic.STATES, 6..10, solving = true) { g -> Science.state(g) },
        Template(Topic.ALIVE, 4..7, solving = true) { g -> Science.alive(g) },
        Template(Topic.PLANETS, 7..12, solving = false, weight = 0.6) { g -> Science.planet(g) },

        // Words and grammar. A topic about one language says so in `langs`.
        Template(Topic.OPPOSITES, 5..10, solving = true) { g -> Grammar.opposite(g) },
        Template(Topic.PLURALS, 5..8, solving = true, langs = setOf(Lang.EN, Lang.HI)) { g -> Grammar.plural(g) },
        Template(Topic.GENDER, 6..9, solving = true, langs = setOf(Lang.EN, Lang.HI)) { g -> Grammar.gender(g) },
        Template(Topic.RHYMES, 4..7, solving = true, langs = setOf(Lang.EN)) { g -> Grammar.rhyme(g) },
        Template(Topic.WORD_CARDS, 5..9, solving = false, weight = 0.6) { g -> Grammar.card(g) },
        Template(Topic.WORD_KINDS, 6..10, solving = true) { g -> Grammar.kind(g) },
        Template(Topic.MATRAS, 5..7, solving = true, langs = setOf(Lang.HI)) { g -> Grammar.matra(g) },

        // The computer cards asked the other way round: what each part is for.
        Template(Topic.COMPUTER_USE, 5..9, solving = true, weight = 0.6) { g ->
            val parts = Words.computer
            val word = g.pick(parts)
            val (choices, answer) = g.shuffleIn(word, parts.filter { it != word }.shuffled(g.random).take(g.optionCount - 1))
            val question = Words.computerUses.getValue(word.emoji)
            Quiz(
                g.id, Topic.COMPUTER_USE, g.level, g.difficulty, question, Stage.None,
                choices.map { Choice.Token(it.emoji) }, answer, reveal = word.fact, say = question + Phrases.tapIt,
                whyNot = whyNot(choices, answer) { it.thisIs() + it.fact!! },
            )
        },
    ) + Words.byTopic.map { (topic, words) ->
        // Small topics with light weights, so together they are about three reels' worth.
        Template(topic, Words.agesOf(topic), solving = false, weight = 0.4) { g ->
            WordCard(g.id, topic, g.level, g.difficulty, words[g.inOrder("words.${topic.name}", words.size)], showFact = g.level >= 4)
        }
    }

    private val byTopic = all.associateBy { it.topic }

    fun of(topic: Topic): Template = byTopic.getValue(topic)

    private fun addition(g: Gen): Reel {
        val (a, b) = when (g.level) {
            4 -> g.upTo(2, 5).let { s -> g.between(1, s - 1).let { it to s - it } }
            5 -> g.upTo(3, 10).let { s -> g.between(1, s - 1).let { it to s - it } }
            6 -> g.upTo(6, 20).let { s -> g.between(1, s - 1).let { it to s - it } }
            7 -> {
                // Two digits plus one; carrying ("27 + 5") only once it is not the easy end.
                var pair: Pair<Int, Int>
                do pair = g.between(10, 49) to g.between(1, 9) while (g.difficulty <= 2 && pair.first % 10 + pair.second >= 10)
                pair
            }
            8 -> {
                var pair: Pair<Int, Int>
                do pair = g.between(10, 60) to g.between(10, 39) while (g.difficulty <= 2 && pair.first % 10 + pair.second % 10 >= 10)
                pair
            }
            9 -> g.between(100, 499) to g.between(10, 99)
            10 -> g.between(100, 499) to g.between(100, 499)
            11 -> g.between(1000, 4999) to g.between(100, 999)
            else -> g.between(1000, 5999) to g.between(1000, 3999)
        }
        val sum = a + b
        val aids = g.level <= 6
        val spread = if (sum <= 20) listOf(1, 2, 3) else listOf(1, 10, 2, 9, 11)
        val (choices, answer) = g.numberChoices(sum, spread, sticks = aids)
        return Quiz(
            g.id, Topic.ADDITION, g.level, g.difficulty, Phrases.addThem, Stage.Sum(a, Op.ADD, b, aids),
            choices, answer, reveal = g.words(Phrases.sumAnswer(a, Op.ADD, b, sum)), say = g.words(Phrases.sumSay(a, Op.ADD, b, aids)),
            whyNot = whyNot(choices.numbers, answer) { Phrases.notSum(it, a, Op.ADD, b, sum, aids) },
        )
    }

    private fun subtraction(g: Gen): Reel {
        val (a, b) = when (g.level) {
            5 -> g.upTo(2, 5).let { it to g.between(1, it - 1) }
            6 -> g.upTo(4, 10).let { it to g.between(1, it) }
            7 -> g.upTo(10, 20).let { it to g.between(1, 10) }
            8 -> {
                var pair: Pair<Int, Int>
                do pair = g.between(20, 99).let { it to g.between(10, it - 1) } while (g.difficulty <= 2 && pair.first % 10 < pair.second % 10)
                pair
            }
            9 -> g.between(100, 500) to g.between(10, 99)
            10 -> g.between(200, 999).let { it to g.between(100, it - 1) }
            11 -> g.between(1000, 4999) to g.between(100, 999)
            else -> g.between(2000, 9999).let { it to g.between(1000, it - 1) }
        }
        val diff = a - b
        val aids = g.level <= 7 && a <= 20
        val spread = if (a <= 20) listOf(1, 2, 3) else listOf(1, 10, 2, 9, 11)
        val (choices, answer) = g.numberChoices(diff, spread, sticks = aids)
        return Quiz(
            g.id, Topic.SUBTRACTION, g.level, g.difficulty, Phrases.takeAway, Stage.Sum(a, Op.SUB, b, aids),
            choices, answer, reveal = g.words(Phrases.sumAnswer(a, Op.SUB, b, diff)), say = g.words(Phrases.sumSay(a, Op.SUB, b, aids)),
            whyNot = whyNot(choices.numbers, answer) { Phrases.notSum(it, a, Op.SUB, b, diff, aids) },
        )
    }

    private fun multiplication(g: Gen): Reel {
        val (a, b) = when (g.level) {
            7 -> g.pick(listOf(2, 5, 10)) to (if (g.difficulty >= 4) g.between(1, 10) else g.between(1, 5))
            8 -> (if (g.difficulty <= 2) g.between(2, 5) else g.between(2, 10)) to g.between(1, 10)
            9 -> g.between(2, 12) to g.between(2, 12)
            10 -> g.between(11, 25) to g.between(2, 9)
            11 -> g.between(12, 25) to g.between(11, 19)
            else -> g.between(20, 99) to g.between(11, 25)
        }
        val product = a * b
        val aids = a <= 6 && b <= 6
        val (choices, answer) = g.numberChoices(product, listOf(a, b, 1, 2, 10).distinct(), sticks = false, min = 1)
        return Quiz(
            g.id, Topic.MULTIPLICATION, g.level, g.difficulty, Phrases.multiply, Stage.Sum(a, Op.MUL, b, aids),
            choices, answer, reveal = Phrases.sumAnswer(a, Op.MUL, b, product), say = Phrases.sumSay(a, Op.MUL, b, aids),
            whyNot = whyNot(choices.numbers, answer) { Phrases.notSum(it, a, Op.MUL, b, product, aids) },
        )
    }

    private fun compare(g: Gen): Reel {
        // The smallest allowed gap: big and obvious for easy reels, one apart for hard ones.
        val gap = when (g.difficulty) {
            1, 2 -> 3
            3 -> 2
            else -> 1
        }
        if (g.level <= 4) {
            val top = if (g.level == 3) 5 else 10
            var x: Int
            var y: Int
            do {
                x = g.between(1, top)
                y = g.between(1, top)
            } while (x == y || kotlin.math.abs(x - y) < gap.coerceAtMost(top - 1))
            val thing = g.pick(Countables)
            val more = maxOf(x, y)
            val choices = listOf(Choice.Group(thing.emoji, x), Choice.Group(thing.emoji, y))
            val answer = choices.indexOfFirst { it.n == more }
            return Quiz(
                g.id, Topic.COMPARE, g.level, g.difficulty, Phrases.whichMore(thing), Stage.None, choices,
                answer, reveal = Phrases.more(more, minOf(x, y)), say = Phrases.whichMoreSay(thing),
                whyNot = whyNot(choices, answer) { Phrases.notFewer(it.n, thing) },
            )
        }
        val range = when (g.level) {
            5 -> 1..20
            6 -> 10..99
            else -> 100..999
        }
        var x: Int
        var y: Int
        do {
            x = g.between(range.first, range.last)
            // Hard: same leading digit, so the child has to look past the first one.
            y = if (g.difficulty >= 4 && x >= 10) {
                val unit = if (x >= 100) 100 else 10
                (x / unit) * unit + g.between(0, unit - 1)
            } else {
                g.between(range.first, range.last)
            }
        } while (x == y || y !in range)
        val smaller = g.difficulty >= 3 && g.chance(0.5)
        val target = if (smaller) minOf(x, y) else maxOf(x, y)
        val other = if (smaller) maxOf(x, y) else minOf(x, y)
        val sticks = g.level == 5
        val choices = listOf(Choice.Number(x, sticks), Choice.Number(y, sticks))
        val answer = choices.indexOfFirst { it.n == target }
        return Quiz(
            g.id, Topic.COMPARE, g.level, g.difficulty, g.words(if (smaller) Phrases.whichSmaller else Phrases.whichBigger),
            Stage.None, choices, answer,
            reveal = if (smaller) Phrases.less(target, other) else Phrases.more(target, other),
            say = g.words(if (smaller) Phrases.whichSmallerSay(x, y) else Phrases.whichBiggerSay(x, y)),
            whyNot = whyNot(choices.numbers, answer) { g.words(if (smaller) Phrases.notSmaller(it, target) else Phrases.notBigger(it, target)) },
        )
    }

    private fun sequence(g: Gen): Reel {
        val terms: List<Int> = when (g.level) {
            6 -> arithmetic(g.between(1, 20), g.pick(if (g.difficulty >= 4) listOf(1, 2, -1) else listOf(1, 2)))
            7 -> arithmetic(g.between(1, 30), g.pick(listOf(2, 5, 10, -1, -2)))
            8 -> arithmetic(g.between(1, 50), g.pick(listOf(3, 4, 5, 10, -5, -10)))
            9 -> if (g.chance(0.4)) geometric(g.between(1, 5), 2) else arithmetic(g.between(1, 60), g.pick(listOf(3, 6, 7, -3)))
            // Rules a top-of-primary class meets: each the sum of the two before, cubes, primes.
            11, 12 -> when (g.between(0, if (g.level == 12) 3 else 2)) {
                0 -> g.between(1, 5).let { a -> generateSequence(a to a + g.between(0, 3)) { (p, q) -> q to p + q }.map { it.first }.take(5).toList() }
                1 -> arithmetic(g.between(10, 200), g.pick(listOf(11, 12, 15, 25, -12, -25)))
                2 -> g.between(1, 4).let { s -> (s..s + 4).map { it * it * it } }
                else -> Numbers.primes.takeWhile { it < 60 }.windowed(5).let(g::pick)
            }
            else -> when (g.between(0, 3)) {
                0 -> g.between(1, 4).let { s -> (s..s + 4).map { it * it } }
                1 -> geometric(g.between(1, 3), 3)
                2 -> geometric(g.between(1, 6), 2)
                // Growing gaps: 1, 2, 4, 7, 11 (+1, +2, +3, +4).
                else -> g.between(1, 10).let { s -> (0 until 5).map { i -> s + i * (i + 1) / 2 } }
            }
        }
        val blank = if (g.difficulty <= 3) terms.lastIndex else g.between(1, terms.lastIndex - 1)
        val step = kotlin.math.abs(terms[1] - terms[0]).coerceAtLeast(1)
        val (choices, answer) = g.numberChoices(terms[blank], listOf(step, 1, 2).distinct(), sticks = false)
        val next = blank == terms.lastIndex
        return Quiz(
            g.id, Topic.SEQUENCE, g.level, g.difficulty, if (next) Phrases.whatNext else Phrases.whatMissing,
            Stage.Row(terms.map { it.toString() }, blank), choices, answer,
            say = if (next) Phrases.whatNextSay(terms.dropLast(1).map { Txt.same(it.toString()) }) else Phrases.whatMissingSay,
            whyNot = whyNot(choices.numbers, answer) { Phrases.notInRow(it, terms.zipWithNext { p, q -> q - p }.distinct().singleOrNull()) },
        )
    }

    /** Five terms from [start], going down only as far as stays positive. */
    private fun arithmetic(start: Int, step: Int): List<Int> {
        val first = if (step < 0) start + -step * 5 else start
        return (0 until 5).map { first + it * step }
    }

    private fun geometric(start: Int, ratio: Int): List<Int> = (0 until 5).runningFold(start) { v, _ -> v * ratio }.take(5)

    private fun clock(g: Gen): Reel {
        val hour = g.between(1, 12)
        val minute = when (g.level) {
            5 -> 0
            6 -> g.pick(listOf(0, 30))
            7 -> g.pick(listOf(0, 15, 30, 45))
            8 -> g.between(0, 11) * 5
            else -> if (g.difficulty >= 4) g.between(0, 59) else g.between(0, 11) * 5
        }
        fun wrap(h: Int) = (h + 11) % 12 + 1
        val candidates = buildList {
            // The classic mix-up: reading the hands the wrong way round.
            if (minute % 5 == 0 && minute / 5 in 1..12 && minute / 5 != hour) add(Choice.Time(minute / 5, (hour * 5) % 60))
            add(Choice.Time(wrap(hour + 1), minute))
            add(Choice.Time(wrap(hour - 1), minute))
            if (g.level >= 6) add(Choice.Time(hour, (minute + 30) % 60))
            repeat(12) { add(Choice.Time(g.between(1, 12), if (g.level == 5) 0 else minute)) }
        }
        val target = Choice.Time(hour, minute)
        val others = candidates.filter { it != target }.distinct().take(g.optionCount - 1)
        val (choices, answer) = g.shuffleIn(target, others)
        return Quiz(
            g.id, Topic.CLOCK, g.level, g.difficulty, Phrases.whatTime, Stage.Clock(hour, minute),
            choices, answer, reveal = Phrases.time(hour, minute), say = Phrases.clockSay,
            whyNot = whyNot(choices, answer) { Phrases.notTime(it.hour, it.minute, hourWrong = it.hour != hour) },
        )
    }

    private fun shapeQuiz(g: Gen): Reel {
        val colours = Colours.bright.shuffled(g.random)
        if (g.level >= 6 && g.chance(0.5)) {
            // Each option has a different number of sides, so exactly one fits.
            val bySides = Shapes.upTo(g.level).filter { it.sides > 0 }.groupBy { it.sides }.values.map { g.pick(it) }
            val picked = bySides.shuffled(g.random).take(g.optionCount)
            val target = picked.first()
            val shuffled = picked.shuffled(g.random)
            val said = target.forAge(g.age)
            return Quiz(
                g.id, Topic.SHAPE_QUIZ, g.level, g.difficulty, g.words(Phrases.whichSides(target.sides)), Stage.None,
                shuffled.mapIndexed { i, s -> Choice.Shape(s.kind, colours[i]) }, shuffled.indexOf(target),
                reveal = txt("${said.name[Lang.EN]}: ${said.fact[Lang.EN]}", "${said.name[Lang.HI]}: ${said.fact[Lang.HI]}"),
                say = g.words(Phrases.whichSidesSay(target.sides)),
                whyNot = whyNot(shuffled, shuffled.indexOf(target)) { g.words(Phrases.notSides(it, target.sides)) },
            )
        }
        val pool = Shapes.upTo(g.level)
        val target = g.pick(pool)
        val (shapes, answer) = g.shuffleIn(target, (pool - target).shuffled(g.random).take(g.optionCount - 1))
        val said = target.forAge(g.age)
        return Quiz(
            g.id, Topic.SHAPE_QUIZ, g.level, g.difficulty, Phrases.whichShape(said), Stage.None,
            shapes.mapIndexed { i, s -> Choice.Shape(s.kind, colours[i]) }, answer,
            reveal = txt("That is the ${said.name[Lang.EN].lowercase()}!", "यह ${said.name[Lang.HI]} है!"),
            say = Phrases.whichShape(said) + Phrases.findAndTap,
            whyNot = whyNot(shapes, answer) { Phrases.notShape(it.forAge(g.age), said) },
        )
    }

    private fun pattern(g: Gen): Reel {
        val shapes = when (g.level) {
            3 -> listOf("AB")
            4 -> listOf("AB", "AAB", "ABB")
            5 -> listOf("AB", "AAB", "ABB", "ABC", "AABB")
            else -> listOf("ABC", "AABB", "ABBC", "ABCD")
        }
        val unit = g.pick(shapes)
        val symbols = g.pick(PatternSets).shuffled(g.random)
        val sequence = List(16) { symbols[unit[it % unit.length] - 'A'].emoji }
        // Show at least two full repeats, so the rule is visible, and at most 7 pieces.
        val shown = (unit.length * 2 + g.between(0, unit.length - 1)).coerceAtMost(7)
        val tokens = sequence.take(shown + 1)
        val target = tokens.last()
        val used = unit.toSet().map { symbols[it - 'A'] }
        val pool = (used + symbols).distinct().map { it.emoji }
        val others = pool.filter { it != target }.take(g.optionCount - 1)
        val (choices, answer) = g.shuffleIn(target, others)
        val names = tokens.take(shown).map { e -> symbols.first { it.emoji == e }.one }
        return Quiz(
            g.id, Topic.PATTERN, g.level, g.difficulty, Phrases.whatNext, Stage.Row(tokens, shown),
            choices.map { Choice.Token(it) }, answer, say = Phrases.whatNextSay(names),
            whyNot = whyNot(choices, answer) { e -> Phrases.notInPattern(symbols.first { it.emoji == e }, unit.map { symbols[it - 'A'] }) },
        )
    }

    private class NumberRule(val name: Txt, val fromAge: Int, val member: (Int) -> Boolean)

    private val numberRules = listOf(
        NumberRule(txt("even numbers", "सम संख्याएँ"), 7) { it % 2 == 0 },
        NumberRule(txt("odd numbers", "विषम संख्याएँ"), 7) { it % 2 == 1 },
        NumberRule(txt("multiples of 5", "5 के गुणज"), 7) { it % 5 == 0 },
        NumberRule(txt("multiples of 10", "10 के गुणज"), 7) { it % 10 == 0 },
        NumberRule(txt("multiples of 3", "3 के गुणज"), 8) { it % 3 == 0 },
        NumberRule(txt("multiples of 4", "4 के गुणज"), 9) { it % 4 == 0 },
        NumberRule(txt("square numbers", "वर्ग संख्याएँ"), 10) { n -> (1..15).any { it * it == n } },
        NumberRule(txt("multiples of 6", "6 के गुणज"), 11) { it % 6 == 0 },
        NumberRule(txt("multiples of 7", "7 के गुणज"), 11) { it % 7 == 0 },
        NumberRule(txt("multiples of 9", "9 के गुणज"), 11) { it % 9 == 0 },
        NumberRule(txt("prime numbers", "अभाज्य संख्याएँ"), 12) { Numbers.isPrime(it) },
    )

    private fun oddOneOut(g: Gen): Reel {
        if (g.level >= 7 && g.chance(0.6)) {
            val rule = g.pick(numberRules.filter { it.fromAge <= g.level })
            val top = if (g.level >= 11) 150 else if (g.level >= 9) 100 else 50
            val members = (1..top).filter(rule.member).shuffled(g.random).take(g.optionCount - 1)
            val odd = g.pick((1..top).filter { !rule.member(it) })
            val (numbers, answer) = g.shuffleIn(odd, members)
            return Quiz(
                g.id, Topic.ODD_ONE_OUT, g.level, g.difficulty, Phrases.oddOneOut, Stage.None,
                numbers.map { Choice.Number(it, sticks = false) }, answer, reveal = Phrases.oddNumber(odd, rule.name), say = Phrases.oddOneOutSay,
                whyNot = whyNot(numbers, answer) { Phrases.notOddNumber(it, rule.name) },
            )
        }
        val main = g.pick(Categories.all)
        val near = main.near.map(Categories::byKey)
        val other = if (g.difficulty >= 4 && g.level >= 5 && near.isNotEmpty()) {
            g.pick(near)
        } else {
            g.pick(Categories.all.filter { it != main && it !in near })
        }
        val odd = g.pick(other.items.filter { it !in main.items })
        val members = main.items.shuffled(g.random).take(g.optionCount - 1)
        val (items, answer) = g.shuffleIn(odd, members)
        return Quiz(
            g.id, Topic.ODD_ONE_OUT, g.level, g.difficulty, Phrases.oddOneOut, Stage.None,
            items.map { Choice.Token(it) }, answer, reveal = Phrases.notInGroup(main.forAge(g.age)), say = Phrases.oddOneOutSay,
            whyNot = whyNot(items, answer) { Phrases.notOddItem(main.forAge(g.age)) },
        )
    }

    private fun sudoku(g: Gen): Reel {
        val size = when (g.level) {
            5, 6 -> 4
            7 -> if (g.difficulty <= 2) 4 else 6
            8 -> 6
            9 -> if (g.difficulty <= 2) 6 else 9
            else -> 9
        }
        // Five-year-olds get fruit or colours: the rule is the same, reading digits is not needed.
        val pictures = if (g.level == 5) g.pick(listOf(PatternSets[0], PatternSets[1])) else null
        val symbols = pictures?.map { it.emoji } ?: (1..size).map { it.toString() }
        val names = pictures?.map { it.one } ?: symbols.map { Txt.same(it) }
        val cells = Sudoku.solved(size, g.random)
        val blank = g.random.nextInt(cells.size)
        val value = cells[blank]
        val values = if (size == 4) (0 until 4).toList() else ((0 until size).filter { it != value }.shuffled(g.random).take(3) + value).sorted()
        val (boxRows, boxCols) = Sudoku.boxes(size)
        return Quiz(
            g.id, Topic.SUDOKU, g.level, g.difficulty, g.words(if (g.level == 5) Phrases.sudokuPicture else Phrases.sudokuNumber),
            Stage.Grid(size, boxRows, boxCols, cells, blank, symbols, hint = g.level <= 6 || g.difficulty <= 2),
            values.map { Choice.Token(symbols[it]) }, values.indexOf(value),
            say = g.words(if (g.level == 5) Phrases.sudokuPictureSay else Phrases.sudokuNumberSay),
            whyNot = whyNot(values, values.indexOf(value)) { g.words(Phrases.alreadyInRow(names[it])) },
        )
    }
}
