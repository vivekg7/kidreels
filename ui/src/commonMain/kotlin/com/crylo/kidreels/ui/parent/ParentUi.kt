package com.crylo.kidreels.ui.parent

import com.crylo.kidreels.core.Channel
import com.crylo.kidreels.core.Templates
import com.crylo.kidreels.core.Topic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.Look
import com.crylo.kidreels.core.Settings
import com.crylo.kidreels.ui.S
import com.crylo.kidreels.ui.common.DrawnIcon
import com.crylo.kidreels.ui.common.Icon
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.pressable
import com.crylo.kidreels.ui.skin.LookPreview

/** The grown-up screens' palette: plain and quiet, light or dark with the system. */
data class ParentColors(
    val bg: Color,
    val surface: Color,
    val ink: Color,
    val sub: Color,
    val accent: Color,
    val onAccent: Color,
    val line: Color,
    val dark: Boolean,
)

private val light = ParentColors(Color(0xFFF6F4FB), Color.White, Color(0xFF1F1B2E), Color(0xFF5E5873), Color(0xFF6C4CF5), Color.White, Color(0xFFE3DEEE), false)
private val dark = ParentColors(Color(0xFF14121A), Color(0xFF1F1C27), Color(0xFFF2EFF9), Color(0xFFABA5BD), Color(0xFF9C87FF), Color(0xFF14121A), Color(0xFF34303F), true)

val LocalParent = staticCompositionLocalOf { light }

@Composable
fun parentColors(): ParentColors = if (isSystemInDarkTheme()) dark else light

@Composable
fun Title(text: String) {
    T(text, color = LocalParent.current.ink, size = 26.sp, weight = FontWeight.ExtraBold, lineHeight = 32.sp)
}

@Composable
fun Body(text: String, modifier: Modifier = Modifier) {
    T(text, modifier, color = LocalParent.current.sub, size = 15.sp, lineHeight = 21.sp)
}

@Composable
fun SectionTitle(text: String) {
    T(text, Modifier.padding(top = 8.dp), color = LocalParent.current.ink, size = 18.sp, weight = FontWeight.Bold)
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val c = LocalParent.current
    Box(
        modifier.clip(RoundedCornerShape(28.dp)).background(if (enabled) c.accent else c.line)
            .pressable(onClick, enabled).padding(horizontal = 24.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) { T(text, color = if (enabled) c.onAccent else c.sub, weight = FontWeight.Bold, size = 16.sp) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalParent.current
    Box(
        modifier.clip(RoundedCornerShape(28.dp)).border(1.5.dp, c.line, RoundedCornerShape(28.dp))
            .pressable(onClick).padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) { T(text, color = c.ink, weight = FontWeight.SemiBold, size = 16.sp) }
}

@Composable
fun Pill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalParent.current
    Box(
        modifier.clip(RoundedCornerShape(50)).background(if (selected) c.accent else c.surface)
            .border(1.5.dp, if (selected) c.accent else c.line, RoundedCornerShape(50))
            .pressable(onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) { T(text, color = if (selected) c.onAccent else c.ink, weight = FontWeight.SemiBold, size = 15.sp) }
}

/**
 * A card of rows, like the phone's own settings: each row says what is set now and opens a
 * page to change it, so the list stays short however many choices there come to be.
 */
@Composable
fun Rows(content: @Composable ColumnScope.() -> Unit) {
    val c = LocalParent.current
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface), content = content)
}

/** Between two rows of a [Rows] card. */
@Composable
fun RowLine() {
    Box(Modifier.padding(start = 16.dp).fillMaxWidth().height(1.dp).background(LocalParent.current.line))
}

/** A row that opens a page: what it is, and what is set now. */
@Composable
fun NavRow(label: String, value: String?, onClick: () -> Unit, valueColor: Color? = null, leading: (@Composable () -> Unit)? = null) {
    val c = LocalParent.current
    Row(
        Modifier.fillMaxWidth().pressable(onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            T(label, color = c.ink, size = 16.sp, weight = FontWeight.SemiBold, maxLines = 1)
            if (value != null) T(value, color = valueColor ?: c.sub, size = 13.sp, lineHeight = 17.sp)
        }
        DrawnIcon(Icon.NEXT, c.sub, Modifier.size(20.dp))
    }
}

/** A switch, as a row of a [Rows] card. */
@Composable
fun Toggle(label: String, on: Boolean, onChange: (Boolean) -> Unit) {
    val c = LocalParent.current
    Row(
        Modifier.fillMaxWidth().pressable({ onChange(!on) }).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        T(label, Modifier.weight(1f), color = c.ink, size = 16.sp, weight = FontWeight.SemiBold)
        Box(
            Modifier.width(50.dp).height(30.dp).clip(RoundedCornerShape(50)).background(if (on) c.accent else c.line).padding(3.dp),
            contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
        ) { Box(Modifier.size(24.dp).clip(CircleShape).background(Color.White)) }
    }
}

/** Up to three languages; tapping adds to the end of the order or takes it out. */
@Composable
fun LanguagePicker(langs: List<Lang>, ui: Lang, onChange: (List<Lang>) -> Unit) {
    val c = LocalParent.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Lang.entries.forEach { lang ->
            val at = langs.indexOf(lang)
            val selected = at >= 0
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface)
                    .border(2.dp, if (selected) c.accent else c.line, RoundedCornerShape(16.dp))
                    .pressable({
                        val next = when {
                            selected && langs.size > 1 -> langs - lang
                            selected -> langs
                            langs.size < Settings.MAX_LANGS -> langs + lang
                            else -> langs
                        }
                        onChange(next)
                    })
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(30.dp).clip(CircleShape).background(if (selected) c.accent else c.line),
                    contentAlignment = Alignment.Center,
                ) { if (selected) T((at + 1).toString(), color = c.onAccent, weight = FontWeight.Bold) }
                T(lang.nativeName, Modifier.padding(start = 14.dp).weight(1f), color = c.ink, size = 18.sp, weight = FontWeight.SemiBold)
                if (at == 0) {
                    T(S.langsMain[ui], color = c.accent, size = 13.sp, weight = FontWeight.Bold)
                } else if (selected) {
                    Pill(S.makeMain[ui], false, { onChange(listOf(lang) + (langs - lang)) })
                }
            }
        }
        Body(S.langsMore[ui])
    }
}

/** The app's own language: every one, by its own name, one tap each. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UiLangPicker(ui: Lang, onChange: (Lang) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Lang.entries.forEach { lang -> Pill(lang.nativeName, lang == ui, { onChange(lang) }) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgePicker(age: Int, ui: Lang, onChange: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Settings.AGES.forEach { a -> Pill(S.years(a)[ui], a == age, { onChange(a) }) }
    }
}

@Composable
fun LookPicker(look: Look, ui: Lang, onChange: (Look) -> Unit) {
    val c = LocalParent.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Look.entries.forEach { l ->
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(c.surface)
                    .border(2.dp, if (l == look) c.accent else c.line, RoundedCornerShape(18.dp))
                    .pressable({ onChange(l) }).padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LookPreview(l, Modifier.fillMaxWidth().aspectRatio(0.62f))
                T(S.lookName(l)[ui], Modifier.padding(top = 8.dp), color = c.ink, weight = FontWeight.Bold)
                T(S.lookAbout(l)[ui], color = c.sub, size = 12.sp, align = TextAlign.Center, lineHeight = 15.sp)
            }
        }
    }
}

/**
 * The usual limits, one tap each, "No limit", and "Custom" for any other: up to six hours,
 * in hours and 5 minutes. Custom shows its own steppers, so the usual case stays one tap.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LimitPicker(minutes: Int, ui: Lang, onChange: (Int) -> Unit) {
    var custom by remember { mutableStateOf(minutes !in Settings.DAILY_MINUTES) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Settings.DAILY_MINUTES.forEach { m ->
                Pill(if (m == 0) S.noLimit[ui] else S.duration(m)[ui], !custom && m == minutes, {
                    custom = false
                    onChange(m)
                })
            }
            Pill(S.custom[ui], custom, {
                custom = true
                // From what is set, or an hour when there was no limit to start from.
                if (minutes == 0) onChange(60)
            })
        }
        if (custom) {
            val range = Settings.STEP..Settings.MAX_DAILY_MINUTES
            TimeSteppers(
                S.perDay(minutes)[ui],
                S.hours[ui], (minutes / 60).toString(),
                (minutes % 60).toString(),
                minutes, range, ui, onChange,
            )
            Body(S.limitUpTo[ui])
        }
    }
}

/** Off, the usual evening times, and "Custom" for any time from 4 pm, in 5 minutes. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BedtimePicker(bedtime: Int, ui: Lang, onChange: (Int) -> Unit) {
    var custom by remember { mutableStateOf(bedtime !in Settings.BEDTIMES) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Settings.BEDTIMES.forEach { m ->
                Pill(if (m == 0) S.off[ui] else S.clock(m)[ui], !custom && m == bedtime, {
                    custom = false
                    onChange(m)
                })
            }
            Pill(S.custom[ui], custom, {
                custom = true
                if (bedtime == 0) onChange(20 * 60)
            })
        }
        if (custom) {
            TimeSteppers(
                S.from(S.clock(bedtime))[ui],
                S.hour[ui], ((bedtime / 60 + 11) % 12 + 1).toString(),
                (bedtime % 60).toString().padStart(2, '0'),
                bedtime, Settings.BEDTIME_RANGE, ui, onChange,
            )
            Body(S.bedtimeRange[ui])
        }
    }
}

/**
 * A time set by hand: what it comes to, then hours and minutes, each with − and +. No
 * keyboard and no dial, which are fiddly one-handed with a child on the other arm. A step
 * up past the end stops at the end; a step down that would pass it is not offered, so the
 * minutes are never lost by surprise.
 */
@Composable
private fun TimeSteppers(
    total: String,
    hoursLabel: String,
    hoursValue: String,
    minutesValue: String,
    value: Int,
    range: IntRange,
    ui: Lang,
    onChange: (Int) -> Unit,
) {
    val c = LocalParent.current
    Rows {
        T(total, Modifier.padding(16.dp), color = c.accent, size = 20.sp, weight = FontWeight.ExtraBold)
        RowLine()
        Stepper(hoursLabel, hoursValue, value - 60 >= range.first, value < range.last, { onChange(value - 60) }, { onChange((value + 60).coerceAtMost(range.last)) })
        RowLine()
        Stepper(S.minutesLabel[ui], minutesValue, value - Settings.STEP >= range.first, value < range.last, { onChange(value - Settings.STEP) }, { onChange((value + Settings.STEP).coerceAtMost(range.last)) })
    }
}

@Composable
private fun Stepper(label: String, value: String, canDown: Boolean, canUp: Boolean, onDown: () -> Unit, onUp: () -> Unit) {
    val c = LocalParent.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        T(label, Modifier.weight(1f), color = c.ink, size = 16.sp, weight = FontWeight.SemiBold)
        StepButton("−", canDown, onDown)
        T(value, Modifier.width(56.dp), color = c.ink, size = 18.sp, weight = FontWeight.Bold, align = TextAlign.Center)
        StepButton("+", canUp, onUp)
    }
}

@Composable
private fun StepButton(sign: String, enabled: Boolean, onClick: () -> Unit) {
    val c = LocalParent.current
    Box(
        Modifier.size(44.dp).clip(CircleShape).border(1.5.dp, c.line, CircleShape).pressable(onClick, enabled),
        contentAlignment = Alignment.Center,
    ) { T(sign, color = if (enabled) c.accent else c.line, size = 22.sp, weight = FontWeight.Bold) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckInPicker(months: Int, ui: Lang, onChange: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Settings.CHECK_IN_MONTHS.forEach { m -> Pill(S.everyMonths(m)[ui], m == months, { onChange(m) }) }
    }
}

/**
 * "All topics", then each channel folded up, so the list stays a handful of lines however
 * many topics are added. One topic at a time: "in order" means nothing across two. Only the
 * child's age's topics are listed unless asked; the others come with their ages.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopicPicker(focus: Topic?, age: Int, langs: List<Lang>, ui: Lang, onPick: (Topic?) -> Unit) {
    val c = LocalParent.current
    val forAge = { topic: Topic -> age in Templates.of(topic).ages }
    var otherAges by remember { mutableStateOf(focus != null && !forAge(focus)) }
    var open by remember { mutableStateOf(setOfNotNull(focus?.channel)) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Pill(S.allTopics[ui], focus == null, { onPick(null) })
        Channel.entries.forEach { channel ->
            // A topic about a language the child does not have (English rhymes for Hindi only) is not offered.
            val topics = Topic.entries.filter { it.channel == channel && it != Topic.BREAK && Templates.of(it).fits(langs) && (otherAges || forAge(it)) }
            if (topics.isEmpty()) return@forEach
            val expanded = channel in open
            val picked = focus?.takeIf { it.channel == channel }
            Rows {
                Row(
                    Modifier.fillMaxWidth().pressable({ open = if (expanded) open - channel else open + channel }).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    T(channel.emoji, size = 20.sp)
                    T(channel.handle[ui], Modifier.padding(start = 12.dp).weight(1f), color = c.ink, size = 16.sp, weight = FontWeight.SemiBold)
                    T(picked?.title?.get(ui) ?: topics.size.toString(), Modifier.padding(horizontal = 8.dp), color = if (picked != null) c.accent else c.sub, size = 14.sp, weight = FontWeight.SemiBold, maxLines = 1)
                    DrawnIcon(if (expanded) Icon.DOWN else Icon.NEXT, c.sub, Modifier.size(20.dp))
                }
                if (expanded) {
                    FlowRow(
                        Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        topics.forEach { topic ->
                            val ages = Templates.of(topic).ages
                            val label = topic.title[ui] + if (age in ages) "" else " · " + S.forAges(ages)[ui]
                            Pill(label, topic == focus, { onPick(if (topic == focus) null else topic) })
                        }
                    }
                }
            }
        }
        Rows { Toggle(S.otherAges[ui], otherAges) { otherAges = it } }
    }
}
