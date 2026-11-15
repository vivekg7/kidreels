package com.crylo.kidreels.ui.reels

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Diagram
import com.crylo.kidreels.core.DiagramCard
import com.crylo.kidreels.core.DiagramPart
import com.crylo.kidreels.core.DiagramTap
import com.crylo.kidreels.core.Diagrams
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.Tone
import com.crylo.kidreels.ui.ReelState
import com.crylo.kidreels.ui.Sfx
import com.crylo.kidreels.ui.art.Arts
import com.crylo.kidreels.ui.art.drawArt
import com.crylo.kidreels.ui.common.MultiText
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.floating
import com.crylo.kidreels.ui.skin.LocalReelColors
import com.crylo.kidreels.ui.skin.reelWidth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Glow = Color(0xFFFFC107)
private val Miss = Color(0xFFE5484D)

/**
 * A drawing (the child, the plant), with [lit] ringed on every side it is on, and [missed] ringed red for
 * a moment after a wrong tap. A tap anywhere is turned into the part it landed on.
 */
@Composable
fun DiagramFigure(d: Diagram, lit: DiagramPart?, missed: DiagramPart?, modifier: Modifier, onTap: (DiagramPart) -> Unit) {
    val pulse by rememberInfiniteTransition().animateFloat(1f, 1.35f, infiniteRepeatable(tween(700), RepeatMode.Reverse))
    val tap by rememberUpdatedState(onTap)
    Canvas(
        modifier.pointerInput(Unit) {
            detectTapGestures { p ->
                val scale = minOf(size.width / d.width, size.height / d.height)
                val x = (p.x - (size.width - d.width * scale) / 2) / scale
                val y = (p.y - (size.height - d.height * scale) / 2) / scale
                Diagrams.at(d, x, y)?.let(tap)
            }
        },
    ) {
        drawArt(Arts.byName.getValue(d.art))
        val scale = minOf(size.width / d.width, size.height / d.height)
        val origin = Offset((size.width - d.width * scale) / 2, (size.height - d.height * scale) / 2)
        fun ring(part: DiagramPart, colour: Color, grow: Float) = part.spots.forEach { (x, y) ->
            val at = origin + Offset(x * scale, y * scale)
            val r = (part.radius + 4) * scale * grow
            drawCircle(colour.copy(alpha = 0.25f), r, at)
            drawCircle(colour, r, at, style = Stroke(3.5.dp.toPx()))
        }
        lit?.let { ring(it, Glow, pulse) }
        missed?.let { ring(it, Miss, 1f) }
    }
}

/** "This is your knee": the part lit up and named. Tapping another part lights and names that one instead. */
@Composable
fun DiagramCardView(reel: DiagramCard, padding: PaddingValues) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    var shown by remember(reel.id) { mutableStateOf(reel.part) }
    Column(
        Modifier.fillMaxSize().padding(padding).reelWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        MultiText(Diagrams.title(shown), env.langs, colors.ink, size = 40.sp, say = { env.say(it) })
        DiagramFigure(reel.diagram, shown, null, Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)) { part ->
            shown = part
            env.sfx(Sfx.POP)
            env.say(listOfNotNull(Diagrams.thisIs(reel.diagram, part), part.fact.takeIf { reel.showFact }).map { Say(it[env.main], env.main) })
        }
        Box(Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
            if (reel.showFact) MultiText(shown.fact, env.langs, colors.ink, size = 20.sp, say = { env.say(it) })
            else T("👆", Modifier.floating(), size = 32.sp)
        }
    }
}

/** "Tap the knee!": a wrong part is ringed red for a moment and named, then the question again. */
@Composable
fun DiagramTapView(reel: DiagramTap, state: ReelState, padding: PaddingValues, onSolved: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val scope = rememberCoroutineScope()
    var missed by remember(reel.id) { mutableStateOf<DiagramPart?>(null) }
    Column(Modifier.fillMaxSize().padding(padding).reelWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        MultiText(reel.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 30.sp, say = { env.say(it) })
        DiagramFigure(reel.diagram, reel.part.takeIf { state.solved }, missed, Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp)) { part ->
            if (state.solved) {
                env.say(Diagrams.thisIs(reel.diagram, part))
            } else if (part == reel.part) {
                missed = null
                env.sfx(Sfx.CORRECT)
                env.praise(Diagrams.found(part) + part.fact, null)
                onSolved()
            } else {
                state.wrongTries++
                missed = part
                env.sfx(Sfx.WRONG)
                env.say(Diagrams.notThis(part, reel.part), tone = Tone.GENTLE)
                scope.launch {
                    delay(1400)
                    if (missed == part) missed = null
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
            if (state.solved) MultiText(Diagrams.found(reel.part), env.langs, colors.ink, size = 20.sp, say = { env.say(it) })
            else T("👆", Modifier.floating(), size = 32.sp)
        }
    }
}
