package com.crylo.kidreels.core

/*
 * One phone, several children. Each child has their own corner of the store: settings,
 * progress, today's time. All the existing classes keep reading plain keys ("age",
 * "time.used"); they are handed a [PrefixedStore] that files them under "p<id>.".
 */

/** The keys of [kv] that start with [prefix], with the prefix taken off. */
class PrefixedStore(private val kv: KeyValueStore, private val prefix: String) : KeyValueStore {
    override fun get(key: String): String? = kv.get(prefix + key)

    override fun put(key: String, value: String?) = kv.put(prefix + key, value)

    override fun keys(): Set<String> = kv.keys().filter { it.startsWith(prefix) }.map { it.removePrefix(prefix) }.toSet()
}

/**
 * What a child picks to know which profile is theirs: an animal on a coloured circle, so a
 * toddler who cannot read their name can still find it. Every one is in [Words], so a
 * child who has no name typed in is called by their animal.
 */
enum class Avatar(val emoji: String) {
    TIGER("🐯"), PANDA("🐼"), FOX("🦊"), RABBIT("🐰"), FROG("🐸"), LION("🦁"), KOALA("🐨"), MONKEY("🐵"),
    DOG("🐶"), CAT("🐱"), BEAR("🐻"), PENGUIN("🐧"), OWL("🦉"), ELEPHANT("🐘"), BUTTERFLY("🦋"), DOLPHIN("🐬"),
    ;

    val word: Word get() = Words.of(emoji) ?: error("$emoji has no name")
}

data class Profile(
    val id: Int,
    /** As the parent typed it; may be blank. */
    val name: String = "",
    val avatar: Avatar = Avatar.TIGER,
    /** Index into the UI's avatar colours. */
    val colour: Int = 0,
) {
    /** The name, or the animal's ("Tiger", "बाघ") when none was typed. */
    fun label(lang: Lang): String = name.ifBlank { avatar.word.title[lang] }

    companion object {
        const val MAX_NAME = 20
        const val COLOURS = 8
    }
}

/**
 * The children on this phone and which one played last. Phone-wide keys live under
 * "device."; everything else belongs to a child.
 */
class Profiles(private val kv: KeyValueStore) {
    init {
        if (kv.get(LIST) == null) adoptOldData()
    }

    /**
     * Version 1.1 kept one child's data at the top level. It all becomes child 1's, so an
     * update keeps every setting and every bit of progress. A fresh install runs this
     * too, with nothing to move.
     */
    private fun adoptOldData() {
        kv.keys().filterNot { it.startsWith(DEVICE) }.forEach { key ->
            kv.put(prefix(1) + key, kv.get(key))
            kv.put(key, null)
        }
        kv.put(LIST, "1")
        kv.put(LAST, "1")
    }

    fun ids(): List<Int> = kv.get(LIST)?.split(',')?.mapNotNull { it.toIntOrNull() }?.takeIf { it.isNotEmpty() } ?: listOf(1)

    val count: Int get() = ids().size

    /** The child who played last; the app opens on them. */
    var last: Int
        get() = kv.get(LAST)?.toIntOrNull()?.takeIf { it in ids() } ?: ids().first()
        set(id) = kv.put(LAST, id.toString())

    /** The phone's own keys, shared by every child: a missing voice is missing for all of them. */
    val device: KeyValueStore = PrefixedStore(kv, DEVICE)

    /** One child's own store: what [SettingsStore], [Progress] and [TimeBudget] are given. */
    fun storeOf(id: Int): KeyValueStore = PrefixedStore(kv, prefix(id))

    fun profile(id: Int): Profile {
        val s = storeOf(id)
        return Profile(
            id = id,
            name = s.get("name").orEmpty(),
            avatar = s.get("avatar")?.let { a -> Avatar.entries.firstOrNull { it.name == a } } ?: Avatar.TIGER,
            colour = s.get("colour")?.toIntOrNull()?.takeIf { it in 0 until Profile.COLOURS } ?: 0,
        )
    }

    fun all(): List<Profile> = ids().map(::profile)

    fun save(p: Profile) {
        val s = storeOf(p.id)
        s.put("name", p.name.trim().take(Profile.MAX_NAME))
        s.put("avatar", p.avatar.name)
        s.put("colour", p.colour.toString())
    }

    /** A new child, set up with [settings]. Returns their id. */
    fun add(p: Profile, settings: Settings): Int {
        val id = (ids().maxOrNull() ?: 0) + 1
        save(p.copy(id = id))
        SettingsStore(storeOf(id)).save(settings)
        kv.put(LIST, (ids() + id).joinToString(","))
        return id
    }

    /** Removes a child and everything kept for them. The last child cannot be removed. */
    fun remove(id: Int): Boolean {
        val ids = ids()
        if (ids.size <= 1 || id !in ids) return false
        val s = storeOf(id)
        s.keys().forEach { s.put(it, null) }
        kv.put(LIST, (ids - id).joinToString(","))
        if (kv.get(LAST)?.toIntOrNull() == id) last = (ids - id).first()
        return true
    }

    /**
     * Whether picking [id] at "Who's playing?" needs a grown-up. Only when the child who
     * played last has used up today's time and [id] is someone else: otherwise a child
     * whose time is up could pick a brother's or sister's profile to keep watching.
     */
    fun needsGate(id: Int, clock: Clock): Boolean = id != last && timeUp(last, clock)

    /** Whether [id] is done for now: today's time used up (with any extra a grown-up gave), or past bedtime. */
    fun timeUp(id: Int, clock: Clock): Boolean {
        val s = SettingsStore(storeOf(id)).load()
        val budget = TimeBudget(storeOf(id), clock)
        return budget.remainingMillis(s.dailyMinutes) == 0L || budget.isBedtime(s.bedtime)
    }

    companion object {
        private const val DEVICE = "device."
        private const val LIST = DEVICE + "profiles"
        private const val LAST = DEVICE + "last"

        private fun prefix(id: Int) = "p$id."

        /** Away at least this long and "Who's playing?" comes back: a different child may have the phone. */
        const val ASK_AGAIN_MILLIS = 15 * 60_000L

        /** Enough for any family the app is likely to meet, and still one screen of faces. */
        const val MAX_CHILDREN = 6
    }
}
