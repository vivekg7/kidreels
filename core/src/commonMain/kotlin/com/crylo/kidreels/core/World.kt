package com.crylo.kidreels.core

/*
 * Everything the reels teach about the world, as data. The UI never hard-codes a name
 * or a fact: it draws what these lists say. Every emoji is from Emoji 13.1 or earlier,
 * the newest set Android 12 (the minimum supported) can draw.
 */

/** Every flat shape the app can draw. The UI's renderer is an exhaustive `when`. */
enum class ShapeKind { CIRCLE, SQUARE, RECTANGLE, TRIANGLE, OVAL, STAR, HEART, DIAMOND, PENTAGON, HEXAGON, OCTAGON, CRESCENT }

/** [sides] is 0 for curved shapes; the shape quiz only asks about sides when it is > 0. */
data class ShapeInfo(
    val kind: ShapeKind,
    val name: Txt,
    val fact: Txt,
    val sides: Int,
    val examples: List<String>,
    val fromAge: Int,
)

object Shapes {
    val all: List<ShapeInfo> = listOf(
        ShapeInfo(ShapeKind.CIRCLE, txt("Circle", "वृत्त"), txt("Round, with no corners", "गोल, कोई कोना नहीं"), 0, listOf("🍪", "🕐", "🍩"), 2),
        ShapeInfo(ShapeKind.SQUARE, txt("Square", "वर्ग"), txt("4 equal sides, 4 corners", "4 बराबर भुजाएँ, 4 कोने"), 4, listOf("🪟", "🧇"), 2),
        ShapeInfo(ShapeKind.TRIANGLE, txt("Triangle", "त्रिभुज"), txt("3 sides, 3 corners", "3 भुजाएँ, 3 कोने"), 3, listOf("🍕", "⛺", "⚠️"), 2),
        ShapeInfo(ShapeKind.RECTANGLE, txt("Rectangle", "आयत"), txt("4 sides, 4 corners", "4 भुजाएँ, 4 कोने"), 4, listOf("🚪", "📱", "📺"), 3),
        ShapeInfo(ShapeKind.STAR, txt("Star", "तारा"), txt("5 points", "5 नोकें"), 0, listOf("🌟", "🎄"), 3),
        ShapeInfo(ShapeKind.HEART, txt("Heart", "दिल"), txt("The shape of love", "प्यार का आकार"), 0, listOf("💌", "🍓"), 3),
        ShapeInfo(ShapeKind.OVAL, txt("Oval", "अंडाकार"), txt("Like a stretched circle", "खिंचे हुए वृत्त जैसा"), 0, listOf("🥚", "🏉"), 4),
        ShapeInfo(ShapeKind.DIAMOND, txt("Diamond", "हीरा"), txt("4 equal sides, standing on a corner", "4 बराबर भुजाएँ, कोने पर खड़ा"), 4, listOf("💎", "🪁"), 4),
        ShapeInfo(ShapeKind.PENTAGON, txt("Pentagon", "पंचभुज"), txt("5 sides, 5 corners", "5 भुजाएँ, 5 कोने"), 5, listOf("🏠"), 5),
        ShapeInfo(ShapeKind.HEXAGON, txt("Hexagon", "षट्भुज"), txt("6 sides, 6 corners", "6 भुजाएँ, 6 कोने"), 6, listOf("🐝", "🔩"), 5),
        ShapeInfo(ShapeKind.CRESCENT, txt("Crescent", "अर्धचंद्र"), txt("Like the moon on some nights", "कुछ रातों के चाँद जैसा"), 0, listOf("🌙", "🥐", "🍌"), 5),
        ShapeInfo(ShapeKind.OCTAGON, txt("Octagon", "अष्टभुज"), txt("8 sides, 8 corners", "8 भुजाएँ, 8 कोने"), 8, listOf("🛑"), 6),
    )

    fun of(kind: ShapeKind): ShapeInfo = all.first { it.kind == kind }

    fun upTo(age: Int): List<ShapeInfo> = all.filter { it.fromAge <= age }
}

enum class SolidKind { CUBE, CUBOID, SPHERE, CYLINDER, CONE, PYRAMID }

data class SolidInfo(
    val kind: SolidKind,
    val name: Txt,
    val fact: Txt,
    val examples: List<String>,
    val fromAge: Int,
)

object Solids {
    val all: List<SolidInfo> = listOf(
        SolidInfo(SolidKind.CUBE, txt("Cube", "घन"), txt("6 square faces, 12 edges, 8 corners", "6 वर्गाकार फलक, 12 किनारे, 8 कोने"), listOf("🎲", "🧊"), 5),
        SolidInfo(SolidKind.SPHERE, txt("Sphere", "गोला"), txt("Round all over, with no edges", "चारों ओर से गोल, कोई किनारा नहीं"), listOf("⚽", "🌍", "🏀"), 5),
        SolidInfo(SolidKind.CYLINDER, txt("Cylinder", "बेलन"), txt("2 flat circles and 1 curved side", "2 सपाट वृत्त और 1 घुमावदार सतह"), listOf("🥫", "🔋"), 5),
        SolidInfo(SolidKind.CONE, txt("Cone", "शंकु"), txt("1 flat circle and a pointy top", "1 सपाट वृत्त और एक नुकीली चोटी"), listOf("🍦", "🎉"), 5),
        SolidInfo(SolidKind.CUBOID, txt("Cuboid", "घनाभ"), txt("6 rectangle faces, 12 edges, 8 corners", "6 आयताकार फलक, 12 किनारे, 8 कोने"), listOf("📦", "🧱"), 6),
        SolidInfo(SolidKind.PYRAMID, txt("Pyramid", "पिरामिड"), txt("A square base and 4 triangle faces", "वर्गाकार आधार और 4 त्रिभुजाकार फलक"), emptyList(), 6),
    )

    fun upTo(age: Int): List<SolidInfo> = all.filter { it.fromAge <= age }
}

enum class AngleKind(val title: Txt, val rule: Txt) {
    ACUTE(txt("Acute angle", "न्यून कोण"), txt("Less than 90°", "90° से कम")),
    RIGHT(txt("Right angle", "समकोण"), txt("Exactly 90°", "ठीक 90°")),
    OBTUSE(txt("Obtuse angle", "अधिक कोण"), txt("Between 90° and 180°", "90° और 180° के बीच")),
    STRAIGHT(txt("Straight angle", "ऋजु कोण"), txt("Exactly 180°", "ठीक 180°")),
    REFLEX(txt("Reflex angle", "प्रतिवर्ती कोण"), txt("More than 180°", "180° से ज़्यादा"));

    companion object {
        /** Right and straight get a few degrees of slack: a finger is not a protractor. */
        const val SLACK = 3

        fun of(degrees: Int): AngleKind = when {
            degrees in 90 - SLACK..90 + SLACK -> RIGHT
            degrees < 90 -> ACUTE
            degrees in 180 - SLACK..180 + SLACK -> STRAIGHT
            degrees < 180 -> OBTUSE
            else -> REFLEX
        }

        /** The degrees to show: snapped to 90 or 180 inside the slack, so they agree with the name. */
        fun snap(degrees: Int): Int = when (of(degrees)) {
            RIGHT -> 90
            STRAIGHT -> 180
            else -> degrees
        }
    }
}

enum class CurveKind { CIRCLE, ELLIPSE, PARABOLA, HYPERBOLA }

data class CurveInfo(
    val kind: CurveKind,
    val name: Txt,
    val fact: Txt,
    val equation: String,
    val example: String,
    val fromAge: Int,
)

object Curves {
    val all: List<CurveInfo> = listOf(
        CurveInfo(CurveKind.CIRCLE, txt("Circle", "वृत्त"), txt("Every point is the same distance from the centre", "हर बिंदु केंद्र से बराबर दूरी पर है"), "x² + y² = r²", "🎡", 8),
        CurveInfo(CurveKind.ELLIPSE, txt("Ellipse", "दीर्घवृत्त"), txt("Planets go around the Sun in ellipses", "ग्रह सूरज के चारों ओर दीर्घवृत्त में घूमते हैं"), "x²/a² + y²/b² = 1", "🪐", 8),
        CurveInfo(CurveKind.PARABOLA, txt("Parabola", "परवलय"), txt("A thrown ball flies along a parabola", "फेंकी गई गेंद परवलय में उड़ती है"), "y = ax²", "🏀", 9),
        CurveInfo(CurveKind.HYPERBOLA, txt("Hyperbola", "अतिपरवलय"), txt("A lamp's light on a wall makes this curve", "दीवार पर लैंप की रोशनी यह वक्र बनाती है"), "x²/a² − y²/b² = 1", "💡", 10),
    )

    fun upTo(age: Int): List<CurveInfo> = all.filter { it.fromAge <= age }
}

data class Colour(val name: Txt, val argb: Long)

object Colours {
    /** Saturated, unambiguous shades: a child should not wonder whether it is pink or red. */
    val all: List<Colour> = listOf(
        Colour(txt("red", "लाल"), 0xFFE53935),
        Colour(txt("blue", "नीला"), 0xFF1E88E5),
        Colour(txt("yellow", "पीला"), 0xFFFDD835),
        Colour(txt("green", "हरा"), 0xFF43A047),
        Colour(txt("orange", "नारंगी"), 0xFFFB8C00),
        Colour(txt("purple", "बैंगनी"), 0xFF8E24AA),
        Colour(txt("pink", "गुलाबी"), 0xFFF06292),
        Colour(txt("brown", "भूरा"), 0xFF795548),
        Colour(txt("black", "काला"), 0xFF212121),
        Colour(txt("white", "सफ़ेद"), 0xFFFFFFFF),
    )

    /** The first four, for two- and three-year-olds. */
    val basic: List<Colour> = all.take(4)

    /** Bright fills for drawn shapes and solids: none so dark it vanishes on a dark skin. */
    val bright: List<Long> = listOf(0xFFE53935, 0xFF1E88E5, 0xFFFDD835, 0xFF43A047, 0xFFFB8C00, 0xFF8E24AA, 0xFFF06292, 0xFF00ACC1)
}

/** [reveal] is the full sentence because Hindi verbs follow the animal's gender. */
data class Animal(val emoji: String, val sound: Txt, val reveal: Txt)

object AnimalSounds {
    val all: List<Animal> = listOf(
        Animal("🐄", txt("Moo", "मूँ मूँ"), txt("The cow says moo!", "गाय बोलती है मूँ मूँ!")),
        Animal("🐶", txt("Woof woof", "भौं भौं"), txt("The dog says woof woof!", "कुत्ता बोलता है भौं भौं!")),
        Animal("🐱", txt("Meow", "म्याऊँ"), txt("The cat says meow!", "बिल्ली बोलती है म्याऊँ!")),
        Animal("🦆", txt("Quack quack", "क्वैक क्वैक"), txt("The duck says quack quack!", "बतख बोलती है क्वैक क्वैक!")),
        Animal("🐓", txt("Cock-a-doodle-doo", "कुकड़ूँ कूँ"), txt("The rooster says cock-a-doodle-doo!", "मुर्गा बोलता है कुकड़ूँ कूँ!")),
        Animal("🦁", txt("Roar", "दहाड़"), txt("The lion goes roar!", "शेर दहाड़ता है!")),
        Animal("🐑", txt("Baa baa", "में में"), txt("The sheep says baa baa!", "भेड़ बोलती है में में!")),
        Animal("🐍", txt("Hiss", "फुस्स"), txt("The snake says hiss!", "साँप करता है फुस्स!")),
        Animal("🐝", txt("Buzz", "भिन भिन"), txt("The bee says buzz!", "मधुमक्खी करती है भिन भिन!")),
        Animal("🐸", txt("Ribbit", "टर्र टर्र"), txt("The frog says ribbit!", "मेंढक बोलता है टर्र टर्र!")),
        Animal("🐦", txt("Tweet tweet", "चीं चीं"), txt("The bird says tweet tweet!", "चिड़िया बोलती है चीं चीं!")),
        Animal("🐴", txt("Neigh", "हिन हिन"), txt("The horse says neigh!", "घोड़ा बोलता है हिन हिन!")),
    )
}

/** Labelled, because a three-year-old may not know that 🌾 means a farm. */
enum class Home(val emoji: String, val label: Txt) {
    WATER("🌊", txt("Water", "पानी")),
    TREE("🌳", txt("Tree", "पेड़")),
    DESERT("🏜️", txt("Desert", "रेगिस्तान")),
    ICE("🧊", txt("Ice", "बर्फ़")),
    JUNGLE("🌴", txt("Jungle", "जंगल")),
    FARM("🌾", txt("Farm", "खेत")),
    HOUSE("🏠", txt("House", "घर")),
}

/** [question] and [reveal] are whole sentences: the postposition depends on the home (पेड़ पर, पानी में). */
data class Dweller(val emoji: String, val home: Home, val question: Txt, val reveal: Txt)

object Habitats {
    val all: List<Dweller> = listOf(
        Dweller("🐟", Home.WATER, txt("Where does the fish live?", "मछली कहाँ रहती है?"), txt("The fish lives in water!", "मछली पानी में रहती है!")),
        Dweller("🐬", Home.WATER, txt("Where does the dolphin live?", "डॉल्फ़िन कहाँ रहती है?"), txt("The dolphin lives in the sea!", "डॉल्फ़िन समुद्र में रहती है!")),
        Dweller("🐫", Home.DESERT, txt("Where does the camel live?", "ऊँट कहाँ रहता है?"), txt("The camel lives in the desert!", "ऊँट रेगिस्तान में रहता है!")),
        Dweller("🐧", Home.ICE, txt("Where does the penguin live?", "पेंगुइन कहाँ रहता है?"), txt("The penguin lives on the ice!", "पेंगुइन बर्फ़ पर रहता है!")),
        Dweller("🐻‍❄️", Home.ICE, txt("Where does the polar bear live?", "ध्रुवीय भालू कहाँ रहता है?"), txt("The polar bear lives on the ice!", "ध्रुवीय भालू बर्फ़ पर रहता है!")),
        Dweller("🦁", Home.JUNGLE, txt("Where does the lion live?", "शेर कहाँ रहता है?"), txt("The lion lives in the jungle!", "शेर जंगल में रहता है!")),
        Dweller("🐯", Home.JUNGLE, txt("Where does the tiger live?", "बाघ कहाँ रहता है?"), txt("The tiger lives in the jungle!", "बाघ जंगल में रहता है!")),
        Dweller("🐒", Home.TREE, txt("Where does the monkey live?", "बंदर कहाँ रहता है?"), txt("The monkey lives in trees!", "बंदर पेड़ों पर रहता है!")),
        Dweller("🦉", Home.TREE, txt("Where does the owl live?", "उल्लू कहाँ रहता है?"), txt("The owl lives in a tree!", "उल्लू पेड़ पर रहता है!")),
        Dweller("🐄", Home.FARM, txt("Where does the cow live?", "गाय कहाँ रहती है?"), txt("The cow lives on a farm!", "गाय खेत में रहती है!")),
        Dweller("🐐", Home.FARM, txt("Where does the goat live?", "बकरी कहाँ रहती है?"), txt("The goat lives on a farm!", "बकरी खेत में रहती है!")),
        Dweller("🐶", Home.HOUSE, txt("Where does the pet dog live?", "पालतू कुत्ता कहाँ रहता है?"), txt("The pet dog lives in our house!", "पालतू कुत्ता हमारे घर में रहता है!")),
        Dweller("🐱", Home.HOUSE, txt("Where does the pet cat live?", "पालतू बिल्ली कहाँ रहती है?"), txt("The pet cat lives in our house!", "पालतू बिल्ली हमारे घर में रहती है!")),
    )
}

/**
 * Groups for "odd one out". [near] names groups that look alike, which make the hard
 * version of the puzzle: picking the vegetable among fruits is harder than the car.
 */
data class Category(val key: String, val name: Txt, val items: List<String>, val near: Set<String> = emptySet())

object Categories {
    val all: List<Category> = listOf(
        Category("fruit", txt("fruits", "फल"), listOf("🍎", "🍌", "🍇", "🍊", "🍉", "🍓", "🍍", "🥭", "🍐", "🍒"), setOf("veg")),
        Category("veg", txt("vegetables", "सब्ज़ियाँ"), listOf("🥕", "🥦", "🌽", "🥔", "🍆", "🧅", "🥒", "🫑"), setOf("fruit")),
        Category("animal", txt("animals", "जानवर"), listOf("🐶", "🐱", "🐭", "🐰", "🦊", "🐻", "🐼", "🐯", "🦁", "🐮", "🐷", "🐵"), setOf("bird", "sea")),
        Category("bird", txt("birds", "पक्षी"), listOf("🐦", "🦜", "🦆", "🦉", "🐧", "🦅", "🦚", "🐔"), setOf("animal", "bug")),
        Category("sea", txt("sea creatures", "समुद्री जीव"), listOf("🐟", "🐠", "🐙", "🦀", "🐬", "🐳", "🦈", "🐡"), setOf("animal")),
        Category("bug", txt("insects", "कीड़े"), listOf("🐝", "🐞", "🦋", "🐜", "🐛", "🦗"), setOf("bird")),
        Category("vehicle", txt("vehicles", "गाड़ियाँ"), listOf("🚗", "🚕", "🚌", "🚓", "🚑", "🚒", "🚜", "🚲", "🚂", "✈️", "🚀", "🚢")),
        Category("clothes", txt("clothes", "कपड़े"), listOf("👕", "👖", "👗", "🧦", "🧢", "👟", "🧤", "🧣")),
        Category("ball", txt("balls", "गेंदें"), listOf("⚽", "🏀", "🏈", "⚾", "🎾", "🏐")),
    )

    fun byKey(key: String): Category = all.first { it.key == key }
}

/** Things that are fun to count and easy to tell apart. */
val Countables: List<String> = listOf("🍎", "🍌", "⭐", "🎈", "🐥", "🚗", "🐟", "🌸", "🍓", "⚽", "🦋", "🐞", "🧁", "🐸")

/** Pattern pieces: each set is clearly different by colour or kind. */
val PatternSets: List<List<String>> = listOf(
    listOf("🔴", "🔵", "🟡", "🟢"),
    listOf("🍎", "🍌", "🍇", "🍊"),
    listOf("🐶", "🐱", "🐰", "🐸"),
    listOf("⭐", "🌙", "☀️", "☁️"),
    listOf("🟥", "🟦", "🟨", "🟩"),
)

object NumberWords {
    private val english = listOf(
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty",
    )
    private val hindi = listOf(
        "शून्य", "एक", "दो", "तीन", "चार", "पाँच", "छह", "सात", "आठ", "नौ", "दस",
        "ग्यारह", "बारह", "तेरह", "चौदह", "पंद्रह", "सोलह", "सत्रह", "अठारह", "उन्नीस", "बीस",
    )

    /** Words for 0 to 20, the range count-along reels use; numerals beyond that. */
    fun of(n: Int): Txt = if (n in english.indices) txt(english[n], hindi[n]) else Txt.same(n.toString())
}
