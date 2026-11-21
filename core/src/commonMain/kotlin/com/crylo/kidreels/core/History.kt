package com.crylo.kidreels.core

/**
 * The last fortnight, day by day: feed time, and per topic the puzzles tried, solved first
 * time, the likes, the puzzles swiped past unsolved and the reels played (engaged with:
 * touched, or stayed on, see [Feed.engaged]). [Progress] keeps running totals, which say
 * how a child did since the age was set but not how this week went; this is what the
 * parent's weekly note reads. Fun reels only ever add plays and likes, so they stay out
 * of the puzzle numbers; each song's plays are kept too, for "their favourite song".
 *
 * Keys are "day.<day>.time", "day.<day>.<TOPIC>" = "tried,firstTry,likes,skipped,plays" and
 * "day.<day>.song.<key>" = plays, where day is days since 1970-01-01. Days written before
 * skips or plays were kept have fewer numbers and read as none. Days older than
 * [KEEP_DAYS] are removed once a day.
 */
class History(private val kv: KeyValueStore, private val clock: Clock) {
    fun addTime(millis: Long) {
        val key = "day.${today()}.time"
        kv.put(key, ((kv.get(key)?.toLongOrNull() ?: 0) + millis).toString())
    }

    fun record(topic: Topic, outcome: Outcome) = bump(
        topic,
        tried = 1,
        first = if (outcome == Outcome.SOLVED_FIRST_TRY) 1 else 0,
        skipped = if (outcome == Outcome.SKIPPED) 1 else 0,
    )

    fun like(topic: Topic) = bump(topic, likes = 1)

    /** A reel of [topic] the child did something with: learning or fun, once per reel. */
    fun played(topic: Topic) = bump(topic, plays = 1)

    /** A song played, for the weekly note's favourite. */
    fun song(key: String) {
        val k = "day.${today()}.song.$key"
        kv.put(k, ((kv.get(k)?.toIntOrNull() ?: 0) + 1).toString())
    }

    /** Whether today has had its minute on the feed: a day played, as the weekly note counts it. */
    fun playedToday(): Boolean = (kv.get("day.${today()}.time")?.toLongOrNull() ?: 0) >= PLAYED_MILLIS

    private fun bump(topic: Topic, tried: Int = 0, first: Int = 0, likes: Int = 0, skipped: Int = 0, plays: Int = 0) {
        val key = "day.${today()}.${topic.name}"
        val n = counts(kv.get(key))
        kv.put(key, "${n[TRIED] + tried},${n[FIRST] + first},${n[LIKES] + likes},${n[SKIPPED] + skipped},${n[PLAYS] + plays}")
    }

    private fun counts(value: String?): List<Int> {
        val n = value?.split(',')?.mapNotNull { it.toIntOrNull() }
        return if (n != null && n.size in 3..FIELDS) n + List(FIELDS - n.size) { 0 } else List(FIELDS) { 0 }
    }

    /** Today, and once a day, a sweep of the days nobody will ask about again. */
    private fun today(): Long {
        val today = clock.today()
        if (kv.get(SWEPT)?.toLongOrNull() != today) {
            kv.keys().filter { it.startsWith("day.") }.forEach { key ->
                val day = key.removePrefix("day.").substringBefore('.').toLongOrNull()
                if (day == null || today - day >= KEEP_DAYS) kv.put(key, null)
            }
            kv.put(SWEPT, today.toString())
        }
        return today
    }

    /** The last seven days, today included. */
    fun week(): Week {
        val today = clock.today()
        val days = (today - 6..today)
        var millis = 0L
        var played = 0
        val byTopic = mutableMapOf<Topic, IntArray>()
        val songs = mutableMapOf<String, Int>()
        for (day in days) {
            val prefix = "day.$day.song."
            kv.keys().filter { it.startsWith(prefix) }.forEach { key ->
                val k = key.removePrefix(prefix)
                songs[k] = (songs[k] ?: 0) + (kv.get(key)?.toIntOrNull() ?: 0)
            }
            val time = kv.get("day.$day.time")?.toLongOrNull() ?: 0
            millis += time
            if (time >= PLAYED_MILLIS) played++
            Topic.entries.forEach { topic ->
                val n = counts(kv.get("day.$day.${topic.name}"))
                val sum = byTopic.getOrPut(topic) { IntArray(FIELDS) }
                for (i in 0 until FIELDS) sum[i] += n[i]
            }
        }
        // Well or hard is judged on the puzzles answered: a skip says the child moved on,
        // not that they got it wrong, and "try one together" for a topic they just scroll
        // past would send the parent after the wrong thing. Skips get their own line.
        val answered = byTopic.mapValues { (_, n) -> n[TRIED] - n[SKIPPED] }
        // Enough tries to mean something, so one lucky or unlucky puzzle is not a verdict.
        val judged = byTopic.filter { answered.getValue(it.key) >= MIN_TRIES }
        val share = { topic: Topic -> byTopic.getValue(topic)[FIRST].toDouble() / answered.getValue(topic) }
        return Week(
            minutes = (millis / TimeBudget.MINUTE).toInt(),
            days = played,
            tried = byTopic.values.sumOf { it[TRIED] },
            firstTry = byTopic.values.sumOf { it[FIRST] },
            skipped = byTopic.values.sumOf { it[SKIPPED] },
            strong = judged.keys.filter { share(it) * 100 >= STRONG_PERCENT }
                .sortedByDescending { answered.getValue(it) }.take(TOP),
            hard = judged.keys.filter { share(it) * 100 < HARD_PERCENT }.sortedBy(share).take(TOP),
            skips = byTopic.filter { (_, n) -> n[TRIED] >= MIN_TRIES && n[SKIPPED] * 100 >= n[TRIED] * SKIPS_PERCENT }
                .entries.sortedByDescending { it.value[SKIPPED] }.take(TOP).map { it.key },
            liked = byTopic.filter { it.value[LIKES] > 0 }.entries.sortedByDescending { it.value[LIKES] }.take(TOP).map { it.key },
            reels = byTopic.values.sumOf { it[PLAYS] },
            funReels = byTopic.filterKeys { it.isFun }.values.sumOf { it[PLAYS] },
            // Played more than once: one play is not a favourite.
            favouriteSong = songs.filterValues { it >= FAVOURITE_PLAYS }.maxByOrNull { it.value }?.key,
        )
    }

    companion object {
        const val KEEP_DAYS = 14
        private const val SWEPT = "history.swept"

        /** A day counts as played after a minute on the feed, not an accidental open. */
        const val PLAYED_MILLIS = TimeBudget.MINUTE
        const val MIN_TRIES = 4
        const val STRONG_PERCENT = 80
        const val HARD_PERCENT = 50

        /** Swiped past at least this share of a topic's puzzles: "often swipes past". */
        const val SKIPS_PERCENT = 50
        private const val TOP = 3

        /** A song played this often in the week is "their favourite". */
        const val FAVOURITE_PLAYS = 2

        // A day's numbers for one topic, in the order they are stored.
        private const val TRIED = 0
        private const val FIRST = 1
        private const val LIKES = 2
        private const val SKIPPED = 3
        private const val PLAYS = 4
        private const val FIELDS = 5
    }
}

/** One week of one child, as the parent's note tells it. */
data class Week(
    val minutes: Int,
    /** Days with at least a minute on the feed. */
    val days: Int,
    val tried: Int,
    val firstTry: Int,
    /** Swiped past unsolved; part of [tried]. */
    val skipped: Int,
    /** Of the puzzles answered, mostly right first time. */
    val strong: List<Topic>,
    /** Of the puzzles answered, mostly not, and worth a look together. */
    val hard: List<Topic>,
    /** Mostly swiped past: too hard to start, or not their thing. */
    val skips: List<Topic>,
    val liked: List<Topic>,
    /** Reels played (engaged with), learning and fun. Zero for days from before plays were kept. */
    val reels: Int = 0,
    /** Of [reels], the fun ones: songs and toys. */
    val funReels: Int = 0,
    /** [Song.key] of the song played most, if one was played at least twice. */
    val favouriteSong: String? = null,
) {
    val firstTryPercent: Int get() = if (tried == 0) 0 else firstTry * 100 / tried
}
