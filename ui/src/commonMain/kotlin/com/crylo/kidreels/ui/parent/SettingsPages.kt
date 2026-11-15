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

/** The children, then what is the phone's rather than a child's: voices, and about. */
@Composable
private fun Home(app: AppState) {
    val main = app.settings.main
    PageScaffold(app, S.settings[main]) {
        SectionTitle(S.children[main])
        Body(S.childrenBody[main])
        Rows {
            app.all.forEachIndexed { i, p ->
                if (i > 0) RowLine()
                val child = app.childOf(p.id)
                val s = child.settings
                val notes = listOfNotNull(
                    S.childLine(s.age, s.dailyMinutes)[main],
                    S.playingNow[main].takeIf { p.id == app.child.id },
                )
                val due = app.checkInDue(child)
                NavRow(
                    p.label(main),
                    if (due) S.checkInDue[main] else notes.joinToString(" · "),
                    { app.page = Page.OfChild(p.id) },
                    valueColor = if (due || p.id == app.child.id) LocalParent.current.accent else null,
                    leading = { AvatarBadge(p, 44.dp) },
                )
            }
        }
        if (app.all.size < Profiles.MAX_CHILDREN) SecondaryButton("+  " + S.addChild[main], { app.page = Page.NewChild }, Modifier.fillMaxWidth())

        SectionTitle(S.voices[main])
        Voices(app)

        SectionTitle(S.about[main])
        Body(S.aboutBody(app.host.appVersion)[main])
        // Behind the gate like everything here: a child never reaches the browser.
        // Opening a link needs no internet permission; the browser does the fetching.
        val links = LocalUriHandler.current
        Pill(S.privacyPolicy[main], false, { runCatching { links.openUri(S.PRIVACY_URL) } })
        app.crashReport?.let { CrashCard(app, it) }
    }
}

/** One child: who they are, their age, time and reels, a row each; then how they are doing. */
@Composable
private fun ChildPage(app: AppState, child: Child) {
    val c = LocalParent.current
    val main = app.settings.main
    val s = child.settings
    val p = child.profile
    val open = { part: ChildPart -> app.page = Page.Part(child.id, part) }
    var confirmRemove by remember { mutableStateOf(false) }
    PageScaffold(app, p.label(main)) {
        if (app.checkInDue(child)) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(LocalParent.current.surface).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) { CheckInContent(app, child, onDone = {}) }
        }
        Rows {
            NavRow(S.profile[main], profileLine(p, s.age, main), { open(ChildPart.PROFILE) }, leading = { AvatarBadge(p, 44.dp) })
        }

        SectionTitle(S.timeGroup[main])
        Rows {
            val left = child.budget.remainingMillis(s.dailyMinutes)
            val limit = if (s.dailyMinutes == 0) S.noLimit[main] else S.duration(s.dailyMinutes)[main]
            NavRow(S.limitTitle[main], limit + (left?.let { " · " + S.leftToday(it / 60_000)[main] } ?: ""), { open(ChildPart.LIMIT) })
            RowLine()
            NavRow(S.bedtimeTitle[main], if (s.bedtime == 0) S.off[main] else S.clock(s.bedtime)[main], { open(ChildPart.BEDTIME) })
        }

        SectionTitle(S.reelsGroup[main])
        Rows {
            val focus = child.feed.focus
            NavRow(
                S.showOnly[main], focus?.title?.get(main) ?: S.allTopics[main], { open(ChildPart.TOPICS) },
                valueColor = if (focus != null) c.accent else null,
            )
            RowLine()
            NavRow(S.languages[main], s.langs.joinToString(", ") { it.nativeName }, { open(ChildPart.LANGUAGES) })
            RowLine()
            NavRow(S.look[main], S.lookName(s.look)[main], { open(ChildPart.LOOK) })
            RowLine()
            Toggle(S.slowSpeech[main], s.slowSpeech) { app.update(s.copy(slowSpeech = it), child) }
        }

        SectionTitle(S.progress[main])
        WeekNote(app, child)
        val levels = (s.age - 1..s.age + 1).filter { it in Settings.AGES }.map { it to child.progress.stats(it) }.filter { it.second.tried > 0 }
        if (levels.isEmpty()) Body(S.noProgress[main])
        levels.forEach { (level, st) -> Body(S.levelLine(level, st.tried, st.firstTryPercent)[main]) }

        if (app.all.size > 1) {
            val label = p.label(main)
            Spacer(Modifier.height(12.dp))
            if (!confirmRemove) {
                SecondaryButton(S.removeChild(label)[main], { confirmRemove = true }, Modifier.fillMaxWidth())
            } else {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.surface).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Body(S.removeBody(label)[main])
                    DangerButton(S.remove[main]) { app.removeChild(child.id) }
                    SecondaryButton(S.cancel[main], { confirmRemove = false }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

private fun profileLine(p: Profile, age: Int, main: com.crylo.kidreels.core.Lang): String =
    listOfNotNull(p.name.ifBlank { null }, p.avatar.word.title[main], S.years(age)[main]).joinToString(" · ")

/** One choice for one child, with what it does. */
@Composable
private fun PartPage(app: AppState, child: Child, part: ChildPart) {
    val main = app.settings.main
    val s = child.settings
    val set = { next: Settings -> app.update(next, child) }
    val title = when (part) {
        ChildPart.PROFILE -> S.profile
        ChildPart.LIMIT -> S.limitTitle
        ChildPart.BEDTIME -> S.bedtimeTitle
        ChildPart.TOPICS -> S.showOnly
        ChildPart.LANGUAGES -> S.languages
        ChildPart.LOOK -> S.look
    }
    // A new topic is usually wanted now: one tap to it, rather than Back up three pages.
    // Only for the child playing; for another, the pick waits for them. Held below the
    // list, so it is there however far down the topic was.
    var picked by remember { mutableStateOf(false) }
    val toReels: (@Composable () -> Unit)? = if (part == ChildPart.TOPICS && picked && child.id == app.child.id) {
        { PrimaryButton(S.goToReels[main], app::closeSettings, Modifier.fillMaxWidth()) }
    } else {
        null
    }
    PageScaffold(app, title[main], toReels) {
        // Whose this is, when a phone has several children to mix up.
        if (app.all.size > 1 && part != ChildPart.PROFILE) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarBadge(child.profile, 32.dp)
                T(child.profile.label(main), Modifier.padding(start = 10.dp), color = LocalParent.current.ink, size = 16.sp, weight = FontWeight.Bold)
            }
        }
        when (part) {
            ChildPart.PROFILE -> {
                // Saved as typed, like every other choice here.
                ProfileFields(child.profile, main) { app.saveProfile(it) }
                SectionTitle(S.ageTitle[main])
                Body(S.ageBody[main])
                AgePicker(s.age, main) { set(s.copy(age = it)) }
                SectionTitle(S.checkInTitle[main])
                Body(S.checkInBody[main])
                CheckInPicker(s.checkInMonths, main) { set(s.copy(checkInMonths = it)) }
            }
            ChildPart.LIMIT -> {
                Body(S.limitBody[main])
                child.budget.remainingMillis(s.dailyMinutes)?.let { Body(S.leftToday(it / 60_000)[main]) }
                LimitPicker(s.dailyMinutes, main) { set(s.copy(dailyMinutes = it)) }
                Body(S.limitAdvice[main])
            }
            ChildPart.BEDTIME -> {
                Body(S.bedtimeBody[main])
                BedtimePicker(s.bedtime, main) { set(s.copy(bedtime = it)) }
            }
            ChildPart.TOPICS -> {
                Body(S.showOnlyBody[main])
                TopicPicker(child.feed.focus, s.age, main) {
                    child.feed.pick(it)
                    picked = true
                }
            }
            ChildPart.LANGUAGES -> {
                Body(S.langsBody[main])
                LanguagePicker(s.langs, main) { set(s.copy(langs = it)) }
            }
            ChildPart.LOOK -> LookPicker(s.look, main) { set(s.copy(look = it)) }
        }
    }
}

/** Name, animal, colour and age; the rest starts as the current child's. */
@Composable
private fun NewChild(app: AppState) {
    val main = app.settings.main
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
    PageScaffold(app, S.addChild[main]) {
        ProfileFields(draft, main) { draft = it }
        SectionTitle(S.ageTitle[main])
        AgePicker(age, main) { age = it }
        Body(S.newChildBody(app.child.profile.label(main))[main])
        PrimaryButton(
            S.save[main],
            {
                val id = app.addChild(draft, age)
                // Straight to their page: the limit and languages are likely next.
                app.page = Page.OfChild(id)
            },
            Modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }
}

/** Every language any child's reels speak: voices are the phone's, not a child's. */
@Composable
private fun Voices(app: AppState) {
    val c = LocalParent.current
    val main = app.settings.main
    val speaker = app.host.speaker
    if (!speaker.hasEngine) {
        Body(S.noEngine[main])
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
                    T(if (ready) S.voiceReady[main] else S.voiceMissing[main], color = if (ready) c.sub else Color(0xFFD9434A), size = 13.sp)
                }
                if (!ready) Pill(S.installVoice[main], true, { app.host.openVoiceSettings() })
            }
        }
    }
}

/** The last seven days in a few lines: how long, how well, and which topics stood out. */
@Composable
private fun WeekNote(app: AppState, child: Child) {
    val c = LocalParent.current
    val main = app.settings.main
    // Read afresh each time the page opens: the feed underneath keeps adding to it.
    val week = remember(child.id) { child.history.week() }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        T(S.thisWeek[main], color = c.ink, size = 16.sp, weight = FontWeight.SemiBold)
        if (week.minutes == 0 && week.tried == 0) {
            Body(S.nothingThisWeek[main])
            return@Column
        }
        Body(S.weekTime(week.minutes, week.days)[main])
        if (week.tried > 0) Body(S.weekPuzzles(week.tried, week.firstTryPercent)[main])
        val names = { topics: List<Topic> -> topics.joinToString(", ") { it.title[main] } }
        if (week.strong.isNotEmpty()) Body(S.doingWell[main] + " " + names(week.strong))
        if (week.hard.isNotEmpty()) Body(S.findsHard[main] + " " + names(week.hard))
        if (week.liked.isNotEmpty()) Body(S.likesMost[main] + " " + names(week.liked))
    }
}

/** The last crash: what it was, and a way to pass it on. The app itself never sends it. */
@Composable
private fun CrashCard(app: AppState, report: String) {
    val c = LocalParent.current
    val main = app.settings.main
    var copied by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(16.dp)).background(c.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        T(S.crashTitle[main], color = c.ink, size = 16.sp, weight = FontWeight.SemiBold)
        Body(S.crashBody[main])
        // The first lines say which version, which phone, when, and what broke.
        T(report.lines().take(5).joinToString("\n"), color = c.sub, size = 12.sp, lineHeight = 16.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Pill(if (copied) S.copied[main] else S.copyReport[main], true, {
                app.host.copyText(report)
                copied = true
            })
            Pill(S.deleteReport[main], false, { app.clearCrash() })
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
