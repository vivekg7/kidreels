package com.crylo.kidreels.core

import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FunTest {
    private fun feed(
        s: Settings = Settings(onboarded = true, age = 4, langs = listOf(Lang.EN, Lang.HI)),
        day: Int = 1,
        seed: Int = 1,
        progress: Progress = Progress(MapStore()),
    ): Feed = Feed({ s }, progress, Random(seed), funDay = { day })

    /** What a child did with a reel, in [Child.play]. */
    private enum class Did { ENGAGED_WELL, FLICKED, TRIED_THEN_SKIPPED }

    /**
     * The feed as the app drives it: two reels made ahead of the one on screen, the reel on
     * screen shown, engaged with or flicked past, and a fun reel put straight after a reel
     * finished well. [shown] is everything that came on screen, in order.
     */
    private class Child(val feed: Feed) {
        val reels = mutableListOf<Reel>()
        val shown = mutableListOf<Pair<Reel, Did>>()
        private var current = 0

        fun play(n: Int, behave: (Reel) -> Did) {
            repeat(n) {
                while (reels.size < current + 3) reels += feed.next()
                val reel = reels[current]
                feed.shown(reel)
                val did = behave(reel)
                when (did) {
                    Did.ENGAGED_WELL -> {
                        feed.engaged(reel)
                        if (reel.needsSolving) feed.record(reel, Outcome.SOLVED_FIRST_TRY)
                        feed.funAfter(reel)?.let { reels.add(current + 1, it) }
                    }
                    Did.TRIED_THEN_SKIPPED -> {
                        feed.engaged(reel)
                        if (reel.needsSolving) feed.record(reel, Outcome.SKIPPED)
                    }
                    Did.FLICKED -> if (reel.needsSolving) feed.record(reel, Outcome.SKIPPED)
                }
                shown += reel to did
                current++
            }
        }

        val share: Double get() = shown.count { it.first.isFun }.toDouble() / shown.size
    }

    private fun engagedChild(f: Feed, n: Int = 2000) = Child(f).apply { play(n) { Did.ENGAGED_WELL } }

    @Test
    fun automaticEasesInOverFourEqualStagesThenSettles() {
        val stages = (1..22).map { FunShare.automatic(it, 21) }
        assertEquals(List(5) { 2 } + List(5) { 3 } + List(5) { 4 } + List(6) { 5 } + 6, stages)
        for (weeks in FunShare.WEEKS) {
            val span = FunShare.spanDays(weeks)
            val lengths = (1..span).groupBy { FunShare.automatic(it, span) }.mapValues { it.value.size }
            assertEquals(listOf(2, 3, 4, 5), lengths.keys.sorted(), "$weeks weeks")
            assertTrue(lengths.values.max() - lengths.values.min() <= 1, "$weeks weeks: stages $lengths are not equal")
            assertEquals(FunShare.SETTLED, FunShare.automatic(span + 1, span))
            assertEquals(FunShare.SETTLED, FunShare.automatic(span + 400, span))
        }
    }

    @Test
    fun eachAutomaticStageGivesItsShare() {
        for (day in listOf(1, 6, 11, 16, 30)) {
            val every = FunShare.automatic(day, 21)
            val share = engagedChild(feed(day = day, seed = day)).share
            assertTrue(abs(share - 1.0 / every) < 0.01, "day $day: $share, wanted 1 in $every")
        }
    }

    @Test
    fun aFixedShareIsHeldAndOffMeansNone() {
        for (every in FunShare.EVERY) {
            val s = Settings(onboarded = true, age = 5, funMode = FunMode.FIXED, funEvery = every)
            // Day 1 or day 100: fixed does not taper.
            for (day in listOf(1, 100)) {
                val share = engagedChild(feed(s, day = day, seed = every)).share
                assertTrue(abs(share - 1.0 / every) < 0.01, "fixed 1 in $every on day $day: $share")
            }
        }
        val off = engagedChild(feed(Settings(onboarded = true, age = 3, funMode = FunMode.OFF)))
        assertEquals(0, off.shown.count { it.first.isFun })
    }

    /** The gap is fixed: exactly N − 1 learning reels between fun ones for a child who plays every reel. */
    @Test
    fun theGapIsFixedNotRandom() {
        val child = engagedChild(feed(Settings(onboarded = true, age = 4, funMode = FunMode.FIXED, funEvery = 4)), 400)
        val at = child.shown.mapIndexedNotNull { i, (r, _) -> i.takeIf { r.isFun } }
        assertTrue(at.size > 50)
        at.zipWithNext().forEach { (a, b) -> assertEquals(4, b - a, "fun reels at $at") }
    }

    @Test
    fun flickingPastPuzzlesDoesNotBringTheNextFunReelSooner() {
        val s = Settings(onboarded = true, age = 5, funMode = FunMode.FIXED, funEvery = 3)
        val flicker = Child(feed(s, seed = 7))
        // Plays every explore reel, flicks every puzzle without touching it.
        flicker.play(2000) { if (it.needsSolving) Did.FLICKED else Did.ENGAGED_WELL }
        var engagedSince = 0
        var funs = 0
        for ((reel, did) in flicker.shown) {
            if (reel.isFun) {
                if (funs > 0) assertTrue(engagedSince >= 2, "a fun reel after only $engagedSince engaged reels")
                funs++
                engagedSince = 0
            } else if (did != Did.FLICKED) {
                engagedSince++
            }
        }
        assertTrue(funs > 50, "only $funs fun reels")
        // Per reel engaged with, the flicker gets no more fun than a child who plays them all.
        val engaged = flicker.shown.count { !it.first.isFun && it.second != Did.FLICKED }
        assertTrue(funs <= engaged / 2 + 1, "$funs fun reels for $engaged engaged")
        assertTrue(flicker.share < 1.0 / 3, "flicking raised the share to ${flicker.share}")
    }

    @Test
    fun aFunReelNeverFollowsASkipAndNeverAnotherFunReel() {
        val r = Random(11)
        val child = Child(feed(seed = 11))
        child.play(3000) {
            when (r.nextInt(5)) {
                0 -> Did.FLICKED
                1 -> Did.TRIED_THEN_SKIPPED
                else -> Did.ENGAGED_WELL
            }
        }
        child.shown.zipWithNext().forEach { (before, reel) ->
            if (!reel.first.isFun) return@forEach
            assertFalse(before.first.isFun, "two fun reels in a row")
            assertEquals(Did.ENGAGED_WELL, before.second, "a fun reel after a reel that was ${before.second}")
        }
        assertTrue(child.shown.count { it.first.isFun } > 300)
    }

    @Test
    fun likesRaiseAFunTopicButNotHowManyFunReelsThereAre() {
        val plain = engagedChild(feed(seed = 5))
        val progress = Progress(MapStore())
        repeat(50) { progress.like(Topic.TAP_TUNE) }
        Topic.entries.filter { !it.isFun }.forEach { t -> repeat(50) { progress.like(t) } }
        val liked = engagedChild(feed(seed = 5, progress = progress))
        assertTrue(abs(plain.share - liked.share) < 0.005, "${plain.share} vs ${liked.share}")
        val songs = { c: Child -> c.shown.count { it.first.topic == Topic.TAP_TUNE } }
        val onlySongsLiked = Progress(MapStore()).also { p -> repeat(50) { p.like(Topic.TAP_TUNE) } }
        val songLover = engagedChild(feed(seed = 5, progress = onlySongsLiked))
        assertTrue(songs(songLover) > songs(plain), "a liked song topic should come up more within the fun slots")
        assertTrue(abs(plain.share - songLover.share) < 0.005)
    }

    @Test
    fun theWindDownHasOnlyCalmFun() {
        // Copy me is lively, so an 8-year-old's wind-down has no fun reel at all.
        for (age in 2..8) {
            val f = feed(Settings(onboarded = true, age = age, langs = listOf(Lang.EN, Lang.HI)), seed = age)
            f.windDown = true
            val child = engagedChild(f, 600)
            val funReels = child.shown.map { it.first }.filter { it.isFun }
            assertEquals(age <= 7, funReels.isNotEmpty(), "age $age: $funReels")
            for (reel in funReels) {
                assertTrue(Templates.of(reel.topic).calm, "age $age: ${reel.topic} in the wind-down")
                if (reel is TuneReel) assertTrue(reel.song.calm, "${reel.song.key} is not a calm song")
                if (reel is BubblePop) assertTrue(reel.slow && reel.game == BubbleGame.COUNT)
                assertEquals(1, reel.difficulty)
            }
        }
    }

    @Test
    fun showOnlyIsUnaffectedAndCanPickAFunTopic() {
        val f = feed()
        f.focus = Topic.LETTERS
        val child = engagedChild(f, 100)
        assertTrue(child.shown.all { it.first.topic == Topic.LETTERS })
        f.focus = Topic.TAP_TUNE
        val songs = List(10) { f.next() }
        assertTrue(songs.all { it is TuneReel })
        // In order, one per song: Twinkle first.
        assertEquals("twinkle", (songs.first() as TuneReel).song.key)
    }

    @Test
    fun funReelsNeverTouchSkillStatsOrRetries() {
        val progress = Progress(MapStore())
        val f = feed(progress = progress)
        f.focus = Topic.BUBBLES
        val bubbles = List(20) { f.next() }
        f.focus = null
        bubbles.forEach { f.record(it, Outcome.SKIPPED) }
        assertEquals(0.0, progress.skill(Topic.BUBBLES))
        assertEquals(0, progress.stats(4).tried)
        // Nothing comes back as a retry: the mix after has no bubbles outside the fun slot.
        val after = List(30) { f.next() }
        assertTrue(after.none { it.isFun }, "a fun reel came back by itself")
    }

    @Test
    fun theFirstReelOfASessionIsFunWhileEasingIn() {
        assertTrue(feed(day = 1).next().isFun)
        assertTrue(feed(day = 21).next().isFun)
        assertFalse(feed(day = 22).next().isFun, "settled: the session starts as usual")
        val fixed = Settings(onboarded = true, age = 4, funMode = FunMode.FIXED, funEvery = 2)
        assertFalse(feed(fixed).next().isFun, "a fixed share does not ease in")
    }

    @Test
    fun anAgeWithoutFunReelsGetsUsualReels() {
        val f = feed(Settings(onboarded = true, age = 10))
        val child = engagedChild(f, 300)
        assertTrue(child.shown.none { it.first.isFun })
    }

    @Test
    fun aFunReelTakenOutUnseenDoesNotBlockTheNext() {
        val f = feed(Settings(onboarded = true, age = 4, funMode = FunMode.FIXED, funEvery = 2))
        val first = f.next()
        f.shown(first)
        f.engaged(first)
        val waiting = f.funAfter(first) ?: error("a fun reel was due")
        assertNull(f.funAfter(first), "one fun reel waits at a time")
        f.dropped(listOf(waiting))
        assertTrue(f.funAfter(first)?.isFun == true)
    }

    @Test
    fun songsArePlayableAndInTheMusicBoxsNotes() {
        for (song in Songs.all) {
            assertTrue(song.lines.isNotEmpty())
            for (line in song.lines) {
                assertTrue(line.notes.all { it in Songs.NOTES }, "${song.key}: ${line.notes} has a note the synth does not make")
                assertTrue(Words.of(line.picture) != null, "${song.key}: ${line.picture} has no name")
                assertFalse('-' in line.words, line.words)
            }
        }
        assertEquals("Twinkle twinkle little star,", Songs.twinkle.lines.first().words)
        assertEquals("Old MacDonald had a farm,", Songs.oldMacDonald(Songs.farm.first()).lines.first().words)
    }

    @Test
    fun aSillyAnimalNamesThePartInBothLanguages() {
        val cow = Silly.all.first { it.face == "🐮" }
        val dog = Silly.all.first { it.face == "🐶" }
        assertEquals("That's the cow's nose!", Silly.thatsThe(cow, SillyPart.NOSE)[Lang.EN])
        assertEquals("यह गाय की नाक है!", Silly.thatsThe(cow, SillyPart.NOSE)[Lang.HI])
        assertEquals("ये कुत्ते के पैर हैं!", Silly.thatsThe(dog, SillyPart.FEET)[Lang.HI])
        assertEquals("यह कुत्ते का पेट है!", Silly.thatsThe(dog, SillyPart.TUMMY)[Lang.HI])
        assertEquals("Those are the dog's ears!", Silly.thatsThe(dog, SillyPart.EARS)[Lang.EN])
        // The animal is named and bridged, then says hello in its silly voice.
        val reel = SillyAnimalReel(1, 4, 3, cow, namesParts = true)
        val intro = reel.intro(listOf(Lang.EN, Lang.HI))
        assertEquals(listOf("This is a cow!", "cow means", "गाय"), intro.take(3).map { it.text })
        assertEquals(Tone.GIANT, intro.last().tone)
    }

    @Test
    fun copyMeKeepsToSittingMovesUnlessJumpingIsOn() {
        val cursor = Progress(MapStore())
        for (level in 2..8) for (seed in 0 until 40) {
            val sitting = CopyMoves.make(Gen(level, 3, Random(seed), listOf(Lang.EN), 1, cursor))
            assertTrue(sitting.steps.none { it.move.standing }, "a standing move with jumping games off")
            assertTrue(sitting.steps.any { it.move.beats > 0 }, "no count to the drum")
            assertTrue(sitting.steps.size in 4..5)
            sitting.steps.mapNotNull { it.move.part }.forEach { key -> assertTrue(Body.parts.any { it.key == key && it.fromAge <= level }, "$key at $level") }
            if (sitting.simon) assertTrue(level >= 5 && sitting.steps.first().simonSays && sitting.steps.any { !it.simonSays })
            val jumping = CopyMoves.make(Gen(level, 3, Random(seed), listOf(Lang.EN), 1, cursor, jumping = true))
            assertTrue(jumping.steps.any { it.move.standing })
            assertEquals(CopyMoves.sitDown, jumping.steps.last().move, "standing moves end sitting down again")
        }
    }

    /** The drawn child acts out every move its own way: two moves sharing a pose would show the wrong one. */
    @Test
    fun everyCopyMoveHasItsOwnAct() {
        val moves = CopyMoves.sitting(10) + CopyMoves.standing(10) + CopyMoves.sitDown
        assertEquals(CopyAct.entries.toSet(), moves.map { it.act }.toSet(), "an act no move uses")
        assertEquals(moves.size, moves.map { it.act }.distinct().size, "two moves share an act")
    }

    /** Each drawing's parts are where its taps land: a nose tap that tickled would be no joke. */
    @Test
    fun sillyAnimalPartsAreWhereTheDrawingHasThem() {
        for (a in Silly.all) {
            val c = a.shape.centre
            assertEquals(SillyZone.NOSE, Silly.zone(a, c, a.shape.noseY), a.face)
            assertEquals(SillyZone.EARS, Silly.zone(a, c - a.shape.earsOut - 0.05f, a.shape.earsTo - 0.05f), a.face)
            assertEquals(SillyZone.EARS, Silly.zone(a, c + a.shape.earsOut + 0.05f, a.shape.earsTo - 0.05f), a.face)
            assertEquals(SillyZone.TUMMY, Silly.zone(a, c, 0.7f), a.face)
            assertEquals(SillyZone.FEET, Silly.zone(a, c - 0.1f, 0.9f), a.face)
            assertEquals(SillyZone.AIR, Silly.zone(a, 0.02f, 0.75f), a.face)
            assertTrue(a.art.startsWith("art:silly-"), a.face)
        }
    }
}
