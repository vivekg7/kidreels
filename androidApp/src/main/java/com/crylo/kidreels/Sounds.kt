package com.crylo.kidreels

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.crylo.kidreels.core.Songs
import com.crylo.kidreels.ui.Sfx
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * The sound effects and the music box, synthesised once at start-up: a short tone
 * sequence each, so the APK carries no audio files and there is nothing to license.
 */
class Sounds {
    private class Note(val hz: Double, val millis: Int)

    private val tracks: Map<Sfx, AudioTrack> = mapOf(
        Sfx.CORRECT to build(listOf(Note(523.25, 90), Note(659.25, 90), Note(783.99, 180)), 0.32),
        Sfx.WRONG to build(listOf(Note(311.13, 110), Note(261.63, 170)), 0.22),
        Sfx.POP to build(listOf(Note(880.0, 50)), 0.22),
        Sfx.SAD to build(listOf(Note(392.0, 160), Note(349.23, 160), Note(311.13, 300)), 0.22),
        Sfx.TICK to build(listOf(Note(1046.5, 40)), 0.16),
        Sfx.BOING to track(boing(), 0.3),
        Sfx.DRUM to track(drum(), 0.5),
        Sfx.CLAP to track(noise(90, decay = 40.0, seed = 1), 0.28),
        Sfx.SNEEZE to track(noise(260, decay = 9.0, seed = 2), 0.22),
        Sfx.BUBBLE to track(plop(), 0.3),
    )

    /**
     * The music box: one note for each of [Songs.NOTES], C major from the G below middle C
     * to the G two octaves up, 400 ms each. Fifteen notes at 22 kHz are about 260 KB, made
     * once; every song, the bubbles' scale and the fanfares play on them.
     */
    private val notes: Map<Int, AudioTrack> = Songs.NOTES.associateWith { track(musicBox(MIDDLE_C * 2.0.pow(it / 12.0)), 0.3) }

    fun play(sfx: Sfx) = restart(tracks[sfx])

    /** A note [semitones] from middle C; the nearest the box has if it has not that one. */
    fun note(semitones: Int) = restart(notes[semitones] ?: notes.minByOrNull { abs(it.key - semitones) }?.value)

    private fun restart(track: AudioTrack?) {
        track ?: return
        runCatching {
            track.stop()
            track.reloadStaticData()
            track.play()
        }
    }

    fun release() = (tracks.values + notes.values).forEach { it.release() }

    private fun build(notes: List<Note>, volume: Double): AudioTrack {
        val samples = DoubleArray(notes.sumOf { it.millis } * RATE / 1000)
        var at = 0
        for (note in notes) {
            val n = note.millis * RATE / 1000
            for (i in 0 until n) {
                val t = i.toDouble() / RATE
                // A soft attack and an exponential tail, so notes chime instead of click.
                val envelope = (i / (RATE * 0.005)).coerceAtMost(1.0) * exp(-t * 9)
                val tone = sin(2 * PI * note.hz * t) + 0.3 * sin(4 * PI * note.hz * t)
                samples[at + i] = tone / 1.3 * envelope
            }
            at += n
        }
        return track(samples, volume)
    }

    /** A music-box tine: bright overtones that die away faster than the note itself. */
    private fun musicBox(hz: Double): DoubleArray = DoubleArray(400 * RATE / 1000) { i ->
        val t = i.toDouble() / RATE
        val attack = (i / (RATE * 0.003)).coerceAtMost(1.0)
        val tone = sin(2 * PI * hz * t) * exp(-t * 5) + 0.35 * sin(4 * PI * hz * t) * exp(-t * 9) + 0.12 * sin(6 * PI * hz * t) * exp(-t * 14)
        tone / 1.47 * attack
    }

    /** A spring: a pitch that slides up with a wobble. */
    private fun boing(): DoubleArray {
        var phase = 0.0
        return DoubleArray(320 * RATE / 1000) { i ->
            val t = i.toDouble() / RATE
            val hz = 220 + 600 * (t / 0.32) + 40 * sin(2 * PI * 18 * t)
            phase += 2 * PI * hz / RATE
            sin(phase) * exp(-t * 6) * (i / (RATE * 0.004)).coerceAtMost(1.0)
        }
    }

    /**
     * A soap bubble popping: a short "plop" whose pitch leaps up as it dies, with a breath
     * of noise at the start for the film tearing. Under the music-box note each pop plays.
     */
    private fun plop(): DoubleArray {
        var phase = 0.0
        val r = Random(4)
        return DoubleArray(70 * RATE / 1000) { i ->
            val t = i.toDouble() / RATE
            val hz = 500 + 1400 * (t / 0.07).pow(0.6)
            phase += 2 * PI * hz / RATE
            sin(phase) * exp(-t * 55) * (i / (RATE * 0.002)).coerceAtMost(1.0) + (r.nextDouble() * 2 - 1) * 0.25 * exp(-t * 300)
        }
    }

    /** A drum: a low sine falling in pitch, with a short knock at the start. */
    private fun drum(): DoubleArray {
        var phase = 0.0
        val r = Random(3)
        return DoubleArray(260 * RATE / 1000) { i ->
            val t = i.toDouble() / RATE
            val hz = 55 + 110 * exp(-t * 30)
            phase += 2 * PI * hz / RATE
            sin(phase) * exp(-t * 11) + (r.nextDouble() * 2 - 1) * 0.3 * exp(-t * 120)
        }
    }

    /** A burst of noise: a clap when short and sharp, the "choo" of a sneeze when longer. */
    private fun noise(millis: Int, decay: Double, seed: Int): DoubleArray {
        val r = Random(seed)
        var last = 0.0
        return DoubleArray(millis * RATE / 1000) { i ->
            val t = i.toDouble() / RATE
            // A little smoothing, so it is a hush rather than a hiss.
            last = last * 0.55 + (r.nextDouble() * 2 - 1) * 0.45
            last * exp(-t * decay) * (i / (RATE * 0.002)).coerceAtMost(1.0) * 1.6
        }
    }

    private fun track(samples: DoubleArray, volume: Double): AudioTrack {
        val pcm = ShortArray(samples.size) { (samples[it].coerceIn(-1.0, 1.0) * volume * Short.MAX_VALUE).toInt().toShort() }
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(pcm, 0, pcm.size)
        return track
    }

    private companion object {
        const val RATE = 22050
        const val MIDDLE_C = 261.63
    }
}
