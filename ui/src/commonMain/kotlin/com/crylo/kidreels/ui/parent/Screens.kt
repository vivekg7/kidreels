package com.crylo.kidreels.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.CheckIn
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.Settings
import com.crylo.kidreels.ui.AppState
import com.crylo.kidreels.ui.S
import com.crylo.kidreels.ui.Screen
import com.crylo.kidreels.ui.common.AvatarBadge
import com.crylo.kidreels.ui.common.DrawnIcon
import com.crylo.kidreels.ui.common.HoldButton
import com.crylo.kidreels.ui.common.Icon
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.pressable

@Composable
internal fun ParentPage(content: @Composable (ParentColors) -> Unit) {
    val c = parentColors()
    CompositionLocalProvider(LocalParent provides c) {
        // Settings sit over the feed; the empty tap handler keeps touches from reaching it.
        Box(
            Modifier.fillMaxSize().background(c.bg).pointerInput(Unit) { detectTapGestures { } }
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(Modifier.widthIn(max = 560.dp).fillMaxSize()) { content(c) }
        }
    }
}

/** First run: what this is, then languages, the child, age, look and time limit. A parent is holding the phone. */
@Composable
fun Onboarding(app: AppState) {
    var step by remember { mutableIntStateOf(0) }
    var draft by remember { mutableStateOf(app.settings) }
    var profile by remember { mutableStateOf(app.child.profile) }
    val main = draft.main
    val last = 5
    ParentPage { c ->
        LaunchedEffect(c.dark) { app.host.darkSystemIcons(!c.dark) }
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 28.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (step) {
                    0 -> Welcome()
                    1 -> {
                        Title(S.langsTitle[main])
                        Body(S.langsBody[main])
                        LanguagePicker(draft.langs, main) { draft = draft.copy(langs = it) }
                    }
                    2 -> {
                        Title(S.childTitle[main])
                        Body(S.childBody[main])
                        ProfileFields(profile, main) { profile = it }
                    }
                    3 -> {
                        Title(S.ageTitle[main])
                        Body(S.ageBody[main])
                        AgePicker(draft.age, main) { draft = draft.copy(age = it) }
                    }
                    4 -> {
                        Title(S.lookTitle[main])
                        Body(S.lookBody[main])
                        LookPicker(draft.look, main) { draft = draft.copy(look = it) }
                    }
                    else -> {
                        Title(S.limitTitle[main])
                        Body(S.limitBody[main])
                        LimitPicker(draft.dailyMinutes, main) { draft = draft.copy(dailyMinutes = it) }
                        Body(S.limitAdvice[main])
                    }
                }
            }
            Dots(step, last + 1)
            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (step > 0) SecondaryButton(S.back[main], { step-- }, Modifier.weight(1f))
                PrimaryButton(
                    when (step) {
                        0 -> S.setUp[main]
                        last -> S.start[main]
                        else -> S.next[main]
                    },
                    { if (step < last) step++ else app.finishOnboarding(draft, profile) },
                    Modifier.weight(if (step > 0) 1.6f else 1f),
                )
            }
        }
    }
}

/** Before a language is picked the welcome is shown in every language. */
@Composable
private fun Welcome() {
    val c = LocalParent.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier.padding(top = 20.dp).size(110.dp).clip(RoundedCornerShape(30.dp)).background(Color(0xFF6C4CF5)),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(54.dp, 80.dp).clip(RoundedCornerShape(10.dp)).background(Color.White), contentAlignment = Alignment.Center) {
                T("⭐", size = 30.sp)
            }
        }
        Lang.entries.forEachIndexed { i, lang ->
            T(S.appName[lang], color = c.ink, size = if (i == 0) 32.sp else 22.sp, weight = FontWeight.ExtraBold)
            T(S.tagline[lang], color = c.sub, size = if (i == 0) 16.sp else 14.sp, align = TextAlign.Center, lineHeight = 21.sp)
        }
        Spacer(Modifier.height(4.dp))
        listOf(S.promiseNoAds to "🚫", S.promiseLimit to "⏱️", S.promiseGate to "🔒").forEach { (t, e) ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                T(e, size = 22.sp)
                Column(Modifier.padding(start = 12.dp)) {
                    Lang.entries.forEachIndexed { i, lang ->
                        T(t[lang], color = if (i == 0) c.ink else c.sub, size = if (i == 0) 15.sp else 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun Dots(step: Int, count: Int) {
    val c = LocalParent.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
        repeat(count) { i ->
            Box(Modifier.size(if (i == step) 22.dp else 8.dp, 8.dp).clip(RoundedCornerShape(50)).background(if (i == step) c.accent else c.line))
        }
    }
}

/** Everything from setup, plus check-ins, speech, progress and about. Changes apply at once. */
@Composable
fun SettingsScreen(app: AppState) {
    val s = app.settings
    val main = s.main
    val close = {
        app.screen = Screen.FEED
        app.feed.recheck()
    }
    ParentPage { c ->
        LaunchedEffect(c.dark) { app.host.darkSystemIcons(!c.dark) }
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).pressable(close), contentAlignment = Alignment.Center) {
                    DrawnIcon(Icon.BACK, c.ink, Modifier.size(26.dp))
                }
                T(S.settings[main], Modifier.padding(start = 4.dp), color = c.ink, size = 22.sp, weight = FontWeight.ExtraBold)
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ChildrenSection(app)
                // Everything below is the picked child's own.
                if (app.all.size > 1) {
                    Row(Modifier.fillMaxWidth().padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                        AvatarBadge(app.child.profile, 36.dp)
                        T(S.settingsFor(app.child.profile.label(main))[main], Modifier.padding(start = 10.dp), color = LocalParent.current.ink, size = 20.sp, weight = FontWeight.ExtraBold)
                    }
                }
                if (app.checkInDue) CheckInCard(app)

                SectionTitle(S.limitTitle[main])
                app.budget.remainingMillis(s.dailyMinutes)?.let { Body(S.leftToday(it / 60_000)[main]) }
                LimitPicker(s.dailyMinutes, main) { app.update(s.copy(dailyMinutes = it)) }

                SectionTitle(S.bedtimeTitle[main])
                Body(S.bedtimeBody[main])
                BedtimePicker(s.bedtime, main) { app.update(s.copy(bedtime = it)) }

                SectionTitle(S.showOnly[main])
                Body(S.showOnlyBody[main])
                TopicPicker(app.feed.focus, s.age, main) { app.feed.pick(it) }

                SectionTitle(S.ageTitle[main])
                AgePicker(s.age, main) { app.update(s.copy(age = it)) }

                SectionTitle(S.langsTitle[main])
                LanguagePicker(s.langs, main) { app.update(s.copy(langs = it)) }

                SectionTitle(S.lookTitle[main])
                LookPicker(s.look, main) { app.update(s.copy(look = it)) }

                SectionTitle(S.checkInTitle[main])
                Body(S.checkInBody[main])
                CheckInPicker(s.checkInMonths, main) { app.update(s.copy(checkInMonths = it)) }

                SectionTitle(S.speech[main])
                Toggle(S.slowSpeech[main], s.slowSpeech) { app.update(s.copy(slowSpeech = it)) }
                Voices(app)

                SectionTitle(S.progress[main])
                val levels = (s.age - 1..s.age + 1).filter { it in Settings.AGES }.map { it to app.progress.stats(it) }.filter { it.second.tried > 0 }
                if (levels.isEmpty()) Body(S.noProgress[main])
                levels.forEach { (level, st) -> Body(S.levelLine(level, st.tried, st.firstTryPercent)[main]) }

                SectionTitle(S.about[main])
                Body(S.aboutBody(app.host.appVersion)[main])
                // Behind the gate like everything here: a child never reaches the browser.
                // Opening a link needs no internet permission; the browser does the fetching.
                val links = LocalUriHandler.current
                Pill(S.privacyPolicy[main], false, { runCatching { links.openUri(S.PRIVACY_URL) } })
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Voices(app: AppState) {
    val c = LocalParent.current
    val main = app.settings.main
    val speaker = app.host.speaker
    if (!speaker.hasEngine) {
        Body(S.noEngine[main])
        return
    }
    app.settings.langs.forEach { lang ->
        val ready = speaker.ready(lang)
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                T(lang.nativeName, color = c.ink, size = 16.sp, weight = FontWeight.SemiBold)
                T(if (ready) S.voiceReady[main] else S.voiceMissing[main], color = if (ready) c.sub else Color(0xFFD9434A), size = 13.sp)
            }
            if (!ready) Pill(S.installVoice[main], true, { app.host.openVoiceSettings() })
        }
    }
}

@Composable
private fun CheckInCard(app: AppState) {
    val c = LocalParent.current
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) { CheckInContent(app, onDone = {}) }
}

/** "Ready for the next age?" with how the child has done at this one. */
@Composable
fun CheckInContent(app: AppState, onDone: () -> Unit) {
    val s = app.settings
    val main = s.main
    val today = app.host.clock.today()
    val months = (((today - s.ageSetDay) / 30).toInt()).coerceAtLeast(s.checkInMonths)
    val stats = app.progress.stats(s.age)
    Title(S.checkInQuestion[main])
    Body(S.checkInSince(months, s.age)[main])
    if (stats.tried > 0) Body(S.levelLine(s.age, stats.tried, stats.firstTryPercent)[main])
    if (s.age < Settings.AGES.last) PrimaryButton(S.moveUp(s.age + 1)[main], { app.update(s.copy(age = s.age + 1)); onDone() }, Modifier.fillMaxWidth())
    SecondaryButton(S.keep(s.age)[main], { app.update(s.copy(ageSetDay = today)); onDone() }, Modifier.fillMaxWidth())
    SecondaryButton(S.later[main], { app.update(CheckIn.snooze(s, today)); onDone() }, Modifier.fillMaxWidth())
}

/** A dimmed screen with one card: the parent gate, or the age check-in after it. */
@Composable
private fun Overlay(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val c = parentColors()
    CompositionLocalProvider(LocalParent provides c) {
        Box(
            Modifier.fillMaxSize().background(Color(0xB3000000)).pressable(onDismiss).windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier.padding(24.dp).widthIn(max = 420.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(c.bg)
                    .pressable({}).padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { content() }
        }
    }
}

@Composable
fun Gate(app: AppState) {
    val main = app.settings.main
    Overlay(app::gateCancelled) {
        val c = LocalParent.current
        T("🔒", size = 40.sp)
        Title(S.gateTitle[main])
        Body(S.gateBody[main])
        HoldButton(S.hold[main], app::gatePassed, Modifier.fillMaxWidth(), background = c.accent, content = c.onAccent)
        SecondaryButton(S.cancel[main], app::gateCancelled, Modifier.fillMaxWidth())
    }
}

@Composable
fun CheckInDialog(app: AppState) {
    Overlay({ app.checkInOpen = false }) {
        CheckInContent(app) { app.checkInOpen = false }
    }
}

/** Why a language's reels are silent, and the way to fix it. From the feed's banner. */
@Composable
fun VoiceDialog(app: AppState, lang: Lang) {
    val main = app.settings.main
    Overlay({ app.voiceOpen = null }) {
        T("🔇", size = 40.sp)
        Title(S.voiceTitle(lang)[main])
        Body(S.voiceBody(lang)[main])
        PrimaryButton(
            S.installVoice[main],
            {
                app.voiceOpen = null
                app.host.openVoiceSettings()
            },
            Modifier.fillMaxWidth(),
        )
        SecondaryButton(S.notNow[main], { app.voiceLater(lang) }, Modifier.fillMaxWidth())
    }
}
