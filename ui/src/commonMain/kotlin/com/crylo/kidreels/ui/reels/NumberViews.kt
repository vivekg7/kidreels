package com.crylo.kidreels.ui.reels

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.NumberCard
import com.crylo.kidreels.core.Numbers
import com.crylo.kidreels.core.Phrases
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.SquareCard
import com.crylo.kidreels.core.TableCard
import com.crylo.kidreels.core.Tone
import com.crylo.kidreels.core.Txt
import com.crylo.kidreels.core.intro
import com.crylo.kidreels.ui.Sfx
import com.crylo.kidreels.ui.common.Confetti
import com.crylo.kidreels.ui.common.MultiText
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.floating
import com.crylo.kidreels.ui.common.popOn
import com.crylo.kidreels.ui.common.pressable
import com.crylo.kidreels.ui.common.tap
import com.crylo.kidreels.ui.draw.Paper
import com.crylo.kidreels.ui.skin.LocalReelColors
import com.crylo.kidreels.ui.skin.reelWidth

/** The maths channel's blue, for the numeral and the filled squares. */
private val NumberBlue = Color(0xFF3D6CE8)

/** "1 · O-N-E · one · 1 elephant", laid out like the alphabet card. */
@Composable
fun NumberView(reel: NumberCard, padding: PaddingValues) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    var bounce by remember(reel.id) { mutableIntStateOf(0) }
    Column(
        Modifier.fillMaxSize().padding(padding).reelWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Box(
            Modifier.shadow(10.dp, RoundedCornerShape(36.dp)).clip(RoundedCornerShape(36.dp)).background(Color.White)
                .pressable({ env.say(reel.intro(env.main)); bounce++ })
                .padding(horizontal = 34.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            T(reel.n.toString(), Modifier.popOn(bounce), color = NumberBlue, size = 96.sp, weight = FontWeight.Black)
        }
        // The English word letter by letter, each letter saying itself: O-N-E.
        if (Lang.EN in env.langs) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                val letters = Numbers.spelling(reel.n)
                letters.forEachIndexed { i, letter ->
                    var pop by remember(reel.id, i) { mutableIntStateOf(0) }
                    Box(
                        Modifier.popOn(pop).clip(RoundedCornerShape(10.dp)).background(colors.card)
                            .tap { pop++; env.say(Say(letter, Lang.EN)) }
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    ) { T(letter, color = colors.cardInk, size = if (letters.size > 6) 22.sp else 28.sp, weight = FontWeight.ExtraBold) }
                }
            }
        }
        MultiText(reel.word, env.langs, colors.ink, size = 34.sp, say = { env.say(it) })
        Things(reel.thing.emoji, reel.n)
        MultiText(reel.thing.of(reel.n), env.langs, colors.ink, size = 24.sp, say = { env.say(it) })
    }
}

/** [n] copies of [emoji] in rows of up to five, smaller as there are more of them. */
@Composable
private fun Things(emoji: String, n: Int) {
    val size = when {
        n == 1 -> 110.sp
        n <= 3 -> 72.sp
        n <= 5 -> 54.sp
        n <= 10 -> 44.sp
        else -> 32.sp
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        (0 until n).chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { row.forEach { _ -> Pic(emoji, size) } }
        }
    }
}

/**
 * A times table filled in a line per tap, each line said as it appears: the child says it
 * along, as in class. The next line waits with a "?" so a child can call it out first.
 */
@Composable
fun TableView(reel: TableCard, padding: PaddingValues) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    var shown by remember(reel.id) { mutableIntStateOf(0) }
    val done = shown == TableCard.LINES
    fun line(times: Int) = Say(Numbers.tableLine(reel.table, times)[env.main], env.main)
    fun next() {
        // Read the state itself: the tap handler can outlive the composition that made it.
        if (shown >= TableCard.LINES) return
        shown++
        if (shown == TableCard.LINES) {
            env.sfx(Sfx.CORRECT)
            env.say(listOf(line(shown), Say(Phrases.tableDone(reel.table)[env.main], env.main, Tone.CHEER)))
        } else {
            env.sfx(Sfx.TICK)
            env.say(line(shown))
        }
    }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(padding).reelWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MultiText(reel.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 30.sp, say = { env.say(it) })
            Box(Modifier.weight(1f).fillMaxWidth().tap(::next), contentAlignment = Alignment.Center) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (times in 1..TableCard.LINES) {
                        val revealed = times <= shown
                        val current = times == shown + 1
                        Row(
                            Modifier.clip(RoundedCornerShape(12.dp))
                                .background(if (current) colors.card else Color.Transparent)
                                .then(if (current) Modifier.border(2.dp, NumberBlue, RoundedCornerShape(12.dp)) else Modifier)
                                .then(if (revealed) Modifier.tap { env.say(line(times)) } else Modifier)
                                .padding(horizontal = 14.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val ink = if (current) colors.cardInk else colors.ink.copy(alpha = if (revealed) 1f else 0.35f)
                            T("${reel.table} × $times", Modifier.width(120.dp), color = ink, size = 26.sp, weight = FontWeight.Bold, align = TextAlign.End)
                            T("=", Modifier.width(36.dp), color = ink, size = 26.sp, weight = FontWeight.Bold, align = TextAlign.Center)
                            T(
                                if (revealed) (reel.table * times).toString() else if (current) "?" else "",
                                Modifier.width(70.dp).popOn(if (revealed) 1 else 0),
                                color = ink,
                                size = 26.sp, weight = FontWeight.Black,
                            )
                        }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                if (shown == 0) T("👆", Modifier.floating(), size = 36.sp)
            }
        }
        Confetti(if (done) 1 else 0)
    }
}

/** A square of tiles, a row per tap, counting up as it fills: 5, 10, 15, 20, 25. */
@Composable
fun SquareView(reel: SquareCard, padding: PaddingValues) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val n = reel.n
    var rows by remember(reel.id) { mutableIntStateOf(0) }
    val done = rows == n
    // The newest row grows in, so each tap visibly adds one row.
    val grow = remember(reel.id) { Animatable(1f) }
    LaunchedEffect(rows) {
        if (rows > 0) {
            grow.snapTo(0f)
            grow.animateTo(1f, tween(260))
        }
    }
    fun next() {
        if (rows >= n) return
        rows++
        if (rows == n) {
            env.sfx(Sfx.CORRECT)
            env.say(listOf(Say((n * n).toString(), env.main), Say(Phrases.squareDone(n)[env.main], env.main, Tone.CHEER)))
        } else {
            env.sfx(Sfx.TICK)
            env.say(Say((rows * n).toString(), env.main))
        }
    }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(padding).reelWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MultiText(reel.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 30.sp, say = { env.say(it) })
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().tap(::next), contentAlignment = Alignment.Center) {
                val side = minOf(maxWidth * 0.86f, maxHeight * 0.95f, 340.dp)
                val empty = Color.Black.copy(alpha = 0.18f)
                // On paper white in every look, like the clock: blue tiles on a purple or
                // dark background were hard to count.
                Canvas(Modifier.size(side).shadow(8.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(Paper).padding(10.dp)) {
                    val cell = size.minDimension / n
                    val gap = cell * 0.12f
                    val radius = CornerRadius(cell * 0.18f)
                    for (r in 0 until n) for (c in 0 until n) {
                        val at = Offset(c * cell + gap / 2, r * cell + gap / 2)
                        val tile = Size(cell - gap, cell - gap)
                        if (r < rows) {
                            val k = if (r == rows - 1) grow.value else 1f
                            val s = Size(tile.width * k, tile.height * k)
                            drawRoundRect(NumberBlue, at + Offset((tile.width - s.width) / 2, (tile.height - s.height) / 2), s, radius)
                        } else {
                            drawRoundRect(empty, at, tile, radius, style = Stroke(1.5.dp.toPx()))
                        }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
                when {
                    rows == 0 -> T("👆", Modifier.floating(), size = 36.sp)
                    done -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Sum("$n × $n = ${n * n}", colors.ink)
                        if (reel.showPower) Sum("$n² = ${n * n}", colors.ink.copy(alpha = 0.8f), 26)
                    }
                    else -> Sum("$rows × $n = ${rows * n}", colors.ink)
                }
            }
        }
        Confetti(if (done) 1 else 0)
    }
}

@Composable
private fun Sum(text: String, color: Color, size: Int = 34) {
    val env = LocalReelEnv.current
    T(text, Modifier.tap { env.say(Txt.same(text.substringAfter("= "))) }, color = color, size = size.sp, weight = FontWeight.Black)
}
