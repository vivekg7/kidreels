package com.crylo.kidreels.core

/**
 * "Apple means" … "सेब": a word said once more in each of the child's other languages,
 * each in its own voice, the way an Indian classroom bridges languages out loud ("A for
 * Apple, apple means सेब"). The child already sees the other language under the main one;
 * this is where they hear it. A reel has at most one bridge, after it names its thing.
 */
object Bridge {
    /**
     * Before the bridged word: shorter than a line break, so frame and word sound like one
     * phrase even though the voice changes in between.
     */
    const val PAUSE = 200L

    /**
     * [word] bridged from [voice] (the language the reel is speaking) into every other
     * language in [langs], in their order: "Apple means" (English) + "सेब" (Hindi). Empty
     * with one language, and for a word the other language does not have, writes the same,
     * or only borrows ("bus" / "बस"): "Bus means बस" teaches nothing.
     */
    fun lines(word: Txt?, voice: Lang, langs: List<Lang>): List<Say> {
        if (word == null) return emptyList()
        return langs.filter { it != voice && bridges(word, voice, it) }.flatMap { to ->
            // Each half needs the other's voice: "Apple means" with nothing after it is worse
            // than no bridge at all, when the phone has no Hindi voice.
            listOf(
                Say(Phrases.means(word)[voice], voice, alsoNeeds = to),
                Say(word[to], to, pauseMillis = PAUSE, alsoNeeds = voice),
            )
        }
    }

    /** Whether [word] is worth saying in [to] after [from]. */
    fun bridges(word: Txt, from: Lang, to: Lang): Boolean {
        if (!word.has(from) || !word.has(to)) return false
        val a = word[from].trim()
        val b = word[to].trim()
        return a.isNotEmpty() && b.isNotEmpty() && !a.equals(b, ignoreCase = true) && !borrowed(a, b)
    }

    /*
     * A borrowed word is the same word in another script: "car" and "कार", "computer" and
     * "कंप्यूटर", and the other way, "jungle" and "जंगल". Both are boiled down to their
     * consonants, by sound (ph and फ़ are both f, c is k or s by the letter after it, m and
     * n are one nasal, as ं is either), with vowels and h left out: the two scripts write
     * vowels too differently to compare. An English plural is also tried without its s
     * ("headphones" / "हेडफ़ोन"). A borrowing then matches exactly, or with one sound more
     * or less ("croissant" / "क्रोसाँ", "unicorn" / "यूनिकॉर्न") in a word of four sounds or
     * more: in short words one sound apart is as often a real translation ("bucket" /
     * "बाल्टी"). Done by rule rather than a list, so a new picture needs no flag;
     * BridgeTest pins words it must catch and translations it must leave alone.
     */

    /** Whether [a] and [b], one in Latin letters and one in Devanagari, are the same borrowed word. */
    fun borrowed(a: String, b: String): Boolean {
        val (latin, deva) = when {
            isLatin(a) && isDevanagari(b) -> a to b
            isDevanagari(a) && isLatin(b) -> b to a
            else -> return false
        }
        val hi = words(deva).map(::devanagariSounds)
        val hiWhole = hi.joinToString("")
        return listOf(words(latin), words(latin).map(::singular)).any { enWords ->
            val en = enWords.map(::latinSounds)
            // "ice cream" / "आइसक्रीम". Exactly, across words: "police car" is one sound from "पुलिस की गाड़ी".
            alike(en.joinToString(""), hiWhole, loosely = en.size == 1 && hi.size == 1) ||
                // "train engine" / "इंजन", "nut and bolt" / "नट-बोल्ट": every Hindi word is one of the English ones.
                (hi.isNotEmpty() && hi.all { h -> en.any { alike(it, h, loosely = true) } })
        }
    }

    private fun alike(x: String, y: String, loosely: Boolean): Boolean {
        val shorter = minOf(x.length, y.length)
        return shorter >= 2 && (x == y || (loosely && shorter >= 4 && oneApart(x, y)))
    }

    /** "cherries" → "cherry", "headphones" → "headphone"; "Uranus" and "glass" are not plurals. */
    private fun singular(w: String): String = when {
        w.endsWith("ies") -> w.dropLast(3) + "y"
        w.endsWith("s") && !w.endsWith("ss") && !w.endsWith("us") && !w.endsWith("is") -> w.dropLast(1)
        else -> w
    }

    /** One sound added or dropped, not changed: a changed sound is as often a different word. */
    private fun oneApart(x: String, y: String): Boolean {
        val (short, long) = if (x.length <= y.length) x to y else y to x
        if (long.length != short.length + 1) return false
        return long.indices.any { long.removeRange(it, it + 1) == short }
    }

    private fun words(s: String): List<String> = s.lowercase().split(' ', '-', ',').filter { it.isNotBlank() }

    private fun isLatin(s: String) = s.any { it in 'a'..'z' || it in 'A'..'Z' } && s.none { it in 'ऀ'..'ॿ' }

    private fun isDevanagari(s: String) = s.any { it in 'ऀ'..'ॿ' } && s.none { it in 'a'..'z' || it in 'A'..'Z' }

    private const val VOWELS = "aeiou"

    /** An English word's consonants by sound: "computer" → "knptr". */
    private fun latinSounds(word: String): String {
        val w = word.filter { it in 'a'..'z' }
            // "C" holds ch, so the c rule below does not read "cheese" as "seese".
            .replace("chr", "kr").replace("sch", "sk").replace("ch", "C").replace("sh", "s")
            .replace("ph", "f").replace("th", "t").replace("wh", "v").replace("ck", "k")
            .replace("gh", "").replace("qu", "kv").replace("x", "ks")
        val out = StringBuilder()
        for ((i, c) in w.withIndex()) {
            val next = w.getOrNull(i + 1)
            val prev = w.getOrNull(i - 1)
            when (c) {
                in VOWELS, 'h' -> Unit
                'C' -> out.append('c')
                'c' -> out.append(if (next != null && next in "eiy") 's' else 'k')
                'g' -> out.append(if (next != null && next in "eiy") 'j' else 'g')
                'z' -> out.append('s')
                'm' -> out.append('n')
                'q' -> out.append('k')
                // "straw", "cow": w after a vowel is part of the vowel, unless one follows ("kiwi").
                'w' -> if (prev == null || prev !in VOWELS || (next != null && next in VOWELS)) out.append('v')
                'y' -> if (i == 0) out.append('y')
                else -> out.append(c)
            }
        }
        return out.collapse()
    }

    /** A Hindi word's consonants by sound: "कंप्यूटर" → "knptr". */
    private fun devanagariSounds(word: String): String {
        val out = StringBuilder()
        for ((i, c) in word.withIndex()) {
            val nukta = word.getOrNull(i + 1) == '़'
            val sound: Char? = when (c) {
                'क', 'ख', 'क़', 'ख़' -> 'k'
                'ग', 'घ', 'ग़' -> 'g'
                'च', 'छ' -> 'c'
                'ज', 'झ' -> if (nukta) 's' else 'j'
                'ज़' -> 's'
                'ट', 'ठ' -> 't'
                'ड', 'ढ' -> if (nukta) 'r' else 'd'
                'ड़', 'ढ़' -> 'r'
                'त', 'थ' -> 't'
                'द', 'ध' -> 'd'
                'ङ', 'ञ', 'ण', 'न', 'म', 'ं', 'ँ' -> 'n'
                'प' -> 'p'
                'फ', 'फ़' -> 'f'
                'ब', 'भ' -> 'b'
                'य', 'य़' -> if (i == 0) 'y' else null
                'र' -> 'r'
                'ल' -> 'l'
                'व' -> 'v'
                'श', 'ष', 'स' -> 's'
                else -> null
            }
            sound?.let(out::append)
        }
        return out.collapse()
    }

    /** "tt" → "t": a doubled letter is one sound. */
    private fun StringBuilder.collapse(): String = buildString { this@collapse.forEach { if (lastOrNull() != it) append(it) } }
}
