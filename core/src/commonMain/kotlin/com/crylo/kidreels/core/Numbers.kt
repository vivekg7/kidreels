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
