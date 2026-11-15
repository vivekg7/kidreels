package com.crylo.kidreels.core

/*
 * Science: the plant's food and the water's journey as animations a child steps through,
 * and quizzes on what floats, what a magnet pulls, solids, liquids and gases, and what is
 * alive. Planets are cards.
 */

/** What to tap next in a lab: the scene draws each where it belongs. */
enum class LabTarget { SUN, CLOUD, AIR, LEAF, SKY, RIVER }

/**
 * One step of a lab: tap [target], and [said] is told while it animates. [simple] is the
 * same without the science word, for children below the lab's [Lab.wordsFrom] age.
 */
data class LabStep(val target: LabTarget, val name: Txt, val said: Txt, val simple: Txt, val label: Txt, val simpleLabel: Txt)

enum class Lab(val intro: Txt, val wordsFrom: Int, val steps: List<LabStep>, val outro: Txt) {
    PHOTOSYNTHESIS(
        txt("Plants make their own food! Let's see how. Tap the sun.", "पौधे अपना खाना ख़ुद बनाते हैं! चलो देखें कैसे। सूरज को छुओ।"),
        10,
        listOf(
            LabStep(
                LabTarget.SUN, txt("the sun", "सूरज"),
                txt("The leaves take in sunlight.", "पत्तियाँ धूप लेती हैं।"),
                txt("The leaves take in sunlight.", "पत्तियाँ धूप लेती हैं।"),
                txt("sunlight", "धूप"), txt("sunlight", "धूप"),
            ),
            LabStep(
                LabTarget.CLOUD, txt("the cloud", "बादल"),
                txt("Rain soaks the soil. The roots drink the water, and the stem carries it up to the leaves.", "बारिश से मिट्टी गीली होती है। जड़ें पानी पीती हैं, और तना उसे पत्तियों तक पहुँचाता है।"),
                txt("Rain soaks the soil. The roots drink the water, and the stem carries it up to the leaves.", "बारिश से मिट्टी गीली होती है। जड़ें पानी पीती हैं, और तना उसे पत्तियों तक पहुँचाता है।"),
                txt("water", "पानी"), txt("water", "पानी"),
            ),
            LabStep(
                LabTarget.AIR, txt("the air", "हवा"),
                txt("The leaves take in carbon dioxide from the air.", "पत्तियाँ हवा से कार्बन डाइऑक्साइड लेती हैं।"),
                txt("The leaves take in air from all around them.", "पत्तियाँ आसपास की हवा अंदर लेती हैं।"),
                txt("carbon dioxide", "कार्बन डाइऑक्साइड"), txt("air", "हवा"),
            ),
            LabStep(
                LabTarget.LEAF, txt("a leaf", "पत्ती"),
                txt("With sunlight, water and air, the leaf makes food for the plant: sugar! This is called photosynthesis.", "धूप, पानी और हवा से पत्ती पौधे का खाना बनाती है: शक्कर! इसे प्रकाश संश्लेषण कहते हैं।"),
                txt("With sunlight, water and air, the leaf makes food for the plant!", "धूप, पानी और हवा से पत्ती पौधे का खाना बनाती है!"),
                txt("sugar", "शक्कर"), txt("food", "खाना"),
            ),
        ),
        txt("And it gives out oxygen: the fresh air we breathe!", "और ऑक्सीजन छोड़ती है: वही ताज़ी हवा जिसमें हम साँस लेते हैं!"),
    ),
    WATER_CYCLE(
        txt("Where does rain come from? Tap the sun!", "बारिश कहाँ से आती है? सूरज को छुओ!"),
        9,
        listOf(
            LabStep(
                LabTarget.SUN, txt("the sun", "सूरज"),
                txt("The sun warms the sea. Some water rises into the air as vapour: evaporation.", "सूरज समुद्र को गरम करता है। कुछ पानी भाप बनकर ऊपर उठता है: वाष्पीकरण।"),
                txt("The sun warms the sea, and some water rises up into the sky.", "सूरज समुद्र को गरम करता है, और कुछ पानी ऊपर आसमान में उठता है।"),
                txt("evaporation", "वाष्पीकरण"), txt("water rises", "पानी ऊपर उठता है"),
            ),
            LabStep(
                LabTarget.SKY, txt("the sky", "आसमान"),
                txt("High up it is cold, so the vapour turns into tiny drops and makes a cloud: condensation.", "ऊपर ठंड होती है, इसलिए भाप छोटी-छोटी बूँदें बनकर बादल बनाती है: संघनन।"),
                txt("High up it is cold, and the water makes a cloud.", "ऊपर ठंड होती है, और पानी से बादल बनता है।"),
                txt("condensation", "संघनन"), txt("a cloud forms", "बादल बनता है"),
            ),
            LabStep(
                LabTarget.CLOUD, txt("the cloud", "बादल"),
                txt("The cloud gets heavy, and the drops fall as rain: precipitation.", "बादल भारी हो जाता है, और बूँदें बारिश बनकर गिरती हैं: वर्षण।"),
                txt("The cloud gets heavy, and it rains!", "बादल भारी हो जाता है, और बारिश होती है!"),
                txt("precipitation", "वर्षण"), txt("rain", "बारिश"),
            ),
            LabStep(
                LabTarget.RIVER, txt("the river", "नदी"),
                txt("Rivers carry the rain back to the sea: collection.", "नदियाँ बारिश का पानी वापस समुद्र तक ले जाती हैं: संग्रहण।"),
                txt("Rivers carry the rain back to the sea.", "नदियाँ बारिश का पानी वापस समुद्र तक ले जाती हैं।"),
                txt("collection", "संग्रहण"), txt("back to the sea", "वापस समुद्र में"),
            ),
        ),
        txt("And it all starts again. This is the water cycle!", "और यह सब फिर से शुरू होता है। इसे जल चक्र कहते हैं!"),
    ),
    ;

    val title: Txt get() = if (this == PHOTOSYNTHESIS) txt("How plants make food", "पौधे खाना कैसे बनाते हैं") else txt("The water cycle", "जल चक्र")
}

/** An animation stepped through by taps: the plant making food, the water going round. */
data class LabReel(
    override val id: Long,
    override val topic: Topic,
    override val level: Int,
    override val difficulty: Int,
    val lab: Lab,
) : Reel {
    /** The science words (evaporation, carbon dioxide) from the lab's age; plain words before. */
    val words: Boolean get() = level >= lab.wordsFrom
    override val needsSolving get() = false
    override val prompt: Txt get() = lab.title
    override val voice: Lang? get() = null

    fun said(step: LabStep): Txt = if (words) step.said else step.simple

    fun label(step: LabStep): Txt = if (words) step.label else step.simpleLabel
}

/** Something to drop in water or hold to a magnet, with whether it floats or sticks and why. */
data class Item(val emoji: String, val name: Txt, val feminine: Boolean, val yes: Boolean, val why: Txt)

enum class State(val word: Txt, val rule: Txt) {
    SOLID(txt("Solid", "ठोस"), txt("A solid keeps its own shape.", "ठोस का अपना आकार होता है।")),
    LIQUID(txt("Liquid", "द्रव"), txt("A liquid flows and takes the shape of its cup.", "द्रव बहता है और बर्तन का आकार ले लेता है।")),
    GAS(txt("Gas", "गैस"), txt("A gas spreads out to fill all the space it has.", "गैस फैलकर पूरी जगह भर देती है।")),
}

/** A planet, or the Sun or the Moon, drawn as a ball of [colours] (bands, inside out). */
data class Planet(val key: String, val name: Txt, val fact: Txt, val order: Txt?, val colours: List<Long>, val size: Float, val rings: Boolean = false)

/** One body of the solar system, in order from the Sun. */
data class PlanetCard(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val planet: Planet,
) : Reel {
    override val topic get() = Topic.PLANETS
    override val needsSolving get() = false
    override val prompt: Txt get() = planet.name
    override val voice: Lang? get() = null
}

object Science {
    private fun item(emoji: String, en: String, hi: String, yes: Boolean, whyEn: String, whyHi: String, feminine: Boolean = false) =
        Item(emoji, txt(en, hi), feminine, yes, txt(whyEn, whyHi))

    val floaters = listOf(
        item("🪵", "wood", "लकड़ी", true, "Wood is light for its size, so it floats.", "लकड़ी अपने आकार के हिसाब से हल्की होती है, इसलिए तैरती है।", feminine = true),
        item("🍃", "leaf", "पत्ता", true, "A leaf is thin and light, so it floats.", "पत्ता पतला और हल्का होता है, इसलिए तैरता है।"),
        item("🥥", "coconut", "नारियल", true, "A coconut has air inside, so it floats.", "नारियल के अंदर हवा होती है, इसलिए तैरता है।"),
        item("🍎", "apple", "सेब", true, "An apple has air inside it, so it floats!", "सेब के अंदर हवा होती है, इसलिए तैरता है!"),
        item("🧽", "sponge", "स्पंज", true, "A sponge is full of tiny air holes, so it floats.", "स्पंज में हवा के छोटे-छोटे छेद होते हैं, इसलिए तैरता है।"),
        item("🪶", "feather", "पंख", true, "A feather is very light, so it floats.", "पंख बहुत हल्का होता है, इसलिए तैरता है।"),
        item("🏐", "ball", "गेंद", true, "A ball is full of air, so it floats.", "गेंद में हवा भरी होती है, इसलिए तैरती है।", feminine = true),
        item("🪨", "stone", "पत्थर", false, "A stone is heavy for its size, so it sinks.", "पत्थर अपने आकार के हिसाब से भारी होता है, इसलिए डूब जाता है।"),
        item("🔑", "key", "चाबी", false, "A key is made of metal, so it sinks.", "चाबी धातु की बनी होती है, इसलिए डूब जाती है।", feminine = true),
        item("🪙", "coin", "सिक्का", false, "A coin is made of metal, so it sinks.", "सिक्का धातु का होता है, इसलिए डूब जाता है।"),
        item("⚓", "anchor", "लंगर", false, "An anchor is heavy, so it sinks and holds the ship still.", "लंगर भारी होता है, इसलिए डूबकर जहाज़ को रोके रखता है।"),
        item("💍", "ring", "अँगूठी", false, "A ring is made of metal, so it sinks.", "अँगूठी धातु की होती है, इसलिए डूब जाती है।", feminine = true),
        item("🧱", "brick", "ईंट", false, "A brick is heavy for its size, so it sinks.", "ईंट अपने आकार के हिसाब से भारी होती है, इसलिए डूब जाती है।", feminine = true),
    )

    val magnetic = listOf(
        item("📎", "paper clip", "पेपर क्लिप", true, "A paper clip is made of iron, and a magnet pulls iron.", "पेपर क्लिप लोहे की होती है, और चुंबक लोहे को खींचता है।", feminine = true),
        item("🔩", "nut and bolt", "नट-बोल्ट", true, "A nut and bolt is made of iron, and a magnet pulls iron.", "नट-बोल्ट लोहे का होता है, और चुंबक लोहे को खींचता है।"),
        item("🧷", "safety pin", "सेफ़्टी पिन", true, "A safety pin is made of steel, and steel has iron in it.", "सेफ़्टी पिन स्टील की होती है, और स्टील में लोहा होता है।", feminine = true),
        item("🪝", "hook", "हुक", true, "This hook is made of iron, and a magnet pulls iron.", "यह हुक लोहे का है, और चुंबक लोहे को खींचता है।"),
        item("✂️", "scissors", "कैंची", true, "The blades of scissors are steel, and steel has iron in it.", "कैंची की धार स्टील की होती है, और स्टील में लोहा होता है।", feminine = true),
        item("🪵", "wood", "लकड़ी", false, "A magnet pulls only iron and steel. Wood has no iron.", "चुंबक सिर्फ़ लोहे और स्टील को खींचता है। लकड़ी में लोहा नहीं होता।", feminine = true),
        item("🧸", "teddy bear", "टेडी बियर", false, "A magnet pulls only iron and steel. A teddy is cloth and cotton.", "चुंबक सिर्फ़ लोहे और स्टील को खींचता है। टेडी कपड़े और रुई का होता है।"),
        item("🍎", "apple", "सेब", false, "A magnet pulls only iron and steel. An apple has no iron in it.", "चुंबक सिर्फ़ लोहे और स्टील को खींचता है। सेब में लोहा नहीं होता।"),
        item("📄", "paper", "काग़ज़", false, "A magnet pulls only iron and steel. Paper is made from wood.", "चुंबक सिर्फ़ लोहे और स्टील को खींचता है। काग़ज़ लकड़ी से बनता है।"),
        item("🎈", "balloon", "गुब्बारा", false, "A magnet pulls only iron and steel. A balloon is rubber.", "चुंबक सिर्फ़ लोहे और स्टील को खींचता है। गुब्बारा रबर का होता है।"),
        item("🖍️", "crayon", "क्रेयॉन", false, "A magnet pulls only iron and steel. A crayon is wax.", "चुंबक सिर्फ़ लोहे और स्टील को खींचता है। क्रेयॉन मोम का होता है।"),
    )

    val states: List<Pair<Item, State>> = listOf(
        item("🧊", "ice", "बर्फ़", true, "", "", feminine = true) to State.SOLID,
        item("🪨", "a stone", "पत्थर", true, "", "") to State.SOLID,
        item("📕", "a book", "किताब", true, "", "", feminine = true) to State.SOLID,
        item("🧱", "a brick", "ईंट", true, "", "", feminine = true) to State.SOLID,
        item("🥄", "a spoon", "चम्मच", true, "", "") to State.SOLID,
        item("💧", "water", "पानी", true, "", "") to State.LIQUID,
        item("🥛", "milk", "दूध", true, "", "") to State.LIQUID,
        item("🧃", "juice", "जूस", true, "", "") to State.LIQUID,
        item("🍯", "honey", "शहद", true, "", "") to State.LIQUID,
        item("☕", "tea", "चाय", true, "", "", feminine = true) to State.LIQUID,
        item("🌬️", "air", "हवा", true, "", "", feminine = true) to State.GAS,
        item("♨️", "steam", "भाप", true, "", "", feminine = true) to State.GAS,
    )

    val living = listOf("🐶", "🐱", "🐘", "🦋", "🐟", "🌳", "🌻", "🐦", "🐄", "🌵")
    val notLiving = listOf("🚗", "⚽", "🪑", "🧸", "⏰", "🎈", "📱", "☂️", "🔑", "🪨")

    private fun planet(key: String, en: String, hi: String, factEn: String, factHi: String, orderEn: String?, orderHi: String?, colours: List<Long>, size: Float, rings: Boolean = false) =
        Planet(key, txt(en, hi), txt(factEn, factHi), orderEn?.let { txt(it, orderHi!!) }, colours, size, rings)

    val planets = listOf(
        planet("sun", "The Sun", "सूर्य", "The Sun is a star. It gives us light and heat.", "सूर्य एक तारा है। यह हमें रोशनी और गर्मी देता है।", null, null, listOf(0xFFFFF176, 0xFFFFCA28, 0xFFFF9800), 1f),
        planet("mercury", "Mercury", "बुध", "Mercury is the smallest planet, and the closest to the Sun.", "बुध सबसे छोटा ग्रह है, और सूर्य के सबसे पास है।", "the 1st planet from the Sun", "सूर्य से पहला ग्रह", listOf(0xFFBDBDBD, 0xFF9E9E9E), 0.35f),
        planet("venus", "Venus", "शुक्र", "Venus is the hottest planet, covered in thick clouds.", "शुक्र सबसे गरम ग्रह है, घने बादलों से ढका।", "the 2nd planet from the Sun", "सूर्य से दूसरा ग्रह", listOf(0xFFFFE0B2, 0xFFFFB74D), 0.5f),
        planet("earth", "Earth", "पृथ्वी", "Earth is our home: the only planet we know with life.", "पृथ्वी हमारा घर है: अकेला ग्रह जहाँ हम जीवन जानते हैं।", "the 3rd planet from the Sun", "सूर्य से तीसरा ग्रह", listOf(0xFF42A5F5, 0xFF1E88E5), 0.52f),
        planet("moon", "The Moon", "चंद्रमा", "The Moon goes round the Earth. It shines with the Sun's light.", "चंद्रमा पृथ्वी के चारों ओर घूमता है। यह सूर्य की रोशनी से चमकता है।", null, null, listOf(0xFFEEEEEE, 0xFFBDBDBD), 0.3f),
        planet("mars", "Mars", "मंगल", "Mars is red with dust, so it is called the red planet.", "मंगल धूल से लाल है, इसलिए इसे लाल ग्रह कहते हैं।", "the 4th planet from the Sun", "सूर्य से चौथा ग्रह", listOf(0xFFE57373, 0xFFD84315), 0.42f),
        planet("jupiter", "Jupiter", "बृहस्पति", "Jupiter is the biggest planet of all.", "बृहस्पति सबसे बड़ा ग्रह है।", "the 5th planet from the Sun", "सूर्य से पाँचवाँ ग्रह", listOf(0xFFFFE0B2, 0xFFD7A86E, 0xFFBF8B5E), 0.95f),
        planet("saturn", "Saturn", "शनि", "Saturn has beautiful rings of ice and rock.", "शनि के चारों ओर बर्फ़ और चट्टानों के सुंदर छल्ले हैं।", "the 6th planet from the Sun", "सूर्य से छठा ग्रह", listOf(0xFFFFF3C4, 0xFFE6C77A), 0.75f, rings = true),
        planet("uranus", "Uranus", "अरुण", "Uranus spins lying on its side.", "अरुण अपनी करवट पर लेटकर घूमता है।", "the 7th planet from the Sun", "सूर्य से सातवाँ ग्रह", listOf(0xFFB2EBF2, 0xFF4DD0E1), 0.6f),
        planet("neptune", "Neptune", "वरुण", "Neptune is the farthest planet, and very windy.", "वरुण सबसे दूर का ग्रह है, और वहाँ बहुत तेज़ हवाएँ चलती हैं।", "the 8th planet from the Sun", "सूर्य से आठवाँ ग्रह", listOf(0xFF7986CB, 0xFF3949AB), 0.58f),
    )

    fun lab(g: Gen, topic: Topic, lab: Lab) = LabReel(g.id, topic, g.level, g.difficulty, lab)

    fun planet(g: Gen): PlanetCard =
        PlanetCard(g.id, g.level, g.difficulty, if (g.ordered) planets[g.inOrder("planets", planets.size)] else g.pick(planets))

    fun planetSay(p: Planet): Txt = (p.order?.let { o -> p.name + txt("is ${o[Lang.EN]}.", "${o[Lang.HI]} है।") } ?: p.name) + p.fact

    private fun Item.agree(m: String, f: String) = if (feminine) f else m

    fun floatSink(g: Gen): Quiz {
        val it = g.pick(floaters)
        val choices = listOf(Choice.Word(txt("Float", it.agree("तैरेगा", "तैरेगी"))), Choice.Word(txt("Sink", it.agree("डूबेगा", "डूबेगी"))))
        val answer = if (it.yes) 0 else 1
        val ask = txt("Will the ${it.name[Lang.EN]} float or sink?", "${it.name[Lang.HI]} ${it.agree("तैरेगा", "तैरेगी")} या ${it.agree("डूबेगा", "डूबेगी")}?")
        return Quiz(
            g.id, Topic.FLOAT_SINK, g.level, g.difficulty, ask, Stage.Tank(it.emoji, it.yes), choices, answer,
            reveal = it.why, say = ask + txt("What do you think?", "तुम्हें क्या लगता है?"),
            whyNot = whyNot(choices, answer) { _ -> txt("Not this time!", "इस बार नहीं!") + it.why },
        )
    }

    fun magnet(g: Gen): Quiz {
        val it = g.pick(magnetic)
        val choices = listOf(Choice.Word(txt("Yes", "हाँ")), Choice.Word(txt("No", "नहीं")))
        val answer = if (it.yes) 0 else 1
        val ask = txt("Will the magnet pull the ${it.name[Lang.EN]}?", "क्या चुंबक ${it.name[Lang.HI]} को खींचेगा?")
        return Quiz(
            g.id, Topic.MAGNETS, g.level, g.difficulty, ask, Stage.Magnet(it.emoji, it.yes), choices, answer,
            reveal = it.why, say = ask + txt("What do you think?", "तुम्हें क्या लगता है?"),
            whyNot = whyNot(choices, answer) { _ -> txt("Not this time!", "इस बार नहीं!") + it.why },
        )
    }

    fun state(g: Gen): Quiz {
        val (it, state) = g.pick(states)
        val choices = State.entries.map { Choice.Word(it.word) }
        val ask = txt("Is ${it.name[Lang.EN]} a solid, a liquid or a gas?", "${it.name[Lang.HI]} ठोस है, द्रव है या गैस?")
        return Quiz(
            g.id, Topic.STATES, g.level, g.difficulty, ask,
            // "This is a stone", but "This is ice": a thing has an article, stuff does not.
            Stage.Picture(it.emoji, Word(it.emoji, txt(it.name[Lang.EN].removePrefix("a "), it.name[Lang.HI]), article = if (it.name[Lang.EN].startsWith("a ")) null else "")), choices, state.ordinal,
            reveal = txt("${it.name[Lang.EN].replaceFirstChar(Char::uppercase)} is a ${state.word[Lang.EN].lowercase()}!", "${it.name[Lang.HI]} ${state.word[Lang.HI]} है!") + state.rule,
            say = ask + Phrases.tapIt,
            whyNot = whyNot(State.entries, state.ordinal) { other -> other.rule + txt("Is that true of ${it.name[Lang.EN]}?", "क्या ${it.name[Lang.HI]} ऐसा है?") },
        )
    }

    fun alive(g: Gen): Quiz {
        val yes = g.pick(living)
        val (choices, answer) = g.shuffleIn(yes, notLiving.shuffled(g.random).take(g.optionCount - 1))
        fun name(e: String) = Words.of(e)!!.name
        val ask = g.words(txt("Which one is alive?", "इनमें से सजीव कौन है?").simpler("Which one is alive?", "इनमें से ज़िंदा कौन है?"))
        return Quiz(
            g.id, Topic.ALIVE, g.level, g.difficulty, ask, Stage.None, choices.map { Choice.Token(it) }, answer,
            reveal = g.words(
                txt("Yes! The ${name(yes)[Lang.EN]} is alive. Living things grow, eat and breathe.", "हाँ! ${name(yes)[Lang.HI]} सजीव है। सजीव चीज़ें बढ़ती हैं, खाती-पीती हैं और साँस लेती हैं।")
                    .simpler("Yes! The ${name(yes)[Lang.EN]} is alive. Living things grow, eat and breathe.", "हाँ! ${name(yes)[Lang.HI]} ज़िंदा है। ज़िंदा चीज़ें बढ़ती हैं, खाती-पीती हैं और साँस लेती हैं।"),
            ),
            say = ask + Phrases.tapIt,
            whyNot = whyNot(choices, answer) {
                g.words(
                    txt("The ${name(it)[Lang.EN]} is not alive: it does not grow, eat or breathe.", "${name(it)[Lang.HI]} निर्जीव है: न बढ़ना, न खाना, न साँस लेना।")
                        .simpler("The ${name(it)[Lang.EN]} is not alive: it does not grow, eat or breathe.", "${name(it)[Lang.HI]} ज़िंदा नहीं है: न बढ़ना, न खाना, न साँस लेना।"),
                )
            },
        )
    }
}
