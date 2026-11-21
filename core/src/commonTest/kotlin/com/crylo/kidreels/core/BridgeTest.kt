package com.crylo.kidreels.core

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

/** "A for Apple" · "Apple means" … "सेब": the word said again in the child's other language. */
class BridgeTest {
    private val enHi = listOf(Lang.EN, Lang.HI)
    private val hiEn = listOf(Lang.HI, Lang.EN)

    private fun letter(lang: Lang, glyph: String) =
        LetterCard(1, 3, 1, lang, Alphabets.of(lang).first { it.glyph == glyph })

    private fun List<Say>.texts() = map { it.text to it.lang }

    @Test
    fun theAlphabetCardSaysWhatTheWordMeans() {
        assertEquals(
            listOf(
                "A" to Lang.EN, "for" to Lang.EN, "Apple" to Lang.EN,
                "Apple means" to Lang.EN, "सेब" to Lang.HI,
                "Say it with me!" to Lang.EN,
                "A" to Lang.EN, "for" to Lang.EN, "Apple" to Lang.EN,
            ),
            letter(Lang.EN, "A").intro(enHi).texts(),
        )
        // The English alphabet speaks English whatever the main language, and so does its bridge.
        assertEquals(letter(Lang.EN, "A").intro(enHi), letter(Lang.EN, "A").intro(hiEn))
    }

    @Test
    fun theHindiAlphabetBridgesInHindi() {
        assertEquals(
            listOf(
                "अ" to Lang.HI, "से" to Lang.HI, "अनानास" to Lang.HI,
                "अनानास यानी" to Lang.HI, "Pineapple" to Lang.EN,
                "मेरे साथ बोलो!" to Lang.HI,
                "अ" to Lang.HI, "से" to Lang.HI, "अनानास" to Lang.HI,
            ),
            letter(Lang.HI, "अ").intro(enHi).texts(),
        )
    }

    @Test
    fun hindiFirstFramesInHindi() {
        val elephant = WordCard(1, Topic.ANIMALS, 3, 1, Words.animals.first { it.emoji == "🐘" }, showFact = false)
        assertEquals(
            listOf("यह हाथी है!" to Lang.HI, "हाथी यानी" to Lang.HI, "elephant" to Lang.EN, "मेरे साथ बोलो!" to Lang.HI, "हाथी" to Lang.HI),
            elephant.intro(hiEn).texts(),
        )
        assertEquals(
            listOf("This is an elephant!" to Lang.EN, "elephant means" to Lang.EN, "हाथी" to Lang.HI, "Say it with me!" to Lang.EN, "Elephant" to Lang.EN),
            elephant.intro(enHi).texts(),
        )
    }

    @Test
    fun theNumberCardBridgesItsWord() {
        val one = NumberCard(1, 4, 1, 1).intro(enHi).texts()
        assertEquals(
            listOf(
                "1" to Lang.EN, "O" to Lang.EN, "N" to Lang.EN, "E" to Lang.EN, "one" to Lang.EN, "1 elephant" to Lang.EN,
                "one means" to Lang.EN, "एक" to Lang.HI, "Say it with me!" to Lang.EN, "one" to Lang.EN,
            ),
            one,
        )
    }

    @Test
    fun oneLanguageSaysWhatItAlwaysDid() {
        assertEquals(
            listOf("A", "for", "Apple", "Say it with me!", "A", "for", "Apple"),
            letter(Lang.EN, "A").intro(listOf(Lang.EN)).map { it.text },
        )
        val triangle = ShapeCard(1, 6, 1, Shapes.of(ShapeKind.TRIANGLE).forAge(6), 0)
        assertEquals(listOf("This is a triangle. 3 sides, 3 corners. Tap it and count the corners!"), triangle.intro(listOf(Lang.EN)).map { it.text })
        assertEquals(listOf("यह त्रिभुज है। 3 भुजाएँ, 3 कोने। छूकर कोने गिनो!"), triangle.intro(listOf(Lang.HI)).map { it.text })
        val earth = PlanetCard(1, 8, 1, Science.planets.first { it.key == "earth" })
        assertEquals(listOf(Science.planetSay(earth.planet)[Lang.EN]), earth.intro(listOf(Lang.EN)).map { it.text })
        for (lang in Lang.entries) for (reel in everyReel(listOf(lang))) {
            assertTrue(said(reel, listOf(lang)).all { it.alsoNeeds == null && it.lang == (reel.voice ?: lang) }, "${reel.topic} bridged with one language")
        }
    }

    @Test
    fun aShapeIsBridgedInTheWordsForTheChildsAge() {
        fun bridged(age: Int) = ShapeCard(1, age, 1, Shapes.of(ShapeKind.TRIANGLE).forAge(age), 0).intro(enHi).first { it.lang == Lang.HI }.text
        assertEquals("तिकोना", bridged(4))
        assertEquals("त्रिभुज", bridged(7))
        assertEquals(
            listOf("This is a triangle.", "Triangle means", "तिकोना", "3 sides, 3 corners. Tap it and count the corners!"),
            ShapeCard(1, 4, 1, Shapes.of(ShapeKind.TRIANGLE).forAge(4), 0).intro(enHi).map { it.text },
        )
    }

    @Test
    fun aRightAnswerIsBridged() {
        val quiz = Templates.of(Topic.COLOURS).make(Gen(2, 1, Random(3), enHi, 1, Progress(MapStore()))) as Quiz
        val colour = Colours.all.first { quiz.reveal == Phrases.yesColour(it) }
        assertEquals(
            listOf("Yes! That is ${colour.name[Lang.EN]}!" to Lang.EN, "${colour.name[Lang.EN]} means" to Lang.EN, colour.name[Lang.HI] to Lang.HI),
            quiz.revealLines(enHi).texts(),
        )
        assertEquals(listOf("Yes! That is ${colour.name[Lang.EN]}!"), quiz.revealLines(listOf(Lang.EN)).map { it.text })
    }

    @Test
    fun aQuizAboutOneLanguagesLettersDoesNotBridgeItsPictureBeforeTheAnswer() {
        for (reel in everyReel(enHi)) {
            if (reel !is Quiz || reel.topic != Topic.FIRST_LETTER) continue
            val tapped = reel.pictureTapped(enHi)
            assertTrue(tapped.all { it.lang == reel.voice }, "\"ball means गेंद\" would send the child to ग: $tapped")
        }
    }

    @Test
    fun aWordThatIsTheSameOrMissingIsNotBridged() {
        assertEquals(emptyList(), Bridge.lines(Txt.same("3"), Lang.EN, enHi))
        assertEquals(emptyList(), Bridge.lines(Txt(mapOf(Lang.EN to "apple")), Lang.EN, enHi), "no Hindi: it would fall back to English")
        assertEquals(emptyList(), Bridge.lines(txt("bus", "बस"), Lang.EN, enHi))
        assertEquals(emptyList(), Bridge.lines(txt("apple", "सेब"), Lang.EN, listOf(Lang.EN)))
    }

    @Test
    fun borrowedWordsAreToldFromTranslations() {
        val borrowed = listOf(
            "car" to "कार", "bus" to "बस", "TV" to "टीवी", "computer" to "कंप्यूटर", "zebra" to "ज़ेबरा", "giraffe" to "जिराफ़",
            "jungle" to "जंगल", "Gas" to "गैस", "Pyramid" to "पिरामिड", "Cone" to "कोन", "strawberry" to "स्ट्रॉबेरी",
            "croissant" to "क्रोसाँ", "unicorn" to "यूनिकॉर्न", "Christmas tree" to "क्रिसमस ट्री", "Xmas tree" to "क्रिसमस ट्री",
            "train engine" to "इंजन", "nut and bolt" to "नट-बोल्ट", "ice cream" to "आइसक्रीम", "Juice" to "जूस", "Yo-yo" to "यो-यो",
            "headphones" to "हेडफ़ोन", "doughnut" to "डोनट", "slice of pizza" to "पिज़्ज़ा", "school" to "स्कूल", "cheese" to "चीज़",
            "phone" to "फ़ोन", "T-shirt" to "टी-शर्ट", "jeans" to "जींस", "taxi" to "टैक्सी", "Engine" to "इंजन",
            "kiwi" to "कीवी", "cherries" to "चेरी", "Van" to "वैन",
        )
        val translated = listOf(
            "apple" to "सेब", "elephant" to "हाथी", "owl" to "उल्लू", "bucket" to "बाल्टी", "Parabola" to "परवलय", "door" to "दरवाज़ा",
            "snake" to "साँप", "tent" to "तंबू", "bamboo" to "बाँस", "star" to "तारा", "flower" to "फूल", "brown" to "भूरा",
            "chest" to "छाती", "stem" to "तना", "eight" to "आठ", "nine" to "नौ", "police car" to "पुलिस की गाड़ी", "Uranus" to "अरुण",
            "bear" to "भालू", "grass" to "घास", "one" to "एक", "Triangle" to "तिकोना", "red" to "लाल", "knee" to "घुटना",
        )
        borrowed.forEach { (en, hi) -> assertTrue(Bridge.borrowed(en, hi), "$en / $hi is borrowed") }
        translated.forEach { (en, hi) -> assertFalse(Bridge.borrowed(en, hi), "$en / $hi is a translation") }
    }

    /*
     * Every reel, every age, both ways round: each line is in its own voice's script, so
     * no voice reads the other script (silence beats Devanagari read by an English voice),
     * and every bridge is a real second word.
     */

    @Test
    fun everyLineIsInItsOwnVoicesScript() {
        for (langs in listOf(enHi, hiEn)) for (reel in everyReel(langs)) for (line in said(reel, langs)) {
            when (line.lang) {
                Lang.EN -> assertFalse(line.text.any(::devanagari), "${reel.topic}: English voice reads \"${line.text}\"")
                Lang.HI -> assertFalse(line.text.any(::latin), "${reel.topic}: Hindi voice reads \"${line.text}\"")
            }
        }
    }

    @Test
    fun everyBridgeIsOneRealWordInTheOtherLanguage() {
        var bridges = 0
        for (langs in listOf(enHi, hiEn)) for (reel in everyReel(langs)) {
            val lines = said(reel, langs)
            val halves = lines.filter { it.alsoNeeds != null }
            assertEquals(0, halves.size % 2, "${reel.topic}: half a bridge in $lines")
            for ((frame, word) in halves.chunked(2).map { it[0] to it[1] }) {
                bridges++
                assertEquals(word.lang, frame.alsoNeeds, "${reel.topic}: $frame")
                assertEquals(frame.lang, word.alsoNeeds, "${reel.topic}: $word")
                assertTrue(frame.lang != word.lang && word.pauseMillis == Bridge.PAUSE, "${reel.topic}: $frame $word")
                assertTrue(word.text.isNotBlank(), "${reel.topic}: empty bridge after \"${frame.text}\"")
                assertFalse(frame.text.contains(word.text, ignoreCase = true), "${reel.topic}: \"${frame.text}\" … \"${word.text}\" says the same word")
                assertTrue(frame.text.endsWith(" means") || frame.text.endsWith(" यानी"), "${reel.topic}: frame \"${frame.text}\"")
            }
            // One bridge per reel, into each other language: frames are said in the reel's voice.
            val frames = reel.intro(langs).count { it.alsoNeeds != null && it.lang == (reel.voice ?: langs.first()) }
            assertTrue(frames <= langs.size - 1, "${reel.topic}: more than one bridge in ${reel.intro(langs)}")
        }
        assertTrue(bridges > 1000, "only $bridges bridges: has the bridge gone?")
    }

    /** Everything a reel can say by itself: its intro, a right answer, a tap on its picture. */
    private fun said(reel: Reel, langs: List<Lang>): List<Say> = buildList {
        addAll(reel.intro(langs))
        if (reel is Quiz) {
            addAll(reel.revealLines(langs))
            if (reel.stage is Stage.Picture) addAll(reel.pictureTapped(langs))
        }
        if (reel is DiagramTap) addAll(Diagrams.foundLines(reel.part, langs))
        if (reel is WordCard) addAll(reel.word.said(reel.voice ?: langs.first(), langs))
    }

    private fun devanagari(c: Char) = c in 'ऀ'..'ॿ'

    private fun latin(c: Char) = c in 'a'..'z' || c in 'A'..'Z'

    private val cursor = Progress(MapStore())

    /** Every template the child's [langs] allow, at every age and difficulty. */
    private fun everyReel(langs: List<Lang>, seeds: Int = 8): Sequence<Reel> = sequence {
        for (t in Templates.all) for (level in t.ages) for (d in 1..5) for (seed in 0 until seeds) {
            val usable = t.usable(langs)
            if (usable.isEmpty()) continue
            val gen = Gen(level, d, Random(seed * 31 + level * 7 + d), usable, 1, cursor)
            yield(
                try {
                    t.make(gen)
                } catch (e: Exception) {
                    fail("${t.topic} level $level difficulty $d seed $seed threw $e")
                },
            )
        }
    }
}
