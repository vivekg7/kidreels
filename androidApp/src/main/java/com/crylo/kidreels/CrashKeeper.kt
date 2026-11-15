package com.crylo.kidreels

import android.app.Application
import android.content.Context
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Keeps the last crash on the phone. The app has no internet permission, so a crash on a
 * family's phone would otherwise never reach anyone; settings (behind the gate) offers the
 * report for a parent to copy into an email or a GitHub issue. Nothing is sent by the app.
 */
class KidReelsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { keep(this, error) }
            previous?.uncaughtException(thread, error)
        }
    }

    private fun keep(context: Context, error: Throwable) {
        val version = runCatching { packageManager.getPackageInfo(packageName, 0) }.getOrNull()
        val report = buildString {
            appendLine("Kid Reels ${version?.versionName} (${version?.longVersionCode})")
            appendLine("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}), ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).format(Date()))
            appendLine()
            append(error.stackTraceToString())
        }.take(MAX_CHARS)
        // commit(), not apply(): the process is about to die.
        context.getSharedPreferences("kidreels", Context.MODE_PRIVATE).edit()
            .putString(CRASH_KEY, report)
            .commit()
    }

    companion object {
        /** Under "device.", so it is the phone's and not moved into a child's profile. */
        const val CRASH_KEY = "device.crash"

        /** The top of a stack trace says what went wrong; a long tail only fills the store. */
        private const val MAX_CHARS = 12_000
    }
}
