package com.crylo.kidreels.core

import kotlin.math.roundToInt
import kotlin.random.Random

enum class Outcome { SOLVED_FIRST_TRY, SOLVED, SKIPPED }

/** How a child did on the reels of one age level: what the age check-in shows the parent. */
data class LevelStats(val tried: Int, val firstTry: Int, val skipped: Int) {
    val firstTryPercent: Int get() = if (tried == 0) 0 else firstTry * 100 / tried
}

/**
 * What the feed remembers between sessions: where each alphabet is up to, how the child
 * is doing per topic, what they liked, and per-level results for the age check-in.
 * Nothing here identifies the child.
 */
class Progress(private val kv: KeyValueStore) : Cursor {
    override fun next(key: String, size: Int): Int {
        val k = "cursor.$key"
        val at = (kv.get(k)?.toIntOrNull() ?: 0) % size
        kv.put(k, ((at + 1) % size).toString())
        return at
    }

    /** −2 (struggling: make it easier) to +2 (breezing through: make it harder). */
    fun skill(topic: Topic): Double = kv.get("skill.${topic.name}")?.toDoubleOrNull() ?: 0.0

    fun adjustSkill(topic: Topic, by: Double) {
        kv.put("skill.${topic.name}", (skill(topic) + by).coerceIn(-2.0, 2.0).toString())
    }

    /** A new age starts every topic from the middle again. */
    fun resetSkills() = Topic.entries.forEach { kv.put("skill.${it.name}", null) }

    fun likes(topic: Topic): Int = kv.get("likes.${topic.name}")?.toIntOrNull() ?: 0

    fun like(topic: Topic) = kv.put("likes.${topic.name}", (likes(topic) + 1).toString())

    fun stats(level: Int): LevelStats {
        val parts = kv.get("stats.$level")?.split(',')?.mapNotNull { it.toIntOrNull() }
        return if (parts?.size == 3) LevelStats(parts[0], parts[1], parts[2]) else LevelStats(0, 0, 0)
    }

    fun record(level: Int, outcome: Outcome) {
        val s = stats(level)
        val next = LevelStats(
            tried = s.tried + 1,
            firstTry = s.firstTry + if (outcome == Outcome.SOLVED_FIRST_TRY) 1 else 0,
            skipped = s.skipped + if (outcome == Outcome.SKIPPED) 1 else 0,
        )
        kv.put("stats.$level", "${next.tried},${next.firstTry},${next.skipped}")
    }
}

/**
 * Decides what comes next. The rules, in order:
 *
 * 1. A puzzle that was swiped past comes back a few reels later, one step easier.
 * 2. After a hard reel (a hard puzzle, or one above the child's age) or a struggle, the
 *    next one is easy: a breather, so the child stays in the mood.
 * 3. Otherwise most reels are at the child's age; one in ten is a gentle step up (to see
 *    whether they are ready) and one in ten a step down (practice that feels easy).
 * 4. No more than three puzzles in a row without something to just explore, and no more
 *    than two explore reels in a row without a puzzle. A topic waits four reels before
 *    it comes back, where the age has enough topics to allow it.
 * 5. Within a topic, difficulty follows how the child has been doing on it.
 * 6. In the last minutes before the time limit ([windDown]), only easy reels.
 *
 * Fun reels (songs and toys, [Topic.isFun]) have a slot of their own, never the learning
 * mix: one reel in N, N from the child's fun setting and day of play ([FunShare]).
 *
 * 7. The gap is fixed, not random, and counted in reels the child did something with
 *    ([engaged]): a puzzle flicked past does not bring the next fun reel nearer.
 * 8. A fun reel only ever comes straight after a reel finished well ([funAfter]): never
 *    after a skip or a struggle, which would teach skipping, and never two in a row.
 * 9. While easing in, the first reel of a session is fun: the first seconds decide
 *    whether a child stays.
 * 10. Likes raise a fun topic within the fun slots, never how many there are. In the
 *    wind-down only calm fun. A picked topic ([focus]) has no fun slot.
 */
class Feed(
    private val settings: () -> Settings,
    private val progress: Progress,
    private val random: Random = Random.Default,
    private val templates: List<Template> = Templates.all,
    /** The child's day of play, 1 on the first, for automatic fun: see [FunDays]. */
    private val funDay: () -> Int = { 1 },
) {
    private class Retry(val topic: Topic, val level: Int, val difficulty: Int, val due: Int)

    private val recent = ArrayDeque<Reel>()
    private val retries = mutableListOf<Retry>()
    private val retried = mutableSetOf<Long>()
    private var made = 0
    private var nextId = 1L
    private var struggled = false

    /** Learning reels engaged with since the last fun reel was on screen: what moves the fun slot on. */
    private var sinceFun = 0
    private val counted = mutableSetOf<Long>()

    /** A fun reel made to go next and not on screen yet: there is never a second one waiting. */
    private var funWaiting: Long? = null

    var windDown: Boolean = false

    /**
     * A topic a parent picked for now: only its reels, in order, and puzzles from easy to
     * hard. Null is the usual mix. Kept in memory only, so the app starts mixed again.
     */
    var focus: Topic? = null
        set(value) {
            field = value
            focusCursor = SessionCursor()
            focusDifficulty = 1
            focusStreak = 0
            focusLang = 0
        }

    /** Positions for [focus]: from the start each time a topic is picked, not where the mix left off. */
    private class SessionCursor : Cursor {
        private val at = mutableMapOf<String, Int>()

        /** Set when a list has just been gone through to the end. */
        var wrapped = false

        override fun next(key: String, size: Int): Int {
            val i = (at[key] ?: 0) % size
            at[key] = i + 1
            if (i + 1 == size) wrapped = true
            return i
        }
    }

    private var focusCursor = SessionCursor()
    private var focusDifficulty = 1
    private var focusStreak = 0
    private var focusLang = 0

    fun next(): Reel {
        val s = settings()
        val age = s.age.coerceIn(Settings.AGES)
        made++
        val topic = focus
        // A picked topic the child's languages no longer allow (Hindi मात्राएँ, then Hindi
        // taken out) is ignored rather than made with no language.
        val reel = if (topic != null && Templates.of(topic).fits(s.langs)) {
            focused(topic, s, age)
        } else {
            firstFun(s, age) ?: dueRetry(s, age) ?: fresh(s, age)
        }
        remember(reel)
        return reel
    }

    private fun remember(reel: Reel) {
        recent.addLast(reel)
        if (recent.size > HISTORY) recent.removeFirst()
    }

    /** One reel in this many is fun now, or null when fun reels are off. */
    fun funEvery(): Int? = FunShare.every(settings(), funDay())

    /** While easing in, a session starts with a fun reel. */
    private fun firstFun(s: Settings, age: Int): Reel? {
        if (made != 1 || !FunShare.easingIn(s, funDay())) return null
        return makeFun(s, age)?.also { funWaiting = it.id }
    }

    /** [reel] came on screen. A fun reel starts the count to the next one again. */
    fun shown(reel: Reel) {
        if (!reel.isFun) return
        if (funWaiting == reel.id) funWaiting = null
        sinceFun = 0
    }

    /**
     * The child did something with [reel]: touched it, or stayed on it a while. Only these
     * reels move the fun slot on, each once, so swiping fast through puzzles does not
     * bring the next fun reel sooner.
     */
    fun engaged(reel: Reel) {
        if (!reel.isFun && reel !is TimeUp && counted.add(reel.id)) sinceFun++
    }

    /**
     * [reel] was finished well: an explore reel engaged with, a puzzle right first time. If
     * the next fun reel is due, it is made now, for the caller to put straight after
     * [reel]: the reels already made ahead were decided before the child got here, and a
     * fun reel there could follow a skip. Null when none is due, none fits (the age has no
     * fun reels, or the wind-down no calm ones), or a topic is picked. A skip or a struggle
     * earlier does not hold it back once a later reel went well: the breather the struggle
     * asked for is still made, one reel later.
     */
    fun funAfter(reel: Reel): Reel? {
        if (focus != null || reel.isFun || funWaiting != null) return null
        val every = funEvery() ?: return null
        if (sinceFun < every - 1) return null
        val s = settings()
        val next = makeFun(s, s.age.coerceIn(Settings.AGES)) ?: return null
        made++
        remember(next)
        funWaiting = next.id
        return next
    }

    /** Reels made but taken out unseen (settings changed, time is up): a fun one among them is no longer waiting. */
    fun dropped(reels: Collection<Reel>) {
        if (reels.any { it.id == funWaiting }) funWaiting = null
    }

    /**
     * A fun reel for the child's age: in the wind-down only a calm one, made calm. The
     * four-reel wait applies to fun topics too, so the same toy does not come round every
     * few minutes; likes weigh as in the mix, within the fun reels only.
     */
    private fun makeFun(s: Settings, age: Int): Reel? {
        val fits = templates.filter { it.isFun && age in it.ages && it.fits(s.langs) && (!windDown || it.calm) }
        if (fits.isEmpty()) return null
        val recentTopics = recent.takeLast(RECENT_TOPICS).map { it.topic }.toSet()
        val lastFun = recent.lastOrNull { it.isFun }?.topic
        val pool = fits.filter { it.topic !in recentTopics }.ifEmpty { fits.filter { it.topic != lastFun }.ifEmpty { fits } }
        val template = weightedPick(pool)
        return make(template, s, age, if (windDown) 1 else 3)
    }

    fun timeUp(bedtime: Boolean = false): Reel = TimeUp(nextId++, bedtime)

    fun record(reel: Reel, outcome: Outcome) {
        // Fun reels are outside the learning numbers: no skill, no stats, no retry.
        if (!reel.needsSolving || reel.isFun) return
        if (focus != null) {
            // Three right first time and the next ones are a step harder; a skip, a step easier.
            when (outcome) {
                Outcome.SOLVED_FIRST_TRY -> if (++focusStreak >= FOCUS_STEP) {
                    focusDifficulty = (focusDifficulty + 1).coerceAtMost(5)
                    focusStreak = 0
                }
                Outcome.SOLVED -> Unit
                Outcome.SKIPPED -> {
                    focusDifficulty = (focusDifficulty - 1).coerceAtLeast(1)
                    focusStreak = 0
                }
            }
            progress.record(reel.level, outcome)
            return
        }
        when (outcome) {
            Outcome.SOLVED_FIRST_TRY -> progress.adjustSkill(reel.topic, 0.34)
            Outcome.SOLVED -> {
                progress.adjustSkill(reel.topic, -0.25)
                struggled = true
            }
            Outcome.SKIPPED -> {
                progress.adjustSkill(reel.topic, -0.5)
                struggled = true
                // Once only: a puzzle skipped twice is not worth a third try today.
                if (retried.add(reel.id)) {
                    val age = settings().age
                    retries += Retry(reel.topic, minOf(reel.level, age), (reel.difficulty - 1).coerceAtLeast(1), made + random.nextInt(3, 6))
                }
            }
        }
        progress.record(reel.level, outcome)
    }

    private fun dueRetry(s: Settings, age: Int): Reel? {
        if (windDown) return null
        val retry = retries.firstOrNull { it.due <= made && it.topic != recent.lastOrNull()?.topic } ?: return null
        retries.remove(retry)
        val level = retry.level.coerceIn((age - 1).coerceAtLeast(Settings.AGES.first), age)
        val template = templates.firstOrNull { it.topic == retry.topic && level in it.ages && it.fits(s.langs) } ?: return null
        return make(template, s, level, retry.difficulty)
    }

    private fun fresh(s: Settings, age: Int): Reel {
        val last = recent.lastOrNull()
        val breather = last != null && (last.isHardFor(age) || struggled)
        struggled = false

        var fixedDifficulty: Int? = null
        val level = when {
            windDown -> age.also { fixedDifficulty = 1 }
            breather -> (if (random.nextDouble() < 0.4) age - 1 else age).also { fixedDifficulty = random.nextInt(1, 3) }
            else -> {
                val roll = random.nextDouble()
                when {
                    roll < STRETCH -> (age + 1).also { fixedDifficulty = random.nextInt(1, 3) }
                    roll < STRETCH + REVIEW -> (age - 1).also { fixedDifficulty = random.nextInt(2, 5) }
                    else -> age
                }
            }
        }.coerceIn(Settings.AGES)

        // Fun reels come only through their own slot.
        val eligible = templates.filter { !it.isFun && level in it.ages && it.fits(s.langs) }
        val solvingRun = recent.reversed().takeWhile { it.needsSolving }.size
        val exploreRun = recent.reversed().takeWhile { !it.needsSolving }.size
        var pool = when {
            windDown && random.nextDouble() < 0.7 -> eligible.filter { !it.solving }
            solvingRun >= MAX_PUZZLES_IN_A_ROW -> eligible.filter { !it.solving }
            exploreRun >= MAX_EXPLORE_IN_A_ROW -> eligible.filter { it.solving }
            else -> eligible
        }.ifEmpty { eligible }
        val recentTopics = recent.takeLast(RECENT_TOPICS).map { it.topic }.toSet()
        pool = pool.filter { it.topic !in recentTopics }.ifEmpty { pool.filter { it.topic != last?.topic }.ifEmpty { pool } }

        val template = weightedPick(pool)
        val difficulty = fixedDifficulty
            ?: (3 + progress.skill(template.topic).roundToInt() + random.nextInt(-1, 2)).coerceIn(1, 5)
        return make(template, s, level, difficulty)
    }

    /**
     * The picked topic only. Made for the child's age where the topic has it, else the
     * nearest age it has: the alphabet for an 8-year-old is the age-6 alphabet. With two
     * languages, one alphabet is gone through to the end before the next begins.
     */
    private fun focused(topic: Topic, s: Settings, age: Int): Reel {
        val template = templates.first { it.topic == topic }
        val level = age.coerceIn(template.ages)
        val difficulty = when {
            windDown -> 1
            template.solving -> focusDifficulty
            else -> 3
        }
        val usable = template.usable(s.langs)
        val langs = listOf(usable[focusLang % usable.size])
        focusCursor.wrapped = false
        val reel = template.make(Gen(level, difficulty, random, langs, nextId++, focusCursor, age = s.age, ordered = true, calm = windDown, jumping = s.jumping))
        if (focusCursor.wrapped) focusLang++
        return reel
    }

    private fun make(template: Template, s: Settings, level: Int, difficulty: Int): Reel =
        template.make(Gen(level, difficulty, random, template.usable(s.langs), nextId++, progress, age = s.age, calm = windDown, jumping = s.jumping))

    /** Liked topics come up a little more often: up to half as often again. */
    private fun weightedPick(pool: List<Template>): Template {
        val weights = pool.map { it.weight * (1 + 0.05 * progress.likes(it.topic).coerceAtMost(10)) }
        var r = random.nextDouble() * weights.sum()
        for ((i, w) in weights.withIndex()) {
            r -= w
            if (r <= 0) return pool[i]
        }
        return pool.last()
    }

    companion object {
        const val STRETCH = 0.1
        const val REVIEW = 0.1
        const val MAX_PUZZLES_IN_A_ROW = 3
        const val MAX_EXPLORE_IN_A_ROW = 2
        /** A topic waits this many reels before it comes round again, when there is choice. */
        const val RECENT_TOPICS = 4
        /** Right first time this many times in a row, and a picked topic gets a step harder. */
        const val FOCUS_STEP = 3
        private const val HISTORY = 6
    }
}
