package com.crylo.kidreels.core

/**
 * The last fortnight, day by day: feed time, and per topic the puzzles tried, solved first
 * time and the likes. [Progress] keeps running totals, which say how a child did since the
 * age was set but not how this week went; this is what the parent's weekly note reads.
 *
 * Keys are "day.<day>.time" and "day.<day>.<TOPIC>" = "tried,firstTry,likes", where day
 * is days since 1970-01-01. Days older than [KEEP_DAYS] are removed once a day.
 */
class History(private val kv: KeyValueStore, private val clock: Clock) {
    fun addTime(millis: Long) {
        val key = "day.${today()}.time"
        kv.put(key, ((kv.get(key)?.toLongOrNull() ?: 0) + millis).toString())
    }

    fun record(topic: Topic, outcome: Outcome) = bump(topic, tried = 1, first = if (outcome == Outcome.SOLVED_FIRST_TRY) 1 else 0)

    fun like(topic: Topic) = bump(topic, likes = 1)

    private fun bump(topic: Topic, tried: Int = 0, first: Int = 0, likes: Int = 0) {
        val key = "day.${today()}.${topic.name}"
        val n = counts(kv.get(key))
        kv.put(key, "${n[0] + tried},${n[1] + first},${n[2] + likes}")
    }

    private fun counts(value: String?): List<Int> = value?.split(',')?.mapNotNull { it.toIntOrNull() }?.takeIf { it.size == 3 } ?: listOf(0, 0, 0)

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
                val sum = byTopic.getOrPut(topic) { IntArray(3) }
                for (i in 0..2) sum[i] += n[i]
            }
        }
        val tried = byTopic.values.sumOf { it[0] }
        val first = byTopic.values.sumOf { it[1] }
        // Enough tries to mean something, so one lucky or unlucky puzzle is not a verdict.
        val judged = byTopic.filter { it.value[0] >= MIN_TRIES }
        return Week(
            minutes = (millis / TimeBudget.MINUTE).toInt(),
            days = played,
            tried = tried,
            firstTry = first,
            strong = judged.filter { it.value[1] * 100 >= it.value[0] * STRONG_PERCENT }
                .entries.sortedByDescending { it.value[0] }.take(TOP).map { it.key },
            hard = judged.filter { it.value[1] * 100 < it.value[0] * HARD_PERCENT }
                .entries.sortedBy { it.value[1].toDouble() / it.value[0] }.take(TOP).map { it.key },
            liked = byTopic.filter { it.value[2] > 0 }.entries.sortedByDescending { it.value[2] }.take(TOP).map { it.key },
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
        private const val TOP = 3
    }
}

/** One week of one child, as the parent's note tells it. */
data class Week(
    val minutes: Int,
    /** Days with at least a minute on the feed. */
    val days: Int,
    val tried: Int,
    val firstTry: Int,
    /** Mostly right first time. */
    val strong: List<Topic>,
    /** Mostly not, and worth a look together. */
    val hard: List<Topic>,
    val liked: List<Topic>,
) {
    val firstTryPercent: Int get() = if (tried == 0) 0 else firstTry * 100 / tried
}
