package com.crylo.kidreels.core

/*
 * Fun reels: songs and toys between the learning reels, so that a child used to Shorts
 * does not feel the switch as a punishment. More of them in the first weeks, fewer once
 * the child has settled in, and how many is the parent's choice. What a fun reel may be
 * (the child makes it happen, it ends by itself, nothing is kept) is in
 * docs/plan-entertainment.md; how the feed fits them in is in [Feed].
 */

/** How many fun reels a child gets. */
enum class FunMode {
    /** From 1 in 2 down to 1 in 6, over [Settings.funWeeks] weeks of play. */
    AUTO,

    /** One in [Settings.funEvery], until a parent changes it. */
    FIXED,

    /** Learning reels only. The celebrations after a right answer stay: they belong to the puzzle. */
    OFF,
}

/**
 * The share of fun reels. Automatic tapers by the days a child has played, never by how
 * much they skip or how long they stay: a feed that adds fun when a child gets bored is
 * how an engagement-optimised app works. The span is cut into four equal stages, 1 in 2,
 * 3, 4 and 5, and after it the share stays at 1 in 6. Settled is not zero.
 */
object FunShare {
    /** The spans a parent can pick, in weeks of play. A week is seven days played, not seven calendar days. */
    val WEEKS = listOf(2, 3, 4, 6, 8)
    const val DEFAULT_WEEKS = 3

    /** "1 in 2" to "1 in 6", for a fixed share. */
    val EVERY = 2..6
    const val DEFAULT_EVERY = 3

    /** Where automatic starts, and where it settles. */
    const val FIRST = 2
    const val SETTLED = 6

    fun spanDays(weeks: Int): Int = weeks * 7

    /**
     * One reel in how many is fun on [day] of play (1 is the first), over a span of
     * [spanDays]. With 21 days: days 1–5 one in 2, 6–10 one in 3, 11–15 one in 4, 16–21
     * one in 5, then one in 6.
     */
    fun automatic(day: Int, spanDays: Int): Int {
        if (day > spanDays) return SETTLED
        val stage = (4 * day.coerceAtLeast(1) + spanDays - 1) / spanDays - 1
        return FIRST + stage.coerceIn(0, 3)
    }

    /** One reel in how many is fun for [s] on [day] of play; null when fun reels are off. */
    fun every(s: Settings, day: Int): Int? = when (s.funMode) {
        FunMode.OFF -> null
        FunMode.FIXED -> s.funEvery.coerceIn(EVERY)
        FunMode.AUTO -> automatic(day, spanDays(s.funWeeks))
    }

    /** Still tapering: automatic, and not settled yet. The first reel of a session is then fun. */
    fun easingIn(s: Settings, day: Int): Boolean = s.funMode == FunMode.AUTO && day <= spanDays(s.funWeeks)
}

/**
 * Which day of play a child is on, for automatic fun: days played, counted as the weekly
 * note counts them (a minute on the feed), not calendar days, so a child who plays once a
 * week is eased in at their own pace. Kept apart from [History], which forgets after two
 * weeks, while a taper can run for eight.
 *
 * Keys "fun.days" (days played since the start) and "fun.last" (the last day counted). A
 * child with neither, new or from before fun reels, is on day 1.
 */
class FunDays(private val kv: KeyValueStore) {
    /** Today's day of play, today included: 1 on the first day, before and after its first minute. */
    fun day(today: Long): Int {
        val days = kv.get(DAYS)?.toIntOrNull() ?: 0
        return if (kv.get(LAST)?.toLongOrNull() == today) days.coerceAtLeast(1) else days + 1
    }

    /** [today] has been played: counted once, however often this is called. */
    fun played(today: Long) {
        if (kv.get(LAST)?.toLongOrNull() == today) return
        kv.put(DAYS, ((kv.get(DAYS)?.toIntOrNull() ?: 0) + 1).toString())
        kv.put(LAST, today.toString())
    }

    /** "Start again": back to day 1 and 1 in 2, after a holiday on YouTube, say. Only a parent does this. */
    fun restart() {
        kv.put(DAYS, null)
        kv.put(LAST, null)
    }

    private companion object {
        const val DAYS = "fun.days"
        const val LAST = "fun.last"
    }
}
