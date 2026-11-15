package com.crylo.kidreels.core

/**
 * A part of the body on the drawn child (art/drawn/body.svg, 240 × 440 units): where it
 * is ([spots], one per side, and [radius] around each), so a tap can be told apart and
 * the part lit up. Emoji show an eye or a hand well, but not an elbow, a knee or a
 * shoulder: on the whole child every part is where it really is.
 *
 * [whom] is the Hindi object form ("घुटने को"); [plural] and [pluralHi] are separate, as
 * "hair" takes "is" and "बाल" takes "हैं".
 */
data class BodyPart(
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

/** "This is your knee": a part lit up on the child, named, and from age 4 what it does. */
data class BodyCard(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val part: BodyPart,
    val showFact: Boolean,
) : Reel {
    override val topic get() = Topic.BODY_PARTS
    override val needsSolving get() = false
    override val prompt: Txt get() = Body.title(part)
    override val voice: Lang? get() = null
}

/** "Tap the knee!": find [part] on the child. */
data class BodyTap(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val part: BodyPart,
) : Reel {
    override val topic get() = Topic.BODY_TAP
    override val needsSolving get() = true
    override val prompt: Txt get() = Body.tapThe(part)
    override val voice: Lang? get() = null
}

object Body {
    const val WIDTH = 240f
    const val HEIGHT = 440f

    private fun both(x: Float, y: Float) = listOf(x to y, WIDTH - x to y)

    private fun part(
        key: String, en: String, hi: String, whom: String, factEn: String, factHi: String,
        spots: List<Pair<Float, Float>>, radius: Float, fromAge: Int, plural: Boolean = false, pluralHi: Boolean = plural,
    ) = BodyPart(key, txt(en, hi), whom, txt(factEn, factHi), spots, radius, fromAge, plural, pluralHi)

    /** In teaching order: what a two-year-old points to first, joints and the finer parts later. */
    val parts: List<BodyPart> = listOf(
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

    fun upTo(age: Int) = parts.filter { it.fromAge <= age }

    /** The part a tap at ([x], [y]) in drawing units lands on: the nearest spot it is inside. */
    fun at(x: Float, y: Float): BodyPart? = parts
        .flatMap { p -> p.spots.map { (sx, sy) -> p to ((sx - x) * (sx - x) + (sy - y) * (sy - y)) / ((p.radius + 4) * (p.radius + 4)) } }
        .filter { it.second <= 1f }
        .minByOrNull { it.second }?.first

    fun title(p: BodyPart) = txt(p.name[Lang.EN].replaceFirstChar { it.uppercase() }, p.name[Lang.HI])

    fun thisIs(p: BodyPart) = txt(
        if (p.plural) "These are your ${p.name[Lang.EN]}!" else "This is your ${p.name[Lang.EN]}!",
        if (p.pluralHi) "ये ${p.name[Lang.HI]} हैं!" else "यह ${p.name[Lang.HI]} है!",
    )

    fun tapThe(p: BodyPart) = txt("Tap the ${p.name[Lang.EN]}!", "${p.whom} को छुओ!")

    fun tapSay(p: BodyPart) = txt(
        "Where ${if (p.plural) "are" else "is"} the ${p.name[Lang.EN]}? Tap it on the picture!",
        "${p.name[Lang.HI]} कहाँ ${if (p.pluralHi) "हैं" else "है"}? तस्वीर पर छूकर बताओ!",
    )

    fun found(p: BodyPart) = txt(
        "Yes! ${if (p.plural) "Those are" else "That is"} the ${p.name[Lang.EN]}!",
        "हाँ! ${if (p.pluralHi) "ये" else "यह"} ${p.name[Lang.HI]} ${if (p.pluralHi) "हैं" else "है"}!",
    )

    /** A tap on another part: what that one is, and the question again. */
    fun notThis(tapped: BodyPart, wanted: BodyPart) = txt(
        "${if (tapped.plural) "Those are" else "That is"} the ${tapped.name[Lang.EN]}. " +
            "Where ${if (wanted.plural) "are" else "is"} the ${wanted.name[Lang.EN]}?",
        "${if (tapped.pluralHi) "ये" else "यह"} ${tapped.name[Lang.HI]} ${if (tapped.pluralHi) "हैं" else "है"}। " +
            "${wanted.name[Lang.HI]} कहाँ ${if (wanted.pluralHi) "हैं" else "है"}?",
    )

    fun card(g: Gen): BodyCard {
        val shown = upTo(g.level)
        val part = if (g.ordered) shown[g.inOrder("body", shown.size)] else g.pick(shown)
        return BodyCard(g.id, g.level, g.difficulty, part, showFact = g.level >= 4)
    }

    /** Mostly the parts newest at this age, so a six-year-old is asked about wrists, not noses. */
    fun tap(g: Gen): BodyTap {
        val known = upTo(g.level)
        val newest = known.filter { it.fromAge >= known.maxOf { k -> k.fromAge } }
        val part = if (g.difficulty >= 3 && g.chance(0.7)) g.pick(newest) else g.pick(known)
        return BodyTap(g.id, g.level, g.difficulty, part)
    }
}
