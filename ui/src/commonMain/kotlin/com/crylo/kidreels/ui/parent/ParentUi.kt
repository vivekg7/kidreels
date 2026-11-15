package com.crylo.kidreels.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

@Composable
fun Toggle(label: String, on: Boolean, onChange: (Boolean) -> Unit) {
    val c = LocalParent.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface).pressable({ onChange(!on) }).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        T(label, Modifier.weight(1f), color = c.ink, size = 16.sp)
        Box(
            Modifier.width(50.dp).height(30.dp).clip(RoundedCornerShape(50)).background(if (on) c.accent else c.line).padding(3.dp),
            contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
        ) { Box(Modifier.size(24.dp).clip(CircleShape).background(Color.White)) }
    }
}

/** Up to three languages; tapping adds to the end of the order or takes it out. */
@Composable
fun LanguagePicker(langs: List<Lang>, main: Lang, onChange: (List<Lang>) -> Unit) {
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
                    T(S.langsMain[main], color = c.accent, size = 13.sp, weight = FontWeight.Bold)
                } else if (selected) {
                    Pill(S.makeMain[main], false, { onChange(listOf(lang) + (langs - lang)) })
                }
            }
        }
        Body(S.langsMore[main])
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgePicker(age: Int, main: Lang, onChange: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Settings.AGES.forEach { a -> Pill(S.years(a)[main], a == age, { onChange(a) }) }
    }
}

@Composable
fun LookPicker(look: Look, main: Lang, onChange: (Look) -> Unit) {
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
                T(S.lookName(l)[main], Modifier.padding(top = 8.dp), color = c.ink, weight = FontWeight.Bold)
                T(S.lookAbout(l)[main], color = c.sub, size = 12.sp, align = TextAlign.Center, lineHeight = 15.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LimitPicker(minutes: Int, main: Lang, onChange: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Settings.DAILY_MINUTES.forEach { m -> Pill(if (m == 0) S.noLimit[main] else S.minutes(m)[main], m == minutes, { onChange(m) }) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckInPicker(months: Int, main: Lang, onChange: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Settings.CHECK_IN_MONTHS.forEach { m -> Pill(S.everyMonths(m)[main], m == months, { onChange(m) }) }
    }
}
