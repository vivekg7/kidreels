package com.crylo.kidreels.core

/**
 * One line of a song: a note per syllable, and a picture that does its thing at the end of
 * the line (the star twinkles, the sheep hops). Notes are semitones from middle C, all in
 * C major between the G below and the G two octaves up: the music box the phone builds
 * at start-up has exactly those fifteen notes. A syllable ending in "-" runs on into the
 * next ("Twin-" "kle"), so the words on screen read as words.
 */
data class SongLine(val picture: String, val notes: List<Int>, val syllables: List<String>) {
    init {
        require(notes.size == syllables.size) { "${notes.size} notes for ${syllables.size} syllables in $syllables" }
    }

    /** The line as said before it is played: "Twinkle, twinkle, little star". */
    val words: String get() = buildString {
        syllables.forEachIndexed { i, s ->
            if (i > 0 && !syllables[i - 1].endsWith("-")) append(' ')
            append(s.removeSuffix("-"))
        }
    }
}

/**
 * A rhyme whose words and tune are both out of copyright, played by our own synth: a
 * recording of a public-domain song is still someone's copyright, so nothing is copied
 * from anywhere. [key] names it in the weekly note's "favourite song"; every Old
 * MacDonald verse shares one. [calm] songs suit the wind-down.
 */
data class Song(val key: String, val title: Txt, val lang: Lang, val lines: List<SongLine>, val calm: Boolean)

/**
 * Tap the tune: every tap anywhere plays the next note, as on a music box, and the word
 * for that note lights up. Tap fast and the song goes fast; stop and it waits. From 5 the
 * lit word is the one to read along with.
 */
data class TuneReel(
    override val id: Long,
    override val level: Int,
    override val difficulty: Int,
    val song: Song,
    val readAlong: Boolean,
) : Reel {
    override val topic get() = Topic.TAP_TUNE
    override val needsSolving get() = false
    override val prompt: Txt get() = song.title

    /** A song is in one language and sung in it, as an alphabet is. */
    override val voice: Lang get() = song.lang
}

object Songs {
    private fun line(picture: String, notes: String, words: String) =
        SongLine(picture, notes.split(' ').map { it.toInt() }, words.split(' '))

    // C major, middle C = 0: G3 = -5, A3 = -3, B3 = -1, D = 2, E = 4, F = 5, G = 7, A = 9, B = 11, C5 = 12.

    val twinkle = Song(
        "twinkle", txt("Twinkle Twinkle Little Star", "ट्विंकल ट्विंकल लिटिल स्टार"), Lang.EN,
        listOf(
            line("⭐", "0 0 7 7 9 9 7", "Twin- kle twin- kle lit- tle star,"),
            line("🌟", "5 5 4 4 2 2 0", "How I won- der what you are!"),
            line("☁️", "7 7 5 5 4 4 2", "Up a- bove the world so high,"),
            line("💎", "7 7 5 5 4 4 2", "Like a dia- mond in the sky."),
            line("⭐", "0 0 7 7 9 9 7", "Twin- kle twin- kle lit- tle star,"),
            line("🌟", "5 5 4 4 2 2 0", "How I won- der what you are!"),
        ),
        calm = true,
    )

    /** The same tune as Twinkle, as it has always been. */
    val baaBaa = Song(
        "baa-baa", txt("Baa Baa Black Sheep", "बा बा ब्लैक शीप"), Lang.EN,
        listOf(
            line("🐑", "0 0 7 7 9 11 12 9 7", "Baa, baa, black sheep, have you a- ny wool?"),
            line("👜", "5 5 4 4 2 2 0", "Yes sir, yes sir, three bags full!"),
            line("🐑", "7 7 7 5 5 4 4 4 2", "One for the mas- ter, one for the dame,"),
            line("🏠", "7 7 7 5 5 5 4 4 4 2 2 0", "And one for the lit- tle boy who lives down the lane."),
        ),
        calm = true,
    )

    val rowYourBoat = Song(
        "row-your-boat", txt("Row Row Row Your Boat", "रो रो रो योर बोट"), Lang.EN,
        listOf(
            line("⛵", "0 0 0 2 4", "Row, row, row your boat"),
            line("🌊", "4 2 4 5 7", "Gent- ly down the stream."),
            line("🎉", "12 12 12 7 7 7 4 4 4 0 0 0", "Mer- ri- ly, mer- ri- ly, mer- ri- ly, mer- ri- ly,"),
            line("🌙", "7 5 4 2 0", "Life is but a dream."),
        ),
        calm = true,
    )

    /** An Old MacDonald verse: an animal and the sound it makes, one syllable each. */
    class FarmAnimal(val emoji: String, val name: String, val sound: String)

    val farm = listOf(
        FarmAnimal("🐄", "cow", "moo"),
        FarmAnimal("🦆", "duck", "quack"),
        FarmAnimal("🐖", "pig", "oink"),
        FarmAnimal("🐑", "sheep", "baa"),
        FarmAnimal("🐕", "dog", "woof"),
    )

    val oldMacDonaldTitle = txt("Old MacDonald Had a Farm", "ओल्ड मैकडॉनल्ड हैड अ फ़ार्म")

    /** A lively one, so never in the wind-down. */
    fun oldMacDonald(a: FarmAnimal): Song {
        val s = a.sound
        return Song(
            "old-macdonald", oldMacDonaldTitle, Lang.EN,
            listOf(
                line("🌾", "0 0 0 -5 -3 -3 -5", "Old Mac- Don- ald had a farm,"),
                line("🌾", "4 4 2 2 0", "E, I, E, I, O!"),
                line(a.emoji, "-5 0 0 0 -5 -3 -3 -5", "And on that farm he had a ${a.name},"),
                line(a.emoji, "4 4 2 2 0", "E, I, E, I, O!"),
                line(a.emoji, "-5 -5 0 0 0 -5 -5 0 0 0", "With a $s $s here, and a $s $s there,"),
                line(a.emoji, "0 0 0 0 0 0 0 0 0 0 0 0", "Here a $s, there a $s, ev- ery- where a $s $s,"),
                line("🌾", "0 0 0 -5 -3 -3 -5", "Old Mac- Don- ald had a farm,"),
                line("🌾", "4 4 2 2 0", "E, I, E, I, O!"),
            ),
            calm = false,
        )
    }

    /** The four songs, one Old MacDonald per farm animal. A Hindi folk rhyme joins once its source is checked. */
    val all: List<Song> = listOf(twinkle, baaBaa, rowYourBoat) + farm.map(::oldMacDonald)

    /** The languages there are songs in: a child with none of them gets no "Tap the tune". */
    val langs: Set<Lang> = all.map { it.lang }.toSet()

    /** The notes the music box has: the synth builds these and nothing else. */
    val NOTES: List<Int> = listOf(-5, -3, -1, 0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19)

    fun byKey(key: String): Song? = all.firstOrNull { it.key == key }

    fun make(g: Gen): TuneReel {
        val usable = all.filter { it.lang in g.langs && (!g.calm || it.calm) }
        // One pick per song, not per verse: five Old MacDonalds would drown out Twinkle.
        val keys = usable.map { it.key }.distinct()
        val key = if (g.ordered) keys[g.inOrder("songs", keys.size)] else g.pick(keys)
        val song = g.pick(usable.filter { it.key == key })
        return TuneReel(g.id, g.level, g.difficulty, song, readAlong = g.level >= 5)
    }

    val tapAnywhere = txt("Tap anywhere to play the song, one note at a time!", "कहीं भी छूकर गाना बजाओ, एक-एक सुर!")

    fun played(song: Song): Txt = txt("You played ${song.title[Lang.EN]}!", "तुमने ${song.title[Lang.HI]} बजा दिया!")
}
