package com.crylo.kidreels

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.crylo.kidreels.core.Clock
import com.crylo.kidreels.core.KeyValueStore
import com.crylo.kidreels.ui.AppState
import com.crylo.kidreels.ui.Host
import com.crylo.kidreels.ui.KidReelsApp
import com.crylo.kidreels.ui.Sfx
import java.time.LocalDate
import java.time.LocalTime

/** The Android shell: storage, clock, speech and sound for the shared app. Nothing else lives here. */
class MainActivity : ComponentActivity(), Host {
    private lateinit var tts: AndroidSpeaker
    private lateinit var sounds: Sounds
    private lateinit var app: AppState

    override val store: KeyValueStore by lazy { PrefsStore(getSharedPreferences("kidreels", Context.MODE_PRIVATE)) }
    override val clock: Clock = object : Clock {
        override fun nowMillis() = System.currentTimeMillis()
        override fun today() = LocalDate.now().toEpochDay()
        override fun minuteOfDay() = LocalTime.now().let { it.hour * 60 + it.minute }
    }
    override val speaker get() = tts
    override val appVersion: String
        get() = runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull().orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        tts = AndroidSpeaker(this)
        sounds = Sounds()
        app = AppState(this)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!app.onBack()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
        setContent { KidReelsApp(app) }
    }

    override fun onResume() {
        super.onResume()
        app.foreground = true
        tts.refresh()
    }

    override fun onPause() {
        app.foreground = false
        tts.stop()
        super.onPause()
    }

    override fun onDestroy() {
        tts.shutdown()
        sounds.release()
        super.onDestroy()
    }

    override fun play(sfx: Sfx) = sounds.play(sfx)

    override fun openVoiceSettings() = tts.openVoiceInstall(this)

    override fun keepScreenOn(on: Boolean) {
        if (on) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun darkSystemIcons(dark: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = dark
            isAppearanceLightNavigationBars = dark
        }
    }
}

private class PrefsStore(private val prefs: android.content.SharedPreferences) : KeyValueStore {
    override fun get(key: String): String? = prefs.getString(key, null)

    override fun put(key: String, value: String?) {
        prefs.edit().apply { if (value == null) remove(key) else putString(key, value) }.apply()
    }

    override fun keys(): Set<String> = prefs.all.keys.toSet()
}
