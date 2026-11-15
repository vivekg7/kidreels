package com.crylo.kidreels

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.crylo.kidreels.ui.Sfx
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * The few sound effects, synthesised once at start-up: a short tone sequence each, so the
 * APK carries no audio files and there is nothing to license.
 */
class Sounds {
    private class Note(val hz: Double, val millis: Int)

    private val tracks: Map<Sfx, AudioTrack> = mapOf(
        Sfx.CORRECT to build(listOf(Note(523.25, 90), Note(659.25, 90), Note(783.99, 180)), 0.32),
        Sfx.WRONG to build(listOf(Note(311.13, 110), Note(261.63, 170)), 0.22),
        Sfx.POP to build(listOf(Note(880.0, 50)), 0.22),
        Sfx.SAD to build(listOf(Note(392.0, 160), Note(349.23, 160), Note(311.13, 300)), 0.22),
        Sfx.TICK to build(listOf(Note(1046.5, 40)), 0.16),
    )

    fun play(sfx: Sfx) {
        val track = tracks[sfx] ?: return
        runCatching {
            track.stop()
            track.reloadStaticData()
            track.play()
        }
    }

    fun release() = tracks.values.forEach { it.release() }

    private fun build(notes: List<Note>, volume: Double): AudioTrack {
        val rate = 22050
        val samples = ShortArray(notes.sumOf { it.millis } * rate / 1000)
        var at = 0
        for (note in notes) {
            val n = note.millis * rate / 1000
            for (i in 0 until n) {
                val t = i.toDouble() / rate
                // A soft attack and an exponential tail, so notes chime instead of click.
                val envelope = (i / (rate * 0.005)).coerceAtMost(1.0) * exp(-t * 9)
                val tone = sin(2 * PI * note.hz * t) + 0.3 * sin(4 * PI * note.hz * t)
                samples[at + i] = (tone / 1.3 * envelope * volume * Short.MAX_VALUE).toInt().toShort()
            }
            at += n
        }
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(samples, 0, samples.size)
        return track
    }
}
