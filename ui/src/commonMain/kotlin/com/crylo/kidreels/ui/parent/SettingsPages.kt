package com.crylo.kidreels.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Avatar
import com.crylo.kidreels.core.FunShare
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.Songs
import com.crylo.kidreels.core.Templates
import com.crylo.kidreels.core.Profile
import com.crylo.kidreels.core.Profiles
import com.crylo.kidreels.core.Settings
import com.crylo.kidreels.core.Topic
import com.crylo.kidreels.ui.AppState
import com.crylo.kidreels.ui.Child
import com.crylo.kidreels.ui.ChildPart
import com.crylo.kidreels.ui.Page
import com.crylo.kidreels.ui.S
import com.crylo.kidreels.ui.common.AvatarBadge
import com.crylo.kidreels.ui.common.DrawnIcon
import com.crylo.kidreels.ui.common.Icon
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.pressable

/**
 * Settings, as pages: the first lists the children and what belongs to the phone; each
 * child's page lists what is set for them, a row each; each row opens one choice. One
 * long page held every child's choices in turn and a topic list that only grows.
 * Changes apply at once.
 */
@Composable
fun SettingsScreen(app: AppState) {
    // A page about a child who is no longer there shows the list instead.
    val about = (app.page as? Page.OfChild)?.id ?: (app.page as? Page.Part)?.id
    val page = if (about != null && app.all.none { it.id == about }) Page.Home else app.page
    // Keyed, so each page starts at its top rather than at the last page's scroll.
    key(page) {
        when (page) {
            Page.Home -> Home(app)
            Page.NewChild -> NewChild(app)
            Page.AppLanguage -> AppLanguage(app)
            is Page.OfChild -> ChildPage(app, app.childOf(page.id))
            is Page.Part -> PartPage(app, app.childOf(page.id), page.part)
        }
    }
}

/** A settings page: the back arrow and title, then [content] scrolling under them, and [bottom] held below. */
@Composable
private fun PageScaffold(
    app: AppState,
    title: String,
    bottom: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ParentPage { c ->
        LaunchedEffect(c.dark) { app.host.darkSystemIcons(!c.dark) }
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).pressable(app::pageBack), contentAlignment = Alignment.Center) {
                    DrawnIcon(Icon.BACK, c.ink, Modifier.size(26.dp))
                }
                T(title, Modifier.padding(start = 4.dp), color = c.ink, size = 22.sp, weight = FontWeight.ExtraBold, maxLines = 1)
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                content()
                Spacer(Modifier.height(24.dp))
            }
            if (bottom != null) Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) { bottom() }
        }
    }
}

/** The children, then what is the phone's rather than a child's: the app's language, voices, and about. */
@Composable
private fun Home(app: AppState) {
    val ui = app.uiLang
    PageScaffold(app, S.settings[ui]) {
        SectionTitle(S.children[ui])
        Body(S.childrenBody[ui])
        Rows {
            app.all.forEachIndexed { i, p ->
                if (i > 0) RowLine()
                val child = app.childOf(p.id)
                val s = child.settings
                val notes = listOfNotNull(
                    S.childLine(s.age, s.dailyMinutes)[ui],
                    S.playingNow[ui].takeIf { p.id == app.child.id },
                )
                val due = app.checkInDue(child)
                NavRow(
                    p.label(ui),
                    if (due) S.checkInDue[ui] else notes.joinToString(" · "),
                    { app.page = Page.OfChild(p.id) },
                    valueColor = if (due || p.id == app.child.id) LocalParent.current.accent else null,
                    leading = { AvatarBadge(p, 44.dp) },
                )
            }
        }
        if (app.all.size < Profiles.MAX_CHILDREN) SecondaryButton("+  " + S.addChild[ui], { app.page = Page.NewChild }, Modifier.fillMaxWidth())

        SectionTitle(S.appLanguage[ui])
        Rows { NavRow(ui.nativeName, S.appLanguageLine[ui], { app.page = Page.AppLanguage }) }

        SectionTitle(S.voices[ui])
        Voices(app)

        SectionTitle(S.about[ui])
        Body(S.aboutBody(app.host.appVersion)[ui])
        // Behind the gate like everything here: a child never reaches the browser.
        // Opening a link needs no internet permission; the browser does the fetching.
        val links = LocalUriHandler.current
        Pill(S.privacyPolicy[ui], false, { runCatching { links.openUri(S.PRIVACY_URL) } })
        app.crashReport?.let { CrashCard(app, it) }
    }
}

/** One child: who they are, their age, time and reels, a row each; then how they are doing. */
@Composable
private fun ChildPage(app: AppState, child: Child) {
    val c = LocalParent.current
    val ui = app.uiLang
    val s = child.settings
    val p = child.profile
    val open = { part: ChildPart -> app.page = Page.Part(child.id, part) }
    var confirmRemove by remember { mutableStateOf(false) }
    PageScaffold(app, p.label(ui)) {
        if (app.checkInDue(child)) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(LocalParent.current.surface).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) { CheckInContent(app, child, onDone = {}) }
        }
        Rows {
            NavRow(S.profile[ui], profileLine(p, s.age, ui), { open(ChildPart.PROFILE) }, leading = { AvatarBadge(p, 44.dp) })
        }

        SectionTitle(S.timeGroup[ui])
        Rows {
            val left = child.budget.remainingMillis(s.dailyMinutes)
            val limit = if (s.dailyMinutes == 0) S.noLimit[ui] else S.duration(s.dailyMinutes)[ui]
            NavRow(S.limitTitle[ui], limit + (left?.let { " · " + S.leftToday(it / 60_000)[ui] } ?: ""), { open(ChildPart.LIMIT) })
            RowLine()
            NavRow(S.bedtimeTitle[ui], if (s.bedtime == 0) S.off[ui] else S.clock(s.bedtime)[ui], { open(ChildPart.BEDTIME) })
        }

        SectionTitle(S.reelsGroup[ui])
        Rows {
            val focus = child.feed.focus
            NavRow(
                S.showOnly[ui], focus?.title?.get(ui) ?: S.allTopics[ui], { open(ChildPart.TOPICS) },
                valueColor = if (focus != null) c.accent else null,
            )
            RowLine()
            NavRow(S.languages[ui], s.langs.joinToString(", ") { it.nativeName }, { open(ChildPart.LANGUAGES) })
            RowLine()
            NavRow(S.look[ui], S.lookName(s.look)[ui], { open(ChildPart.LOOK) })
            RowLine()
            val day = app.funDay(child)
            NavRow(S.funReels[ui], S.funLine(s.funMode, day, FunShare.spanDays(s.funWeeks), FunShare.every(s, day))[ui], { open(ChildPart.FUN) })
            RowLine()
            Toggle(S.slowSpeech[ui], s.slowSpeech) { app.update(s.copy(slowSpeech = it), child) }
        }

        SectionTitle(S.progress[ui])
        WeekNote(app, child)
        val levels = (s.age - 1..s.age + 1).filter { it in Settings.AGES }.map { it to child.progress.stats(it) }.filter { it.second.tried > 0 }
        if (levels.isEmpty()) Body(S.noProgress[ui])
        levels.forEach { (level, st) -> Body(S.levelLine(level, st)[ui]) }

        if (app.all.size > 1) {
            val label = p.label(ui)
            Spacer(Modifier.height(12.dp))
            if (!confirmRemove) {
                SecondaryButton(S.removeChild(label)[ui], { confirmRemove = true }, Modifier.fillMaxWidth())
            } else {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.surface).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Body(S.removeBody(label)[ui])
                    DangerButton(S.remove[ui]) { app.removeChild(child.id) }
                    SecondaryButton(S.cancel[ui], { confirmRemove = false }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

private fun profileLine(p: Profile, age: Int, ui: com.crylo.kidreels.core.Lang): String =
    listOfNotNull(p.name.ifBlank { null }, p.avatar.word.title[ui], S.years(age)[ui]).joinToString(" · ")

/** One choice for one child, with what it does. */
@Composable
private fun PartPage(app: AppState, child: Child, part: ChildPart) {
    val ui = app.uiLang
    val s = child.settings
    val set = { next: Settings -> app.update(next, child) }
    val title = when (part) {
        ChildPart.PROFILE -> S.profile
        ChildPart.LIMIT -> S.limitTitle
        ChildPart.BEDTIME -> S.bedtimeTitle
        ChildPart.TOPICS -> S.showOnly
        ChildPart.LANGUAGES -> S.languages
        ChildPart.LOOK -> S.look
        ChildPart.FUN -> S.funReels
    }
    // A new topic is usually wanted now: one tap to it, rather than Back up three pages.
    // Only for the child playing; for another, the pick waits for them. Held below the
    // list, so it is there however far down the topic was.
    var picked by remember { mutableStateOf(false) }
    val toReels: (@Composable () -> Unit)? = if (part == ChildPart.TOPICS && picked && child.id == app.child.id) {
        { PrimaryButton(S.goToReels[ui], app::closeSettings, Modifier.fillMaxWidth()) }
    } else {
        null
    }
    PageScaffold(app, title[ui], toReels) {
        // Whose this is, when a phone has several children to mix up.
        if (app.all.size > 1 && part != ChildPart.PROFILE) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarBadge(child.profile, 32.dp)
                T(child.profile.label(ui), Modifier.padding(start = 10.dp), color = LocalParent.current.ink, size = 16.sp, weight = FontWeight.Bold)
            }
        }
        when (part) {
            ChildPart.PROFILE -> {
                // Saved as typed, like every other choice here.
                ProfileFields(child.profile, ui) { app.saveProfile(it) }
                SectionTitle(S.ageTitle[ui])
                Body(S.ageBody[ui])
                AgePicker(s.age, ui) { set(s.copy(age = it)) }
                SectionTitle(S.checkInTitle[ui])
                Body(S.checkInBody[ui])
                CheckInPicker(s.checkInMonths, ui) { set(s.copy(checkInMonths = it)) }
            }
            ChildPart.LIMIT -> {
                Body(S.limitBody[ui])
                child.budget.remainingMillis(s.dailyMinutes)?.let { Body(S.leftToday(it / 60_000)[ui]) }
                LimitPicker(s.dailyMinutes, ui) { set(s.copy(dailyMinutes = it)) }
                Body(S.limitAdvice[ui])
            }
            ChildPart.BEDTIME -> {
                Body(S.bedtimeBody[ui])
                BedtimePicker(s.bedtime, ui) { set(s.copy(bedtime = it)) }
            }
            ChildPart.TOPICS -> {
                Body(S.showOnlyBody[ui])
                TopicPicker(child.feed.focus, s.age, s.langs, ui) {
                    child.feed.pick(it)
                    picked = true
                }
            }
            ChildPart.LANGUAGES -> {
                Body(S.langsBody[ui])
                LanguagePicker(s.langs, ui) { set(s.copy(langs = it)) }
            }
            ChildPart.LOOK -> LookPicker(s.look, ui) { set(s.copy(look = it)) }
            ChildPart.FUN -> {
                Body(S.funBody[ui])
                val funAges = Templates.all.filter { it.isFun }.let { t -> t.minOf { it.ages.first }..t.maxOf { it.ages.last } }
                if (s.age !in funAges) Body(S.funNoneForAge(s.age, funAges)[ui])
                if (Lang.EN !in s.langs) Body(S.funSongsEnglish[ui])
                FunPicker(s, app.funDay(child), ui, set) { app.restartFun(child) }
            }
        }
    }
}

/** Name, animal, colour and age; the rest starts as the current child's. */
@Composable
private fun NewChild(app: AppState) {
    val ui = app.uiLang
    // A new child starts on an animal and colour no other child has, so no two look alike.
    var draft by remember {
        val taken = app.all
        mutableStateOf(
            Profile(
                0,
                avatar = Avatar.entries.firstOrNull { a -> taken.none { it.avatar == a } } ?: Avatar.TIGER,
                colour = (0 until Profile.COLOURS).firstOrNull { i -> taken.none { it.colour == i } } ?: 0,
            ),
        )
    }
    var age by remember { mutableIntStateOf(app.settings.age) }
    PageScaffold(app, S.addChild[ui]) {
        ProfileFields(draft, ui) { draft = it }
        SectionTitle(S.ageTitle[ui])
        AgePicker(age, ui) { age = it }
        Body(S.newChildBody(app.child.profile.label(ui))[ui])
        PrimaryButton(
            S.save[ui],
            {
                val id = app.addChild(draft, age)
                // Straight to their page: the limit and languages are likely next.
                app.page = Page.OfChild(id)
            },
            Modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }
}

/**
 * The language of these screens, for the whole phone. The page itself switches as soon as
 * one is tapped, which is the clearest sign of what the choice does.
 */
@Composable
private fun AppLanguage(app: AppState) {
    val ui = app.uiLang
    PageScaffold(app, S.appLanguage[ui]) {
        Body(S.appLanguageBody[ui])
        UiLangPicker(ui, app::changeUiLang)
    }
}

/** Every language any child's reels speak: voices are the phone's, not a child's. */
@Composable
private fun Voices(app: AppState) {
    val c = LocalParent.current
    val ui = app.uiLang
    val speaker = app.host.speaker
    if (!speaker.hasEngine) {
        Body(S.noEngine[ui])
        return
    }
    val langs = app.all.flatMap { app.childOf(it.id).settings.langs }.distinct()
    Rows {
        langs.forEachIndexed { i, lang ->
            if (i > 0) RowLine()
            val ready = speaker.ready(lang)
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    T(lang.nativeName, color = c.ink, size = 16.sp, weight = FontWeight.SemiBold)
                    T(if (ready) S.voiceReady[ui] else S.voiceMissing[ui], color = if (ready) c.sub else Color(0xFFD9434A), size = 13.sp)
                }
                if (!ready) Pill(S.installVoice[ui], true, { app.host.openVoiceSettings() })
            }
        }
    }
}

/** The last seven days in a few lines: how long, how well, and which topics stood out. */
@Composable
private fun WeekNote(app: AppState, child: Child) {
    val c = LocalParent.current
    val ui = app.uiLang
    // Read afresh each time the page opens: the feed underneath keeps adding to it.
    val week = remember(child.id) { child.history.week() }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        T(S.thisWeek[ui], color = c.ink, size = 16.sp, weight = FontWeight.SemiBold)
        if (week.minutes == 0 && week.tried == 0) {
            Body(S.nothingThisWeek[ui])
            return@Column
        }
        Body(S.weekTime(week.minutes, week.days)[ui])
        if (week.tried > 0) Body(S.weekPuzzles(week.tried, week.firstTryPercent, week.skipped)[ui])
        val names = { topics: List<Topic> -> topics.joinToString(", ") { it.title[ui] } }
        if (week.strong.isNotEmpty()) Body(S.doingWell[ui] + " " + names(week.strong))
        if (week.hard.isNotEmpty()) Body(S.findsHard[ui] + " " + names(week.hard))
        if (week.skips.isNotEmpty()) Body(S.oftenSkips[ui] + " " + names(week.skips))
        if (week.liked.isNotEmpty()) Body(S.likesMost[ui] + " " + names(week.liked))
        // So a parent sees the fun and is not surprised by it, and is pointed at a song to sing together.
        if (week.reels > 0) {
            val s = child.settings
            val day = app.funDay(child)
            Body(S.weekFun(week.reels, week.funReels)[ui] + " " + S.funNote(s.funMode, day, FunShare.spanDays(s.funWeeks), FunShare.every(s, day))[ui])
        }
        week.favouriteSong?.let { Songs.byKey(it) }?.let { Body(S.favouriteSong(it.title)[ui]) }
    }
}

/** The last crash: what it was, and a way to pass it on. The app itself never sends it. */
@Composable
private fun CrashCard(app: AppState, report: String) {
    val c = LocalParent.current
    val ui = app.uiLang
    var copied by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(16.dp)).background(c.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        T(S.crashTitle[ui], color = c.ink, size = 16.sp, weight = FontWeight.SemiBold)
        Body(S.crashBody[ui])
        // The first lines say which version, which phone, when, and what broke.
        T(report.lines().take(5).joinToString("\n"), color = c.sub, size = 12.sp, lineHeight = 16.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Pill(if (copied) S.copied[ui] else S.copyReport[ui], true, {
                app.host.copyText(report)
                copied = true
            })
            Pill(S.deleteReport[ui], false, { app.clearCrash() })
        }
    }
}

@Composable
private fun DangerButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color(0xFFD9434A))
            .pressable(onClick).padding(horizontal = 24.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) { T(text, color = Color.White, weight = FontWeight.Bold, size = 16.sp) }
}
