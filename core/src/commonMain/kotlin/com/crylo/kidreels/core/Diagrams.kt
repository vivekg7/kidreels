package com.crylo.kidreels.core

/**
 * A part of a drawn diagram (the child in art/drawn/body.svg, the plant in plant.svg):
 * where it is, in the drawing's own units ([spots], one per side, and [radius] around
 * each), so a tap can be told apart and the part lit up. Emoji show an eye or a hand
 * well, but not an elbow, a knee or a plant's roots: on a whole drawing every part is
 * where it really is.
 *
 * [whom] is the Hindi object form ("घुटने को"); [plural] and [pluralHi] are separate, as
 * "hair" takes "is" and "बाल" takes "हैं".
 */
data class DiagramPart(
    val key: String,
    val name: Txt,
    val whom: String,
    val fact: Txt,
    val spots: List<Pair<Float, Float>>,
    val radius: Float,
    val fromAge: Int,
    val plural: Boolean = false,
    val pluralHi: Boolean = plural,
)

/** A drawing with parts to name and find: [art] is its file in art/drawn/. [yours] says "your knee", not "the knee". */
enum class Diagram(val art: String, val width: Float, val height: Float, val yours: Boolean) {
    BODY("body", 240f, 440f, true),
    PLANT("plant", 240f, 360f, false),
    ;

    val parts: List<DiagramPart> get() = if (this == BODY) Body.parts else Plant.parts
}

/** "This is your knee": a part lit up on the drawing, named, and from age 4 what it does. */
data class DiagramCard(
    override val id: Long,
    override val topic: Topic,
    override val level: Int,
    override val difficulty: Int,
    val diagram: Diagram,
    val part: DiagramPart,
    val showFact: Boolean,
) : Reel {
    override val needsSolving get() = false
    override val prompt: Txt get() = Diagrams.title(part)
    override val voice: Lang? get() = null
}

/** "Tap the knee!": find [part] on the drawing. */
data class DiagramTap(
    override val id: Long,
    override val topic: Topic,
    override val level: Int,
    override val difficulty: Int,
    val diagram: Diagram,
    val part: DiagramPart,
) : Reel {
    override val needsSolving get() = true
    override val prompt: Txt get() = Diagrams.tapThe(part)
    override val voice: Lang? get() = null
}

private fun part(
    key: String, en: String, hi: String, whom: String, factEn: String, factHi: String,
    spots: List<Pair<Float, Float>>, radius: Float, fromAge: Int, plural: Boolean = false, pluralHi: Boolean = plural,
) = DiagramPart(key, txt(en, hi), whom, txt(factEn, factHi), spots, radius, fromAge, plural, pluralHi)

/** The drawn child, 240 × 440: short sleeves, shorts and bare feet, so elbows, knees and toes show. */
object Body {
    private fun both(x: Float, y: Float) = listOf(x to y, 240f - x to y)

    /** In teaching order: what a two-year-old points to first, joints and the finer parts later. */
    val parts: List<DiagramPart> = listOf(
        part("hair", "hair", "बाल", "बालों", "Hair grows on our head.", "बाल हमारे सिर पर उगते हैं।", listOf(120f to 38f), 24f, 2, pluralHi = true),
        part("eye", "eye", "आँख", "आँख", "We see with our eyes.", "हम आँखों से देखते हैं।", both(104f, 84f), 10f, 2),
        part("nose", "nose", "नाक", "नाक", "We smell with our nose.", "हम नाक से सूँघते हैं।", listOf(120f to 98f), 8f, 2),
        part("mouth", "mouth", "मुँह", "मुँह", "We eat and talk with our mouth.", "हम मुँह से खाते और बोलते हैं।", listOf(120f to 114f), 10f, 2),
        part("ear", "ear", "कान", "कान", "We hear with our ears.", "हम कानों से सुनते हैं।", both(75f, 84f), 11f, 2),
        part("hand", "hand", "हाथ", "हाथ", "We hold things with our hands.", "हम हाथों से चीज़ें पकड़ते हैं।", both(57f, 300f), 14f, 2),
        part("foot", "foot", "पैर", "पैर", "We stand on our feet.", "हम पैरों पर खड़े होते हैं।", both(96f, 402f), 13f, 2),
        part("tummy", "tummy", "पेट", "पेट", "Our food goes into the tummy.", "खाना हमारे पेट में जाता है।", listOf(120f to 212f), 22f, 2),
        part("neck", "neck", "गर्दन", "गर्दन", "The neck holds up our head.", "गर्दन हमारे सिर को सँभालती है।", listOf(120f to 137f), 7f, 4),
        part("shoulder", "shoulder", "कंधा", "कंधे", "We carry a school bag on our shoulders.", "स्कूल बैग हम कंधों पर टाँगते हैं।", both(86f, 152f), 12f, 4),
        part("arm", "arm", "बाँह", "बाँह", "Our arms reach up high.", "बाँहों से हम ऊपर तक पहुँचते हैं।", both(70f, 210f), 11f, 4),
        part("elbow", "elbow", "कोहनी", "कोहनी", "The elbow lets our arm bend.", "कोहनी से बाँह मुड़ती है।", both(66f, 236f), 10f, 4),
        part("finger", "finger", "उँगली", "उँगली", "We have five fingers on each hand.", "हर हाथ में पाँच उँगलियाँ होती हैं।", both(54f, 318f), 8f, 4),
        part("leg", "leg", "टाँग", "टाँग", "We run and jump with our legs.", "हम टाँगों से दौड़ते और कूदते हैं।", both(100f, 302f), 11f, 4),
        part("knee", "knee", "घुटना", "घुटने", "The knee lets our leg bend.", "घुटने से टाँग मुड़ती है।", both(100f, 334f), 10f, 4),
        part("toes", "toes", "पैर की उँगलियाँ", "पैर की उँगलियों", "We have ten toes.", "हमारे पैरों में दस उँगलियाँ होती हैं।", both(84f, 410f), 8f, 4, plural = true),
        part("cheek", "cheek", "गाल", "गाल", "Our cheeks go pink when we laugh.", "हँसने पर गाल गुलाबी हो जाते हैं।", both(95f, 102f), 8f, 6),
        part("chin", "chin", "ठुड्डी", "ठुड्डी", "The chin is under the mouth.", "ठुड्डी मुँह के नीचे होती है।", listOf(120f to 125f), 6f, 6),
        part("eyebrow", "eyebrow", "भौंह", "भौंह", "Eyebrows keep sweat out of our eyes.", "भौंहें पसीने को आँखों में जाने से रोकती हैं।", both(104f, 66f), 8f, 6),
        part("chest", "chest", "छाती", "छाती", "Our heart is inside the chest.", "छाती के अंदर हमारा दिल होता है।", listOf(120f to 170f), 16f, 6),
        part("wrist", "wrist", "कलाई", "कलाई", "We wear a watch on the wrist.", "घड़ी हम कलाई पर पहनते हैं।", both(60f, 283f), 7f, 6),
        part("thumb", "thumb", "अँगूठा", "अँगूठे", "The thumb helps us hold a pencil.", "अँगूठे से हम पेंसिल पकड़ते हैं।", both(70f, 293f), 6f, 6),
        part("ankle", "ankle", "टखना", "टखने", "The ankle joins the foot to the leg.", "टखना पैर को टाँग से जोड़ता है।", both(101f, 389f), 7f, 6),
    )
}

/** The plant with the ground cut away, 240 × 360, so its roots show. */
object Plant {
    val parts: List<DiagramPart> = listOf(
        part("root", "root", "जड़", "जड़", "Roots hold the plant in the soil and drink water.", "जड़ें पौधे को मिट्टी में पकड़े रखती हैं और पानी पीती हैं।", listOf(120f to 306f), 24f, 2),
        part("stem", "stem", "तना", "तने", "The stem holds the plant up and carries water to the leaves.", "तना पौधे को सीधा रखता है और पत्तियों तक पानी पहुँचाता है।", listOf(120f to 242f), 9f, 2),
        part("leaf", "leaf", "पत्ती", "पत्ती", "Leaves make food for the plant from sunlight.", "पत्तियाँ धूप से पौधे का खाना बनाती हैं।", listOf(84f to 216f, 158f to 194f, 88f to 146f), 15f, 2),
        part("flower", "flower", "फूल", "फूल", "Flowers turn into fruits with seeds.", "फूल से फल और बीज बनते हैं।", listOf(120f to 78f), 26f, 2),
        part("fruit", "fruit", "फल", "फल", "The fruit keeps the seeds inside it.", "फल के अंदर बीज होते हैं।", listOf(163f to 160f), 15f, 2),
    )
}

object Diagrams {
    /** The part a tap at ([x], [y]) in drawing units lands on: the nearest spot it is inside. */
    fun at(d: Diagram, x: Float, y: Float): DiagramPart? = d.parts
        .flatMap { p -> p.spots.map { (sx, sy) -> p to ((sx - x) * (sx - x) + (sy - y) * (sy - y)) / ((p.radius + 4) * (p.radius + 4)) } }
        .filter { it.second <= 1f }
        .minByOrNull { it.second }?.first

    fun title(p: DiagramPart) = txt(p.name[Lang.EN].replaceFirstChar { it.uppercase() }, p.name[Lang.HI])

    fun thisIs(d: Diagram, p: DiagramPart): Txt {
        val whose = if (d.yours) "your" else "the"
        return txt(
            if (p.plural) "These are $whose ${p.name[Lang.EN]}!" else "This is $whose ${p.name[Lang.EN]}!",
            if (p.pluralHi) "ये ${p.name[Lang.HI]} हैं!" else "यह ${p.name[Lang.HI]} है!",
        )
    }

    fun tapThe(p: DiagramPart) = txt("Tap the ${p.name[Lang.EN]}!", "${p.whom} को छुओ!")

    fun tapSay(p: DiagramPart) = txt(
        "Where ${if (p.plural) "are" else "is"} the ${p.name[Lang.EN]}? Tap it on the picture!",
        "${p.name[Lang.HI]} कहाँ ${if (p.pluralHi) "हैं" else "है"}? तस्वीर पर छूकर बताओ!",
    )

    fun found(p: DiagramPart) = txt(
        "Yes! ${if (p.plural) "Those are" else "That is"} the ${p.name[Lang.EN]}!",
        "हाँ! ${if (p.pluralHi) "ये" else "यह"} ${p.name[Lang.HI]} ${if (p.pluralHi) "हैं" else "है"}!",
    )

    /** Said once [part] is found, after the cheer: "Yes! That is the knee!", what it does, then "knee means" … "घुटना". */
    fun foundLines(p: DiagramPart, langs: List<Lang>): List<Say> {
        val main = langs.first()
        return listOf(Say((found(p) + p.fact)[main], main)) + Bridge.lines(p.name, main, langs)
    }

    /** A tap on another part: what that one is, and the question again. */
    fun notThis(tapped: DiagramPart, wanted: DiagramPart) = txt(
        "${if (tapped.plural) "Those are" else "That is"} the ${tapped.name[Lang.EN]}. " +
            "Where ${if (wanted.plural) "are" else "is"} the ${wanted.name[Lang.EN]}?",
        "${if (tapped.pluralHi) "ये" else "यह"} ${tapped.name[Lang.HI]} ${if (tapped.pluralHi) "हैं" else "है"}। " +
            "${wanted.name[Lang.HI]} कहाँ ${if (wanted.pluralHi) "हैं" else "है"}?",
    )

    fun card(g: Gen, topic: Topic, d: Diagram): DiagramCard {
        val shown = d.parts.filter { it.fromAge <= g.level }
        val part = if (g.ordered) shown[g.inOrder("diagram.${d.name}", shown.size)] else g.pick(shown)
        return DiagramCard(g.id, topic, g.level, g.difficulty, d, part, showFact = g.level >= 4)
    }

    /** Mostly the parts newest at this age, so a six-year-old is asked about wrists, not noses. */
    fun tap(g: Gen, topic: Topic, d: Diagram): DiagramTap {
        val known = d.parts.filter { it.fromAge <= g.level }
        val newest = known.filter { it.fromAge >= known.maxOf { k -> k.fromAge } }
        val part = if (g.difficulty >= 3 && g.chance(0.7)) g.pick(newest) else g.pick(known)
        return DiagramTap(g.id, topic, g.level, g.difficulty, d, part)
    }
}
