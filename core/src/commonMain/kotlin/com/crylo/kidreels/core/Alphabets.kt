package com.crylo.kidreels.core

/** What a card shows as its picture: an emoji from the system font, or a drawn shape. */
sealed interface Picture {
    data class Emoji(val text: String) : Picture

    data class Drawn(val shape: ShapeKind) : Picture
}

/**
 * One alphabet card. [like] marks a letter no everyday word starts with (ङ, ञ, ण, अः,
 * क्ष): it then reads "ण, जैसे हिरण" instead of pretending "ण से …" exists.
 * [meaning] is the word in every language, shown under the card for the others.
 */
data class Letter(
    val lang: Lang,
    val glyph: String,
    val word: String,
    val picture: Picture,
    val meaning: Txt,
    val like: Boolean = false,
) {
    val phrase: String
        get() = when (lang) {
            Lang.EN -> "$glyph for $word"
            Lang.HI -> if (like) "$glyph, जैसे $word" else "$glyph से $word"
        }
}

/**
 * The alphabets, in teaching order. Words are picked so the emoji shows exactly that
 * thing (अनानास, not अनार, because there is no pomegranate emoji), and every emoji is
 * from Emoji 13.1 or earlier so Android 12 can draw it. Carried over from Kids Learn,
 * where the same lists were checked letter by letter.
 */
object Alphabets {
    private fun en(glyph: String, word: String, emoji: String, hi: String) =
        Letter(Lang.EN, glyph, word, Picture.Emoji(emoji), txt(word, hi))

    private fun hi(glyph: String, word: String, emoji: String, en: String, like: Boolean = false) =
        Letter(Lang.HI, glyph, word, Picture.Emoji(emoji), txt(en, word), like)

    val english: List<Letter> = listOf(
        en("A", "Apple", "🍎", "सेब"),
        en("B", "Ball", "⚽", "गेंद"),
        en("C", "Cat", "🐱", "बिल्ली"),
        en("D", "Dog", "🐶", "कुत्ता"),
        en("E", "Elephant", "🐘", "हाथी"),
        en("F", "Fish", "🐟", "मछली"),
        en("G", "Grapes", "🍇", "अंगूर"),
        en("H", "House", "🏠", "घर"),
        en("I", "Ice cream", "🍦", "आइसक्रीम"),
        en("J", "Juice", "🧃", "जूस"),
        en("K", "Kite", "🪁", "पतंग"),
        en("L", "Lion", "🦁", "शेर"),
        en("M", "Mango", "🥭", "आम"),
        en("N", "Nose", "👃", "नाक"),
        en("O", "Orange", "🍊", "संतरा"),
        en("P", "Parrot", "🦜", "तोता"),
        en("Q", "Queen", "👸", "रानी"),
        en("R", "Rabbit", "🐰", "खरगोश"),
        en("S", "Sun", "🌞", "सूरज"),
        en("T", "Tiger", "🐯", "बाघ"),
        en("U", "Umbrella", "☔", "छाता"),
        en("V", "Van", "🚐", "वैन"),
        en("W", "Watch", "⌚", "घड़ी"),
        en("X", "Xmas tree", "🎄", "क्रिसमस ट्री"),
        en("Y", "Yo-yo", "🪀", "यो-यो"),
        en("Z", "Zebra", "🦓", "ज़ेबरा"),
    )

    /** स्वर then व्यंजन. */
    val hindi: List<Letter> = listOf(
        hi("अ", "अनानास", "🍍", "Pineapple"),
        hi("आ", "आम", "🥭", "Mango"),
        hi("इ", "इंजन", "🚂", "Engine"),
        hi("ई", "ईंट", "🧱", "Brick"),
        hi("उ", "उल्लू", "🦉", "Owl"),
        hi("ऊ", "ऊँट", "🐫", "Camel"),
        hi("ऋ", "ऋषि", "🧘", "Sage"),
        hi("ए", "एड़ी", "🦶", "Heel"),
        hi("ऐ", "ऐनक", "👓", "Glasses"),
        hi("ओ", "ओस", "💧", "Dew"),
        hi("औ", "औरत", "👩", "Woman"),
        hi("अं", "अंगूर", "🍇", "Grapes"),
        hi("अः", "छः", "6️⃣", "Six", like = true),
        hi("क", "केला", "🍌", "Banana"),
        hi("ख", "खरगोश", "🐰", "Rabbit"),
        hi("ग", "गाय", "🐄", "Cow"),
        hi("घ", "घर", "🏠", "House"),
        hi("ङ", "शङ्ख", "🐚", "Conch", like = true),
        hi("च", "चम्मच", "🥄", "Spoon"),
        hi("छ", "छतरी", "☔", "Umbrella"),
        hi("ज", "जहाज", "🚢", "Ship"),
        hi("झ", "झंडा", "🇮🇳", "Flag"),
        hi("ञ", "पञ्जा", "✋", "Palm", like = true),
        hi("ट", "टमाटर", "🍅", "Tomato"),
        hi("ठ", "ठंड", "🥶", "Cold"),
        hi("ड", "डिब्बा", "📦", "Box"),
        hi("ढ", "ढोल", "🥁", "Drum"),
        hi("ण", "हिरण", "🦌", "Deer", like = true),
        hi("त", "तरबूज", "🍉", "Watermelon"),
        hi("थ", "थैला", "👜", "Bag"),
        hi("द", "दाँत", "🦷", "Tooth"),
        hi("ध", "धनुष", "🏹", "Bow"),
        hi("न", "नल", "🚰", "Tap"),
        hi("प", "पतंग", "🪁", "Kite"),
        hi("फ", "फूल", "🌻", "Flower"),
        hi("ब", "बतख", "🦆", "Duck"),
        hi("भ", "भालू", "🐻", "Bear"),
        hi("म", "मछली", "🐟", "Fish"),
        hi("य", "यान", "🚀", "Rocket"),
        hi("र", "राजा", "🤴", "King"),
        hi("ल", "लड़का", "👦", "Boy"),
        hi("व", "वृक्ष", "🌳", "Tree"),
        hi("श", "शेर", "🦁", "Lion"),
        Letter(Lang.HI, "ष", "षट्कोण", Picture.Drawn(ShapeKind.HEXAGON), txt("Hexagon", "षट्कोण")),
        hi("स", "सेब", "🍎", "Apple"),
        hi("ह", "हाथी", "🐘", "Elephant"),
        hi("क्ष", "कक्षा", "🏫", "Classroom", like = true),
        hi("त्र", "त्रिशूल", "🔱", "Trident"),
        hi("ज्ञ", "ज्ञानी", "🧑‍🏫", "Wise person"),
    )

    fun of(lang: Lang): List<Letter> = when (lang) {
        Lang.EN -> english
        Lang.HI -> hindi
    }
}
