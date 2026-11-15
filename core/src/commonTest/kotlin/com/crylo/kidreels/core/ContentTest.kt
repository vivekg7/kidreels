package com.crylo.kidreels.core

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
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
        }
        val missing = pictures.distinct().filter { Words.of(it) == null }
        assertTrue(missing.isEmpty(), "No name for $missing")
    }

    @Test
    fun pictureCardsHaveAFactAndDoNotRepeat() {
        val all = Words.byTopic.values.flatten()
        assertTrue(all.all { it.fact != null })
        assertEquals(all.size, all.map { it.emoji }.toSet().size, "a picture is in two groups")
        assertTrue(Words.byTopic.keys.all { !Templates.of(it).solving })
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
