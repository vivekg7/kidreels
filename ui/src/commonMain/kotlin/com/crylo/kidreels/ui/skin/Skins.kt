package com.crylo.kidreels.ui.skin

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Channel
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.Look
import com.crylo.kidreels.core.Reel
import com.crylo.kidreels.ui.S
import com.crylo.kidreels.ui.common.DrawnIcon
import com.crylo.kidreels.ui.common.HeartBurst
import com.crylo.kidreels.ui.common.Icon
import com.crylo.kidreels.ui.common.ProgressLine
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.pressable
import kotlin.math.sin

/**
 * The colours reel content draws with. Reels never pick colours themselves, so each one
 * reads well on a black Classic screen, a bright Candy gradient and a pale Calm one.
 */
data class ReelColors(
    val ink: Color,
    val card: Color,
    val cardInk: Color,
    val cardLine: Color,
    val stick: Color,
    val good: Color,
    val bad: Color,
    val dark: Boolean,
)

val LocalReelColors = staticCompositionLocalOf { colorsFor(Look.CANDY) }

fun channelColor(channel: Channel): Color = when (channel) {
    Channel.MATHS -> Color(0xFF4F7DF3)
    Channel.LETTERS -> Color(0xFFF2545B)
    Channel.SHAPES -> Color(0xFF2BB673)
    Channel.PUZZLES -> Color(0xFF9B5DE5)
    Channel.WORLD -> Color(0xFFF7A325)
    Channel.THINKING -> Color(0xFF14A3A3)
    Channel.MEMORY -> Color(0xFFE0629A)
    Channel.WORDS -> Color(0xFF5B8C2A)
    Channel.SCIENCE -> Color(0xFF0E8FB5)
}

private fun calmBackground(channel: Channel): Color = when (channel) {
    Channel.MATHS -> Color(0xFFEAF1FF)
    Channel.LETTERS -> Color(0xFFFFEEEE)
    Channel.SHAPES -> Color(0xFFE9F8EF)
    Channel.PUZZLES -> Color(0xFFF3ECFF)
    Channel.WORLD -> Color(0xFFFFF5DD)
    Channel.THINKING -> Color(0xFFE2F6F5)
    Channel.MEMORY -> Color(0xFFFFEAF3)
    Channel.WORDS -> Color(0xFFEEF6E4)
    Channel.SCIENCE -> Color(0xFFE3F4FA)
}

/** Deep enough at both ends that white text stays readable across the whole gradient. */
private val candyGradients = listOf(
    Color(0xFFFF4F86) to Color(0xFFFF8F4F),
    Color(0xFF5B6CFF) to Color(0xFFB34CFD),
    Color(0xFF0F8F84) to Color(0xFF2FA35F),
    Color(0xFFE8740C) to Color(0xFFE5484D),
    Color(0xFF6A00E0) to Color(0xFFC800D6),
    Color(0xFF00719A) to Color(0xFF0098BC),
)

fun colorsFor(look: Look): ReelColors = when (look) {
    Look.CLASSIC -> ReelColors(
        ink = Color.White,
        card = Color(0x26FFFFFF),
        cardInk = Color.White,
        cardLine = Color(0x40FFFFFF),
        stick = Color(0xFFFFC062),
        good = Color(0xFF2ED573),
        bad = Color(0xFFFF6B6B),
        dark = true,
    )
    Look.CANDY -> ReelColors(
        ink = Color.White,
        card = Color.White,
        cardInk = Color(0xFF2D2440),
        cardLine = Color(0x22000000),
        stick = Color(0xFFB86B2B),
        good = Color(0xFF19B35A),
        bad = Color(0xFFE5484D),
        dark = true,
    )
    Look.CALM -> ReelColors(
        ink = Color(0xFF2E2A3A),
        card = Color.White,
        cardInk = Color(0xFF2E2A3A),
        cardLine = Color(0x1F000000),
        stick = Color(0xFFC07A3C),
        good = Color(0xFF1E9E55),
        bad = Color(0xFFD9434A),
        dark = false,
    )
}

/** What the frame around a reel shows: the like state, the time line, the channel. */
class FrameInfo(
    val reel: Reel,
    val index: Int,
    val liked: Boolean,
    val likeBurst: Int,
    val langs: List<Lang>,
    val timeFraction: Float?,
)

/** Room each look keeps clear for its own buttons, on top of the system bars. */
private fun chromePadding(look: Look): PaddingValues = when (look) {
    Look.CLASSIC -> PaddingValues(start = 16.dp, top = 60.dp, end = 76.dp, bottom = 92.dp)
    Look.CANDY -> PaddingValues(start = 16.dp, top = 64.dp, end = 16.dp, bottom = 118.dp)
    Look.CALM -> PaddingValues(start = 20.dp, top = 60.dp, end = 20.dp, bottom = 96.dp)
}

/** The hint's pill is 40 dp, and it bounces up by 7. */
private val HINT_BAND = 48.dp

@Composable
private fun plus(a: PaddingValues, b: PaddingValues): PaddingValues {
    val dir = LocalLayoutDirection.current
    return PaddingValues(
        start = a.calculateStartPadding(dir) + b.calculateStartPadding(dir),
        top = a.calculateTopPadding() + b.calculateTopPadding(),
        end = a.calculateEndPadding(dir) + b.calculateEndPadding(dir),
        bottom = a.calculateBottomPadding() + b.calculateBottomPadding(),
    )
}

/**
 * One page of the feed: background, the look's buttons, and [content] in the space they
 * leave. Content gets that space as padding so it can still draw edge to edge.
 */
@Composable
fun ReelFrame(
    look: Look,
    info: FrameInfo,
    onLike: () -> Unit,
    onAgain: () -> Unit,
    /**
     * The puzzle from the start, kept apart from [onAgain]: a child taps that mid-game to
     * hear the question, and it must not wipe the board. Null on reels with nothing to solve.
     */
    onRestart: (() -> Unit)?,
    /** More room at the top while the feed shows a banner there (time almost up, check-in). */
    extraTop: Dp = 0.dp,
    /**
     * The "swipe up" hint, given a band of its own under the content. Laid over the content,
     * it landed on whatever a reel had at that height: a shape's examples, a quiz's answer.
     */
    hint: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    val frame = plus(plus(insets, chromePadding(look)), PaddingValues(top = extraTop))
    val padding = if (hint != null) plus(frame, PaddingValues(bottom = HINT_BAND)) else frame
    Box(Modifier.fillMaxSize()) {
        Background(look, info)
        content(padding)
        if (hint != null) {
            Box(
                Modifier.fillMaxWidth().padding(frame).height(HINT_BAND).align(Alignment.BottomCenter),
                contentAlignment = Alignment.BottomCenter,
            ) { hint() }
        }
        Box(Modifier.fillMaxSize().padding(insets)) {
            when (look) {
                Look.CLASSIC -> ClassicChrome(info, onLike, onAgain, onRestart)
                Look.CANDY -> CandyChrome(info, onLike, onAgain, onRestart)
                Look.CALM -> CalmChrome(onAgain, onRestart)
            }
        }
    }
}

@Composable
private fun Background(look: Look, info: FrameInfo) {
    val channel = info.reel.topic.channel
    when (look) {
        Look.CLASSIC -> {
            val glow = channelColor(channel)
            Box(
                Modifier.fillMaxSize().background(Color(0xFF0B0B0F)).background(
                    Brush.radialGradient(listOf(glow.copy(alpha = 0.32f), Color.Transparent), radius = 1400f),
                ),
            )
        }
        Look.CANDY -> {
            val (a, b) = candyGradients[info.index % candyGradients.size]
            val t = rememberInfiniteTransition()
            val shift by t.animateFloat(0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse))
            Canvas(Modifier.fillMaxSize()) {
                drawRect(
                    Brush.linearGradient(
                        listOf(a, b),
                        start = Offset(size.width * shift, 0f),
                        end = Offset(size.width * (1 - shift), size.height),
                    ),
                )
            }
            Bubbles()
        }
        Look.CALM -> Box(Modifier.fillMaxSize().background(calmBackground(channel)))
    }
}

/** Soft circles drifting upward behind Candy reels. */
@Composable
private fun Bubbles() {
    val t = rememberInfiniteTransition()
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(14000, easing = LinearEasing)))
    Canvas(Modifier.fillMaxSize()) {
        repeat(9) { i ->
            val phase = (p + i / 9f) % 1f
            val x = size.width * ((i * 37 % 100) / 100f) + sin((phase + i) * 6f) * 18f
            val y = size.height * (1.1f - phase * 1.2f)
            drawCircle(Color.White.copy(alpha = 0.10f), radius = (18 + i * 7 % 30).toFloat() * density / 2 + 10f, center = Offset(x, y))
        }
    }
}

private fun handle(info: FrameInfo): String = "@" + info.reel.topic.channel.handle[info.langs.first()].replace(" ", "").lowercase()

private fun tags(info: FrameInfo): String =
    info.langs.map { "#" + info.reel.topic.title[it].replace(" ", "") }.distinct().joinToString("  ")

@Composable
private fun ClassicChrome(info: FrameInfo, onLike: () -> Unit, onAgain: () -> Unit, onRestart: (() -> Unit)?) {
    val main = info.langs.first()
    Box(Modifier.fillMaxSize()) {
        // The action rail on the right, the way short-video apps put it.
        Column(
            Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            RailButton(S.like[main], onLike) {
                Box(contentAlignment = Alignment.Center) {
                    DrawnIcon(Icon.HEART, if (info.liked) Color(0xFFFF3B5C) else Color.White, Modifier.size(32.dp), filled = info.liked)
                    HeartBurst(info.likeBurst)
                }
            }
            RailButton(S.again[main], onAgain) { DrawnIcon(Icon.SPEAKER, Color.White, Modifier.size(32.dp)) }
            onRestart?.let { RailButton(S.restart[main], it) { DrawnIcon(Icon.RESTART, Color.White, Modifier.size(32.dp)) } }
            SpinningDisc(info.reel.topic.channel)
        }
        Column(
            Modifier.align(Alignment.BottomStart).padding(start = 14.dp, end = 84.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(info.reel.topic.channel, 34.dp)
                T(handle(info), Modifier.padding(start = 10.dp), color = Color.White, weight = FontWeight.Bold, size = 15.sp)
            }
            T(tags(info), color = Color.White.copy(alpha = 0.85f), size = 13.sp, maxLines = 1)
        }
        info.timeFraction?.let { ProgressLine(it, Color.White, Modifier.align(Alignment.BottomCenter)) }
    }
}

@Composable
private fun RailButton(label: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Column(Modifier.pressable(onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        icon()
        T(label, Modifier.padding(top = 2.dp), color = Color.White, size = 12.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun Avatar(channel: Channel, size: Dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(channelColor(channel)).border(1.5.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) { T(channel.emoji, size = (size.value * 0.5f).sp) }
}

@Composable
private fun SpinningDisc(channel: Channel) {
    val t = rememberInfiniteTransition()
    val angle by t.animateFloat(0f, 360f, infiniteRepeatable(tween(5000, easing = LinearEasing)))
    Box(
        Modifier.size(40.dp).graphicsLayer { rotationZ = angle }.clip(CircleShape)
            .background(Brush.sweepGradient(listOf(Color(0xFF333333), Color(0xFF111111), Color(0xFF333333))))
            .border(6.dp, Color(0xFF1A1A1A), CircleShape),
        contentAlignment = Alignment.Center,
    ) { T(channel.emoji, size = 14.sp) }
}

@Composable
private fun CandyChrome(info: FrameInfo, onLike: () -> Unit, onAgain: () -> Unit, onRestart: (() -> Unit)?) {
    val main = info.langs.first()
    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = 12.dp)
                .clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.22f))
                .padding(start = 4.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(30.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                T(info.reel.topic.channel.emoji, size = 15.sp)
            }
            T(info.reel.topic.title[main], Modifier.padding(start = 8.dp), color = Color.White, weight = FontWeight.Bold, size = 15.sp)
        }
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            BubbleButton(if (info.liked) "❤️" else "🤍", S.like[main], onLike, info.likeBurst)
            BubbleButton("🔊", S.again[main], onAgain, 0)
            onRestart?.let { BubbleButton("🔄", S.restart[main], it, 0) }
        }
        info.timeFraction?.let {
            ProgressLine(it, Color.White, Modifier.align(Alignment.BottomCenter).padding(horizontal = 40.dp, vertical = 8.dp))
        }
    }
}

@Composable
private fun BubbleButton(emoji: String, label: String, onClick: () -> Unit, burst: Int) {
    Column(Modifier.pressable(onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(62.dp).shadow(8.dp, CircleShape).clip(CircleShape).background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            T(emoji, size = 28.sp)
            HeartBurst(burst)
        }
        T(label, Modifier.padding(top = 4.dp), color = Color.White, size = 13.sp, weight = FontWeight.Bold)
    }
}

@Composable
private fun CalmChrome(onAgain: () -> Unit, onRestart: (() -> Unit)?) {
    Box(Modifier.fillMaxSize()) {
        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 22.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            CalmButton(Icon.SPEAKER, onAgain)
            onRestart?.let { CalmButton(Icon.RESTART, it) }
        }
    }
}

@Composable
private fun CalmButton(icon: Icon, onClick: () -> Unit) {
    Box(
        Modifier.size(58.dp).shadow(3.dp, CircleShape).clip(CircleShape).background(Color.White).pressable(onClick),
        contentAlignment = Alignment.Center,
    ) { DrawnIcon(icon, Color(0xFF2E2A3A), Modifier.size(28.dp)) }
}

/** A miniature of [look] for the picker in setup and settings. */
@Composable
fun LookPreview(look: Look, modifier: Modifier = Modifier) {
    val colors = colorsFor(look)
    Box(modifier.clip(RoundedCornerShape(14.dp))) {
        when (look) {
            Look.CLASSIC -> Box(Modifier.fillMaxSize().background(Color(0xFF0B0B0F)))
            Look.CANDY -> Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(candyGradients[1].first, candyGradients[1].second))))
            Look.CALM -> Box(Modifier.fillMaxSize().background(calmBackground(Channel.MATHS)))
        }
        Column(
            Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            T("2 + 3", color = colors.ink, size = 18.sp, weight = FontWeight.ExtraBold)
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(2) {
                    Box(
                        Modifier.width(28.dp).size(28.dp, 22.dp).clip(RoundedCornerShape(6.dp)).background(colors.card),
                        contentAlignment = Alignment.Center,
                    ) { T(if (it == 0) "5" else "4", color = colors.cardInk, size = 11.sp, weight = FontWeight.Bold) }
                }
            }
        }
        when (look) {
            Look.CLASSIC -> Column(Modifier.align(Alignment.BottomEnd).padding(6.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                DrawnIcon(Icon.HEART, Color.White, Modifier.size(12.dp))
                DrawnIcon(Icon.SPEAKER, Color.White, Modifier.size(12.dp))
            }
            Look.CANDY -> Row(Modifier.align(Alignment.BottomCenter).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) { Box(Modifier.size(14.dp).clip(CircleShape).background(Color.White)) }
            }
            Look.CALM -> Box(Modifier.align(Alignment.BottomCenter).padding(6.dp).size(14.dp).clip(CircleShape).background(Color.White))
        }
    }
}

/** Caps content width on tablets and in landscape, where a reel would otherwise stretch. */
fun Modifier.reelWidth(max: Dp = 520.dp): Modifier = this.layout { measurable, constraints ->
    val maxPx = max.roundToPx()
    val width = constraints.maxWidth.coerceAtMost(maxPx)
    val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) {
        placeable.place((constraints.maxWidth - placeable.width) / 2, 0)
    }
}.fillMaxWidth()
