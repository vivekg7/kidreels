package com.crylo.kidreels.core

/*
 * Reasoning puzzles with figures, after the Mental Ability Test of the Jawahar Navodaya
 * Vidyalaya entrance exam (taken at about 10): the same figure, the odd one out, pattern
 * completion, figure series, mirror images, analogies, completing a square, hidden
 * figures and folded-paper punching. Easier versions start at 4. Every wrong option is a
 * classic trap (turned instead of mirrored, upside down instead of mirrored, slid
 * instead of reflected), and each says why it is wrong.
 */

/** Tries [make] until it returns non-null: the generators draw at random and reject bad draws. */
private fun <T : Any> retry(what: String, make: () -> T?): T {
    repeat(2000) { make()?.let { return it } }
    error("Could not make $what")
}

internal object Reasoning {
    private val easyKinds = listOf(MarkKind.DOT, MarkKind.RING, MarkKind.SQUARE, MarkKind.BOX, MarkKind.TRIANGLE, MarkKind.ARROW)
    private val allKinds = MarkKind.entries
    private val cells = (-1..1).flatMap { y -> (-1..1).map { x -> x to y } }

    private fun Gen.marks(): Int = when {
        level <= 5 -> 2
        level <= 7 -> 3
        difficulty >= 3 -> 4
        else -> 3
    }

    private fun Gen.figure(n: Int = marks(), kinds: List<MarkKind> = if (level <= 6) easyKinds else allKinds, at: List<Pair<Int, Int>> = cells): Figure =
        Figure(at.shuffled(random).take(n).map { (x, y) -> Mark(pick(kinds), x, y, 90 * random.nextInt(4)) })

    /** Every way of turning and flipping [f], which a chiral figure keeps all different. */
    private fun images(f: Figure) = (0..3).map { f.turned(it) } + (0..3).map { f.mirrored().turned(it) }

    /** Turning and flipping all give different figures: nothing looks like it by accident. */
    private fun chiral(f: Figure) = allDifferent(images(f))

    // A changed figure, and why it is not the same: the "same figure" and young "odd one out".
    private fun Gen.variant(f: Figure, level: Int): Pair<Figure, Txt> = retry("variant") {
        val i = random.nextInt(f.marks.size)
        val m = f.marks[i]
        val free = cells.filter { c -> f.marks.none { it.x == c.first && it.y == c.second } }
        val choice = random.nextInt(if (level >= 6) 5 else 3)
        val (g, why) = when (choice) {
            0 -> Figure(f.marks.toMutableList().also { it[i] = m.copy(kind = pick(easyKinds - m.kind)) }) to
                txt("Look closely: one of its marks is different.", "ध्यान से देखो: इसका एक निशान अलग है।")
            1 -> Figure(f.marks.toMutableList().also { it[i] = pick(free).let { (x, y) -> m.copy(x = x, y = y) } }) to
                txt("One mark is in a different place.", "एक निशान दूसरी जगह पर है।")
            2 -> if (f.marks.size > 1) Figure(f.marks - m) to txt("Something is missing from that one.", "इसमें कुछ छूट गया है।") else return@retry null
            3 -> f.mirrored() to txt("That one is flipped, as in a mirror.", "यह शीशे की तरह उलटी है।")
            else -> f.turned(1 + random.nextInt(3)) to txt("That one is turned round.", "यह घुमाई हुई है।")
        }
        if (g.same(f)) null else g to why
    }

    fun sameFigure(g: Gen): Reel = retry("same figure") {
        val f = g.figure()
        if (!chiral(f)) return@retry null
        val wrong = List(g.optionCount - 1) { g.variant(f, g.level) }
        if (!allDifferent(wrong.map { it.first } + f)) return@retry null
        val (choices, answer) = g.shuffleIn(f to null as Txt?, wrong)
        val prompt = txt("Which one is exactly the same?", "कौन सी आकृति बिल्कुल एक जैसी है?").simpler("Which one is just the same?", "कौन सी बिल्कुल ऐसी ही है?")
        Quiz(
            g.id, Topic.SAME_FIGURE, g.level, g.difficulty, g.words(prompt), Stage.Figures(listOf(f), Layout.ONE),
            choices.map { Choice.Fig(it.first) }, answer,
            reveal = txt("Just the same, every little mark!", "बिल्कुल एक जैसी, हर निशान तक!"),
            say = g.words(prompt + txt("Look at every little mark.", "हर छोटा निशान ध्यान से देखो।")),
            whyNot = choices.map { it.second },
        )
    }

    fun oddFigure(g: Gen): Reel = retry("odd figure") {
        val f = g.figure()
        if (!chiral(f)) return@retry null
        val n = g.optionCount
        val (same, odd, whySame, reveal) = if (g.level <= 5) {
            val (v, _) = g.variant(f, 5)
            Quad(List(n - 1) { f }, v, txt("That one is like the others.", "यह बाकियों जैसी है।"), txt("That one is different!", "यही अलग है!"))
        } else {
            // Three turns of one figure, and its mirror image turned: the test's favourite.
            val turns = (0..3).shuffled(g.random).take(n - 1)
            Quad(
                turns.map { f.turned(it) }, f.mirrored().turned(g.random.nextInt(4)),
                txt("That one is the same figure, just turned.", "यह वही आकृति है, बस घुमाई हुई।"),
                txt("That one is flipped, not just turned!", "यह सिर्फ़ घुमाई नहीं, उलटी भी है!"),
            )
        }
        if (same.any { it.same(odd) }) return@retry null
        // Copies of one figure are still separate choices: [Choice.Fig.copy] tells them apart.
        val (choices, answer) = g.shuffleIn(Choice.Fig(odd), same.mapIndexed { i, s -> Choice.Fig(s, copy = i + 1) })
        Quiz(
            g.id, Topic.ODD_FIGURE, g.level, g.difficulty, Phrases.oddOneOut, Stage.None, choices, answer,
            reveal = reveal, say = Phrases.oddOneOutSay, whyNot = whyNot(choices, answer) { whySame },
        )
    }

    fun completePattern(g: Gen): Reel = retry("pattern") {
        val q = g.figure(n = if (g.level <= 6) 2 else 3)
        if (!chiral(q)) return@retry null
        val repeat = g.level == 5 || (g.level == 6 && g.difficulty <= 2)
        val grid = if (repeat) List(4) { q } else listOf(q, q.mirrored(), q.flipped(), q.turned(2))
        val gap = g.random.nextInt(4)
        val right = grid[gap]
        val wrong = (if (repeat) listOf(q.mirrored(), q.flipped(), q.turned(1)) else grid - right).take(g.optionCount - 1)
        val (choices, answer) = g.shuffleIn(right, wrong)
        val prompt = txt("Which piece completes the pattern?", "कौन सा टुकड़ा पैटर्न पूरा करेगा?")
        val why = if (repeat) txt("Here all four pieces are the same. Look again!", "यहाँ चारों टुकड़े एक जैसे हैं। फिर से देखो!")
        else txt("That does not match. The pattern is like a mirror: look at the piece beside the gap.", "यह मेल नहीं खाता। पैटर्न शीशे जैसा है: खाली जगह के बगल वाला टुकड़ा देखो।")
        Quiz(
            g.id, Topic.COMPLETE_PATTERN, g.level, g.difficulty, prompt,
            Stage.Figures(grid.mapIndexed { i, f -> f.takeIf { i != gap } }, Layout.GRID), choices.map { Choice.Fig(it) }, answer,
            say = prompt + txt("Look at the other three.", "बाकी तीनों को देखो।"),
            reveal = txt("The pattern is complete!", "पैटर्न पूरा हो गया!"), whyNot = whyNot(choices, answer) { why },
        )
    }

    fun figureSeries(g: Gen): Reel = retry("series") {
        val f = g.figure(n = if (g.level <= 7) 2 else 3, at = if (g.level <= 7) cells.filter { it != 0 to 0 } else cells)
        val twoSpeeds = g.level >= 8 && g.difficulty >= 3
        val speeds = f.marks.map { if (twoSpeeds) 1 + g.random.nextInt(2) else 1 }
        if (twoSpeeds && speeds.toSet().size < 2) return@retry null
        // Up to 7 the marks only move round the edge; from 8 they turn as they go, too.
        val turn = g.level >= 8
        val terms = (0..4).map { f.stepped(it, speeds, turn) }
        val right = terms[4]
        val candidates = listOf(
            terms[3] to txt("That is the same as the last one. Something should move!", "यह तो पिछली वाली जैसी ही है। कुछ तो आगे बढ़ना चाहिए!"),
            f.stepped(5, speeds, turn) to txt("That one has gone a step too far.", "यह एक कदम ज़्यादा आगे चली गई।"),
            right.mirrored() to txt("That one is flipped. Watch how each mark moves, one to the next.", "यह उलटी है। देखो हर निशान एक से अगली तक कैसे बढ़ता है।"),
            f.stepped(4, turn = turn) to txt("Watch each mark: they do not all move the same amount.", "हर निशान देखो: सब बराबर नहीं बढ़ते।"),
        )
        val wrong = candidates.distinctBy { it.first.look }.filter { !it.first.same(right) }.take(g.optionCount - 1)
        if (wrong.size < g.optionCount - 1 || !allDifferent(terms)) return@retry null
        val (choices, answer) = g.shuffleIn(right to null as Txt?, wrong)
        Quiz(
            g.id, Topic.FIGURE_SERIES, g.level, g.difficulty, Phrases.whatNext,
            Stage.Figures(terms.take(4) + null, Layout.ROW), choices.map { Choice.Fig(it.first) }, answer,
            say = txt("Each picture moves on a little. What comes next?", "हर बार निशान थोड़ा आगे बढ़ते हैं। आगे क्या आएगा?"),
            reveal = txt("Every mark kept moving!", "हर निशान आगे बढ़ता रहा!"), whyNot = choices.map { it.second },
        )
    }

    fun mirror(g: Gen): Reel = retry("mirror") {
        val f = g.figure()
        if (!chiral(f)) return@retry null
        val wrong = listOf(
            f to txt("That is just the same picture. A mirror swaps left and right!", "यह तो वही तस्वीर है। शीशे में बायाँ और दायाँ बदल जाते हैं!"),
            f.flipped() to txt("That one is upside down. A mirror swaps left and right, not top and bottom.", "यह उल्टी है। शीशे में बायाँ-दायाँ बदलता है, ऊपर-नीचे नहीं।"),
            f.turned(2) to txt("That one is turned round. Look at which side each mark is on.", "यह घुमाई हुई है। देखो हर निशान किस तरफ़ है।"),
        ).shuffled(g.random).take(g.optionCount - 1)
        val (choices, answer) = g.shuffleIn(f.mirrored() to null as Txt?, wrong)
        val prompt = txt("What does it look like in the mirror?", "शीशे में यह कैसी दिखेगी?")
        Quiz(
            g.id, Topic.MIRROR, g.level, g.difficulty, prompt, Stage.Figures(listOf(f), Layout.MIRROR),
            choices.map { Choice.Fig(it.first) }, answer,
            say = prompt + txt("The mirror is on its right.", "शीशा इसके दाईं ओर है।"),
            reveal = txt("In a mirror, left and right swap!", "शीशे में बायाँ और दायाँ बदल जाते हैं!"), whyNot = choices.map { it.second },
        )
    }

    private class Change(val apply: (Figure) -> Figure, val reveal: Txt)

    private val changes = listOf(
        Change({ it.turned(1) }, txt("Each one turns a quarter of the way round!", "हर एक चौथाई घूमती है!")),
        Change({ it.turned(2) }, txt("Each one turns right round, upside down!", "हर एक आधी घूमकर उल्टी हो जाती है!")),
        Change({ it.turned(3) }, txt("Each one turns a quarter, the other way!", "हर एक दूसरी ओर चौथाई घूमती है!")),
        Change({ it.mirrored() }, txt("Each one flips, as in a mirror!", "हर एक शीशे की तरह उलटती है!")),
        Change({ it.flipped() }, txt("Each one flips upside down!", "हर एक ऊपर से नीचे उलटती है!")),
    )

    fun analogy(g: Gen): Reel = retry("analogy") {
        val a = g.figure()
        val c = g.figure()
        if (!chiral(a) || !chiral(c) || images(a).any { it.same(c) }) return@retry null
        val change = g.pick(changes)
        val right = change.apply(c)
        val wrong = (listOf(c) + (changes - change).map { it.apply(c) }).filter { !it.same(right) }.distinctBy { it.look }
            .shuffled(g.random).take(g.optionCount - 1)
        val (choices, answer) = g.shuffleIn(right, wrong)
        val prompt = txt("The first two go together. What goes with the third?", "पहली दो का जो रिश्ता है, वही तीसरी का किससे है?")
        Quiz(
            g.id, Topic.ANALOGY, g.level, g.difficulty, prompt,
            Stage.Figures(listOf(a, change.apply(a), c, null), Layout.ANALOGY), choices.map { Choice.Fig(it) }, answer,
            say = prompt + txt("See what changed from the first to the second.", "देखो पहली से दूसरी में क्या बदला।"),
            reveal = change.reveal,
            whyNot = whyNot(choices, answer) { txt("Not quite. Do to the third exactly what happened to the first.", "ऐसा नहीं। तीसरी के साथ ठीक वही करो जो पहली के साथ हुआ।") },
        )
    }

    /** Pieces cut from the top-left of a 4 × 4 board; each puzzle turns one to another corner. */
    private val cuts = listOf(
        listOf(0 to 0, 2 to 0, 0 to 2),
        listOf(0 to 0, 3 to 0, 3 to 1, 0 to 2),
        listOf(0 to 0, 4 to 0, 4 to 1, 2 to 2, 0 to 1),
        listOf(0 to 0, 3 to 0, 3 to 1, 1 to 1, 1 to 2, 0 to 2),
        listOf(0 to 0, 2 to 0, 2 to 1, 3 to 1, 3 to 2, 0 to 2),
        listOf(0 to 0, 3 to 0, 2 to 2, 0 to 2),
        listOf(0 to 0, 1 to 0, 2 to 1, 2 to 2, 0 to 2),
        listOf(0 to 0, 4 to 0, 3 to 1, 1 to 1, 0 to 2),
    ).map { p -> Polygon(p.map { (x, y) -> Pt(x, y) }) }

    private const val BOARD = 4

    fun fitPiece(g: Gen): Reel = retry("fit piece") {
        val turnsOnBoard = g.random.nextInt(4)
        val piece = g.pick(cuts).turned(turnsOnBoard, BOARD)
        // From 9 the right piece is shown turned, so it has to be turned back in the mind.
        val turnedShown = g.level >= 9 && g.difficulty >= 3
        val show = if (turnedShown) piece.turned(1 + g.random.nextInt(3), BOARD) else piece
        val rotations = (0..3).map { piece.turned(it, BOARD).shape }
        val flipped = txt("That piece is flipped over. It would not fit.", "यह टुकड़ा उलटा है। यह फिट नहीं होगा।")
        val turned = txt("That piece is turned the wrong way.", "यह टुकड़ा गलत तरफ़ घूमा हुआ है।")
        val other = txt("That piece has a different shape. Count its corners!", "इस टुकड़े का आकार अलग है। इसके कोने गिनो!")
        val candidates = buildList {
            add(show.mirrored(BOARD) to flipped)
            if (!turnedShown) add(piece.turned(1 + g.random.nextInt(3), BOARD) to turned)
            (cuts.map { it.turned(turnsOnBoard, BOARD) }).shuffled(g.random).forEach { add(it to other) }
        }
        val wrong = candidates.filter { (p, _) -> if (turnedShown) p.shape !in rotations else p.shape != piece.shape }
            .distinctBy { it.first.shape }.take(g.optionCount - 1)
        if (wrong.size < g.optionCount - 1) return@retry null
        val (choices, answer) = g.shuffleIn(show to null as Txt?, wrong)
        val prompt = txt("Which piece fills the gap in the square?", "कौन सा टुकड़ा वर्ग की खाली जगह भरेगा?")
        Quiz(
            g.id, Topic.FIT_PIECE, g.level, g.difficulty, prompt, Stage.Cutout(BOARD, piece),
            choices.map { Choice.Cut(it.first) }, answer,
            say = prompt + txt(if (turnedShown) "The pieces may be turned." else "Look at the shape of the gap.", if (turnedShown) "टुकड़े घुमाए हुए भी हो सकते हैं।" else "खाली जगह का आकार देखो।"),
            reveal = txt("A perfect fit!", "बिल्कुल फिट!"), whyNot = choices.map { it.second },
        )
    }

    private fun path(vararg p: Pair<Int, Int>) = Lines(p.toList().zipWithNext { (ax, ay), (bx, by) -> Edge.of(Pt(ax, ay), Pt(bx, by)) }.toSet())

    private val shapes = listOf(
        path(0 to 0, 1 to 0, 1 to 1, 0 to 1, 0 to 0),
        path(0 to 0, 0 to 1, 1 to 1, 0 to 0),
        path(1 to 0, 1 to 1, 0 to 1, 1 to 0),
        path(0 to 0, 1 to 0, 0 to 1, 0 to 0),
        path(0 to 0, 1 to 0, 1 to 1, 0 to 0),
        path(0 to 0, 0 to 1, 0 to 2, 1 to 2, 2 to 2, 1 to 1, 0 to 0),
        path(0 to 0, 1 to 0, 2 to 0, 2 to 1, 1 to 1, 0 to 1, 0 to 0),
        path(0 to 0, 1 to 0, 1 to 1, 1 to 2, 0 to 2, 0 to 1, 0 to 0),
        path(1 to 0, 2 to 1, 1 to 2, 0 to 1, 1 to 0),
        path(1 to 0, 2 to 0, 1 to 1, 0 to 1, 1 to 0),
        path(0 to 0, 1 to 0, 2 to 1, 1 to 1, 0 to 0),
        path(1 to 0, 2 to 0, 3 to 1, 2 to 1, 1 to 1, 0 to 1, 1 to 0),
        path(0 to 0, 1 to 0, 2 to 0, 2 to 1, 2 to 2, 1 to 2, 0 to 2, 0 to 1, 0 to 0),
    ).map { it.atOrigin() }

    private const val LATTICE = 3

    /** Every unit line of the 3 × 3 lattice: across, down and both diagonals of each cell. */
    private val allEdges: List<Edge> = buildList {
        for (x in 0..LATTICE) for (y in 0..LATTICE) {
            if (x < LATTICE) add(Edge.of(Pt(x, y), Pt(x + 1, y)))
            if (y < LATTICE) add(Edge.of(Pt(x, y), Pt(x, y + 1)))
            if (x < LATTICE && y < LATTICE) {
                add(Edge.of(Pt(x, y), Pt(x + 1, y + 1)))
                add(Edge.of(Pt(x + 1, y), Pt(x, y + 1)))
            }
        }
    }

    fun hidden(g: Gen): Reel = retry("hidden figure") {
        val pool = if (g.difficulty >= 3) shapes.filter { it.edges.size >= 4 } else shapes
        val target = g.pick(pool)
        val placed = target.moved(g.random.nextInt(LATTICE - target.width + 1), g.random.nextInt(LATTICE - target.height + 1))
        val extra = allEdges.shuffled(g.random).take(4 + g.difficulty * 2)
        val figure = Lines(placed.edges + extra)
        val wrong = shapes.filter { it != target && !figure.hides(it, LATTICE) }.shuffled(g.random).take(g.optionCount - 1)
        if (wrong.size < g.optionCount - 1) return@retry null
        val (choices, answer) = g.shuffleIn(target, wrong)
        val prompt = txt("Which shape is hidden in the picture?", "तस्वीर में कौन सी आकृति छिपी है?")
        Quiz(
            g.id, Topic.HIDDEN_FIGURE, g.level, g.difficulty, prompt, Stage.Hidden(figure, LATTICE, placed),
            choices.map { Choice.Shapes(it) }, answer,
            say = prompt + txt("It is the same shape, and the same way up.", "उसका आकार वही है, और वह उसी तरह रखी है।"),
            reveal = txt("Found it!", "मिल गई!"),
            whyNot = whyNot(choices, answer) { txt("That shape is not in the picture. Follow the lines with your finger!", "यह आकृति तस्वीर में नहीं है। उँगली से रेखाएँ देखो!") },
        )
    }

    fun paperFold(g: Gen): Reel = retry("paper fold") {
        val twice = g.level >= 11 || (g.level == 10 && g.difficulty >= 3)
        val vertical = twice || g.chance(0.5)
        // Folding the left half over the right leaves the right half; the top over the bottom, the bottom.
        val visible = (0..3).flatMap { c -> (0..3).map { r -> Hole(c, r) } }.filter { h ->
            when {
                twice -> h.col >= 2 && h.row >= 2
                vertical -> h.col >= 2
                else -> h.row >= 2
            }
        }
        val punched = visible.shuffled(g.random).take(if (g.difficulty >= 3 && !twice) 2 else 1).toSet()
        fun acrossV(s: Set<Hole>) = s + s.map { Hole(3 - it.col, it.row) }
        fun acrossH(s: Set<Hole>) = s + s.map { Hole(it.col, 3 - it.row) }
        val right = when {
            twice -> acrossV(acrossH(punched))
            vertical -> acrossV(punched)
            else -> acrossH(punched)
        }
        val onlyPunched = txt("Remember: the hole goes through every layer of the folded paper.", "याद रखो: छेद मुड़े हुए काग़ज़ की हर परत में होता है।")
        val slid = txt("Nearly! When the paper opens, holes are mirrored across the fold, not slid along.", "करीब! खुलने पर छेद मोड़ के दूसरी ओर शीशे की तरह बनते हैं, खिसकते नहीं।")
        val wrongAxis = txt("Look at where the fold line is.", "देखो मोड़ की रेखा कहाँ है।")
        val half = txt("The paper was folded twice. Open both folds!", "काग़ज़ दो बार मुड़ा था। दोनों मोड़ खोलो!")
        val candidates = buildList {
            add(punched to onlyPunched)
            if (twice) {
                add(acrossV(punched) to half)
                add(acrossH(punched) to half)
                add((punched + punched.map { Hole(it.col - 2, it.row) } + punched.map { Hole(it.col, it.row - 2) } + punched.map { Hole(it.col - 2, it.row - 2) }) to slid)
            } else if (vertical) {
                add(acrossH(punched) to wrongAxis)
                add((punched + punched.map { Hole(it.col - 2, it.row) }) to slid)
            } else {
                add(acrossV(punched) to wrongAxis)
                add((punched + punched.map { Hole(it.col, it.row - 2) }) to slid)
            }
        }
        val wrong = candidates.filter { it.first != right }.distinctBy { it.first }.take(g.optionCount - 1)
        if (wrong.size < g.optionCount - 1) return@retry null
        val (choices, answer) = g.shuffleIn(right to null as Txt?, wrong)
        val prompt = txt("The paper is folded and punched. What does it look like opened?", "काग़ज़ मोड़कर छेद किया गया। खोलने पर कैसा दिखेगा?")
        Quiz(
            g.id, Topic.PAPER_FOLD, g.level, g.difficulty, prompt, Stage.Fold(vertical, twice, punched),
            choices.map { Choice.Holes(it.first) }, answer,
            say = prompt + txt("The hole goes through every layer.", "छेद काग़ज़ की हर परत में होता है।"),
            reveal = txt("The holes mirror across the fold!", "छेद मोड़ के दोनों ओर बन जाते हैं!"), whyNot = choices.map { it.second },
        )
    }

    private data class Quad(val same: List<Figure>, val odd: Figure, val whySame: Txt, val reveal: Txt)
}
