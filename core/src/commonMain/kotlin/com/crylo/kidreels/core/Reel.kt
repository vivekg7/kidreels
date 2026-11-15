package com.crylo.kidreels.core

/** The "channel" a reel appears to come from in the familiar-looking skins. */
enum class Channel(val handle: Txt, val emoji: String) {
    MATHS(txt("Maths", "गणित"), "🔢"),
    LETTERS(txt("Letters", "अक्षर"), "🔤"),
    SHAPES(txt("Shapes", "आकार"), "🔷"),
    PUZZLES(txt("Puzzles", "पहेली"), "🧩"),
    WORLD(txt("Our world", "हमारी दुनिया"), "🌍"),
    THINKING(txt("Thinking", "सोचो"), "🧠"),
}

enum class Topic(val channel: Channel, val title: Txt) {
    COUNT_ALONG(Channel.MATHS, txt("Counting", "गिनती")),
    COUNTING(Channel.MATHS, txt("How many", "कितने")),
    ADDITION(Channel.MATHS, txt("Adding", "जोड़")),
    SUBTRACTION(Channel.MATHS, txt("Taking away", "घटाना")),
    MULTIPLICATION(Channel.MATHS, txt("Times tables", "पहाड़े")),
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
    SIZES(Channel.WORLD, txt("Big and small", "बड़ा और छोटा")),
    COLOURS(Channel.WORLD, txt("Colours", "रंग")),
    ANIMAL_SOUNDS(Channel.WORLD, txt("Animal sounds", "जानवरों की आवाज़")),
    HABITATS(Channel.WORLD, txt("Animal homes", "जानवरों के घर")),
    FEEDING(Channel.WORLD, txt("Feed the animals", "जानवरों को खिलाओ")),
    ANIMALS(Channel.WORLD, txt("Animals", "जानवर")),
    BIRDS(Channel.WORLD, txt("Birds", "चिड़ियाँ")),
    VEHICLES(Channel.WORLD, txt("Vehicles", "गाड़ियाँ")),
    BODY(Channel.WORLD, txt("My body", "मेरा शरीर")),
    FRUITS(Channel.WORLD, txt("Fruits", "फल")),
    VEGETABLES(Channel.WORLD, txt("Vegetables", "सब्ज़ियाँ")),
    HOME_THINGS(Channel.WORLD, txt("At home", "घर की चीज़ें")),
    CLOTHES(Channel.WORLD, txt("Clothes", "कपड़े")),
    SAME_FIGURE(Channel.THINKING, txt("Same figure", "एक जैसी आकृति")),
    ODD_FIGURE(Channel.THINKING, txt("Odd figure out", "अलग आकृति")),
    COMPLETE_PATTERN(Channel.THINKING, txt("Complete the pattern", "पैटर्न पूरा करो")),
    FIGURE_SERIES(Channel.THINKING, txt("Figure series", "आकृति शृंखला")),
    MIRROR(Channel.THINKING, txt("Mirror image", "दर्पण प्रतिबिंब")),
    ANALOGY(Channel.THINKING, txt("Analogy", "सादृश्यता")),
    FIT_PIECE(Channel.THINKING, txt("Complete the square", "वर्ग पूरा करो")),
    HIDDEN_FIGURE(Channel.THINKING, txt("Hidden figure", "छिपी आकृति")),
    PAPER_FOLD(Channel.THINKING, txt("Fold and punch", "मोड़ो और छेद करो")),
    BREAK(Channel.WORLD, txt("Break time", "आराम का समय")),
}

/**
 * One screen of the feed. Reels are plain data made by the generators in `gen/`; the UI
 * decides how each kind looks, and the skin decides the frame around it.
 */
sealed interface Reel {
    val id: Long
    val topic: Topic

    /** The age this reel was made for, 2 to 10. Not always the child's age: see Feed. */
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

/** The last reel of the day, once the parent's time limit is used up. */
data class TimeUp(override val id: Long) : Reel {
    override val topic get() = Topic.BREAK
    override val level get() = 2
    override val difficulty get() = 1
    override val needsSolving get() = false
    override val prompt get() = Phrases.allDone
    override val voice: Lang? get() = null
}

/**
 * What the voice says when a reel comes on screen, in the right voice: what is on the
 * screen and what to do with it, the way a grown-up beside the child would say it. The
 * speaker button says the same again.
 */
fun Reel.intro(main: Lang): List<Say> {
    val lang = voice ?: main
    fun line(t: Txt, tone: Tone = Tone.TALK) = Say(t[lang], lang, tone)
    return when (this) {
        is TimeUp -> listOf(line(Phrases.allDone, Tone.CALM), line(Phrases.allDoneBody, Tone.CALM))
        // Said, then said again with the child: toddlers learn a letter by repeating it.
        is LetterCard -> letter.spoken() + line(Phrases.sayWithMe) + letter.spoken()
        is Quiz -> listOf(line(say ?: prompt))
        is DragGame -> listOf(line(say))
        is CountAlong -> listOf(line(Phrases.countAlong(thing)))
        is ShapeCard -> listOf(line(Phrases.thisShape(shape)))
        is SolidCard -> listOf(line(Phrases.thisSolid(solid, showFact)))
        is AngleLab -> listOf(line(target?.let(Phrases::makeAngleSay) ?: Phrases.dragAngle))
        is CurveLab -> listOf(line(Phrases.thisCurve(curve)))
        // Named, then named again with the child, as on the alphabet card.
        is WordCard -> listOfNotNull(
            line(word.thisIs()), line(Phrases.sayWithMe), Say(word.title[lang], lang, pauseMillis = Letter.PART_PAUSE),
            word.fact?.takeIf { showFact }?.let { line(it) },
        )
    }
}

/** Hard reels are followed by an easy one: see Feed. */
fun Reel.isHardFor(age: Int): Boolean = level > age || (needsSolving && difficulty >= 4)
