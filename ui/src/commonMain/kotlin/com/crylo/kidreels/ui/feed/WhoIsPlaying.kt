package com.crylo.kidreels.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Phrases
import com.crylo.kidreels.core.Say
import com.crylo.kidreels.core.Tone
import com.crylo.kidreels.ui.AppState
import com.crylo.kidreels.ui.common.AvatarBadge
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.floating
import com.crylo.kidreels.ui.common.pressable

/**
 * "Who's playing?": each child's animal, big enough to find without reading. Spoken too,
 * for children who cannot read the question. A child whose time is up today shows a moon.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WhoIsPlaying(app: AppState) {
    val main = app.settings.main
    val children = app.all
    LaunchedEffect(Unit) {
        app.host.darkSystemIcons(false)
        app.host.speaker.speak(listOf(Say(Phrases.whoIsPlayingSay[main], main, Tone.TALK)))
    }
    BoxWithConstraints(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF6C4CF5), Color(0xFF9B5DE5))))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // Centred when it fits; six children on a small phone scroll.
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight).padding(horizontal = 16.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(36.dp, Alignment.CenterVertically),
        ) {
            T(Phrases.whoIsPlaying[main], color = Color.White, size = 34.sp, weight = FontWeight.Black, align = TextAlign.Center)
            // Two to a row: big targets for small fingers.
            val tile = if (children.size <= 4) 132.dp else 104.dp
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(28.dp),
                maxItemsInEachRow = 2,
            ) {
                children.forEachIndexed { i, p ->
                    val done = app.timeUp(p.id)
                    val pick = { if (app.needsGate(p.id)) app.requestGate { app.choose(p.id) } else app.choose(p.id) }
                    Column(
                        Modifier.width(tile + 20.dp).clip(RoundedCornerShape(24.dp)).pressable(pick).padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box {
                            AvatarBadge(p, tile, Modifier.floating(6f, 1400 + i * 170).alpha(if (done) 0.6f else 1f), ring = Color.White)
                            if (done) {
                                Box(
                                    Modifier.align(Alignment.TopEnd).offset(4.dp, (-4).dp).size(tile * 0.34f).clip(CircleShape)
                                        .background(Color(0xFF2B2440)),
                                    contentAlignment = Alignment.Center,
                                ) { T("🌙", size = (tile.value * 0.18f).sp) }
                            }
                        }
                        T(p.label(main), color = Color.White, size = 22.sp, weight = FontWeight.ExtraBold, align = TextAlign.Center, maxLines = 1)
                    }
                }
            }
        }
    }
}
