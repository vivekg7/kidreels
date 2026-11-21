package com.crylo.kidreels.core

/** The "channel" a reel appears to come from in the familiar-looking skins. */
enum class Channel(val handle: Txt, val emoji: String) {
    MATHS(txt("Maths", "गणित"), "🔢"),
    LETTERS(txt("Letters", "अक्षर"), "🔤"),
    SHAPES(txt("Shapes", "आकार"), "🔷"),
    PUZZLES(txt("Puzzles", "पहेली"), "🧩"),
    WORLD(txt("Our world", "हमारी दुनिया"), "🌍"),
    THINKING(txt("Thinking", "सोचो"), "🧠"),
    MEMORY(txt("Memory", "याददाश्त"), "💭"),
    WORDS(txt("Words", "शब्द"), "📝"),
    SCIENCE(txt("Science", "विज्ञान"), "🔬"),
}

enum class Topic(val channel: Channel, val title: Txt) {
    COUNT_ALONG(Channel.MATHS, txt("Counting", "गिनती")),
    COUNTING(Channel.MATHS, txt("How many", "कितने")),
    NUMBERS(Channel.MATHS, txt("Numbers", "संख्याएँ")),
    ADDITION(Channel.MATHS, txt("Adding", "जोड़")),
    SUBTRACTION(Channel.MATHS, txt("Taking away", "घटाना")),
    MULTIPLICATION(Channel.MATHS, txt("Multiplying", "गुणा")),
    TABLES(Channel.MATHS, txt("Times tables", "पहाड़े")),
    SQUARES(Channel.MATHS, txt("Square numbers", "वर्ग संख्याएँ")),
    EVEN_ODD(Channel.MATHS, txt("Even and odd", "सम और विषम")),
    PRIMES(Channel.MATHS, txt("Prime numbers", "अभाज्य संख्याएँ")),
    SIEVE(Channel.MATHS, txt("Find the primes", "अभाज्य संख्याएँ ढूँढो")),
    COMPARE(Channel.MATHS, txt("More or less", "कम या ज़्यादा")),
    SEQUENCE(Channel.MATHS, txt("Number patterns", "संख्या क्रम")),
    CLOCK(Channel.MATHS, txt("Telling time", "घड़ी")),
    BASKET(Channel.MATHS, txt("Fill the basket", "टोकरी भरो")),
    LETTERS(Channel.LETTERS, txt("Alphabet", "वर्णमाला")),
    FIRST_LETTER(Channel.LETTERS, txt("First letter", "पहला अक्षर")),
    SHAPES(Channel.SHAPES, txt("Shapes", "आकार")),
    SHAPE_QUIZ(Channel.SHAPES, txt("Find the shape", "आकार ढूँढो")),
    SOLIDS(Channel.SHAPES, txt("3D shapes", "ठोस आकार")),
    ANGLES(Channel.SHAPES, txt("Angles", "कोण")),
    CURVES(Channel.SHAPES, txt("Curves", "वक्र")),
    SHAPE_SORTER(Channel.SHAPES, txt("Shape sorter", "आकार की जगह")),
    PATTERN(Channel.PUZZLES, txt("Patterns", "पैटर्न")),
    ODD_ONE_OUT(Channel.PUZZLES, txt("Odd one out", "अलग कौन")),
    SUDOKU(Channel.PUZZLES, txt("Sudoku", "सुडोकू")),
    MISSING_PIECE(Channel.PUZZLES, txt("Missing piece", "छूटा टुकड़ा")),
    SORTING(Channel.PUZZLES, txt("Sort it out", "छाँटो")),
    MAZE(Channel.PUZZLES, txt("Mazes", "भूलभुलैया")),
    SIZES(Channel.WORLD, txt("Big and small", "बड़ा और छोटा")),
    COLOURS(Channel.WORLD, txt("Colours", "रंग")),
    ANIMAL_SOUNDS(Channel.WORLD, txt("Animal sounds", "जानवरों की आवाज़")),
    HABITATS(Channel.WORLD, txt("Animal homes", "जानवरों के घर")),
    FEEDING(Channel.WORLD, txt("Feed the animals", "जानवरों को खिलाओ")),
    ANIMALS(Channel.WORLD, txt("Animals", "जानवर")),
    BIRDS(Channel.WORLD, txt("Birds", "चिड़ियाँ")),
    VEHICLES(Channel.WORLD, txt("Vehicles", "गाड़ियाँ")),
    BODY(Channel.WORLD, txt("My body", "मेरा शरीर")),
    BODY_PARTS(Channel.WORLD, txt("Parts of the body", "शरीर के अंग")),
    BODY_TAP(Channel.WORLD, txt("Find it on the body", "शरीर पर ढूँढो")),
    FRUITS(Channel.WORLD, txt("Fruits", "फल")),
    VEGETABLES(Channel.WORLD, txt("Vegetables", "सब्ज़ियाँ")),
    HOME_THINGS(Channel.WORLD, txt("At home", "घर की चीज़ें")),
    CLOTHES(Channel.WORLD, txt("Clothes", "कपड़े")),
    COMPUTER(Channel.WORLD, txt("Computer parts", "कंप्यूटर के हिस्से")),
    COMPUTER_USE(Channel.WORLD, txt("What is it for?", "किस काम आता है?")),
    SAME_FIGURE(Channel.THINKING, txt("Same figure", "एक जैसी आकृति")),
    ODD_FIGURE(Channel.THINKING, txt("Odd figure out", "अलग आकृति")),
    COMPLETE_PATTERN(Channel.THINKING, txt("Complete the pattern", "पैटर्न पूरा करो")),
    FIGURE_SERIES(Channel.THINKING, txt("Figure series", "आकृति शृंखला")),
    MIRROR(Channel.THINKING, txt("Mirror image", "दर्पण प्रतिबिंब")),
    ANALOGY(Channel.THINKING, txt("Analogy", "सादृश्यता")),
    FIT_PIECE(Channel.THINKING, txt("Complete the square", "वर्ग पूरा करो")),
    HIDDEN_FIGURE(Channel.THINKING, txt("Hidden figure", "छिपी आकृति")),
    PAPER_FOLD(Channel.THINKING, txt("Fold and punch", "मोड़ो और छेद करो")),
    CUPS(Channel.MEMORY, txt("Cup shuffle", "कप का खेल")),
    PAIRS(Channel.MEMORY, txt("Find the pairs", "जोड़ी ढूँढो")),
    COPY_ORDER(Channel.MEMORY, txt("Copy the order", "क्रम दोहराओ")),
    WHATS_MISSING(Channel.MEMORY, txt("What's missing?", "क्या ग़ायब है?")),
    OPPOSITES(Channel.WORDS, txt("Opposites", "विलोम शब्द")),
    PLURALS(Channel.WORDS, txt("One and many", "एक और अनेक")),
    GENDER(Channel.WORDS, txt("Male and female", "पुल्लिंग और स्त्रीलिंग")),
    RHYMES(Channel.WORDS, txt("Rhyming words", "तुकबंदी (अंग्रेज़ी)")),
    WORD_CARDS(Channel.WORDS, txt("Words in a sentence", "वाक्य में शब्द")),
    WORD_KINDS(Channel.WORDS, txt("Naming, doing, describing", "संज्ञा, क्रिया, विशेषण")),
    MATRAS(Channel.WORDS, txt("Hindi vowel signs", "मात्राएँ")),
    PLANT_PARTS(Channel.SCIENCE, txt("Parts of a plant", "पौधे के भाग")),
    PLANT_TAP(Channel.SCIENCE, txt("Find it on the plant", "पौधे पर ढूँढो")),
    PHOTOSYNTHESIS(Channel.SCIENCE, txt("How plants make food", "पौधे खाना कैसे बनाते हैं")),
    WATER_CYCLE(Channel.SCIENCE, txt("The water cycle", "जल चक्र")),
    FLOAT_SINK(Channel.SCIENCE, txt("Float or sink", "तैरेगा या डूबेगा")),
    MAGNETS(Channel.SCIENCE, txt("Magnets", "चुंबक")),
    STATES(Channel.SCIENCE, txt("Solid, liquid, gas", "ठोस, द्रव, गैस")),
    ALIVE(Channel.SCIENCE, txt("Living or not", "सजीव या निर्जीव")),
    PLANETS(Channel.SCIENCE, txt("The solar system", "सौर मंडल")),
    BREAK(Channel.WORLD, txt("Break time", "आराम का समय")),
}

/**
 * One screen of the feed. Reels are plain data made by the generators in `gen/`; the UI
 * decides how each kind looks, and the skin decides the frame around it.
 */
sealed interface Reel {
    val id: Long
    val topic: Topic

    /** The age this reel was made for, 2 to 12. Not always the child's age: see Feed. */
    val level: Int

    /** 1 (easiest) to 5 (hardest) among reels of the same [level]. */
    val difficulty: Int

    /** False for reels that are explored rather than answered, like "A for Apple". */
    val needsSolving: Boolean

    /** The headline, shown in every chosen language. */
    val prompt: Txt?

    /**
     * Set when the reel is about one language (its alphabet): it is then spoken in that
     * language whatever the main language is. Null means "speak the main language".
     */
    val voice: Lang?
}

/** Something to answer by tapping one of [choices]. */
data class Quiz(
    override val id: Long,
    override val topic: Topic,
    override val level: Int,
    override val difficulty: Int,
    override val prompt: Txt,
    val stage: Stage,
    val choices: List<Choice>,
    val answer: Int,
    /** Shown and spoken after the right answer, e.g. "2 plus 3 is 5!". */
    val reveal: Txt? = null,
    /**
     * Spoken on arrival: the [prompt] said in full, naming what is on screen and what to
     * do ("How many butterflies are there on the phone? Count them and tell me!"). The
     * screen keeps the short [prompt]: the youngest cannot read it anyway.
     */
    val say: Txt? = null,
    override val voice: Lang? = null,
    /**
     * Spoken in place of [reveal] when it needs gaps inside it: "S" … "for" … "Sun", as on
     * the alphabet card.
     */
    val revealSaid: List<Say>? = null,
    /**
     * For each choice, why it is not the answer, said when it is tapped: "That is green,
     * not blue. Find blue!". Null at [answer]; empty for a quiz with nothing to explain.
     */
    val whyNot: List<Txt?> = emptyList(),
    /**
     * The one thing the right answer names ("red", "knee", "Ball"), said again after the
     * reveal in the child's other languages: "red means" … "लाल". Null where the answer
     * is a number, a figure or a rule. See [Bridge].
     */
    val bridge: Txt? = null,
) : Reel {
    override val needsSolving: Boolean get() = true

    init {
        require(answer in choices.indices) { "Answer $answer out of ${choices.size} choices" }
        require(whyNot.isEmpty() || whyNot.size == choices.size) { "whyNot for ${whyNot.size} of ${choices.size} choices in $topic" }
        require(choices.toSet().size == choices.size) { "Duplicate choices in $topic: $choices" }
    }
}

/** The picture a quiz asks about. */
sealed interface Stage {
    data object None : Stage

    /** "3 + 4": with sticks (or dots for ×) under the numbers when [aids] is set. */
    data class Sum(val a: Int, val op: Op, val b: Int, val aids: Boolean) : Stage

    data class Count(val emoji: String, val n: Int, val scattered: Boolean) : Stage

    /** [name] is said when the picture is tapped; null means its usual one ([Words.of]). */
    data class Picture(val emoji: String, val name: Word? = null) : Stage

    /** A row of tokens with one missing, shown as "?" at [blank]. */
    data class Row(val tokens: List<String>, val blank: Int) : Stage

    data class Clock(val hour: Int, val minute: Int) : Stage

    /** A sudoku with one empty cell; [cells] index [symbols] (0-based), row by row. */
    data class Grid(
        val size: Int,
        val boxRows: Int,
        val boxCols: Int,
        val cells: List<Int>,
        val blank: Int,
        val symbols: List<String>,
        val hint: Boolean,
    ) : Stage

    data class Figures(val figures: List<Figure?>, val layout: Layout) : Stage

    /** A square board of [board] × [board] with [piece] cut out of it. */
    data class Cutout(val board: Int, val piece: Polygon) : Stage

    /** Busy [lines] on a [size] × [size] lattice; [answer] is where the hidden shape is, shown once solved. */
    data class Hidden(val lines: Lines, val size: Int, val answer: Lines) : Stage

    /**
     * "What is missing?": [items] are shown for [lookMillis], then again without the one at
     * [missing], with a "?" in its place, or in a new order when [reshuffle] is set.
     */
    data class Memory(val items: List<String>, val missing: Int, val lookMillis: Long, val reshuffle: Boolean) : Stage

    /** A tank of water: once answered, the picture drops in and floats or sinks. */
    data class Tank(val emoji: String, val floats: Boolean) : Stage

    /** A magnet over a picture: once answered, the picture jumps to it, or stays put. */
    data class Magnet(val emoji: String, val sticks: Boolean) : Stage

    /** [n] dots in pairs, for "even or odd?": an odd number leaves one without a partner. */
    data class Pairs(val n: Int) : Stage

    /** A square of paper folded in half (left over right, or top over bottom; [twice]: both), with [holes] punched. */
    data class Fold(val vertical: Boolean, val twice: Boolean, val holes: Set<Hole>) : Stage
}

/** How the figures of a reasoning puzzle are laid out; a null figure is the "?". */
enum class Layout {
    /** One figure: "which is the same?". */
    ONE,

    /** One figure with a mirror stood on its right. */
    MIRROR,

    /** A series, left to right. */
    ROW,

    /** Four quarters of one square pattern, row by row. */
    GRID,

    /** A → B, C → ?. */
    ANALOGY,
}

enum class Op(val sign: String) { ADD("+"), SUB("−"), MUL("×") }

sealed interface Choice {
    data class Number(val n: Int, val sticks: Boolean) : Choice

    /** An emoji, a letter or a numeral, with an optional caption under it. */
    data class Token(val text: String, val label: Txt? = null) : Choice

    /** [n] copies of [emoji], for "which has more?". */
    data class Group(val emoji: String, val n: Int) : Choice

    data class Shape(val kind: ShapeKind, val argb: Long) : Choice

    data class Colour(val argb: Long) : Choice

    /** The same emoji drawn at [scale] (1 = normal), for "which one is big?". */
    data class Sized(val emoji: String, val scale: Float) : Choice

    data class Time(val hour: Int, val minute: Int) : Choice

    /** [copy] tells apart choices that show the same figure ("which one is different?"). */
    data class Fig(val figure: Figure, val copy: Int = 0) : Choice

    data class Cut(val piece: Polygon) : Choice

    data class Shapes(val lines: Lines) : Choice

    /** A word, shown in every chosen language: "Even" / "सम". */
    data class Word(val text: Txt) : Choice

    /** An unfolded square of paper with these holes. */
    data class Holes(val holes: Set<Hole>) : Choice
}

/** Count along: tap each thing and hear the running count. Nothing to get wrong. */
data class CountAlong(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val thing: Thing,
    val n: Int,
) : Reel {
    val emoji: String get() = thing.emoji
    override val topic get() = Topic.COUNT_ALONG
    override val needsSolving get() = false
    override val prompt get() = Phrases.letsCount(thing)
    override val voice: Lang? get() = null
}

/** "A for Apple": a letter, a word that starts with it, and a picture. */
data class LetterCard(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val lang: Lang,
    val letter: Letter,
) : Reel {
    override val topic get() = Topic.LETTERS
    override val needsSolving get() = false
    override val prompt: Txt? get() = null
    override val voice: Lang? get() = lang
}

data class ShapeCard(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val shape: ShapeInfo,
    val argb: Long,
) : Reel {
    override val topic get() = Topic.SHAPES
    override val needsSolving get() = false
    override val prompt: Txt get() = shape.name
    override val voice: Lang? get() = null
}

/** A 3D solid the child can spin with a finger. */
data class SolidCard(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val solid: SolidInfo,
    val argb: Long,
    val showFact: Boolean,
) : Reel {
    override val topic get() = Topic.SOLIDS
    override val needsSolving get() = false
    override val prompt: Txt get() = solid.name
    override val voice: Lang? get() = null
}

/**
 * Drag an arm to open an angle and see its name. With a [target] it is a challenge
 * ("Make a right angle!") and counts as solved when released inside it.
 */
data class AngleLab(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val startDegrees: Int,
    val target: AngleKind?,
) : Reel {
    override val topic get() = Topic.ANGLES
    override val needsSolving get() = target != null
    override val prompt: Txt
        get() = target?.let { Phrases.makeAngle(it) } ?: Phrases.dragAngle
    override val voice: Lang? get() = null
}

/** A curve that draws itself, with a slider to stretch it. */
data class CurveLab(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val curve: CurveInfo,
    val showEquation: Boolean,
) : Reel {
    override val topic get() = Topic.CURVES
    override val needsSolving get() = false
    override val prompt: Txt get() = curve.name
    override val voice: Lang? get() = null
}

/**
 * "This is an elephant": a picture and its name, nothing to answer. From age 4 it adds a
 * [Word.fact] ("The elephant has a long trunk").
 */
data class WordCard(
    override val id: Long,
    override val topic: Topic,
    override val level: Int,
    override val difficulty: Int,
    val word: Word,
    val showFact: Boolean,
) : Reel {
    override val needsSolving get() = false
    override val prompt: Txt get() = word.title
    override val voice: Lang? get() = null
}

/** The last reel of the day: the parent's time limit is used up, or it is [bedtime]. */
data class TimeUp(override val id: Long, val bedtime: Boolean = false) : Reel {
    override val topic get() = Topic.BREAK
    override val level get() = 2
    override val difficulty get() = 1
    override val needsSolving get() = false
    override val prompt get() = if (bedtime) Phrases.bedtime else Phrases.allDone
    override val voice: Lang? get() = null
}

/**
 * What the voice says when a reel comes on screen, in the right voice: what is on the
 * screen and what to do with it, the way a grown-up beside the child would say it. The
 * speaker button says the same again.
 *
 * [langs] are the child's languages, main first. A reel that names one thing says it once
 * more in each of the others, in that language's voice ("A for Apple" · "Apple means" …
 * "सेब"), before "say it with me" where there is one: see [Bridge]. With one language
 * there is nothing to bridge, and the reel says what it always did.
 */
fun Reel.intro(langs: List<Lang>): List<Say> {
    val lang = voice ?: langs.first()
    fun line(t: Txt, tone: Tone = Tone.TALK) = Say(t[lang], lang, tone)
    fun bridge(word: Txt) = Bridge.lines(word, lang, langs)

    /** "This is a triangle." · the bridge · the rest; one line, as before, when there is no bridge. */
    fun named(name: Txt, word: Txt, rest: Txt?): List<Say> {
        val b = bridge(word)
        if (b.isEmpty()) return listOf(line(rest?.let { name + it } ?: name))
        return listOf(line(name)) + b + listOfNotNull(rest?.let { line(it) })
    }
    return when (this) {
        is TimeUp -> if (bedtime) {
            listOf(line(Phrases.bedtime, Tone.CALM), line(Phrases.bedtimeBody, Tone.CALM))
        } else {
            listOf(line(Phrases.allDone, Tone.CALM), line(Phrases.allDoneBody, Tone.CALM))
        }
        // Said, bridged, then said again with the child: toddlers learn a letter by repeating
        // it. The bridge is the word's meaning in the other language, in the card's voice:
        // "A for Apple" · "Apple means" … "सेब"; "अ से अनानास" · "अनानास यानी" … "pineapple".
        is LetterCard -> letter.spoken() + bridge(letter.meaning) + line(Phrases.sayWithMe) + letter.spoken()
        is Quiz -> listOf(line(say ?: prompt))
        is DragGame -> listOf(line(say))
        is CountAlong -> listOf(line(Phrases.countAlong(thing)))
        is ShapeCard -> named(Phrases.thisShapeName(shape), shape.name, Phrases.thisShapeRest(shape))
        is SolidCard -> named(Phrases.thisSolidName(solid), solid.name, Phrases.thisSolidRest(solid, showFact))
        is AngleLab -> listOf(line(target?.let(Phrases::makeAngleSay) ?: Phrases.dragAngle))
        is CurveLab -> named(Phrases.thisCurveName(curve), curve.name, Phrases.thisCurveRest(curve))
        // Like the alphabet card: "1", "O", "N", "E", "one", "1 elephant", "one means" … "एक",
        // then again together.
        is NumberCard -> buildList {
            val part = { t: String, l: Lang -> Say(t, l, pauseMillis = Letter.PART_PAUSE) }
            add(part(n.toString(), lang))
            if (lang == Lang.EN && level >= 4) Numbers.spelling(n).forEach { add(part(it, lang)) }
            add(part(word[lang], lang))
            add(line(thing.of(n)))
            addAll(bridge(word))
            add(line(Phrases.sayWithMe))
            add(part(word[lang], lang))
        }
        is TableCard -> listOf(line(Phrases.tableSay(table)))
        is SquareCard -> listOf(line(Phrases.squareSay(n)))
        is SieveLab -> listOf(line(NumberKinds.sieveSay(top)))
        is CupGame -> listOf(line(Memory.cupsSay(prize)))
        is PairsGame -> listOf(line(Memory.pairsSay))
        is OrderGame -> listOf(line(Memory.orderSay))
        is MazeGame -> listOf(line(Mazes.say(runner)))
        // Named, bridged, then named again with the child, as on the picture cards.
        is DiagramCard -> listOf(line(Diagrams.thisIs(diagram, part))) + bridge(part.name) + listOfNotNull(
            line(Phrases.sayWithMe), Say(part.name[lang], lang, pauseMillis = Letter.PART_PAUSE),
            part.fact.takeIf { showFact }?.let { line(it) },
        )
        is DiagramTap -> listOf(line(Diagrams.tapSay(part)))
        is WordUseCard -> listOf(line(word.word)) + bridge(word.word) + line(Grammar.cardSay(word, level))
        is LabReel -> listOf(line(lab.intro))
        // "Earth is the 3rd planet from the Sun." · "Earth means" … "पृथ्वी" · its fact. The Sun's
        // and the Moon's facts start with their name, so their bridge comes after.
        is PlanetCard -> {
            val name = Science.bareName(planet)
            planet.order?.let { named(Science.planetOrder(planet), name, planet.fact) }
                ?: (listOf(line(planet.fact)) + bridge(name))
        }
        // Named, bridged, then named again with the child, as on the alphabet card.
        is WordCard -> listOf(line(word.thisIs())) + bridge(word.name) + listOfNotNull(
            line(Phrases.sayWithMe), Say(word.title[lang], lang, pauseMillis = Letter.PART_PAUSE),
            word.fact?.takeIf { showFact }?.let { line(it) },
        )
    }
}

/**
 * What a right answer says after the cheer: the [Quiz.reveal] (or [Quiz.revealSaid], in
 * parts), then the [Quiz.bridge] into the child's other [langs]: "Yes! That is red!" ·
 * "red means" … "लाल".
 */
fun Quiz.revealLines(langs: List<Lang>): List<Say> {
    val lang = voice ?: langs.first()
    val said = revealSaid ?: reveal?.let { listOf(Say(it[lang], lang)) }.orEmpty()
    return said + Bridge.lines(bridge, lang, langs)
}

/**
 * A tap on a quiz's picture: its name, then the question again ("This is a fish. Where
 * does the fish live?"), with the name bridged in between. Not in a quiz about one
 * language's words ([Reel.voice]): "ball means गेंद" before "Ball starts with which
 * letter?" would send the child to ग.
 */
fun Quiz.pictureTapped(langs: List<Lang>): List<Say> {
    val lang = voice ?: langs.first()
    val picture = stage as? Stage.Picture
    val name = picture?.let { it.name ?: Words.of(it.emoji) }
    return name?.said(lang, if (voice == null) langs else listOf(lang)).orEmpty() + Say((say ?: prompt)[lang], lang)
}

/** Hard reels are followed by an easy one: see Feed. */
fun Reel.isHardFor(age: Int): Boolean = level > age || (needsSolving && difficulty >= 4)
