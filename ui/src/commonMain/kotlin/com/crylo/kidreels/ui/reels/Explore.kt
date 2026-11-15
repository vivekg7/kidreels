package com.crylo.kidreels.ui.reels

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.CountAlong
import com.crylo.kidreels.core.LetterCard
import com.crylo.kidreels.core.NumberWords
import com.crylo.kidreels.core.Phrases
import com.crylo.kidreels.core.Picture
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.ShapeCard
import com.crylo.kidreels.core.Tone
import com.crylo.kidreels.ui.S
import com.crylo.kidreels.ui.Sfx
import com.crylo.kidreels.ui.common.Confetti
import com.crylo.kidreels.ui.common.HoldButton
import com.crylo.kidreels.ui.common.MultiText
import com.crylo.kidreels.ui.common.Snooze
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.floating
import com.crylo.kidreels.ui.common.popOn
import com.crylo.kidreels.ui.common.pressable
import com.crylo.kidreels.ui.common.tap
import com.crylo.kidreels.ui.draw.countedCorners
import com.crylo.kidreels.ui.draw.drawShape
import com.crylo.kidreels.ui.skin.LocalReelColors
import com.crylo.kidreels.ui.skin.reelWidth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Tap each one to count it. When all are counted: the number, big, and a cheer. */
@Composable
fun CountAlongView(reel: CountAlong, padding: PaddingValues) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val counted = remember(reel.id) { mutableStateListOf<Int>() }
    val done = counted.size == reel.n
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(padding).reelWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MultiText(reel.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 24.sp, say = { env.say(it) })
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    (0 until reel.n).chunked(if (reel.n <= 6) 3 else 5).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { i ->
                                val number = counted.indexOf(i) + 1
                                Box(
                                    Modifier.size(if (reel.n <= 6) 92.dp else 62.dp).clip(RoundedCornerShape(20.dp))
                                        .background(colors.card.copy(alpha = if (number > 0) 1f else colors.card.alpha * 0.7f))
                                        .tap {
                                            if (i in counted) return@tap
                                            counted += i
                                            if (counted.size == reel.n) {
                                                env.sfx(Sfx.CORRECT)
                                                env.say(Phrases.counted(reel.n, reel.thing), tone = Tone.CHEER)
                                            } else {
                                                env.sfx(Sfx.TICK)
                                                env.say(NumberWords.of(counted.size))
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    T(reel.emoji, Modifier.popOn(number), size = if (reel.n <= 6) 52.sp else 34.sp)
                                    if (number > 0) {
                                        Box(
                                            Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).clip(CircleShape).background(colors.good),
                                            contentAlignment = Alignment.Center,
                                        ) { T(number.toString(), color = Color.White, size = 13.sp, weight = FontWeight.Bold) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(110.dp), contentAlignment = Alignment.Center) {
                if (done) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        T(reel.n.toString(), Modifier.popOn(1), color = colors.ink, size = 64.sp, weight = FontWeight.Black)
                        MultiText(NumberWords.of(reel.n), env.langs, colors.ink, size = 26.sp, say = { env.say(it) })
                    }
                } else if (counted.isEmpty()) {
                    T("👆", Modifier.floating(), size = 40.sp)
                }
            }
        }
        Confetti(if (done) 1 else 0)
    }
}

/** "A for Apple": the letter, the picture, the word, and the word in the other languages. */
@Composable
fun LetterView(reel: LetterCard, padding: PaddingValues) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val letter = reel.letter
    val lang = reel.lang
    var bounce by remember(reel.id) { mutableIntStateOf(0) }
    Column(
        Modifier.fillMaxSize().padding(padding).reelWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Box(
            Modifier.shadow(10.dp, RoundedCornerShape(36.dp)).clip(RoundedCornerShape(36.dp)).background(Color.White)
                .pressable({ env.say(Say(letter.glyph, lang)); bounce++ })
                .padding(horizontal = 34.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            // The capital only: "A a" side by side reads as two letters to a new learner.
            T(letter.glyph, Modifier.popOn(bounce), color = Color(0xFFE5484D), size = 104.sp, weight = FontWeight.Black)
        }
        Box(Modifier.tap { env.say(letter.spoken()); bounce++ }) {
            when (val p = letter.picture) {
                is Picture.Emoji -> T(p.text, Modifier.floating().popOn(bounce), size = 130.sp)
                is Picture.Drawn -> Canvas(Modifier.size(150.dp).floating().popOn(bounce)) { drawShape(p.shape, Color(0xFFFFB300)) }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T(letter.phrase, Modifier.tap { env.say(letter.spoken()) }, color = colors.ink, size = 34.sp, weight = FontWeight.ExtraBold)
            // The word in the other chosen languages: अनानास is "Pineapple" too.
            val others = env.langs.filter { it != lang && letter.meaning[it] != letter.word }
            others.forEach { other ->
                T(
                    letter.meaning[other],
                    Modifier.padding(top = 4.dp).tap { env.say(Say(letter.meaning[other], other)) },
                    color = colors.ink.copy(alpha = 0.8f), size = 22.sp, weight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** A big shape. Tap it to count its corners one by one. */
@Composable
fun ShapeView(reel: ShapeCard, padding: PaddingValues) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val shape = reel.shape
    var lit by remember(reel.id) { mutableIntStateOf(0) }
    var running by remember(reel.id) { mutableStateOf(false) }
    val wobble = remember(reel.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val color = Color(reel.argb)

    fun countCorners(cornerCount: Int) {
        if (running) return
        running = true
        scope.launch {
            wobble.animateTo(8f, tween(90))
            wobble.animateTo(-8f, tween(140))
            wobble.animateTo(0f, tween(90))
            if (cornerCount == 0) {
                env.say(listOf(Say(shape.name[env.main], env.main), Say(shape.fact[env.main], env.main)))
            } else {
                for (i in 1..cornerCount) {
                    lit = i
                    env.sfx(Sfx.TICK)
                    env.say(NumberWords.of(i))
                    delay(700)
                }
                env.say(listOf(Say(shape.fact[env.main], env.main)))
                delay(1200)
                lit = 0
            }
            running = false
        }
    }

    Column(
        Modifier.fillMaxSize().padding(padding).reelWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        MultiText(shape.name, env.langs, colors.ink, size = 40.sp, say = { env.say(it) })
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                Modifier.size(220.dp).floating(6f, 1800).graphicsLayer { rotationZ = wobble.value }
                    .tap { countCorners(countedCorners(shape.kind, androidx.compose.ui.geometry.Size(1f, 1f)).size) },
            ) {
                drawShape(shape.kind, color, Color.White.copy(alpha = 0.9f))
                countedCorners(shape.kind, size).take(lit).forEach { p ->
                    drawCircle(Color.White, size.minDimension * 0.06f, p)
                    drawCircle(Color(0xFF2D2440), size.minDimension * 0.06f, p, style = Stroke(size.minDimension * 0.015f))
                }
            }
        }
        MultiText(shape.fact, env.langs, colors.ink, size = 20.sp, say = { env.say(it) })
        if (shape.examples.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                shape.examples.forEach { e ->
                    var pop by remember(reel.id, e) { mutableIntStateOf(0) }
                    T(e, Modifier.popOn(pop).tap { pop++; env.sfx(Sfx.POP) }, size = 48.sp)
                }
            }
        }
    }
}

/** The goodbye reel. Calm on purpose: no confetti, nothing to tap but the parent button. */
@Composable
fun TimeUpView(padding: PaddingValues, onMoreTime: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    Column(
        Modifier.fillMaxSize().padding(padding).reelWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Box {
            T("🌙", Modifier.floating(6f, 2600), size = 120.sp)
            Snooze(colors.ink, Modifier.align(Alignment.TopEnd).padding(top = 10.dp))
        }
        MultiText(Phrases.allDone, env.langs, colors.ink, size = 32.sp, say = { env.say(it) })
        MultiText(Phrases.allDoneBody, env.langs, colors.ink, size = 20.sp, say = { env.say(it) })
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            listOf("🍽️", "⚽", "🧸", "📚", "🛏️").forEach { T(it, size = 38.sp) }
        }
        HoldButton(S.moreTime[env.main], onMoreTime, Modifier.fillMaxWidth(), background = colors.ink.copy(alpha = 0.16f), content = colors.ink)
    }
}
