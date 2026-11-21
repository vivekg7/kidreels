package com.crylo.kidreels.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Phrases
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.Tone
import com.crylo.kidreels.core.TimeBudget
import com.crylo.kidreels.core.TimeUp
import com.crylo.kidreels.core.intro
import com.crylo.kidreels.core.isFun
import com.crylo.kidreels.ui.AppState
import com.crylo.kidreels.ui.S
import com.crylo.kidreels.ui.Screen
import com.crylo.kidreels.ui.common.AvatarBadge
import com.crylo.kidreels.ui.common.Celebration
import com.crylo.kidreels.ui.common.DrawnIcon
import com.crylo.kidreels.ui.common.Icon
import com.crylo.kidreels.ui.common.SadBubble
import com.crylo.kidreels.ui.common.SwipeHint
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.pressable
import com.crylo.kidreels.ui.reels.LocalReelEnv
import com.crylo.kidreels.ui.reels.ReelEnv
import com.crylo.kidreels.ui.reels.ReelView
import com.crylo.kidreels.ui.skin.FrameInfo
import com.crylo.kidreels.ui.skin.LocalReelColors
import com.crylo.kidreels.ui.skin.ReelFrame
import com.crylo.kidreels.ui.skin.colorsFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** The swipe feed: one reel per page, the look's frame around it, and the parent's limits. */
@Composable
fun FeedScreen(app: AppState) {
    val model = app.feed
    val settings = app.settings
    val host = app.host
    val speaker = host.speaker
    val colors = colorsFor(settings.look)
    val scope = rememberCoroutineScope()

    if (model.reels.isEmpty()) model.fill(model.current)
    val pager = rememberPagerState(initialPage = model.current) { model.reels.size }
    val onScreen = app.foreground && app.screen == Screen.FEED && app.gateAction == null && !app.checkInOpen && app.voiceOpen == null
    val onScreenNow by rememberUpdatedState(onScreen)

    LaunchedEffect(colors.dark, app.screen) { if (app.screen == Screen.FEED) host.darkSystemIcons(!colors.dark) }
    DisposableEffect(Unit) {
        host.keepScreenOn(true)
        onDispose { host.keepScreenOn(false) }
    }
    LaunchedEffect(onScreen) { if (!onScreen) speaker.stop() }

    // Moving between reels: score the one left behind, top up the queue, say the new one.
    // Keyed on the reel too, not only the page: a new age or language replaces the reel on
    // screen without a swipe, and that one should be said once settings close.
    LaunchedEffect(pager) {
        var previous = pager.settledPage
        snapshotFlow { pager.settledPage to model.reels.getOrNull(pager.settledPage)?.id }.collectLatest { (page, _) ->
            if (page != previous) model.reels.getOrNull(previous)?.let { model.left(it, forward = page > previous) }
            previous = page
            model.current = page
            model.fill(page)
            model.onScreen(page)
            val reel = model.reels.getOrNull(page) ?: return@collectLatest
            delay(300)
            snapshotFlow { onScreenNow }.first { it }
            val main = app.settings.main
            val hello = model.takeGreeting()?.let { listOf(Say(it[main], main, Tone.CHEER)) }.orEmpty()
            speaker.speak(hello + reel.intro(app.settings.langs))
        }
    }
    LaunchedEffect(pager) {
        snapshotFlow { pager.isScrollInProgress }.collectLatest { if (it) speaker.stop() }
    }
    // Five seconds on a reel counts as doing something with it, as a tap does: an explore
    // card can be just listened to. A reel flicked past sooner does not count, so swiping
    // fast does not bring the next fun reel nearer. Not mid-swipe, as a fun reel may be put
    // in below.
    LaunchedEffect(pager.settledPage, onScreen) {
        if (!onScreen) return@LaunchedEffect
        val page = pager.settledPage
        val reel = model.reels.getOrNull(page) ?: return@LaunchedEffect
        delay(ENGAGED_MILLIS)
        snapshotFlow { pager.isScrollInProgress }.first { !it }
        if (pager.settledPage == page) model.engaged(reel)
    }

    // Only time with the feed on screen counts toward the limit.
    LaunchedEffect(onScreen, model.timeUp) {
        if (!onScreen || model.timeUp) return@LaunchedEffect
        while (true) {
            delay(1000)
            if (model.tick(1000, pager.currentPage)) break
        }
    }
    // When time is up mid-puzzle, the child gets a minute to finish before the goodbye reel.
    LaunchedEffect(model.timeUp) {
        if (!model.timeUp) return@LaunchedEffect
        delay(60_000)
        val last = model.reels.lastIndex
        if (pager.currentPage < last) pager.animateScrollToPage(last)
    }

    val onTimeUpReel = model.reels.getOrNull(pager.currentPage) is TimeUp
    val limitMillis = settings.dailyMinutes * TimeBudget.MINUTE
    val timeFraction = model.remainingMillis?.let { (1f - it.toFloat() / limitMillis).coerceIn(0f, 1f) }
    val windingDown = model.windingDown
    val focus = model.focus
    // One note for a grown-up at a time, under the header: the age check-in first, as it
    // changes what the child sees; then a voice the reels need and the phone lacks. In the
    // app's language, like everything a grown-up reads; the reels keep the child's.
    val missingVoice = app.missingVoice
    val banner: Pair<String, () -> Unit>? = when {
        app.checkInDue -> "👋  " + S.checkInBanner[app.uiLang] to { app.requestGate { app.checkInOpen = true } }
        missingVoice != null -> "🔇  " + S.voiceBanner(missingVoice)[app.uiLang] to { app.requestGate { app.voiceOpen = missingVoice } }
        else -> null
    }
    val extraTop = (if (banner != null) 44.dp else 0.dp) + (if (focus != null) 40.dp else 0.dp)

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        CompositionLocalProvider(LocalReelColors provides colors) {
            VerticalPager(
                state = pager,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                key = { model.reels[it].id },
                // A quarter-screen drag turns the page: small hands rarely flick hard or far.
                flingBehavior = PagerDefaults.flingBehavior(pager, snapPositionalThreshold = 0.25f),
                userScrollEnabled = !onTimeUpReel,
            ) { page ->
                val reel = model.reels[page]
                val state = model.state(reel)
                val env = ReelEnv(
                    settings.langs, active = onScreen && page == pager.settledPage, speak = speaker::speak, play = host::play,
                    grownUps = app.uiLang, calm = windingDown, note = host::playNote,
                )
                CompositionLocalProvider(LocalReelEnv provides env) {
                    Box(Modifier.fillMaxSize().onTap(model, reel) { model.engaged(reel) }) {
                        ReelFrame(
                            settings.look,
                            FrameInfo(reel, page, state.liked, state.hearts, settings.langs, timeFraction),
                            onLike = { model.like(reel) },
                            onAgain = { env.say(reel.intro(settings.langs)) },
                            // The question again too: a memory game's timing waits for it.
                            // Fun reels too: start over plays the song again.
                            onRestart = if (reel.needsSolving || reel.isFun) {
                                {
                                    model.restart(reel)
                                    env.say(reel.intro(settings.langs))
                                }
                            } else {
                                null
                            },
                            extraTop = extraTop,
                            hint = if (page < HINTED_REELS && reel !is TimeUp) {
                                { SwipeHintFor(page, done = !reel.needsSolving || state.counted, main = settings.main) }
                            } else {
                                null
                            },
                        ) { padding ->
                            // A new round remounts the view, so everything it remembers starts over.
                            key(state.round) {
                                ReelView(
                                    reel, state, padding,
                                    onSolved = { model.solved(reel) },
                                    onMoreTime = {
                                        model.addTime(10)
                                        scope.launch {
                                            delay(150)
                                            pager.animateScrollToPage(page + 1)
                                        }
                                    },
                                )
                            }
                        }
                        Celebration(state.celebrate, state.burst, calm = windingDown, note = env::note)
                    }
                }
            }
        }

        // Fixed on top of the pager, like a reels app's header.
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            // Right of centre, beside the gear: Candy's channel pill has the top left.
            Row(
                Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (windingDown) Chip("🌙 " + Phrases.almostDone[settings.main], colors.dark)
                // Whose feed this is, for the grown-up; held past the gate, it switches child.
                if (app.all.size > 1) {
                    AvatarBadge(
                        app.child.profile, 44.dp,
                        Modifier.clip(CircleShape).pressable({ app.requestGate { app.openPicker() } }),
                        // A ring, so a pink circle still shows on Candy's pink.
                        ring = Color.White.copy(alpha = 0.85f),
                    )
                }
                Gear(app, colors.dark)
            }
            if (banner != null) {
                Box(
                    Modifier.align(Alignment.TopCenter).padding(top = 58.dp).clip(RoundedCornerShape(50))
                        .background(Color(0xEE2B2440))
                        .pressable(banner.second)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) { T(banner.first, color = Color.White, size = 13.sp, weight = FontWeight.SemiBold) }
            }
            // For the grown-up: a reminder that the feed is showing one topic only. Its own
            // line, under the header and any check-in banner: a long topic name would run
            // into the channel pill beside the gear.
            if (focus != null) {
                Box(Modifier.align(Alignment.TopCenter).padding(top = if (banner != null) 102.dp else 60.dp)) {
                    Chip(focus.channel.emoji + " " + S.onlyTopic(focus.title)[app.uiLang], colors.dark)
                }
            }
            SadBubble(model.skippedPuzzle, Phrases.skipped[settings.main], Modifier.align(BiasAlignment(0f, -0.45f)))
        }
    }
}

@Composable
private fun Gear(app: AppState, dark: Boolean) {
    Box(
        Modifier.size(44.dp).clip(CircleShape)
            .background(if (dark) Color(0x33000000) else Color(0x14000000))
            .pressable({ app.requestGate { app.openSettings() } }),
        contentAlignment = Alignment.Center,
    ) { DrawnIcon(Icon.GEAR, if (dark) Color.White.copy(alpha = 0.85f) else Color(0xFF2E2A3A).copy(alpha = 0.7f), Modifier.size(22.dp)) }
}

@Composable
private fun Chip(text: String, dark: Boolean) {
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(if (dark) Color(0x55000000) else Color(0x22000000))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) { T(text, color = if (dark) Color.White else Color(0xFF2E2A3A), size = 13.sp, weight = FontWeight.Bold) }
}

/**
 * "Swipe up" under a finished reel, for the first few reels of a session only: by then a
 * child has got it, and a hint on every reel would just be noise.
 */
private const val HINTED_REELS = 4

/** On screen this long and a reel counts as engaged with, touched or not. */
private const val ENGAGED_MILLIS = 5_000L

/**
 * Sees every tap on a reel, whatever inside it takes the tap, without taking it: a touch
 * that lifts where it went down. A swipe moves, so it is not one.
 */
private fun Modifier.onTap(vararg keys: Any?, onTap: () -> Unit): Modifier = pointerInput(*keys) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        var moved = false
        while (true) {
            val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: break
            if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) moved = true
            if (!change.pressed) break
        }
        if (!moved) onTap()
    }
}

@Composable
private fun SwipeHintFor(page: Int, done: Boolean, main: com.crylo.kidreels.core.Lang) {
    var show by remember(page) { mutableStateOf(false) }
    LaunchedEffect(page, done) {
        show = false
        if (!done) return@LaunchedEffect
        delay(if (page == 0) 4000 else 2500)
        show = true
    }
    if (show) SwipeHint(Phrases.swipeUp[main])
}
