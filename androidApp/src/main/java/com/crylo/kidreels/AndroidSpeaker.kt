package com.crylo.kidreels

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.Tone
import com.crylo.kidreels.ui.Speaker
import java.util.Locale

/**
 * The platform [TextToSpeech], carried over from Kids Learn: no bundled audio, no network
 * of our own. Whatever engine the phone has (usually Google's, offline once a voice is
 * downloaded) does the speaking. A language without an installed voice stays silent
 * rather than being read out by the wrong voice.
 */
class AndroidSpeaker(context: Context) : Speaker {
    private var started by mutableStateOf(false)
    private var noEngine by mutableStateOf(false)
    private val readyLangs = mutableStateMapOf<Lang, Boolean>()
    private var englishLocale: Locale = Locale.ENGLISH
    private var rate = NORMAL_RATE
    private var utterance = 0
    private val tts = TextToSpeech(context.applicationContext) { onInit(it) }

    override val hasEngine: Boolean get() = !noEngine

    /** Optimistic until checked, so the "install a voice" note does not flash on every launch. */
    override fun ready(lang: Lang): Boolean = readyLangs[lang] ?: true

    private fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            noEngine = true
            return
        }
        // Indian English first: it says "zed" and reads names the way Indian parents do.
        englishLocale = ENGLISH_PREFERENCE.firstOrNull { availability(it) >= TextToSpeech.LANG_AVAILABLE } ?: Locale.ENGLISH
        started = true
        refresh()
    }

    /** Re-checks the voices; called on resume, as a parent may have just installed one. */
    fun refresh() {
        if (!started) return
        Lang.entries.forEach { readyLangs[it] = hasLocalVoice(locale(it)) }
    }

    private fun locale(lang: Lang): Locale = when (lang) {
        Lang.EN -> englishLocale
        Lang.HI -> HINDI
    }

    override fun speak(lines: List<Say>) {
        if (!started) return
        tts.stop()
        var first = true
        for (line in lines) {
            if (readyLangs[line.lang] == false || line.alsoNeeds?.let { readyLangs[it] == false } == true) continue
            // Language, pitch and pace are fixed per call to speak(), so queued lines can
            // each have their own.
            if (tts.setLanguage(locale(line.lang)) < TextToSpeech.LANG_AVAILABLE) continue
            if (!first) tts.playSilentUtterance(line.pauseMillis, TextToSpeech.QUEUE_ADD, "p${utterance++}")
            first = false
            val (pitch, pace) = sound(line.tone)
            tts.setPitch(pitch)
            tts.setSpeechRate(rate * pace)
            tts.speak(line.text, TextToSpeech.QUEUE_ADD, null, "u${utterance++}")
        }
    }

    /**
     * Pitch and pace for each tone. The engine has no emotions, so this borrows the way
     * adults talk to toddlers: a little higher and slower than usual, brighter for praise,
     * softer for a miss. Small steps: much past these, the voice turns cartoonish.
     */
    private fun sound(tone: Tone): Pair<Float, Float> = when (tone) {
        Tone.TALK -> 1.1f to 1f
        Tone.CHEER -> 1.25f to 1.08f
        Tone.GENTLE -> 1.05f to 0.9f
        Tone.CALM -> 0.95f to 0.85f
    }

    override fun stop() {
        if (started) tts.stop()
    }

    override fun setSlow(slow: Boolean) {
        rate = if (slow) SLOW_RATE else NORMAL_RATE
    }

    fun shutdown() = tts.shutdown()

    /** The engine's "install voice data" screen, falling back to the system TTS settings. */
    fun openVoiceInstall(context: Context) {
        val install = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
            runCatching { tts.defaultEngine }.getOrNull()?.let { setPackage(it) }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val settings = Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        for (intent in listOf(install, settings)) {
            try {
                context.startActivity(intent)
                return
            } catch (_: ActivityNotFoundException) {
                // Try the next one.
            } catch (_: SecurityException) {
                // Some engines do not export the install activity.
            }
        }
    }

    private fun availability(locale: Locale): Int =
        runCatching { tts.isLanguageAvailable(locale) }.getOrDefault(TextToSpeech.LANG_NOT_SUPPORTED)

    /**
     * "Available" can mean "downloadable" or "network only". A voice counts only if it is
     * installed and works offline; engines that list no voices are trusted on the language check.
     */
    private fun hasLocalVoice(locale: Locale): Boolean {
        if (availability(locale) < TextToSpeech.LANG_AVAILABLE) return false
        val voices = runCatching { tts.voices }.getOrNull().orEmpty().filter { it.locale.language == locale.language }
        if (voices.isEmpty()) return true
        return voices.any { !it.isNetworkConnectionRequired && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }
    }

    companion object {
        val HINDI: Locale = Locale.forLanguageTag("hi-IN")

        /** Below the engine's 1.0: that pace is for adults, and young children need time to follow. */
        private const val NORMAL_RATE = 0.85f
        private const val SLOW_RATE = 0.7f
        private val ENGLISH_PREFERENCE = listOf("en-IN", "en-GB", "en-US").map(Locale::forLanguageTag)
    }
}
