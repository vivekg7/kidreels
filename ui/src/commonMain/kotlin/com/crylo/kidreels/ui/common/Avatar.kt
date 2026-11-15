package com.crylo.kidreels.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Avatar
import com.crylo.kidreels.core.Profile

/** A child's circle colours, by [Profile.colour]. Bright enough to tell apart at a glance. */
val AvatarColours = listOf(
    Color(0xFFFFB547), Color(0xFF4CC9F0), Color(0xFFFF6B8B), Color(0xFF7BD389),
    Color(0xFF9C87FF), Color(0xFFFF8C42), Color(0xFF2EC4B6), Color(0xFFF7D6E0),
)

fun avatarColour(index: Int): Color = AvatarColours[index.mod(AvatarColours.size)]

/** A child's animal on their colour. */
@Composable
fun AvatarBadge(profile: Profile, size: Dp, modifier: Modifier = Modifier, ring: Color? = null) {
    AvatarCircle(profile.avatar, profile.colour, size, modifier, ring)
}

@Composable
fun AvatarCircle(avatar: Avatar, colour: Int, size: Dp, modifier: Modifier = Modifier, ring: Color? = null) {
    Box(
        modifier.size(size).clip(CircleShape).background(avatarColour(colour))
            .then(if (ring != null) Modifier.border(3.dp, ring, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) { T(avatar.emoji, size = (size.value * 0.55f).sp) }
}
