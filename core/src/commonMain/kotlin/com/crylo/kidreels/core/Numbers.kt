package com.crylo.kidreels.core

/**
 * Numbers as the alphabet teaches letters: "1 · O-N-E · one · 1 elephant". Each number
 * keeps its own picture, as each letter does, so a child meets the same elephant at 1
 * every time and the cards become familiar.
 */
object Numbers {
    private fun thing(emoji: String, en: String, enMany: String, hi: String, hiMany: String, feminine: Boolean = false) =
        Thing(emoji, txt(en, hi), txt(enMany, hiMany), feminine)

    /** The picture for each number, 1 to 20: things a child can tell apart at a glance. */
    val things: List<Thing> = listOf(
        thing("🐘", "elephant", "elephants", "हाथी", "हाथी"),
        thing("🚗", "car", "cars", "गाड़ी", "गाड़ियाँ", feminine = true),
        thing("🎈", "balloon", "balloons", "गुब्बारा", "गुब्बारे"),
        thing("🍎", "apple", "apples", "सेब", "सेब"),
        thing("⭐", "star", "stars", "तारा", "तारे"),
        thing("🐟", "fish", "fish", "मछली", "मछलियाँ", feminine = true),
        thing("🦋", "butterfly", "butterflies", "तितली", "तितलियाँ", feminine = true),
        thing("🌸", "flower", "flowers", "फूल", "फूल"),
        thing("⚽", "ball", "balls", "गेंद", "गेंदें", feminine = true),
        thing("🍌", "banana", "bananas", "केला", "केले"),
        thing("🦆", "duck", "ducks", "बतख", "बतखें", feminine = true),
        thing("🥚", "egg", "eggs", "अंडा", "अंडे"),
        thing("🐸", "frog", "frogs", "मेंढक", "मेंढक"),
        thing("🧁", "cupcake", "cupcakes", "केक", "केक"),
        thing("🍪", "biscuit", "biscuits", "बिस्कुट", "बिस्कुट"),
        thing("🍓", "strawberry", "strawberries", "स्ट्रॉबेरी", "स्ट्रॉबेरी", feminine = true),
        thing("🐥", "chick", "chicks", "चूज़ा", "चूज़े"),
        thing("🐞", "ladybird", "ladybirds", "लाल कीड़ा", "लाल कीड़े"),
        thing("🐜", "ant", "ants", "चींटी", "चींटियाँ", feminine = true),
        thing("🐝", "bee", "bees", "मधुमक्खी", "मधुमक्खियाँ", feminine = true),
    )

    val MAX = things.size

    fun isPrime(n: Int): Boolean = n >= 2 && (2..n / 2).none { n % it == 0 }

    /** The primes up to 200, more than any reel needs. */
    val primes: List<Int> = (2..200).filter(::isPrime)

    /** The English word in capitals, letter by letter: "ONE" → O, N, E. */
    fun spelling(n: Int): List<String> = NumberWords.of(n)[Lang.EN].uppercase().map { it.toString() }

    /**
     * How a पहाड़ा is chanted in Hindi: "दो एकम दो, दो दूनी चार, दो तिया छह…". Every
     * Indian child learns the table this way, so the Hindi voice says it so too.
     */
    private val chant = listOf("एकम", "दूनी", "तिया", "चौके", "पंजे", "छक्के", "सत्ते", "अट्ठे", "नवाँ", "दहाई")

    /** One line of the table of [table]: "3 times 4 is 12" / "3 चौके 12". */
    fun tableLine(table: Int, times: Int): Txt =
        txt("$table times $times is ${table * times}", "$table ${chant[times - 1]} ${table * times}")
}

/** "1 · O-N-E · one · 1 elephant": a numeral, its word, and that many things. */
data class NumberCard(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val n: Int,
) : Reel {
    val thing: Thing get() = Numbers.things[n - 1]
    val word: Txt get() = NumberWords.of(n)
    override val topic get() = Topic.NUMBERS
    override val needsSolving get() = false
    override val prompt: Txt? get() = null
    override val voice: Lang? get() = null
}

/** A times table, one line per tap, said the way it is chanted at school. */
data class TableCard(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val table: Int,
) : Reel {
    override val topic get() = Topic.TABLES
    override val needsSolving get() = false
    override val prompt: Txt get() = Phrases.tableOf(table)
    override val voice: Lang? get() = null

    companion object {
        const val LINES = 10
    }
}

/** A square of [n] × [n] tiles, filled one row per tap: 5, 10, 15, 20, 25. */
data class SquareCard(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val n: Int,
    /** "5² = 25" beside "5 × 5 = 25", once a child has met powers. */
    val showPower: Boolean,
) : Reel {
    val square: Int get() = n * n
    override val topic get() = Topic.SQUARES
    override val needsSolving get() = false
    override val prompt: Txt get() = Phrases.squareOf(n)
    override val voice: Lang? get() = null
}

/**
 * The sieve of Eratosthenes on a 1-to-[top] board: tap 2 and its multiples are crossed
 * out, then 3, 5 and 7; what is left are the primes. Each step waits for the child's tap.
 */
data class SieveLab(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val top: Int,
) : Reel {
    /** The primes whose multiples get crossed out: those up to the square root of [top]. */
    val steps: List<Int> get() = Numbers.primes.takeWhile { it * it <= top }
    override val topic get() = Topic.SIEVE
    override val needsSolving get() = false
    override val prompt: Txt get() = NumberKinds.sieveTitle
    override val voice: Lang? get() = null
}

/** Even and odd, and primes: quizzes for what a number is, not what it adds up to. */
object NumberKinds {
    private val even = txt("even", "सम")
    private val odd = txt("odd", "विषम")

    private fun kind(n: Int) = if (n % 2 == 0) even else odd

    /**
     * Up to 7, a number is shown as dots in pairs and the question is even or odd: one
     * left over, or every dot with a partner, is what "odd" and "even" mean. From 8,
     * which of four numbers is even (or odd), told by the last digit.
     */
    fun evenOdd(g: Gen): Reel {
        if (g.level <= 7) {
            val n = g.between(2, if (g.level == 6) 10 else 20)
            val choices = listOf(Choice.Word(txt("Even", "सम")), Choice.Word(txt("Odd", "विषम")))
            val answer = n % 2
            return Quiz(
                g.id, Topic.EVEN_ODD, g.level, g.difficulty, txt("Is $n even or odd?", "$n सम है या विषम?"),
                Stage.Pairs(n), choices, answer,
                reveal = if (n % 2 == 0) txt("$n is even: every dot has a partner!", "$n सम है: हर बिंदु की जोड़ी बन गई!")
                else txt("$n is odd: one dot is left over!", "$n विषम है: एक बिंदु बच गया!"),
                say = txt(
                    "Here are $n dots in pairs. Does every dot have a partner? Is $n even or odd?",
                    "ये $n बिंदु जोड़ियों में हैं। क्या हर बिंदु की जोड़ी बनी? $n सम है या विषम?",
                ),
                whyNot = if (n % 2 == 0) {
                    listOf(null, txt("Every dot has a partner, none is left over. So $n is not odd.", "हर बिंदु की जोड़ी बन गई, कोई नहीं बचा। इसलिए $n विषम नहीं है।"))
                } else {
                    listOf(txt("Look: one dot has no partner. So $n is not even.", "देखो: एक बिंदु की जोड़ी नहीं बनी। इसलिए $n सम नहीं है।"), null)
                },
            )
        }
        val wantEven = g.chance(0.5)
        val (lo, hi) = if (g.level == 8) 10 to 99 else 100 to 999
        fun draw(evenOne: Boolean): Int {
            var n: Int
            do n = g.between(lo, hi) while ((n % 2 == 0) != evenOne)
            return n
        }
        val others = generateSequence { draw(!wantEven) }.distinct().take(g.optionCount - 1).toList()
        val (numbers, answer) = g.shuffleIn(draw(wantEven), others)
        val want = if (wantEven) even else odd
        val target = numbers[answer]
        return Quiz(
            g.id, Topic.EVEN_ODD, g.level, g.difficulty,
            txt("Which number is ${want[Lang.EN]}?", "कौन-सी संख्या ${want[Lang.HI]} है?"), Stage.None,
            numbers.map { Choice.Number(it, sticks = false) }, answer,
            reveal = txt("$target is ${want[Lang.EN]}: it ends in ${target % 10}!", "$target ${want[Lang.HI]} है: इसके आख़िर में ${target % 10} है!"),
            say = txt(
                "Which of these numbers is ${want[Lang.EN]}? Look at the last digit!",
                "इनमें से कौन-सी संख्या ${want[Lang.HI]} है? आख़िरी अंक देखो!",
            ),
            whyNot = whyNot(numbers, answer) {
                txt("$it ends in ${it % 10}, so it is ${kind(it)[Lang.EN]}.", "$it के आख़िर में ${it % 10} है, इसलिए यह ${kind(it)[Lang.HI]} है।")
            },
        )
    }

    /** Odd numbers that are not prime: the ones a child calls prime by mistake. */
    private val traps = (9..99 step 2).filter { !Numbers.isPrime(it) && it % 5 != 0 }

    /** Which of four is prime? The wrong ones get easier to rule out at the easy end (even, or ending in 5). */
    fun prime(g: Gen): Reel {
        val top = when (g.level) { 10 -> 30; 11 -> 60; else -> 100 }
        val answerN = g.pick(Numbers.primes.filter { it <= top })
        val pool = when {
            g.difficulty >= 4 -> traps.filter { it <= top } + listOf(1)
            g.difficulty >= 3 -> (1..top).filter { !Numbers.isPrime(it) && it % 2 == 1 }
            else -> (4..top).filter { !Numbers.isPrime(it) }
        }
        val others = pool.shuffled(g.random).take(g.optionCount - 1)
        val (numbers, answer) = g.shuffleIn(answerN, others)
        return Quiz(
            g.id, Topic.PRIMES, g.level, g.difficulty, txt("Which is a prime number?", "अभाज्य संख्या कौन-सी है?"), Stage.None,
            numbers.map { Choice.Number(it, sticks = false) }, answer,
            reveal = txt("$answerN is prime: only 1 and $answerN divide it exactly!", "$answerN अभाज्य है: इसे सिर्फ़ 1 और $answerN से ही पूरा भाग दिया जा सकता है!"),
            say = txt(
                "Which of these is a prime number? A prime can only be divided by 1 and by itself.",
                "इनमें से अभाज्य संख्या कौन-सी है? अभाज्य संख्या को सिर्फ़ 1 से और ख़ुद से ही भाग दिया जा सकता है।",
            ),
            whyNot = whyNot(numbers, answer) { n ->
                if (n == 1) {
                    txt("1 is not prime: a prime has exactly two divisors, 1 and itself.", "1 अभाज्य नहीं है: अभाज्य संख्या के ठीक दो भाजक होते हैं, 1 और वह ख़ुद।")
                } else {
                    val d = (2..n).first { n % it == 0 }
                    txt("$n is $d × ${n / d}, so it is not prime.", "$n = $d × ${n / d}, इसलिए यह अभाज्य नहीं है।")
                }
            },
        )
    }

    val sieveTitle = txt("Find the primes", "अभाज्य संख्याएँ ढूँढो")

    fun sieveSay(top: Int) = txt(
        "Let's find every prime number up to $top! 1 is not prime, so it is crossed out. Now tap 2.",
        "चलो $top तक की सारी अभाज्य संख्याएँ ढूँढें! 1 अभाज्य नहीं है, इसलिए वह कट गया। अब 2 को छुओ।",
    )

    /** Said as [p]'s multiples are crossed out, then which number to tap next. */
    fun sieveStep(p: Int, next: Int?) = txt(
        "$p is prime! Every number $p goes into is not: cross them out." + (next?.let { " Now tap $it." } ?: ""),
        "$p अभाज्य है! $p के सारे गुणज अभाज्य नहीं हैं: उन्हें काट दो।" + (next?.let { " अब $it को छुओ।" } ?: ""),
    )

    fun sieveDone(top: Int, count: Int) = txt(
        "Every number left is prime! There are $count primes up to $top.",
        "जो बचे, वे सब अभाज्य हैं! $top तक $count अभाज्य संख्याएँ हैं।",
    )

    fun sieveCount(top: Int, count: Int) = txt("$count primes up to $top", "$top तक $count अभाज्य संख्याएँ")

    fun sieveTapNext(p: Int) = txt("Tap $p next!", "अब $p को छुओ!")
}
