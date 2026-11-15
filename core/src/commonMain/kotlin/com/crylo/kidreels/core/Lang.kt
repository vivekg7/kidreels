package com.crylo.kidreels.core

/**
 * A language the app can show and speak. Adding one means: an entry here, its text in
 * every [Txt] (missing ones fall back to English, so the app still runs while a
 * translation is in progress), and optionally its alphabet in `content/Alphabets.kt`.
 */
enum class Lang(val tag: String, val nativeName: String) {
    EN("en", "English"),
    HI("hi", "हिन्दी"),
}

/**
 * One piece of text in every supported language. A reel shows the same line in up to
 * three of them at once, so text is never a single string with a locale picked later.
 */
class Txt(private val byLang: Map<Lang, String>) {
    init {
        require(Lang.EN in byLang) { "Every Txt needs English, the fallback: $byLang" }
    }

    operator fun get(lang: Lang): String = byLang[lang] ?: byLang.getValue(Lang.EN)

    fun has(lang: Lang): Boolean = lang in byLang

    override fun equals(other: Any?): Boolean = other is Txt && other.byLang == byLang

    override fun hashCode(): Int = byLang.hashCode()

    override fun toString(): String = byLang[Lang.EN].orEmpty()

    companion object {
        /** Text that reads the same in every language: a numeral, an emoji, a formula. */
        fun same(text: String): Txt = Txt(Lang.entries.associateWith { text })
    }
}

fun txt(en: String, hi: String): Txt = Txt(mapOf(Lang.EN to en, Lang.HI to hi))

/** One stretch of speech, in one voice. A reel's speech is a list of these. */
data class Say(val text: String, val lang: Lang, val tone: Tone = Tone.TALK)

/**
 * How a line should sound. Speech engines offer no "happy" switch, so each platform turns
 * a tone into pitch and pace: the sing-song, slower speech adults use with small children.
 */
enum class Tone {
    /** Questions and explanations: warm and unhurried. */
    TALK,

    /** Praise: brighter and a little quicker, like a real "yay!". */
    CHEER,

    /** "Try again": soft and slow, so a miss never sounds like a telling-off. */
    GENTLE,

    /** The goodbye reel: low and slow, winding down. */
    CALM,
}
