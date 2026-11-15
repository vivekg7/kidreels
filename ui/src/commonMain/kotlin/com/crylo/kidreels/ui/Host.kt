package com.crylo.kidreels.ui

import com.crylo.kidreels.core.Clock
import com.crylo.kidreels.core.KeyValueStore
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.Say

/**
 * Text to speech. Each platform has its own engine; the shared UI only says what to say
 * and in which language. Readiness must be Compose state, so screens update as soon as
 * the engine reports in or a parent installs a voice.
 */
interface Speaker {
    /** False when the phone has no speech engine at all. */
    val hasEngine: Boolean

    /** True when [lang] has a voice installed that works offline. */
    fun ready(lang: Lang): Boolean

    /** Stops whatever is being said and says [lines] in order. */
    fun speak(lines: List<Say>)

    fun stop()

    fun setSlow(slow: Boolean)
}

/** Short sounds, made on the device (no audio files): a chime, a soft boop, a pop. */
enum class Sfx { CORRECT, WRONG, POP, SAD, TICK }

/** What the platform shell provides. Android's is MainActivity; a desktop or iOS shell implements the same. */
interface Host {
    val store: KeyValueStore
    val clock: Clock
    val speaker: Speaker
    val appVersion: String

    fun play(sfx: Sfx)

    /** The phone's voice settings, to install a missing language. Only reached past the parent gate. */
    fun openVoiceSettings()

    /** A reel playing should not let the screen go dark mid-puzzle. */
    fun keepScreenOn(on: Boolean)

    /** Whether the status bar icons should be dark (on a light background). */
    fun darkSystemIcons(dark: Boolean)
}
