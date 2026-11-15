package com.crylo.kidreels.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.crylo.kidreels.ui.feed.FeedScreen
import com.crylo.kidreels.ui.feed.WhoIsPlaying
import com.crylo.kidreels.ui.parent.ChildEditor
import com.crylo.kidreels.ui.parent.CheckInDialog
import com.crylo.kidreels.ui.parent.VoiceDialog
import com.crylo.kidreels.ui.parent.Gate
import com.crylo.kidreels.ui.parent.Onboarding
import com.crylo.kidreels.ui.parent.SettingsScreen

/**
 * The whole app. Settings open over the feed rather than replacing it, so the child comes
 * back to the same reel, mid-puzzle, when a parent is done.
 */
@Composable
fun KidReelsApp(app: AppState) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val base = LocalDensity.current
        val scale = screenScale(maxWidth.value, maxHeight.value)
        CompositionLocalProvider(LocalDensity provides Density(base.density * scale, base.fontScale)) {
            Screens(app)
        }
    }
}

@Composable
private fun Screens(app: AppState) {
    Box(Modifier.fillMaxSize()) {
        when (app.screen) {
            Screen.ONBOARDING -> Onboarding(app)
            Screen.PICKER -> WhoIsPlaying(app)
            else -> {
                // Each child's feed is its own: a switch starts the pager afresh.
                key(app.child.id) { FeedScreen(app) }
                if (app.screen == Screen.SETTINGS) {
                    SettingsScreen(app)
                    app.editing?.let { ChildEditor(app, it) }
                }
            }
        }
        if (app.checkInOpen) CheckInDialog(app)
        app.voiceOpen?.let { VoiceDialog(app, it) }
        if (app.gateAction != null) Gate(app)
    }
}

/** The size every screen is laid out for: a typical phone, in dp. */
private const val PHONE_WIDTH = 400f
private const val PHONE_HEIGHT = 880f

/**
 * How big to draw everything on a tablet, so the phone layout fills it the way it fills a
 * phone: upright, a tablet otherwise shows phone-sized pictures in a sea of background;
 * on its side it is a little shorter than a phone, and the swipe hint ran into the reel.
 * The scale is what the phone layout can grow (or must shrink) by to fit both ways; the
 * reels' own width cap keeps the sides as background. Phones stay at 1, where the app was
 * tuned, and their portrait lock means they never see the short way round.
 */
fun screenScale(widthDp: Float, heightDp: Float): Float {
    if (minOf(widthDp, heightDp) < TABLET) return 1f
    return minOf(widthDp / PHONE_WIDTH, heightDp / PHONE_HEIGHT).coerceIn(MIN_SCALE, MAX_SCALE)
}

/** Android's own line between a phone and a tablet: the shorter side, in dp. */
private const val TABLET = 600f
private const val MIN_SCALE = 0.8f
private const val MAX_SCALE = 1.6f
