package com.crylo.kidreels.ui.reels

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import com.crylo.kidreels.ui.art.Picture
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.popOn
import com.crylo.kidreels.ui.common.tap
import androidx.compose.runtime.staticCompositionLocalOf
import com.crylo.kidreels.core.AngleLab
import com.crylo.kidreels.core.BodyCard
import com.crylo.kidreels.core.BodyTap
import com.crylo.kidreels.core.CountAlong
import com.crylo.kidreels.core.CupGame
import com.crylo.kidreels.core.OrderGame
import com.crylo.kidreels.core.PairsGame
import com.crylo.kidreels.core.CurveLab
import com.crylo.kidreels.core.DragGame
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.LetterCard
import com.crylo.kidreels.core.MazeGame
import com.crylo.kidreels.core.NumberCard
import com.crylo.kidreels.core.SieveLab
import com.crylo.kidreels.core.SquareCard
import com.crylo.kidreels.core.TableCard
import com.crylo.kidreels.core.Phrases
import com.crylo.kidreels.core.Quiz
import com.crylo.kidreels.core.Reel
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.ShapeCard
import com.crylo.kidreels.core.SolidCard
import com.crylo.kidreels.core.TimeUp
import com.crylo.kidreels.core.Tone
import com.crylo.kidreels.core.Txt
import com.crylo.kidreels.core.Word
import com.crylo.kidreels.core.WordCard
import com.crylo.kidreels.core.WordUseCard
import com.crylo.kidreels.core.Words
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

    /** "This is an elephant!" for a tapped picture; [word] when the reel names it its own way. */
    fun name(emoji: String, voice: Lang? = null, word: Word? = null) {
        val w = word ?: Words.of(emoji) ?: return
        say(w.thisIs(), voice)
    }

    /** A tapped piece of a row or grid: a picture says its name, a numeral is read out. */
    fun token(text: String, voice: Lang? = null) {
        if (Words.of(text) != null) name(text, voice) else say(Txt.same(text), voice)
    }

    fun sfx(s: Sfx) {
        if (active) play(s)
    }

    /**
     * A cheer, then the [reveal], or [said] when the reveal is spoken in parts. [first] goes
     * before the cheer: the last piece's own line in a drag game.
     */
    fun praise(reveal: Txt?, voice: Lang?, said: List<Say>? = null, first: Txt? = null) {
        val lines = listOfNotNull(first?.let { Say(it[main], main) }).toMutableList()
        lines += Say(Phrases.pick(Phrases.praise, Random)[main], main, Tone.CHEER)
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
        is WordCard -> WordView(reel, padding)
        is NumberCard -> NumberView(reel, padding)
        is TableCard -> TableView(reel, padding)
        is SquareCard -> SquareView(reel, padding)
        is SieveLab -> SieveView(reel, padding)
        is CupGame -> CupView(reel, state, padding, onSolved)
        is PairsGame -> PairsView(reel, state, padding, onSolved)
        is OrderGame -> OrderView(reel, state, padding, onSolved)
        is MazeGame -> MazeView(reel, state, padding, onSolved)
        is BodyCard -> BodyCardView(reel, padding)
        is BodyTap -> BodyTapView(reel, state, padding, onSolved)
        is WordUseCard -> WordUseView(reel, padding)
        is DragGame -> DragView(reel, state, padding, onSolved)
        is TimeUp -> TimeUpView(reel.bedtime, padding, onMoreTime)
    }
}

/** A picture that says what it is when tapped, with a little pop. */
@Composable
fun Pic(emoji: String, size: TextUnit, modifier: Modifier = Modifier, voice: Lang? = null, word: Word? = null) {
    val env = LocalReelEnv.current
    var pop by remember { mutableIntStateOf(0) }
    Picture(emoji, size, modifier.popOn(pop).tap { pop++; env.name(emoji, voice, word) })
}
