package com.crylo.kidreels.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
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
