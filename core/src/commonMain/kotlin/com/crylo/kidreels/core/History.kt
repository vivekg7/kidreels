package com.crylo.kidreels.core

/**
 * The last fortnight, day by day: feed time, and per topic the puzzles tried, solved first
 * time, the likes and the puzzles swiped past unsolved. [Progress] keeps running totals,
 * which say how a child did since the age was set but not how this week went; this is
 * what the parent's weekly note reads.
 *
 * Keys are "day.<day>.time" and "day.<day>.<TOPIC>" = "tried,firstTry,likes,skipped", where
 * day is days since 1970-01-01. Days written before skips were kept have three numbers and
 * read as none skipped. Days older than [KEEP_DAYS] are removed once a day.
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

    private fun bump(topic: Topic, tried: Int = 0, first: Int = 0, likes: Int = 0, skipped: Int = 0) {
        val key = "day.${today()}.${topic.name}"
        val n = counts(kv.get(key))
        kv.put(key, "${n[TRIED] + tried},${n[FIRST] + first},${n[LIKES] + likes},${n[SKIPPED] + skipped}")
    }

    private fun counts(value: String?): List<Int> {
        val n = value?.split(',')?.mapNotNull { it.toIntOrNull() }
        return when (n?.size) {
            FIELDS -> n
            FIELDS - 1 -> n + 0
            else -> List(FIELDS) { 0 }
        }
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
        for (day in days) {
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

        // A day's numbers for one topic, in the order they are stored.
        private const val TRIED = 0
        private const val FIRST = 1
        private const val LIKES = 2
        private const val SKIPPED = 3
        private const val FIELDS = 4
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
) {
    val firstTryPercent: Int get() = if (tried == 0) 0 else firstTry * 100 / tried
}
