package com.crylo.kidreels.core

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FeedTest {
    private fun feed(age: Int, seed: Int = 1, langs: List<Lang> = listOf(Lang.EN, Lang.HI)): Feed {
        val settings = Settings(onboarded = true, age = age, langs = langs)
        return Feed({ settings }, Progress(MapStore()), Random(seed))
    }

    private fun Feed.take(n: Int): List<Reel> = List(n) { next() }

    @Test
    fun sameTopicNeverTwiceInARow() {
        for (age in Settings.AGES) {
            val reels = feed(age).take(300)
            reels.zipWithNext().forEach { (a, b) -> assertTrue(a.topic != b.topic, "age $age: ${a.topic} twice") }
        }
    }

    @Test
    fun hardReelIsFollowedByAnEasyOne() {
        for (age in Settings.AGES) {
            val reels = feed(age, seed = age).take(400)
            reels.zipWithNext().forEach { (a, b) ->
                if (a.isHardFor(age)) assertFalse(b.isHardFor(age), "age $age: hard ${a.topic} then hard ${b.topic}")
            }
        }
    }

    @Test
    fun levelsStayWithinOneOfTheAge() {
        for (age in Settings.AGES) {
            val levels = feed(age).take(400).map { it.level }
            assertTrue(levels.all { it in age - 1..age + 1 }, "age $age: $levels")
            assertTrue(levels.count { it == age } > levels.size / 2, "age $age should mostly get its own level")
            if (age < 10) assertTrue(levels.any { it == age + 1 }, "age $age never got a stretch reel")
            if (age > 2) assertTrue(levels.any { it == age - 1 }, "age $age never got a review reel")
        }
    }

    @Test
    fun puzzlesAreBrokenUpByExploreReels() {
        for (age in Settings.AGES) {
            var run = 0
            for (reel in feed(age).take(300)) {
                run = if (reel.needsSolving) run + 1 else 0
                // AngleLab challenges are puzzles made by an explore template, hence +1.
                assertTrue(run <= Feed.MAX_PUZZLES_IN_A_ROW + 1, "age $age: $run puzzles in a row")
            }
        }
    }

    @Test
    fun skippedPuzzleComesBackEasier() {
        val f = feed(5, seed = 3)
        val skipped = generateSequence { f.next() }.first { it.needsSolving && it.difficulty >= 2 }
        f.record(skipped, Outcome.SKIPPED)
        val again = f.take(7).firstOrNull { it.topic == skipped.topic }
        assertNotNull(again, "${skipped.topic} did not come back")
        assertTrue(again.difficulty < skipped.difficulty || again.level < skipped.level)
    }

    @Test
    fun windDownOnlyGivesEasyReels() {
        val f = feed(6)
        f.windDown = true
        f.take(100).forEach { assertTrue(it.difficulty == 1 && it.level == 6, "$it") }
    }

    @Test
    fun onlyChosenAlphabetsAppear() {
        val reels = feed(3, langs = listOf(Lang.HI)).take(300)
        val letters = reels.filterIsInstance<LetterCard>()
        assertTrue(letters.isNotEmpty())
        assertTrue(letters.all { it.lang == Lang.HI })
    }

    @Test
    fun alphabetIsShownInOrder() {
        val letters = feed(2, langs = listOf(Lang.EN)).take(300).filterIsInstance<LetterCard>().map { it.letter.glyph }
        assertEquals(('A'..'Z').map { it.toString() }.take(letters.size.coerceAtMost(26)), letters.take(26))
    }

    @Test
    fun hindiLetterSpeaksHindiWhateverTheMainLanguage() {
        val card = feed(3, langs = listOf(Lang.EN, Lang.HI)).take(300).filterIsInstance<LetterCard>().first { it.lang == Lang.HI }
        val intro = card.intro(Lang.EN)
        assertTrue(intro.all { it.lang == Lang.HI }, "$intro")
        val parts = intro.take(3).map { it.text }
        assertEquals(listOf(card.letter.glyph, if (card.letter.like) "जैसे" else "से", card.letter.word), parts, "the letter, then a pause, then the word")
    }

    /** Everything a reel says or shows in Hindi, as one string to search. */
    private fun Reel.hindi(): String = buildString {
        intro(Lang.HI).forEach { append(it.text).append(' ') }
        prompt?.let { append(it[Lang.HI]).append(' ') }
        if (this@hindi is Quiz) reveal?.let { append(it[Lang.HI]) }
        if (this@hindi is ShapeCard) append(shape.name[Lang.HI]).append(' ').append(shape.fact[Lang.HI])
        if (this@hindi is SolidCard) append(solid.name[Lang.HI])
    }

    @Test
    fun littleChildrenHearHomeWordsNotSchoolWords() {
        val schoolWords = listOf("वृत्त", "त्रिभुज", "आयत", "भुजा", "संख्या", "पक्षी", "ध्रुवीय", "पंचभुज", "घन ", "घनाभ", "फलक", "शंकु", "लगभग")
        for (age in 2..5) for (seed in 1..3) {
            for (reel in feed(age, seed, listOf(Lang.HI, Lang.EN)).take(300)) {
                val said = reel.hindi()
                schoolWords.forEach { assertFalse(it in said, "age $age heard '$it' in: $said") }
            }
        }
        val olderSaid = (1..3).flatMap { feed(7, it, listOf(Lang.HI, Lang.EN)).take(300) }.joinToString(" ") { it.hindi() }
        assertTrue("त्रिभुज" in olderSaid || "वर्ग" in olderSaid, "a 7-year-old should hear the school names for shapes")
    }

    @Test
    fun theVoiceSaysWhatIsOnScreenAndWhatToDo() {
        for (reel in feed(4).take(300)) {
            if (reel is Quiz) assertTrue(reel.say != null, "${reel.topic} has nothing to say beyond its prompt")
            val said = reel.intro(Lang.EN).joinToString(" ") { it.text }
            assertFalse("help me" in said, "a filler line instead of the reel: $said")
            if (reel is Quiz && reel.stage is Stage.Count) {
                val emoji = (reel.stage as Stage.Count).emoji
                val thing = Countables.first { it.emoji == emoji }
                assertTrue(thing.many[Lang.EN] in said && "Count them" in said, said)
            }
        }
    }

    @Test
    fun hindiQuestionsAgreeWithWhatIsCounted() {
        val butterfly = Countables.first { it.emoji == "🦋" }
        val apple = Countables.first { it.emoji == "🍎" }
        assertEquals("फ़ोन पर कितनी तितलियाँ हैं? गिनकर बताओ!", Phrases.howManyOnPhone(butterfly)[Lang.HI])
        assertEquals("कितने सेब हैं?", Phrases.howMany(apple)[Lang.HI])
        assertEquals("कौन सी तितली बड़ी है?", Phrases.whichSize(butterfly, big = true, ofThree = false)[Lang.HI])
        assertEquals("1 तितली है!", Phrases.thereAre(1, butterfly)[Lang.HI])
    }

    @Test
    fun firstLetterQuestionAgreesWithTheWord() {
        val duck = Alphabets.hindi.first { it.word == "बतख" }
        val apple = Alphabets.hindi.first { it.word == "सेब" }
        val ball = Alphabets.english.first { it.word == "Ball" }
        assertEquals("बतख किस अक्षर से शुरू होती है?", Phrases.startsWith(duck)[Lang.HI])
        assertEquals("सेब किस अक्षर से शुरू होता है?", Phrases.startsWith(apple)[Lang.HI])
        assertEquals("Ball किस अक्षर से शुरू होती है?", Phrases.startsWith(ball)[Lang.HI])
    }

    @Test
    fun firstLetterAnswerIsSpokenInParts() {
        val quiz = feed(5).take(400).filterIsInstance<Quiz>().first { it.topic == Topic.FIRST_LETTER }
        assertEquals(3, quiz.revealSaid?.size, "the answer is said as letter, link, word")
    }

    @Test
    fun timeBudgetCountsPerDay() {
        val store = MapStore()
        var day = 100L
        val clock = object : Clock {
            override fun nowMillis() = 0L
            override fun today() = day
        }
        val budget = TimeBudget(store, clock)
        assertNull(budget.remainingMillis(0))
        budget.add(5 * TimeBudget.MINUTE)
        assertEquals(15 * TimeBudget.MINUTE, budget.remainingMillis(20))
        budget.addBonus(10)
        assertEquals(25 * TimeBudget.MINUTE, budget.remainingMillis(20))
        budget.add(40 * TimeBudget.MINUTE)
        assertEquals(0, budget.remainingMillis(20))
        day++
        assertEquals(20 * TimeBudget.MINUTE, budget.remainingMillis(20))
    }

    @Test
    fun checkInComesRoundAndSnoozes() {
        val s = Settings(onboarded = true, checkInMonths = 3, ageSetDay = 1000)
        assertFalse(CheckIn.isDue(s, 1000 + 89))
        assertTrue(CheckIn.isDue(s, 1000 + 90))
        val snoozed = CheckIn.snooze(s, 1100)
        assertFalse(CheckIn.isDue(snoozed, 1106))
        assertTrue(CheckIn.isDue(snoozed, 1107))
        assertFalse(CheckIn.isDue(s.copy(checkInMonths = 0), 5000))
        assertFalse(CheckIn.isDue(s.copy(age = 10), 5000))
    }

    @Test
    fun settingsSurviveARoundTrip() {
        val store = SettingsStore(MapStore())
        val s = Settings(true, listOf(Lang.HI, Lang.EN), 7, Look.CALM, 45, 1, 12345, true)
        store.save(s)
        assertEquals(s, store.load())
    }
}
