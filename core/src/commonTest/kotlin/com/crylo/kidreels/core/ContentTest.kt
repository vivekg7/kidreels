package com.crylo.kidreels.core

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContentTest {
    @Test
    fun englishAlphabetIsComplete() {
        assertEquals(('A'..'Z').map { it.toString() }, Alphabets.english.map { it.glyph })
    }

    @Test
    fun hindiAlphabetHasEverySwarAndVyanjan() {
        assertEquals(13 + 36, Alphabets.hindi.size)
        assertEquals(Alphabets.hindi.size, Alphabets.hindi.map { it.glyph }.toSet().size)
    }

    @Test
    fun wordsStartWithTheirLetter() {
        for (letter in Alphabets.english + Alphabets.hindi) {
            if (letter.like) {
                // "अः, जैसे छः": the word holds the letter's sign, not the letter itself.
                assertTrue(letter.word.contains(letter.glyph.last()), "${letter.word} should contain ${letter.glyph}")
            } else {
                assertTrue(letter.word.startsWith(letter.glyph), "${letter.word} should start with ${letter.glyph}")
            }
        }
    }

    @Test
    fun everyTextHasEveryLanguage() {
        val texts = buildList {
            Topic.entries.forEach { add(it.title) }
            Channel.entries.forEach { add(it.handle) }
            Shapes.all.forEach { add(it.name); add(it.fact) }
            Solids.all.forEach { add(it.name); add(it.fact) }
            Curves.all.forEach { add(it.name); add(it.fact) }
            AngleKind.entries.forEach { add(it.title); add(it.rule) }
            Colours.all.forEach { add(it.name) }
            AnimalSounds.all.forEach { add(it.sound); add(it.reveal) }
            Habitats.all.forEach { add(it.question); add(it.reveal) }
            Home.entries.forEach { add(it.label) }
            Categories.all.forEach { add(it.name) }
            (Alphabets.english + Alphabets.hindi).forEach { add(it.meaning) }
            Words.byTopic.values.flatten().forEach { add(it.name); add(it.fact!!) }
            // Fun reels.
            Silly.all.forEach { add(it.sound); add(Silly.tapThe(it)); SillyPart.entries.forEach { p -> add(Silly.thatsThe(it, p)) } }
            SillyPart.entries.forEach { add(it.word); add(it.does) }
            add(Silly.tapParts)
            (2..8).forEach { level -> (CopyMoves.sitting(level) + CopyMoves.standing(level)).forEach { add(it.say) } }
            addAll(listOf(CopyMoves.sitDown.say, CopyMoves.title, CopyMoves.simonTitle, CopyMoves.intro, CopyMoves.simonIntro, CopyMoves.go, CopyMoves.simonSays, CopyMoves.gotcha, CopyMoves.done, CopyMoves.again))
            Songs.all.forEach { add(it.title); add(Songs.played(it)) }
            add(Songs.tapAnywhere)
            val cursor = Progress(MapStore())
            for (level in 2..7) for (seed in 0 until 30) {
                val b = Bubbles.make(Gen(level, 3, Random(seed), Lang.entries, 1, cursor))
                add(Bubbles.prompt(b)); add(Bubbles.say(b)); add(Bubbles.done(b)); add(Bubbles.notThis(b, 0))
            }
            (1..20).forEach { add(Bubbles.counted(it)) }
        }
        for (t in texts) for (lang in Lang.entries) assertTrue(t.has(lang), "'$t' has no $lang")
    }

    @Test
    fun categoriesDoNotOverlap() {
        val seen = mutableMapOf<String, String>()
        for (c in Categories.all) for (item in c.items) {
            val before = seen.put(item, c.key)
            assertTrue(before == null, "$item is in both $before and ${c.key}")
        }
    }

    @Test
    fun sudokuGridsAreValid() {
        for (size in listOf(4, 6, 9)) repeat(200) { seed ->
            assertTrue(Sudoku.isValid(Sudoku.solved(size, Random(seed)), size), "size $size seed $seed")
        }
    }

    @Test
    fun anglesAreNamedWithSlack() {
        assertEquals(AngleKind.ACUTE, AngleKind.of(45))
        assertEquals(AngleKind.RIGHT, AngleKind.of(88))
        assertEquals(AngleKind.RIGHT, AngleKind.of(93))
        assertEquals(AngleKind.OBTUSE, AngleKind.of(120))
        assertEquals(AngleKind.STRAIGHT, AngleKind.of(179))
        assertEquals(AngleKind.REFLEX, AngleKind.of(250))
        assertEquals(90, AngleKind.snap(91))
    }

    /** Every picture a child can tap says what it is, so none of them is silently dead. */
    @Test
    fun everyPictureHasAName() {
        val pictures = buildList {
            Countables.forEach { add(it.emoji) }
            Numbers.things.forEach { add(it.emoji) }
            Mazes.runners.forEach { add(it.emoji); add(it.goal) }
            addAll(Science.living)
            addAll(Science.notLiving)
            PatternSets.flatten().forEach { add(it.emoji) }
            Categories.all.forEach { addAll(it.items) }
            AnimalSounds.all.forEach { add(it.emoji) }
            Habitats.all.forEach { add(it.emoji) }
            Home.entries.forEach { add(it.emoji) }
            Shapes.all.forEach { addAll(it.examples) }
            Solids.all.forEach { addAll(it.examples) }
            Curves.all.forEach { add(it.example) }
            addAll(Phrases.goodbyeThings)
            addAll(Phrases.bedtimeThings)
            Silly.all.forEach { add(it.face) }
            Bubbles.words.forEach { add(it.second) }
            Songs.all.forEach { song -> song.lines.forEach { add(it.picture) } }
            Songs.farm.forEach { add(it.emoji) }
        }
        val missing = pictures.distinct().filter { Words.of(it) == null }
        assertTrue(missing.isEmpty(), "No name for $missing")
    }

    /** Every spot on every drawing is inside it and taps as its own part. */
    @Test
    fun diagramPartsCanEachBeTapped() {
        for (d in Diagram.entries) {
            for (p in d.parts) for ((x, y) in p.spots) {
                assertTrue(x in 0f..d.width && y in 0f..d.height, "${d.name} ${p.key} is off the picture")
                assertEquals(p, Diagrams.at(d, x, y), "a tap on the ${p.key} lands on ${Diagrams.at(d, x, y)?.key}")
            }
            assertEquals(d.parts.size, d.parts.map { it.key }.distinct().size)
        }
    }

    @Test
    fun pictureCardsHaveAFactAndDoNotRepeat() {
        val all = Words.byTopic.values.flatten()
        assertTrue(all.all { it.fact != null })
        assertEquals(all.size, all.map { it.emoji }.toSet().size, "a picture is in two groups")
        assertTrue(Words.byTopic.keys.all { !Templates.of(it).solving })
    }

    @Test
    fun aPlanetNeverSaysItsNameTwiceInARow() {
        // The Sun's and Moon's facts start with their name, so the card says only the fact.
        for (p in Science.planets) for (lang in Lang.entries) {
            val said = Science.planetSay(p)[lang]
            assertFalse("${p.name[lang]} ${p.name[lang]}" in said, said)
        }
        assertEquals("The Sun is a star. It gives us light and heat.", Science.planetSay(Science.planets.first { it.key == "sun" })[Lang.EN])
    }

    @Test
    fun sieveNeverCrossesOutThePrime() {
        // "Every number 2 goes into" would take in 2 itself.
        assertTrue("other numbers 2 goes into" in NumberKinds.sieveStep(2, 3)[Lang.EN])
        assertTrue("बाक़ी गुणज" in NumberKinds.sieveStep(2, 3)[Lang.HI])
    }

    @Test
    fun tappingAPictureSaysWhatItIs() {
        fun say(emoji: String) = Words.of(emoji)!!.thisIs().let { it[Lang.EN] to it[Lang.HI] }
        assertEquals("This is an elephant!" to "यह हाथी है!", say("🐘"))
        assertEquals("These are grapes!" to "ये अंगूर हैं!", say("🍇"))
        assertEquals("These are glasses!" to "यह चश्मा है!", say("👓"))
        assertEquals("This is corn!" to "यह भुट्टा है!", say("🌽"))
        assertEquals("This is the colour red!" to "यह लाल रंग है!", say("🔴"))
        assertEquals("This is an auto rickshaw!" to "यह ऑटो रिक्शा है!", say("🛺"))
        // The variation selector is optional: ✈ and ✈️ are the same picture.
        assertEquals(Words.of("✈️"), Words.of("✈"))
    }
}
