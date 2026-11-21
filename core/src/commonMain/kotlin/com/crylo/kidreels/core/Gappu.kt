package com.crylo.kidreels.core

/**
 * Gappu / गप्पू, the app's baby elephant (docs/design.md#gappu). A loved character is a
 * hook, so he is only ever used for the stop, never against it: he yawns in the wind-down,
 * goes to eat on the goodbye reel and is asleep on the good-night reel. He never needs
 * feeding or care, never asks the child to stay and is never sad that they leave; his one
 * sad face is about a puzzle swiped past, for a moment.
 */
object Gappu {
    /** A name, not a word: tapping him says it in the reel's voice, with no bridge. */
    val name = txt("Gappu", "गप्पू")

    /** On the goodbye reel, after "time to eat, play and rest": he goes too, so it is not only the child who stops. */
    val eatsToo = txt("Gappu is going to eat too.", "गप्पू भी खाना खाने जा रहा है।")

    /** On the good-night reel: he is asleep already, the gentlest way to say it is bedtime. */
    val asleep = txt("Gappu is asleep already.", "गप्पू सो गया है।")
}

/**
 * Gappu's drawings, each an SVG in art/drawn/ ("art:gappu-happy"). Every one is a picture
 * with a name, so tapping him anywhere he can be tapped says "This is Gappu!".
 */
enum class GappuPose {
    /** Waving. */
    HAPPY,
    CHEER,
    SURPRISED,

    /** His cap flies off. */
    SNEEZE,
    YAWN,

    /** Curled up, lying down: wider than tall. */
    ASLEEP,

    /** Gentle, ears drooping: sad about a puzzle, never about the child. */
    SAD,

    /** A banana. */
    EATING,

    /** On one foot. */
    DANCE,
    ;

    /** The picture's key, as a word uses it. */
    val art: String get() = "art:gappu-" + name.lowercase()
}
