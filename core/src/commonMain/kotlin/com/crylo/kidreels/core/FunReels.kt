package com.crylo.kidreels.core

import kotlin.math.abs

/*
 * The first fun reels (phase 1 of docs/plan-entertainment.md): silly animals, bubble pop
 * and copy me here, tap the tune in Songs.kt. None of them is a puzzle (`needsSolving` is
 * false), so none has a sad face, a retry or a skill score, and each ends by itself or
 * never escalates: the hundredth tap is as funny as the first, not more.
 */

/** A part of a silly animal to tap, and what it does when tapped. */
enum class SillyPart(val word: Txt, val plural: Boolean, val feminineHi: Boolean, val does: Txt) {
    NOSE(txt("nose", "नाक"), false, true, txt("A-a-a-CHOO!", "आ... आ... आछीं!")),
    TUMMY(txt("tummy", "पेट"), false, false, txt("Hee hee hee! That tickles!", "ही ही ही! गुदगुदी होती है!")),
    FEET(txt("feet", "पैर"), true, false, txt("Hop! Hop! Boing!", "फुदक! फुदक! उछल!")),
    EARS(txt("ears", "कान"), true, false, txt("Flap, flap, flap!", "फड़, फड़, फड़!")),
}

/**
 * One silly animal, drawn whole in Gappu's style (art/drawn/silly-*.svg), standing
 * front-on so it has a nose to tap, a tummy to tickle, feet to hop on and ears to flap.
 * [face] is its emoji, which names it. [hiOblique] is the Hindi name before "का/की/के":
 * कुत्ते की नाक, not कुत्ता की. [voice] is its silly voice; [shape] is where its parts are.
 */
data class SillyAnimal(val face: String, val art: String, val hiOblique: String, val sound: Txt, val voice: Tone, val shape: SillyShape) {
    val word: Word get() = Words.of(face) ?: error("$face has no name")
}

/**
 * Where a drawn animal's parts are, as fractions of its picture (0 is the left or top
 * edge): its middle [centre] (a tail to one side moves it), its nose at [noseY], and ears
 * above [earsTo] and further than [earsOut] from the middle. The head ends and the tummy
 * starts at the neck, at the same height in every drawing; the feet are at the bottom.
 */
data class SillyShape(val noseY: Float, val earsTo: Float, val earsOut: Float, val centre: Float = 0.5f)

/** Where a tap on a silly animal landed. */
enum class SillyZone { NOSE, TUMMY, FEET, EARS, HEAD, AIR }

/** "Tap the silly cow!": tap its nose and it sneezes, its tummy and it giggles. From 4 the voice names the part. */
data class SillyAnimalReel(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val animal: SillyAnimal,
    val namesParts: Boolean,
) : Reel {
    override val topic get() = Topic.SILLY_ANIMALS
    override val needsSolving get() = false
    override val prompt: Txt get() = Silly.tapThe(animal)
    override val voice: Lang? get() = null
}

object Silly {
    /** A handful to start with, a booming voice for the big ones and a squeak for the small. */
    val all: List<SillyAnimal> = listOf(
        SillyAnimal("🐮", "art:silly-cow", "गाय", txt("Moooooo!", "मूँ मूँ मूँ!"), Tone.GIANT, SillyShape(noseY = 0.45f, earsTo = 0.32f, earsOut = 0.3f)),
        SillyAnimal("🦁", "art:silly-lion", "शेर", txt("Roooar!", "गुर्रर्रर्र!"), Tone.GIANT, SillyShape(noseY = 0.42f, earsTo = 0.3f, earsOut = 0.17f, centre = 0.465f)),
        SillyAnimal("🐷", "art:silly-pig", "सूअर", txt("Oink oink oink!", "घुर्र घुर्र!"), Tone.GIANT, SillyShape(noseY = 0.39f, earsTo = 0.23f, earsOut = 0.2f)),
        SillyAnimal("🐭", "art:silly-mouse", "चूहे", txt("Squeak squeak!", "चीं चीं चीं!"), Tone.TINY, SillyShape(noseY = 0.435f, earsTo = 0.33f, earsOut = 0.2f)),
        SillyAnimal("🐱", "art:silly-cat", "बिल्ली", txt("Meeeow!", "म्याऊँ म्याऊँ!"), Tone.TINY, SillyShape(noseY = 0.38f, earsTo = 0.22f, earsOut = 0.22f)),
        // Its ears hang down the sides of its head, so they reach low.
        SillyAnimal("🐶", "art:silly-dog", "कुत्ते", txt("Woof woof!", "भौं भौं!"), Tone.TINY, SillyShape(noseY = 0.385f, earsTo = 0.62f, earsOut = 0.26f, centre = 0.485f)),
    )

    fun tapThe(a: SillyAnimal): Txt = txt("Tap the silly ${a.word.name[Lang.EN]}!", "मज़ेदार ${a.word.name[Lang.HI]} को छुओ!")

    val tapParts = txt("Tap its nose, its tummy, its feet and its ears!", "इसकी नाक, पेट, पैर और कान छूकर देखो!")

    /** "That's the cow's nose!" / "यह गाय की नाक है!": a body-part word on every tap, from age 4. */
    fun thatsThe(a: SillyAnimal, part: SillyPart): Txt {
        val en = a.word.name[Lang.EN]
        val of = when {
            part.plural -> "के"
            part.feminineHi -> "की"
            else -> "का"
        }
        return txt(
            if (part.plural) "Those are the $en's ${part.word[Lang.EN]}!" else "That's the $en's ${part.word[Lang.EN]}!",
            "${if (part.plural) "ये" else "यह"} ${a.hiOblique} $of ${part.word[Lang.HI]} ${if (part.plural) "हैं" else "है"}!",
        )
    }

    /**
     * Which part a tap at ([x], [y]) is on, as fractions of [a]'s picture. Generous: a
     * two-year-old's finger is wide, and a near miss of the nose that tickles the head
     * instead is still a laugh.
     */
    fun zone(a: SillyAnimal, x: Float, y: Float): SillyZone {
        val s = a.shape
        val dx = abs(x - s.centre)
        return when {
            x !in 0f..1f || y !in 0f..1f -> SillyZone.AIR
            abs(y - s.noseY) < 0.08f && dx < 0.13f -> SillyZone.NOSE
            y < s.earsTo && dx > s.earsOut -> SillyZone.EARS
            y < NECK -> if (dx < 0.46f) SillyZone.HEAD else SillyZone.AIR
            y < 0.84f -> if (dx < 0.33f) SillyZone.TUMMY else SillyZone.AIR
            else -> if (dx < 0.36f) SillyZone.FEET else SillyZone.AIR
        }
    }

    /** Where every drawing's head meets its body. */
    private const val NECK = 0.56f

    fun make(g: Gen): SillyAnimalReel {
        val animal = if (g.ordered) all[g.inOrder("silly", all.size)] else g.pick(all)
        return SillyAnimalReel(g.id, g.level, g.difficulty, animal, namesParts = g.level >= 4)
    }
}

/** What the bubbles ask for. */
enum class BubbleGame {
    /** Pop them all, counting. */
    COUNT,

    /** Pop only the ones of one colour. */
    COLOUR,

    /** Pop 1, 2, 3… in order. */
    NUMBERS,

    /** Pop the letters of a short English word, in order. */
    WORD,
}

/** One bubble: what is inside it (a picture, a numeral or a letter), its colour, and whether it is one to pop. */
data class Bubble(val inside: String, val argb: Long, val wanted: Boolean)

/**
 * Bubble pop: bubbles float up, each with something inside; popping one says the count and
 * plays the next note up a scale. A fixed number of them, then no more come: bubble
 * popping is a classic time-waster, so it ends. [slow] in the wind-down.
 */
data class BubblePop(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val game: BubbleGame,
    val bubbles: List<Bubble>,
    /** What every bubble holds in [BubbleGame.COUNT]; the colour asked for in [BubbleGame.COLOUR]. */
    val thing: Thing?,
    val colour: Colour?,
    /** [BubbleGame.WORD]: the word and its picture. */
    val word: Pair<String, String>?,
    val slow: Boolean,
) : Reel {
    override val topic get() = Topic.BUBBLES
    override val needsSolving get() = false
    override val prompt: Txt get() = Bubbles.prompt(this)
    override val voice: Lang? get() = if (game == BubbleGame.WORD) Lang.EN else null

    /** How many bubbles are there to pop before it is over. */
    val toPop: Int get() = bubbles.count { it.wanted }
}

object Bubbles {
    /** Short English words whose letters a five-year-old can pop in order, each with its picture. */
    val words: List<Pair<String, String>> = listOf(
        "CAT" to "🐈", "DOG" to "🐕", "SUN" to "☀️", "BUS" to "🚌", "HEN" to "🐔", "COW" to "🐄",
        "PIG" to "🐖", "BEE" to "🐝", "FISH" to "🐟", "STAR" to "⭐", "FROG" to "🐸", "DUCK" to "🦆",
    )

    /** How many to count: 5 at 2, 10 at 4 and 20 from 5, as count-along climbs. */
    fun countFor(level: Int): Int = when (level) {
        2 -> 5
        3 -> 7
        4 -> 10
        else -> 20
    }

    fun make(g: Gen): BubblePop {
        val englishToo = Lang.EN in g.langs
        val game = when {
            g.calm || g.ordered || g.level < 4 -> BubbleGame.COUNT
            else -> {
                val roll = g.random.nextDouble()
                when {
                    roll < 0.4 -> BubbleGame.COUNT
                    roll < 0.65 -> BubbleGame.COLOUR
                    roll < 0.9 || !englishToo || g.level < 5 -> BubbleGame.NUMBERS
                    else -> BubbleGame.WORD
                }
            }
        }
        val bright = Colours.bright
        fun colourOf(i: Int) = bright[(i * 3 + g.random.nextInt(bright.size)) % bright.size]
        return when (game) {
            BubbleGame.COUNT -> {
                val thing = g.pick(Countables)
                val n = if (g.calm) minOf(countFor(g.level), 10) else countFor(g.level)
                BubblePop(g.id, g.level, g.difficulty, game, List(n) { Bubble(thing.emoji, colourOf(it), true) }, thing, null, null, g.calm)
            }
            BubbleGame.COLOUR -> {
                val palette = Colours.all.take(4).shuffled(g.random)
                val target = palette.first()
                val wanted = if (g.level <= 4) 5 else 8
                val others = palette.drop(1).take(2)
                val thing = g.pick(Countables)
                val bubbles = (List(wanted) { Bubble(thing.emoji, target.argb, true) } + List(wanted) { Bubble(thing.emoji, others[it % others.size].argb, false) })
                    .shuffled(g.random)
                BubblePop(g.id, g.level, g.difficulty, game, bubbles, thing, target, null, g.calm)
            }
            BubbleGame.NUMBERS -> {
                val top = if (g.level == 4) 5 else 10
                BubblePop(g.id, g.level, g.difficulty, game, (1..top).map { Bubble(it.toString(), colourOf(it), true) }.shuffled(g.random), null, null, null, g.calm)
            }
            BubbleGame.WORD -> {
                val word = g.pick(words)
                BubblePop(g.id, g.level, g.difficulty, game, word.first.map { Bubble(it.toString(), colourOf(it.code), true) }.shuffled(g.random), null, null, word, g.calm)
            }
        }
    }

    fun prompt(b: BubblePop): Txt = when (b.game) {
        BubbleGame.COUNT -> txt("Pop the bubbles!", "बुलबुले फोड़ो!")
        BubbleGame.COLOUR -> txt("Pop the ${b.colourName[Lang.EN]} ones!", "${b.colourName[Lang.HI]} वाले फोड़ो!")
        BubbleGame.NUMBERS -> txt("Pop 1 to ${b.toPop} in order!", "1 से ${b.toPop} तक, एक के बाद एक फोड़ो!")
        BubbleGame.WORD -> txt("Pop the letters of ${b.wordText}!", "${b.wordText} के अक्षर फोड़ो!")
    }

    /** What the voice says on arrival. */
    fun say(b: BubblePop): Txt = when (b.game) {
        BubbleGame.COUNT -> txt("Pop the bubbles, and count them with me!", "बुलबुले फोड़ो, और मेरे साथ गिनो!")
        BubbleGame.COLOUR -> {
            val c = b.colourName
            txt("Lots of bubbles! Pop only the ${c[Lang.EN]} ones, and count them!", "कितने सारे बुलबुले! सिर्फ़ ${c[Lang.HI]} वाले फोड़ो, और गिनते जाओ!")
        }
        BubbleGame.NUMBERS -> txt("Find 1 and pop it, then 2, then 3, all the way to ${b.toPop}!", "पहले 1 ढूँढकर फोड़ो, फिर 2, फिर 3, ${b.toPop} तक!")
        BubbleGame.WORD -> {
            val w = b.wordText
            val letters = w.toList().joinToString(", ")
            txt("Pop the letters of ${w.lowercase()}: $letters!", "${w.lowercase()} के अक्षर फोड़ो: $letters!")
        }
    }

    /** The count after a pop: "One!", "Two!". */
    fun counted(n: Int): Txt = NumberWords.of(n).let { txt("${it[Lang.EN].replaceFirstChar { c -> c.uppercase() }}!", "${it[Lang.HI]}!") }

    /** A bubble that is not the next one: no shake and no sound of a miss, just what to look for. */
    fun notThis(b: BubblePop, popped: Int): Txt = when (b.game) {
        BubbleGame.COLOUR -> txt("Not that one! Pop the ${b.colourName[Lang.EN]} ones!", "वो नहीं! ${b.colourName[Lang.HI]} वाले फोड़ो!")
        BubbleGame.NUMBERS -> txt("Find ${popped + 1}!", "${popped + 1} ढूँढो!")
        BubbleGame.WORD -> b.wordText[popped].toString().let { txt("Find $it!", "$it ढूँढो!") }
        BubbleGame.COUNT -> txt("Pop!", "फट!")
    }

    /** The cheer at the end. */
    fun done(b: BubblePop): Txt = when (b.game) {
        BubbleGame.COUNT -> NumberWords.of(b.toPop).let { txt("${it[Lang.EN].replaceFirstChar { c -> c.uppercase() }} bubbles! Yay!", "${it[Lang.HI]} बुलबुले! वाह!") }
        BubbleGame.COLOUR -> NumberWords.of(b.toPop).let {
            txt("${it[Lang.EN].replaceFirstChar { c -> c.uppercase() }} ${b.colourName[Lang.EN]} bubbles! Yay!", "${it[Lang.HI]} ${b.colourName[Lang.HI]} बुलबुले! वाह!")
        }
        BubbleGame.NUMBERS -> txt("1 to ${b.toPop}! You popped them all in order!", "1 से ${b.toPop} तक! तुमने सब एक के बाद एक फोड़ दिए!")
        BubbleGame.WORD -> b.wordText.let { txt("${it.toList().joinToString("-")}, ${it.lowercase()}!", "${it.toList().joinToString("-")}, ${it.lowercase()}!") }
    }
}

/**
 * What the drawn child does for a move, as a puppet: where its hands go, how its head,
 * face and legs move. Each move has its own, so the child on screen does what the voice
 * says rather than bouncing for every one.
 */
enum class CopyAct {
    NOSE, EARS, HAIR, EYES, PAT_TUMMY, HANDS_UP, TONGUE, SHAKE_HEAD, CLAP, PAT_HEAD, WIGGLE,
    SHOULDERS, KNEES, ELBOWS, BLINK, CHIN, PUFF_CHEEKS, JUMP, STOMP, ONE_LEG, TURN, TOES, SIT_DOWN,
}

/**
 * One thing to copy: what to say, which part of the drawn child to ring ([Body] keys),
 * what the child does, how many beats to count with the drum (claps, pats, jumps), and
 * whether it needs the child to stand: those come only with the parent's jumping-games
 * switch.
 */
data class CopyMove(val say: Txt, val part: String?, val act: CopyAct, val beats: Int = 0, val standing: Boolean = false, val fromAge: Int = 2)

/** A step of a game: the move, and in Simon says whether Simon said it. */
data class CopyStep(val move: CopyMove, val simonSays: Boolean = true)

/**
 * "Copy me! Touch your nose!": four or five moves to copy, with the drawn child doing each
 * and the part ringed, then "Great copying!". The child taps Go; after that the moves wait
 * for nothing, as the point is to look away from the screen. From 5, sometimes Simon says.
 */
data class CopyMe(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val steps: List<CopyStep>,
    val simon: Boolean,
) : Reel {
    override val topic get() = Topic.COPY_ME
    override val needsSolving get() = false
    override val prompt: Txt get() = if (simon) CopyMoves.simonTitle else CopyMoves.title
    override val voice: Lang? get() = null
}

object CopyMoves {
    private fun beats(n: Int, en: String, hi: String) = txt(en.replace("#", n.toString()), hi.replace("#", n.toString()))

    /** Moves a child can do sitting at the table. */
    fun sitting(level: Int): List<CopyMove> {
        val claps = if (level <= 3) 2 else if (level <= 5) 3 else 4
        return listOf(
            CopyMove(txt("Touch your nose!", "अपनी नाक छुओ!"), "nose", CopyAct.NOSE),
            CopyMove(txt("Touch your ears!", "अपने कान छुओ!"), "ear", CopyAct.EARS),
            CopyMove(txt("Touch your hair!", "अपने बाल छुओ!"), "hair", CopyAct.HAIR),
            CopyMove(txt("Point to your eyes!", "अपनी आँखें दिखाओ!"), "eye", CopyAct.EYES),
            CopyMove(txt("Pat your tummy!", "अपना पेट थपथपाओ!"), "tummy", CopyAct.PAT_TUMMY),
            CopyMove(txt("Hands up high!", "हाथ ऊपर करो!"), "hand", CopyAct.HANDS_UP),
            CopyMove(txt("Stick out your tongue!", "अपनी जीभ बाहर निकालो!"), "mouth", CopyAct.TONGUE),
            CopyMove(txt("Shake your head!", "अपना सिर हिलाओ!"), "hair", CopyAct.SHAKE_HEAD),
            CopyMove(beats(claps, "Clap # times!", "# बार ताली बजाओ!"), "hand", CopyAct.CLAP, beats = claps),
            CopyMove(beats(claps, "Pat your head # times!", "अपने सिर पर # बार थपकी दो!"), "hair", CopyAct.PAT_HEAD, beats = claps, fromAge = 3),
            CopyMove(txt("Wiggle your fingers!", "अपनी उँगलियाँ हिलाओ!"), "finger", CopyAct.WIGGLE, fromAge = 4),
            CopyMove(txt("Touch your shoulders!", "अपने कंधे छुओ!"), "shoulder", CopyAct.SHOULDERS, fromAge = 4),
            CopyMove(txt("Touch your knees!", "अपने घुटने छुओ!"), "knee", CopyAct.KNEES, fromAge = 4),
            CopyMove(txt("Touch your elbows!", "अपनी कोहनियाँ छुओ!"), "elbow", CopyAct.ELBOWS, fromAge = 4),
            CopyMove(txt("Blink your eyes!", "अपनी आँखें झपकाओ!"), "eye", CopyAct.BLINK, fromAge = 4),
            CopyMove(txt("Touch your chin!", "अपनी ठुड्डी छुओ!"), "chin", CopyAct.CHIN, fromAge = 6),
            CopyMove(txt("Puff out your cheeks!", "अपने गाल फुलाओ!"), "cheek", CopyAct.PUFF_CHEEKS, fromAge = 6),
        ).filter { it.fromAge <= level }
    }

    /** Moves that need the child up on their feet: only with the jumping-games switch. */
    fun standing(level: Int): List<CopyMove> {
        val jumps = if (level <= 3) 2 else if (level <= 5) 3 else 5
        return listOf(
            CopyMove(beats(jumps, "Stand up and jump # times!", "खड़े हो जाओ और # बार कूदो!"), "foot", CopyAct.JUMP, beats = jumps, standing = true),
            CopyMove(beats(jumps, "Stomp your feet # times!", "# बार पैर पटको!"), "foot", CopyAct.STOMP, beats = jumps, standing = true),
            CopyMove(txt("Stand on one leg!", "एक पैर पर खड़े हो जाओ!"), "leg", CopyAct.ONE_LEG, standing = true, fromAge = 4),
            CopyMove(txt("Turn all the way round!", "पूरा गोल घूम जाओ!"), null, CopyAct.TURN, standing = true),
            CopyMove(txt("Touch your toes!", "अपने पैर की उँगलियाँ छुओ!"), "toes", CopyAct.TOES, standing = true, fromAge = 4),
        ).filter { it.fromAge <= level }
    }

    /** After standing moves, back down: the app is often used at meals. */
    val sitDown = CopyMove(txt("Now sit down again!", "अब फिर से बैठ जाओ!"), null, CopyAct.SIT_DOWN)

    fun make(g: Gen): CopyMe {
        val count = if (g.level <= 3) 4 else 5
        val sitting = sitting(g.level).shuffled(g.random)
        // One clap or pat to the drum in every game: counting along is half the fun.
        val counted = sitting.first { it.beats > 0 }
        var moves = (listOf(counted) + sitting.filter { it !== counted }.take(count - 1)).shuffled(g.random)
        if (g.jumping) {
            val up = standing(g.level).shuffled(g.random).take(2)
            moves = moves.take(count - up.size) + up + sitDown
        }
        val simon = !g.ordered && g.level >= 5 && g.chance(0.5)
        val steps = if (!simon) {
            moves.map { CopyStep(it) }
        } else {
            // One or two "gotcha"s, never the first step, so the game is clear before it tricks.
            val gotchas = (1 until moves.size).filter { moves[it] !== sitDown }.shuffled(g.random).take(if (g.level >= 7) 2 else 1).toSet()
            moves.mapIndexed { i, m -> CopyStep(m, simonSays = i !in gotchas || m === sitDown) }
        }
        return CopyMe(g.id, g.level, g.difficulty, steps, simon)
    }

    val title = txt("Copy me!", "मेरी नकल करो!")
    val simonTitle = txt("Simon says!", "साइमन कहता है!")
    val intro = txt("Copy me! Watch, listen and do what I say. Tap Go when you are ready!", "मेरी नकल करो! देखो, सुनो और जो मैं कहूँ वो करो। तैयार हो तो 'चलो' छुओ!")
    val simonIntro = txt(
        "Let's play Simon says! Copy me only when I say Simon says. Tap Go when you are ready!",
        "साइमन कहता है खेलें! नकल तभी करना जब मैं कहूँ, साइमन कहता है। तैयार हो तो 'चलो' छुओ!",
    )
    val go = txt("Go!", "चलो!")
    val simonSays = txt("Simon says:", "साइमन कहता है:")
    val gotcha = txt("Gotcha! Simon didn't say! Did you stay still?", "पकड़ा! साइमन ने तो कहा ही नहीं! तुम रुके रहे?")
    val done = txt("Great copying!", "बहुत बढ़िया नकल!")
    val again = txt("Again!", "फिर से!")

    /** A step as said: "Simon says: touch your ears!", or just "Touch your toes!" for a gotcha. */
    fun said(step: CopyStep, simon: Boolean): Txt =
        if (simon && step.simonSays && step.move !== sitDown) simonSays + step.move.say else step.move.say
}

private val BubblePop.colourName: Txt get() = colour!!.name
private val BubblePop.wordText: String get() = word!!.first
