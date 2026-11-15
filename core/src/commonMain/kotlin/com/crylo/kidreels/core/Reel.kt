package com.crylo.kidreels.core

import kotlin.random.Random

/** The "channel" a reel appears to come from in the familiar-looking skins. */
enum class Channel(val handle: Txt, val emoji: String) {
    MATHS(txt("Maths", "गणित"), "🔢"),
    LETTERS(txt("Letters", "अक्षर"), "🔤"),
    SHAPES(txt("Shapes", "आकार"), "🔷"),
    PUZZLES(txt("Puzzles", "पहेली"), "🧩"),
    WORLD(txt("Our world", "हमारी दुनिया"), "🌍"),
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
    LETTERS(Channel.LETTERS, txt("Alphabet", "वर्णमाला")),
    FIRST_LETTER(Channel.LETTERS, txt("First letter", "पहला अक्षर")),
    SHAPES(Channel.SHAPES, txt("Shapes", "आकार")),
    SHAPE_QUIZ(Channel.SHAPES, txt("Find the shape", "आकार ढूँढो")),
    SOLIDS(Channel.SHAPES, txt("3D shapes", "ठोस आकार")),
    ANGLES(Channel.SHAPES, txt("Angles", "कोण")),
    CURVES(Channel.SHAPES, txt("Curves", "वक्र")),
    PATTERN(Channel.PUZZLES, txt("Patterns", "पैटर्न")),
    ODD_ONE_OUT(Channel.PUZZLES, txt("Odd one out", "अलग कौन")),
    SUDOKU(Channel.PUZZLES, txt("Sudoku", "सुडोकू")),
    SIZES(Channel.WORLD, txt("Big and small", "बड़ा और छोटा")),
    COLOURS(Channel.WORLD, txt("Colours", "रंग")),
    ANIMAL_SOUNDS(Channel.WORLD, txt("Animal sounds", "जानवरों की आवाज़")),
    HABITATS(Channel.WORLD, txt("Animal homes", "जानवरों के घर")),
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
    /** Spoken after the right answer, e.g. "2 plus 3 is 5!". */
    val reveal: Txt? = null,
    /** Spoken on arrival when it should differ from the [prompt] on screen. */
    val say: Txt? = null,
    override val voice: Lang? = null,
) : Reel {
    override val needsSolving: Boolean get() = true

    init {
        require(answer in choices.indices) { "Answer $answer out of ${choices.size} choices" }
        require(choices.toSet().size == choices.size) { "Duplicate choices in $topic: $choices" }
    }
}

/** The picture a quiz asks about. */
sealed interface Stage {
    data object None : Stage

    /** "3 + 4": with sticks (or dots for ×) under the numbers when [aids] is set. */
    data class Sum(val a: Int, val op: Op, val b: Int, val aids: Boolean) : Stage

    data class Count(val emoji: String, val n: Int, val scattered: Boolean) : Stage

    data class Picture(val emoji: String) : Stage

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
}

/** Count along: tap each thing and hear the running count. Nothing to get wrong. */
data class CountAlong(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val emoji: String,
    val n: Int,
) : Reel {
    override val topic get() = Topic.COUNT_ALONG
    override val needsSolving get() = false
    override val prompt get() = Phrases.letsCount
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
 * Whatever is spoken when a reel comes on screen, in the right voice: a friendly opener,
 * the reel's own line, then what to do with it. With [greet] false (the child asked to
 * hear it again) the opener is left out and the rest is repeated.
 */
fun Reel.intro(main: Lang, greet: Boolean = true): List<Say> {
    val lang = voice ?: main
    fun line(t: Txt, tone: Tone = Tone.TALK) = Say(t[lang], lang, tone)
    if (this is TimeUp) return listOf(line(Phrases.allDone, Tone.CALM), line(Phrases.allDoneBody, Tone.CALM))
    val openers = when (this) {
        is Quiz -> Phrases.askOpeners
        is CountAlong, is AngleLab -> Phrases.playOpeners
        else -> Phrases.lookOpeners
    }
    val own = when (this) {
        is LetterCard -> Say(letter.phrase, lang)
        is Quiz -> line(say ?: prompt)
        else -> prompt?.let { line(it) }
    }
    val hint = when (this) {
        // From age 4 children know to tap an answer; younger ones are told.
        is Quiz -> Phrases.tapTheAnswer.takeIf { level <= 3 }
        is ShapeCard -> Phrases.tapAndSee
        is SolidCard -> Phrases.spinIt
        is CurveLab -> Phrases.stretchIt
        else -> null
    }
    val opener = if (greet) line(Phrases.pick(openers, Random(id))) else null
    return listOfNotNull(opener, own, hint?.let { line(it) })
}

/** Hard reels are followed by an easy one: see Feed. */
fun Reel.isHardFor(age: Int): Boolean = level > age || (needsSolving && difficulty >= 4)
