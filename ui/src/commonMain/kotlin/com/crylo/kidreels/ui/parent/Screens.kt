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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.CheckIn
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.Settings
import com.crylo.kidreels.ui.AppState
import com.crylo.kidreels.ui.Child
import com.crylo.kidreels.ui.S
import com.crylo.kidreels.ui.common.HoldButton
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

/** "Ready for the next age?" with how the child has done at this one. */
@Composable
fun CheckInContent(app: AppState, child: Child, onDone: () -> Unit) {
    val s = child.settings
    val main = app.settings.main
    val today = app.host.clock.today()
    val months = (((today - s.ageSetDay) / 30).toInt()).coerceAtLeast(s.checkInMonths)
    val stats = child.progress.stats(s.age)
    Title(S.checkInQuestion[main])
    Body(S.checkInSince(months, s.age)[main])
    if (stats.tried > 0) Body(S.levelLine(s.age, stats.tried, stats.firstTryPercent)[main])
    if (s.age < Settings.AGES.last) PrimaryButton(S.moveUp(s.age + 1)[main], { app.update(s.copy(age = s.age + 1), child); onDone() }, Modifier.fillMaxWidth())
    SecondaryButton(S.keep(s.age)[main], { app.update(s.copy(ageSetDay = today), child); onDone() }, Modifier.fillMaxWidth())
    SecondaryButton(S.later[main], { app.update(CheckIn.snooze(s, today), child); onDone() }, Modifier.fillMaxWidth())
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
        CheckInContent(app, app.child) { app.checkInOpen = false }
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
