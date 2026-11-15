package com.crylo.kidreels.ui.reels

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.crylo.kidreels.core.AngleLab
import com.crylo.kidreels.core.CountAlong
import com.crylo.kidreels.core.CurveLab
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.LetterCard
import com.crylo.kidreels.core.Phrases
import com.crylo.kidreels.core.Quiz
import com.crylo.kidreels.core.Reel
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.ShapeCard
import com.crylo.kidreels.core.SolidCard
import com.crylo.kidreels.core.TimeUp
import com.crylo.kidreels.core.Tone
import com.crylo.kidreels.core.Txt
import com.crylo.kidreels.ui.ReelState
import com.crylo.kidreels.ui.Sfx
import kotlin.random.Random

/** What a reel can do besides drawing itself: talk, make a sound, report it was solved. */
class ReelEnv(
    val langs: List<Lang>,
    /** True only for the reel on screen: off-screen pages stay still and silent. */
    val active: Boolean,
    private val speak: (List<Say>) -> Unit,
    private val play: (Sfx) -> Unit,
) {
    val main: Lang get() = langs.first()

    fun say(lines: List<Say>) {
        if (active) speak(lines)
    }

    fun say(line: Say) = say(listOf(line))

    /** [text] in the main language, or in [voice] for a reel about that language. */
    fun say(text: Txt, voice: Lang? = null, tone: Tone = Tone.TALK) = say(Say(text[voice ?: main], voice ?: main, tone))

    fun sfx(s: Sfx) {
        if (active) play(s)
    }

    /** A cheer, then the [reveal], or [said] when the reveal is spoken in parts. */
    fun praise(reveal: Txt?, voice: Lang?, said: List<Say>? = null) {
        val lines = mutableListOf(Say(Phrases.pick(Phrases.praise, Random)[main], main, Tone.CHEER))
        lines += said ?: reveal?.let { listOf(Say(it[voice ?: main], voice ?: main)) }.orEmpty()
        say(lines)
    }
}

val LocalReelEnv = staticCompositionLocalOf<ReelEnv> { error("No ReelEnv") }

@Composable
fun ReelView(
    reel: Reel,
    state: ReelState,
    padding: PaddingValues,
    onSolved: () -> Unit,
    onMoreTime: () -> Unit,
) {
    when (reel) {
        is Quiz -> QuizView(reel, state, padding, onSolved)
        is CountAlong -> CountAlongView(reel, padding)
        is LetterCard -> LetterView(reel, padding)
        is ShapeCard -> ShapeView(reel, padding)
        is SolidCard -> SolidView(reel, padding)
        is AngleLab -> AngleView(reel, state, padding, onSolved)
        is CurveLab -> CurveView(reel, padding)
        is TimeUp -> TimeUpView(padding, onMoreTime)
    }
}
