package com.crylo.kidreels.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
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
import com.crylo.kidreels.ui.S
import com.crylo.kidreels.ui.common.AvatarBadge
import com.crylo.kidreels.ui.common.AvatarCircle
import com.crylo.kidreels.ui.common.AvatarColours
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.pressable

private const val PICTURES_PER_ROW = 6

/** Name, picture and colour: the same in first-run setup, adding a child and on a child's page. */
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
        // Both grids share out the full width, so their edges line up with the name field
        // above on any phone. Flowing at a fixed size left a gap on the right, and the last
        // colour alone on a second line.
        T(S.picture[main], color = c.sub, size = 13.sp, weight = FontWeight.SemiBold)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Avatar.entries.chunked(PICTURES_PER_ROW).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { a ->
                        BoxWithConstraints(Modifier.weight(1f)) {
                            AvatarCircle(
                                a, p.colour, maxWidth,
                                Modifier.clip(CircleShape).pressable({ onChange(p.copy(avatar = a)) }),
                                ring = if (a == p.avatar) c.accent else null,
                            )
                        }
                    }
                    repeat(PICTURES_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        T(S.colour[main], color = c.sub, size = 13.sp, weight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AvatarColours.forEachIndexed { i, colour ->
                Box(
                    Modifier.weight(1f).aspectRatio(1f).clip(CircleShape).background(colour)
                        .border(if (i == p.colour) 3.dp else 0.dp, if (i == p.colour) c.ink else Color.Transparent, CircleShape)
                        .pressable({ onChange(p.copy(colour = i)) }),
                )
            }
        }
    }
}
