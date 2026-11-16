package com.crylo.kidreels.core

/*
 * Memory games: follow the cup, find the pairs, copy the order, spot what is missing.
 * Each watches first and answers after, so each reel has a "look" part that runs by
 * itself and an "answer" part that waits for the child.
 */

/**
 * The cup shuffle: [prize] goes under cup [start], the cups swap places ([swaps] are
 * pairs of positions, left to right), and the child taps where it ended up.
 */
data class CupGame(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val cups: Int,
    val start: Int,
    val swaps: List<Pair<Int, Int>>,
    /** How long one swap takes: quicker for older children. */
    val swapMillis: Int,
    val prize: Thing,
) : Reel {
    /** Where the prize is after every swap. */
    val answer: Int get() = swaps.fold(start) { at, (a, b) -> if (at == a) b else if (at == b) a else at }
    override val topic get() = Topic.CUPS
    override val needsSolving get() = true
    override val prompt: Txt get() = Memory.whereIs(prize)
    override val voice: Lang? get() = null
}

/** Cards face down in [columns] columns; turn two at a time to find every pair. */
data class PairsGame(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val cards: List<Thing>,
    val columns: Int,
    /** All cards are shown face up this long first, for the youngest; 0 for none. */
    val peekMillis: Long,
) : Reel {
    override val topic get() = Topic.PAIRS
    override val needsSolving get() = true
    override val prompt: Txt get() = Memory.findPairs
    override val voice: Lang? get() = null
}

/** Pads light up in [sequence]; the child taps them in the same order. */
data class OrderGame(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val pads: Int,
    val sequence: List<Int>,
    /** How long each pad stays lit while the order is shown. */
    val stepMillis: Int,
) : Reel {
    override val topic get() = Topic.COPY_ORDER
    override val needsSolving get() = true
    override val prompt: Txt get() = Memory.copyOrder
    override val voice: Lang? get() = null
}

object Memory {
    /** Things to hide, pair and remember: named, with a Hindi gender for the sentences about them. */
    val things: List<Thing> = (Countables + Numbers.things).distinctBy { it.emoji }

    /**
     * Whether turning [first] then [second] of [cards], a miss, is a slip of memory: the
     * partner of [first] had been seen ([seen] holds every card turned so far), so the child
     * could have turned it, or [second] itself had been seen, so they knew it was not the
     * one. A [second] never seen is luck, even if its own partner was seen: the child could
     * not know what it was before turning it.
     */
    fun pairsSlip(cards: List<Thing>, seen: Collection<Int>, first: Int, second: Int): Boolean =
        second in seen || seen.any { it != first && cards[it] == cards[first] }

    /** The pads of "copy the order": colours a child can name, each with its own place. */
    val padColours: List<Colour> get() = Colours.all.take(4)

    fun whereIs(t: Thing) = txt("Where is the ${t.one[Lang.EN]}?", "${t.one[Lang.HI]} कहाँ है?")

    fun cupsSay(t: Thing) = txt(
        "Keep your eyes on the ${t.one[Lang.EN]}! It goes under a cup, and the cups will move.",
        "${t.one[Lang.HI]} पर नज़र रखो! वह एक कप के नीचे ${t.agree("जाएगा", "जाएगी")}, फिर कप घूमेंगे।",
    )

    fun cupsPick(t: Thing) = whereIs(t) + txt("Tap the cup!", "कप को छुओ!")

    fun cupsFound(t: Thing) = txt("You found the ${t.one[Lang.EN]}!", "तुमने ${t.one[Lang.HI]} ढूँढ ${t.agree("लिया", "ली")}!")

    fun cupsEmpty(t: Thing) = txt("Not under this one! Try another cup.", "इसके नीचे नहीं है! दूसरा कप देखो।")

    val findPairs = txt("Find the pairs", "जोड़ी ढूँढो")

    val pairsSay = txt(
        "Every picture has a twin hiding under a card. Turn two cards: if they match, they stay!",
        "हर तस्वीर का एक जुड़वाँ कार्ड के नीचे छिपा है। दो कार्ड पलटो: अगर एक जैसे हुए, तो खुले रहेंगे!",
    )

    val pairsPeek = txt("Look at them all, quick!", "जल्दी से सबको देख लो!")

    val pairsDone = txt("You found every pair!", "तुमने सारी जोड़ियाँ ढूँढ लीं!")

    val copyOrder = txt("Copy the order", "क्रम दोहराओ")

    val orderSay = txt("Watch which ones light up, then tap them in the same order!", "देखो कौन-से चमकते हैं, फिर उसी क्रम में उन्हें छुओ!")

    val orderYourTurn = txt("Your turn!", "अब तुम्हारी बारी!")

    val orderAgain = txt("Oops! Watch again.", "उफ़! फिर से देखो।")

    fun orderDone(n: Int) = txt("You remembered all $n!", "तुम्हें सारे $n याद रहे!")

    val whatMissing = txt("What is missing?", "क्या ग़ायब है?")

    val missingSay = txt(
        "Look at these carefully and remember them all! Then one will go away. Which one is missing?",
        "इन्हें ध्यान से देखो और सब याद रखो! फिर एक चला जाएगा। कौन-सा ग़ायब है?",
    )

    fun missingWas(t: Thing) = txt("Yes! The ${t.one[Lang.EN]} was missing!", "हाँ! ${t.one[Lang.HI]} ग़ायब ${t.agree("था", "थी")}!")

    fun neverThere(t: Thing) = txt(
        "The ${t.one[Lang.EN]} was not there before. Which one has gone?",
        "${t.one[Lang.HI]} तो पहले ${t.agree("था", "थी")} ही नहीं। कौन-सा चला गया?",
    )

    fun cups(g: Gen): CupGame {
        val (cups, swaps, millis) = when {
            g.level <= 3 -> Triple(2, g.upTo(1, 2), 900)
            g.level == 4 -> Triple(3, g.upTo(2, 3), 800)
            g.level <= 6 -> Triple(3, g.upTo(3, 5), 650)
            g.level <= 9 -> Triple(if (g.difficulty >= 3) 4 else 3, g.upTo(4, 7), 520)
            else -> Triple(4, g.upTo(6, 9), 420)
        }
        val pairs = (0 until cups).flatMap { a -> (a + 1 until cups).map { a to it } }
        val moves = mutableListOf<Pair<Int, Int>>()
        // Never the same two cups twice running, which only undoes the last move, unless
        // there are just two cups and no other move exists.
        while (moves.size < swaps) g.pick(pairs).let { if (pairs.size == 1 || it != moves.lastOrNull()) moves += it }
        return CupGame(g.id, g.level, g.difficulty, cups, g.between(0, cups - 1), moves, millis, g.pick(Memory.things))
    }

    fun pairs(g: Gen): PairsGame {
        val (count, columns, peek) = when {
            g.level <= 3 -> Triple(2, 2, 2500L)
            g.level == 4 -> Triple(3, 3, 2000L)
            g.level == 5 -> Triple(4, 4, 1500L)
            g.level == 6 -> Triple(4, 4, 0L)
            g.level <= 9 -> Triple(6, 4, 0L)
            else -> Triple(8, 4, 0L)
        }
        val picked = things.shuffled(g.random).take(count)
        return PairsGame(g.id, g.level, g.difficulty, (picked + picked).shuffled(g.random), columns, peek)
    }

    fun order(g: Gen): OrderGame {
        val pads = if (g.level <= 5) 3 else 4
        val length = when {
            g.level <= 4 -> 2
            g.level <= 6 -> g.upTo(3, 4)
            g.level <= 8 -> g.upTo(4, 5)
            g.level <= 10 -> g.upTo(5, 6)
            else -> g.upTo(6, 7)
        }
        // A pad may come back, but not twice running: a double flash is easy to miss.
        val sequence = mutableListOf<Int>()
        while (sequence.size < length) g.between(0, pads - 1).let { if (it != sequence.lastOrNull()) sequence += it }
        return OrderGame(g.id, g.level, g.difficulty, pads, sequence, stepMillis = if (g.level <= 6) 700 else if (g.level <= 9) 550 else 450)
    }

    /** "What is missing?": [Stage.Memory] shows the things, then takes one away. */
    fun missing(g: Gen): Quiz {
        val n = when {
            g.level <= 3 -> 3
            g.level <= 5 -> 4
            g.level <= 7 -> 5
            else -> 6
        }
        val shown = things.shuffled(g.random).take(n)
        val gone = g.between(0, n - 1)
        val others = things.filter { it !in shown }.shuffled(g.random).take(g.optionCount - 1)
        val (choices, answer) = g.shuffleIn(shown[gone], others)
        // Longer to look for more things, and for the little ones; shorter at the hard end.
        val look = ((1500 + 700 * n) * (1.3 - 0.1 * g.difficulty)).toLong()
        return Quiz(
            g.id, Topic.WHATS_MISSING, g.level, g.difficulty, whatMissing,
            Stage.Memory(shown.map { it.emoji }, gone, look, reshuffle = g.level >= 8 && g.difficulty >= 3),
            choices.map { Choice.Token(it.emoji) }, answer, reveal = missingWas(shown[gone]), say = missingSay,
            whyNot = whyNot(choices, answer) { neverThere(it) },
        )
    }
}
