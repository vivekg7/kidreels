package com.crylo.kidreels.core

/** What a piece or a place looks like in a drag game. */
sealed interface Sprite {
    data class Emoji(val text: String) : Sprite

    data class Shape(val kind: ShapeKind, val argb: Long) : Sprite

    /**
     * One square of [emoji] cut into a [grid] × [grid] board: cell [cell], counted row by
     * row, turned [turns] quarter turns clockwise.
     */
    data class Crop(val emoji: String, val grid: Int, val cell: Int, val turns: Int = 0) : Sprite
}

/** Where pieces go. Full once it holds [capacity] pieces. */
data class DropTarget(val sprite: Sprite, val label: Txt? = null, val capacity: Int = 1)

/**
 * Something to drag. [target] is where it belongs (null: nowhere, a wrong option).
 * [whyNot] says, for each target, why it does not go there; null where it does. [placed]
 * is said when it lands in the right place.
 */
data class Piece(val sprite: Sprite, val target: Int?, val whyNot: List<Txt?>, val placed: Txt? = null)

enum class DragKind { MISSING_PIECE, SHAPE_SORTER, SORT, FEED, BASKET }

/**
 * Drag the pieces to where they belong. Solved when every target is full, which also
 * clears the pieces left over. A piece dropped in the wrong place slides back and
 * [Piece.whyNot] says why.
 */
data class DragGame(
    override val id: Long,
    override val topic: Topic,
    override val level: Int,
    override val difficulty: Int,
    val kind: DragKind,
    override val prompt: Txt,
    val say: Txt,
    val targets: List<DropTarget>,
    val pieces: List<Piece>,
    val reveal: Txt? = null,
) : Reel {
    override val needsSolving get() = true
    override val voice: Lang? get() = null

    init {
        require(pieces.all { it.whyNot.size == targets.size }) { "whyNot per target in $kind" }
        targets.forEachIndexed { t, target ->
            require(pieces.count { it.target == t } >= target.capacity) { "$kind target $t cannot be filled" }
        }
    }

    /** Whether [piece] may go into [target] now, with [filled] pieces already there. */
    fun fits(piece: Int, target: Int, filled: Int): Boolean = pieces[piece].target == target && filled < targets[target].capacity
}

/** Who eats what, for "feed the animals". Hindi needs the oblique: कुत्ते को, कुत्ते का. */
private class Meal(val animal: String, val en: String, val hiOblique: String, val food: String, val foodEn: String, val foodHi: String)

private val meals = listOf(
    Meal("🐶", "dog", "कुत्ते", "🦴", "the bone", "हड्डी"),
    Meal("🐒", "monkey", "बंदर", "🍌", "the banana", "केला"),
    Meal("🐰", "rabbit", "खरगोश", "🥕", "the carrot", "गाजर"),
    Meal("🐱", "cat", "बिल्ली", "🥛", "milk", "दूध"),
    Meal("🐭", "mouse", "चूहे", "🧀", "cheese", "चीज़"),
    Meal("🐻", "bear", "भालू", "🍯", "honey", "शहद"),
    Meal("🐝", "bee", "मधुमक्खी", "🌻", "the flower", "फूल"),
    Meal("🐔", "hen", "मुर्गी", "🌽", "corn", "मक्का"),
    Meal("🐄", "cow", "गाय", "🌿", "grass", "घास"),
    Meal("🐼", "panda", "पांडा", "🎋", "bamboo", "बाँस"),
)

/** Faces fill their square, so every piece of them has something on it. */
private val puzzlePictures = listOf("🐶", "🐱", "🦁", "🐯", "🐼", "🐸", "🐵", "🐷", "🐮", "🐰", "🦊", "🐻", "🐨")

/** "a circle", "an oval". */
private fun a(name: Txt): String = name[Lang.EN].lowercase().let { if (it.first() in "aeiou") "an $it" else "a $it" }

internal object DragGames {
    fun feed(g: Gen): Reel {
        val chosen = meals.shuffled(g.random).take(if (g.level == 2) 2 else 3)
        val targets = chosen.map { DropTarget(Sprite.Emoji(it.animal)) }
        val pieces = chosen.mapIndexed { i, m ->
            Piece(
                Sprite.Emoji(m.food), i,
                chosen.mapIndexed { t, other ->
                    if (t == i) null else txt(
                        "That is not the ${other.en}'s food. Whose food is ${m.foodEn}?",
                        "यह ${other.hiOblique} का खाना नहीं है। ${m.foodHi} किसका खाना है?",
                    )
                },
                placed = txt("The ${m.en} loves ${m.foodEn}!", "${m.hiOblique} को ${m.foodHi} पसंद है!"),
            )
        }.shuffled(g.random)
        val prompt = txt("Give each animal its food!", "हर जानवर को उसका खाना दो!")
        return DragGame(
            g.id, Topic.FEEDING, g.level, g.difficulty, DragKind.FEED, prompt,
            prompt + txt("Drag the food to the animal.", "खाना खींचकर जानवर तक ले जाओ।"), targets, pieces,
            reveal = txt("Everyone is full. Yum!", "सबका पेट भर गया। वाह!"),
        )
    }

    fun shapeSorter(g: Gen): Reel {
        val count = when (g.level) {
            2 -> 2
            3 -> 3
            else -> 4
        }
        // Easy boards keep apart the shapes that differ only by a turn or a stretch.
        val lookAlike = listOf(setOf(ShapeKind.SQUARE, ShapeKind.DIAMOND), setOf(ShapeKind.SQUARE, ShapeKind.RECTANGLE), setOf(ShapeKind.CIRCLE, ShapeKind.OVAL))
        var shapes: List<ShapeInfo>
        do shapes = Shapes.upTo(g.level.coerceAtLeast(3)).shuffled(g.random).take(count)
        while (g.difficulty <= 2 && lookAlike.any { pair -> shapes.count { it.kind in pair } == 2 })
        shapes = shapes.map { it.forAge(g.age) }
        val colours = Colours.bright.shuffled(g.random)
        val targets = shapes.map { DropTarget(Sprite.Shape(it.kind, 0)) }
        val pieces = shapes.mapIndexed { i, s ->
            Piece(
                Sprite.Shape(s.kind, colours[i]), i,
                shapes.mapIndexed { t, other ->
                    if (t == i) null else txt(
                        "That is ${a(s.name)}, not ${a(other.name)}. Find its own place!",
                        "यह ${s.name[Lang.HI]} है, ${other.name[Lang.HI]} नहीं। इसकी अपनी जगह ढूँढो!",
                    )
                },
                placed = txt("${s.name[Lang.EN]}!", "${s.name[Lang.HI]}!"),
            )
        }.shuffled(g.random)
        val prompt = txt("Put each shape in its place!", "हर आकार को उसकी जगह पर रखो!")
        return DragGame(
            g.id, Topic.SHAPE_SORTER, g.level, g.difficulty, DragKind.SHAPE_SORTER, prompt,
            prompt + txt("Drag it to the hole that looks the same.", "उसे खींचकर उसी जैसे खाँचे में रखो।"), targets, pieces,
            reveal = txt("Every shape is home!", "सारे आकार अपनी जगह पर!"),
        )
    }

    fun basket(g: Gen): Reel {
        val n = when (g.level) {
            3 -> g.upTo(1, 3)
            4 -> g.upTo(2, 5)
            5 -> g.upTo(3, 8)
            else -> g.upTo(5, 10)
        }
        val thing = g.pick(Countables)
        val extra = if (n <= 3) 2 else 3
        val pieces = List(n + extra) { Piece(Sprite.Emoji(thing.emoji), 0, listOf(null)) }
        val prompt = txt("Put ${thing.of(n)[Lang.EN]} in the basket!", "टोकरी में ${thing.of(n)[Lang.HI]} रखो!")
        return DragGame(
            g.id, Topic.BASKET, g.level, g.difficulty, DragKind.BASKET, prompt,
            prompt + txt("Count as you go.", "गिनते जाओ।"),
            listOf(DropTarget(Sprite.Emoji("🧺"), capacity = n)), pieces,
            reveal = txt("${thing.of(n)[Lang.EN]} in the basket!", "टोकरी में ${thing.of(n)[Lang.HI]}!"),
        )
    }

    fun sort(g: Gen): Reel {
        val near = listOf("fruit" to "veg", "animal" to "bird", "sea" to "animal", "bird" to "bug")
        val far = listOf("fruit" to "vehicle", "animal" to "clothes", "veg" to "ball", "vehicle" to "sea", "clothes" to "fruit")
        val (ka, kb) = g.pick(if (g.difficulty >= 3 || g.level >= 6) near else far).let { if (g.chance(0.5)) it else it.second to it.first }
        val a = Categories.byKey(ka).forAge(g.age)
        val b = Categories.byKey(kb).forAge(g.age)
        val each = when (g.level) {
            4 -> 2
            5, 6 -> 3
            else -> 4
        }
        // The basket shows a member that is not among the pieces, as a sign of what goes in it.
        val (signA, itemsA) = a.items.shuffled(g.random).let { it.first() to it.drop(1).take(each) }
        val (signB, itemsB) = b.items.shuffled(g.random).let { it.first() to it.drop(1).take(each) }
        val cats = listOf(a, b)
        fun why(mine: Category) = cats.map { other ->
            if (other == mine) null else txt(
                "That is ${mine.one[Lang.EN]}, not ${other.one[Lang.EN]}!",
                "यह ${mine.one[Lang.HI]} है, ${other.one[Lang.HI]} नहीं!",
            )
        }
        val pieces = (itemsA.map { Piece(Sprite.Emoji(it), 0, why(a)) } + itemsB.map { Piece(Sprite.Emoji(it), 1, why(b)) }).shuffled(g.random)
        val targets = listOf(
            DropTarget(Sprite.Emoji(signA), a.name, capacity = itemsA.size),
            DropTarget(Sprite.Emoji(signB), b.name, capacity = itemsB.size),
        )
        val prompt = txt("Put each one in the right basket!", "हर चीज़ को सही टोकरी में रखो!")
        val cap = { t: Txt -> t[Lang.EN].replaceFirstChar { it.uppercase() } }
        return DragGame(
            g.id, Topic.SORTING, g.level, g.difficulty, DragKind.SORT, prompt,
            prompt + txt("${cap(a.name)} in one, ${b.name[Lang.EN]} in the other.", "${a.name[Lang.HI]} एक में, ${b.name[Lang.HI]} दूसरी में।"),
            targets, pieces,
            reveal = txt("All sorted!", "सब छँट गए!"),
        )
    }

    fun missingPiece(g: Gen): Reel {
        val grid = when {
            g.level <= 4 -> 2
            g.level <= 6 && g.difficulty <= 2 -> 2
            else -> 3
        }
        val cells = grid * grid
        val picture = g.pick(puzzlePictures)
        val hole = g.random.nextInt(cells)
        val options = if (g.level >= 7) 4 else 3
        val fromOthers = txt("That piece is from another picture!", "यह टुकड़ा किसी और तस्वीर का है!")
        val elsewhere = txt("That piece is from another part of the picture. Look at its edges!", "यह टुकड़ा तस्वीर की किसी और जगह का है। इसके किनारे देखो!")
        val turned = txt("Nearly! But that piece is turned the wrong way.", "करीब-करीब! पर यह टुकड़ा उल्टा घूमा हुआ है।")
        val wrong: List<Pair<Sprite.Crop, Txt>> = when {
            // The youngest compare whole pictures: the same corner of another face.
            g.level <= 4 || g.difficulty <= 2 ->
                (puzzlePictures - picture).shuffled(g.random).take(options - 1).map { Sprite.Crop(it, grid, hole) to fromOthers }
            // The oldest also get the right piece turned round: the classic trap.
            g.level >= 7 && g.difficulty >= 3 -> {
                val turnedPieces = listOf(2, 1).take(if (g.difficulty >= 4) 2 else 1).map { Sprite.Crop(picture, grid, hole, it) to turned }
                val others = (0 until cells).filter { it != hole }.shuffled(g.random).map { Sprite.Crop(picture, grid, it) to elsewhere }
                (turnedPieces + others).take(options - 1)
            }
            else -> (0 until cells).filter { it != hole }.shuffled(g.random).take(options - 1).map { Sprite.Crop(picture, grid, it) to elsewhere }
        }
        val pieces = (listOf(Piece(Sprite.Crop(picture, grid, hole), 0, listOf(null), placed = txt("It fits!", "बिल्कुल ठीक बैठा!"))) +
            wrong.map { (crop, why) -> Piece(crop, null, listOf(why)) }).shuffled(g.random)
        val prompt = txt("Find the missing piece!", "छूटा हुआ टुकड़ा ढूँढो!")
        return DragGame(
            g.id, Topic.MISSING_PIECE, g.level, g.difficulty, DragKind.MISSING_PIECE, prompt,
            prompt + txt("Drag it into the gap.", "उसे खींचकर खाली जगह में रखो।"),
            listOf(DropTarget(Sprite.Crop(picture, grid, hole))), pieces,
            reveal = Words.of(picture)?.thisIs(),
        )
    }
}
