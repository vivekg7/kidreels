package com.crylo.kidreels.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Avatar
import com.crylo.kidreels.core.Lang
import com.crylo.kidreels.core.Profile
import com.crylo.kidreels.core.Profiles
import com.crylo.kidreels.ui.AppState
import com.crylo.kidreels.ui.S
import com.crylo.kidreels.ui.common.AvatarBadge
import com.crylo.kidreels.ui.common.AvatarCircle
import com.crylo.kidreels.ui.common.AvatarColours
import com.crylo.kidreels.ui.common.DrawnIcon
import com.crylo.kidreels.ui.common.Icon
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.pressable

/** Name, animal and colour: the same in first-run setup and when adding a child. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileFields(p: Profile, main: Lang, onChange: (Profile) -> Unit) {
    val c = LocalParent.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AvatarBadge(p, 72.dp)
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                T(S.name[main], color = c.sub, size = 13.sp, weight = FontWeight.SemiBold)
                Box(
                    Modifier.padding(top = 4.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.surface)
                        .border(1.5.dp, c.line, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    if (p.name.isEmpty()) T(S.nameHint[main] + " · " + p.label(main), color = c.sub.copy(alpha = 0.7f), size = 17.sp)
                    BasicTextField(
                        p.name,
                        { onChange(p.copy(name = it.take(Profile.MAX_NAME))) },
                        Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(color = c.ink, fontSize = 17.sp),
                        cursorBrush = SolidColor(c.accent),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    )
                }
            }
        }
        T(S.animal[main], color = c.sub, size = 13.sp, weight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Avatar.entries.forEach { a ->
                AvatarCircle(
                    a, p.colour, 52.dp,
                    Modifier.clip(CircleShape).pressable({ onChange(p.copy(avatar = a)) }),
                    ring = if (a == p.avatar) c.accent else null,
                )
            }
        }
        T(S.colour[main], color = c.sub, size = 13.sp, weight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AvatarColours.forEachIndexed { i, colour ->
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(colour)
                        .border(if (i == p.colour) 3.dp else 0.dp, if (i == p.colour) c.ink else Color.Transparent, CircleShape)
                        .pressable({ onChange(p.copy(colour = i)) }),
                )
            }
        }
    }
}

/** Settings' first section: every child, which one is playing, and adding another. */
@Composable
fun ChildrenSection(app: AppState) {
    val c = LocalParent.current
    val main = app.settings.main
    SectionTitle(S.children[main])
    Body(S.childrenBody[main])
    app.all.forEach { p ->
        val active = p.id == app.child.id
        val s = if (active) app.settings else null
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface)
                .border(2.dp, if (active) c.accent else c.line, RoundedCornerShape(16.dp))
                .pressable({ app.select(p.id) }, enabled = !active).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarBadge(p, 48.dp)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                T(p.label(main), color = c.ink, size = 17.sp, weight = FontWeight.Bold, maxLines = 1)
                val line = s?.let { S.childLine(it.age, it.dailyMinutes)[main] + " · " + S.playingNow[main] } ?: S.tapToSwitch[main]
                T(line, color = if (active) c.accent else c.sub, size = 13.sp)
            }
            Pill(S.edit[main], false, { app.editing = p.id })
        }
    }
    if (app.all.size < Profiles.MAX_CHILDREN) SecondaryButton("+  " + S.addChild[main], { app.editing = NEW_CHILD }, Modifier.fillMaxWidth())
}

const val NEW_CHILD = 0

/** Adding a child ([id] is [NEW_CHILD]) or changing one's name, animal and colour. Over settings. */
@Composable
fun ChildEditor(app: AppState, id: Int) {
    val main = app.settings.main
    val adding = id == NEW_CHILD
    // A new child starts on an animal and colour no other child has, so no two look alike.
    var draft by remember(id) {
        val taken = app.all
        mutableStateOf(
            if (adding) {
                Profile(
                    0,
                    avatar = Avatar.entries.firstOrNull { a -> taken.none { it.avatar == a } } ?: Avatar.TIGER,
                    colour = (0 until Profile.COLOURS).firstOrNull { i -> taken.none { it.colour == i } } ?: 0,
                )
            } else {
                app.profiles.profile(id)
            },
        )
    }
    var age by remember(id) { mutableIntStateOf(app.settings.age) }
    var confirmRemove by remember(id) { mutableStateOf(false) }
    val close = { app.editing = null }
    ParentPage { c ->
        LaunchedEffect(c.dark) { app.host.darkSystemIcons(!c.dark) }
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).pressable(close), contentAlignment = Alignment.Center) {
                    DrawnIcon(Icon.BACK, c.ink, Modifier.size(26.dp))
                }
                val title = if (adding) S.addChild[main] else S.editChild(app.profiles.profile(id).label(main))[main]
                T(title, Modifier.padding(start = 4.dp), color = c.ink, size = 22.sp, weight = FontWeight.ExtraBold, maxLines = 1)
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ProfileFields(draft, main) { draft = it }
                if (adding) {
                    SectionTitle(S.ageTitle[main])
                    AgePicker(age, main) { age = it }
                    Body(S.newChildBody(app.child.profile.label(main))[main])
                }
                PrimaryButton(
                    S.save[main],
                    {
                        if (adding) app.addChild(draft, age) else app.saveProfile(draft)
                        close()
                    },
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                if (!adding && app.all.size > 1) {
                    val label = app.profiles.profile(id).label(main)
                    Spacer(Modifier.height(12.dp))
                    if (!confirmRemove) {
                        SecondaryButton(S.removeChild(label)[main], { confirmRemove = true }, Modifier.fillMaxWidth())
                    } else {
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.surface).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Body(S.removeBody(label)[main])
                            DangerButton(S.remove[main], {
                                app.removeChild(id)
                                close()
                            })
                            SecondaryButton(S.cancel[main], { confirmRemove = false }, Modifier.fillMaxWidth())
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
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
