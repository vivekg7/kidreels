package com.crylo.kidreels.core

/** The platform's small key-value storage (SharedPreferences on Android). */
interface KeyValueStore {
    fun get(key: String): String?

    /** A null [value] removes the key. */
    fun put(key: String, value: String?)

    /** Every key held: to move one child's data, or remove it. */
    fun keys(): Set<String>
}

/** In memory only: for tests, previews and a platform that has nothing better yet. */
class MapStore(private val map: MutableMap<String, String> = mutableMapOf()) : KeyValueStore {
    override fun get(key: String): String? = map[key]

    override fun put(key: String, value: String?) {
        if (value == null) map.remove(key) else map[key] = value
    }

    override fun keys(): Set<String> = map.keys.toSet()
}

/**
 * Wall-clock time from the platform. [today] is the local calendar day, as days since
 * 1970-01-01; [minuteOfDay] is the local time, 0 at midnight to 1439.
 */
interface Clock {
    fun nowMillis(): Long

    fun today(): Long

    fun minuteOfDay(): Int
}

/** The kid-facing look of the feed. Parent screens look the same whatever is picked. */
enum class Look { CLASSIC, CANDY, CALM }

data class Settings(
    val onboarded: Boolean = false,
    /** One to three languages; the first is the main one, used for speech. */
    val langs: List<Lang> = listOf(Lang.EN),
    val age: Int = 4,
    val look: Look = Look.CANDY,
    /** 0 means no limit. */
    val dailyMinutes: Int = 20,
    /** How often to ask whether the child is ready for the next age; 0 is never. */
    val checkInMonths: Int = 3,
    /** The day [age] was last chosen or confirmed (days since 1970-01-01). */
    val ageSetDay: Long = 0,
    val slowSpeech: Boolean = false,
    /** No reels from this time (minutes after midnight) until [Bedtime.MORNING]; 0 is off. */
    val bedtime: Int = 0,
) {
    val main: Lang get() = langs.first()

    companion object {
        const val MAX_LANGS = 3
        val AGES = 2..10
        val DAILY_MINUTES = listOf(10, 15, 20, 30, 45, 60, 0)
        val CHECK_IN_MONTHS = listOf(1, 3, 6, 0)
        val BEDTIMES = listOf(0, 19 * 60, 19 * 60 + 30, 20 * 60, 20 * 60 + 30, 21 * 60, 21 * 60 + 30)
    }
}

class SettingsStore(private val kv: KeyValueStore) {
    fun load(): Settings {
        val d = Settings()
        val langs = kv.get("langs")?.split(',')?.mapNotNull { tag -> Lang.entries.firstOrNull { it.tag == tag } }?.distinct()
        return Settings(
            onboarded = kv.get("onboarded") == "true",
            langs = langs?.takeIf { it.isNotEmpty() }?.take(Settings.MAX_LANGS) ?: d.langs,
            age = kv.get("age")?.toIntOrNull()?.coerceIn(Settings.AGES) ?: d.age,
            look = kv.get("look")?.let { name -> Look.entries.firstOrNull { it.name == name } } ?: d.look,
            dailyMinutes = kv.get("dailyMinutes")?.toIntOrNull()?.coerceAtLeast(0) ?: d.dailyMinutes,
            checkInMonths = kv.get("checkInMonths")?.toIntOrNull()?.coerceAtLeast(0) ?: d.checkInMonths,
            ageSetDay = kv.get("ageSetDay")?.toLongOrNull() ?: d.ageSetDay,
            slowSpeech = kv.get("slowSpeech") == "true",
            bedtime = kv.get("bedtime")?.toIntOrNull()?.takeIf { it == 0 || it in Bedtime.MORNING until 24 * 60 } ?: d.bedtime,
        )
    }

    fun save(s: Settings) {
        kv.put("onboarded", s.onboarded.toString())
        kv.put("langs", s.langs.joinToString(",") { it.tag })
        kv.put("age", s.age.toString())
        kv.put("look", s.look.name)
        kv.put("dailyMinutes", s.dailyMinutes.toString())
        kv.put("checkInMonths", s.checkInMonths.toString())
        kv.put("ageSetDay", s.ageSetDay.toString())
        kv.put("slowSpeech", s.slowSpeech.toString())
        kv.put("bedtime", s.bedtime.toString())
    }
}

/**
 * The parent's daily limit. Only time on the feed counts (the app passes in each second
 * it is on screen), and the count starts again each calendar day.
 */
class TimeBudget(private val kv: KeyValueStore, private val clock: Clock) {
    private fun rollOver() {
        val today = clock.today().toString()
        if (kv.get("time.day") != today) {
            kv.put("time.day", today)
            kv.put("time.used", "0")
            kv.put("time.bonus", "0")
        }
    }

    fun usedMillis(): Long {
        rollOver()
        return kv.get("time.used")?.toLongOrNull() ?: 0
    }

    fun add(millis: Long) {
        val used = usedMillis()
        kv.put("time.used", (used + millis).toString())
    }

    /** "10 more minutes", granted by a parent from the time-up reel. Today only. */
    fun addBonus(minutes: Int) {
        rollOver()
        val bonus = kv.get("time.bonus")?.toLongOrNull() ?: 0
        kv.put("time.bonus", (bonus + minutes * MINUTE).toString())
    }

    /** Null when there is no limit. Never negative. */
    fun remainingMillis(dailyMinutes: Int): Long? {
        if (dailyMinutes <= 0) return null
        val used = usedMillis()
        val bonus = kv.get("time.bonus")?.toLongOrNull() ?: 0
        return (dailyMinutes * MINUTE + bonus - used).coerceAtLeast(0)
    }

    /**
     * Whether it is past this child's [bedtime] and before morning, and no grown-up has
     * given extra minutes tonight. The extra is wall-clock time, "ten more minutes before
     * bed", rather than feed time like [addBonus].
     */
    fun isBedtime(bedtime: Int): Boolean =
        Bedtime.isNight(bedtime, clock.minuteOfDay()) && clock.nowMillis() >= (kv.get("bed.until")?.toLongOrNull() ?: 0)

    fun addBedtimeMinutes(minutes: Int) {
        val from = maxOf(clock.nowMillis(), kv.get("bed.until")?.toLongOrNull() ?: 0)
        kv.put("bed.until", (from + minutes * MINUTE).toString())
    }

    /**
     * Minutes left before bed, for the calm last stretch: until [bedtime], or past it, until
     * the end of a grown-up's extra minutes. Null when bedtime is off or already here.
     */
    fun minutesToBedtime(bedtime: Int): Int? {
        if (bedtime <= 0) return null
        val now = clock.minuteOfDay()
        if (!Bedtime.isNight(bedtime, now)) return bedtime - now
        val extra = (kv.get("bed.until")?.toLongOrNull() ?: 0) - clock.nowMillis()
        return if (extra > 0) ((extra + MINUTE - 1) / MINUTE).toInt() else null
    }

    companion object {
        const val MINUTE = 60_000L

        /** The last stretch before the limit, when only calm and easy reels are shown. */
        const val WIND_DOWN_MILLIS = 2 * MINUTE
    }
}

/**
 * "Is your child ready for harder reels?", every [Settings.checkInMonths] months. A month
 * is taken as 30 days: the exact day does not matter, only that it comes round. Never at
 * the top age, where there is nothing harder to move to.
 */
object CheckIn {
    fun isDue(s: Settings, today: Long): Boolean =
        s.onboarded && s.age < Settings.AGES.last && s.checkInMonths > 0 && today - s.ageSetDay >= s.checkInMonths * 30L

    /** "Ask me later": comes back in a week. */
    fun snooze(s: Settings, today: Long): Settings = s.copy(ageSetDay = today - s.checkInMonths * 30L + 7)
}

/**
 * No reels at night. From the parent's bedtime until [MORNING] the feed shows the
 * good-night reel instead; a limit on minutes alone would still let a child who saved
 * them up watch at 11 pm.
 */
object Bedtime {
    /** 6 am: early enough that a child up at dawn is not met by "good night". */
    const val MORNING = 6 * 60

    fun isNight(bedtime: Int, minuteOfDay: Int): Boolean = bedtime > 0 && (minuteOfDay >= bedtime || minuteOfDay < MORNING)
}
