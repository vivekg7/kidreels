package com.crylo.kidreels.ui.reels

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crylo.kidreels.core.Choice
import com.crylo.kidreels.core.NumberWords
import com.crylo.kidreels.core.Op
import com.crylo.kidreels.core.Phrases
import com.crylo.kidreels.core.Quiz
import com.crylo.kidreels.core.Stage
import com.crylo.kidreels.core.Tone
import com.crylo.kidreels.core.Txt
import com.crylo.kidreels.core.pictureTapped
import com.crylo.kidreels.core.revealLines
import com.crylo.kidreels.ui.ReelState
import com.crylo.kidreels.ui.Sfx
import com.crylo.kidreels.ui.art.Picture
import com.crylo.kidreels.ui.art.artFor
import com.crylo.kidreels.ui.common.Motion
import com.crylo.kidreels.ui.common.MultiText
import com.crylo.kidreels.ui.common.moveOn
import com.crylo.kidreels.ui.common.T
import com.crylo.kidreels.ui.common.floating
import com.crylo.kidreels.ui.common.popOn
import com.crylo.kidreels.ui.common.pressable
import com.crylo.kidreels.ui.common.shakeOn
import com.crylo.kidreels.ui.common.tap
import com.crylo.kidreels.ui.draw.Dots
import com.crylo.kidreels.ui.draw.Ink
import com.crylo.kidreels.ui.draw.Paper
import com.crylo.kidreels.ui.draw.ShapeIcon
import com.crylo.kidreels.ui.draw.Sticks
import com.crylo.kidreels.ui.draw.drawClock
import com.crylo.kidreels.ui.skin.LocalReelColors
import com.crylo.kidreels.ui.skin.reelWidth
import kotlin.random.Random

@Composable
fun QuizView(quiz: Quiz, state: ReelState, padding: PaddingValues, onSolved: () -> Unit) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    var shake by remember(quiz.id) { mutableStateOf(0) }
    var shaking by remember(quiz.id) { mutableStateOf(-1) }
    // "What is missing?" hides its answers while the child looks: see MemoryStage.
    var looking by remember(quiz.id) { mutableStateOf(quiz.stage is Stage.Memory && !state.solved) }

    fun choose(i: Int) {
        if (state.solved || i in state.wrong) return
        if (i == quiz.answer) {
            env.sfx(Sfx.CORRECT)
            env.praise(quiz.reveal, quiz.voice, quiz.revealLines(env.langs))
            onSolved()
        } else {
            state.wrong += i
            state.wrongTries++
            shaking = i
            shake++
            env.sfx(Sfx.WRONG)
            // Why that one is not it ("That is green, not blue"), not just "try again".
            env.say(quiz.whyNot.getOrNull(i) ?: Phrases.pick(Phrases.tryAgain, Random), quiz.voice, Tone.GENTLE)
        }
    }

    Column(
        Modifier.fillMaxSize().padding(padding).reelWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MultiText(
            quiz.prompt, env.langs, colors.ink, Modifier.fillMaxWidth(), size = 26.sp,
            say = { env.say(it) }, first = env.main,
        )
        if (quiz.stage == Stage.None) {
            // Nothing to look at but the choices: centre them and make them big.
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Choices(quiz, state, shake, shaking, big = true, choose = ::choose)
            }
        } else {
            Box(Modifier.weight(1f).fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                StageView(quiz, state, looking) { looking = false }
            }
            if (looking) {
                Box(Modifier.fillMaxWidth().height(204.dp), contentAlignment = Alignment.Center) { T("👀", Modifier.floating(), size = 44.sp) }
            } else {
                Choices(quiz, state, shake, shaking, big = false, choose = ::choose)
            }
        }
        Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
            val reveal = quiz.reveal
            if (state.solved && reveal != null) {
                // A tap says the answer as the cheer did: "S" … "for" … "Sun", not run together.
                val parts = quiz.revealSaid
                MultiText(reveal, env.langs, colors.ink, size = 17.sp, first = quiz.voice ?: env.main, say = { line ->
                    env.say(parts?.takeIf { it.first().lang == line.lang } ?: listOf(line))
                })
            }
        }
    }
}

@Composable
private fun StageView(quiz: Quiz, state: ReelState, looking: Boolean, onLooked: () -> Unit) {
    val colors = LocalReelColors.current
    val env = LocalReelEnv.current
    // The puzzle's own pictures celebrate a right answer: the counted apples jump, the
    // fish spins. Not in the wind-down, where nothing new gets lively.
    val cheer = if (env.calm) 0 else state.celebrate
    when (val stage = quiz.stage) {
        Stage.None -> Unit
        is Stage.Sum -> SumStage(stage, if (state.solved) (quiz.choices[quiz.answer] as Choice.Number).n else null)
        is Stage.Count -> CountStage(stage, quiz.id, cheer)
        is Stage.Picture -> {
            var pop by remember(quiz.id) { mutableStateOf(0) }
            // Its name, then the question again: "This is a fish. Where does the fish live?"
            T(
                stage.emoji,
                Modifier.floating().popOn(pop).moveOn(cheer, Motion.HOP, times = 2).moveOn(cheer, Motion.SPIN, delayMillis = 1040).tap {
                    pop++
                    env.say(quiz.pictureTapped(env.langs))
                },
                size = 120.sp,
            )
        }
        is Stage.Row -> RowStage(stage, if (state.solved) stage.tokens[stage.blank] else null) { env.say(quiz.say ?: quiz.prompt, quiz.voice) }
        is Stage.Clock -> {
            val measurer = rememberTextMeasurer()
            // A paper-white face in every look: a clock is read by its hands, so contrast wins.
            Canvas(Modifier.fillMaxHeight(0.95f).aspectRatio(1f)) {
                drawClock(stage.hour, stage.minute, Color(0xFFFFFDF7), Color(0xFF2D2440), Color(0xFFE5484D), measurer)
            }
        }
        is Stage.Grid -> GridStage(stage, state.solved) { env.say(quiz.say ?: quiz.prompt, quiz.voice) }
        is Stage.Figures -> FiguresStage(stage, (quiz.choices[quiz.answer] as? Choice.Fig)?.figure?.takeIf { state.solved })
        is Stage.Cutout -> CutoutStage(stage, state.solved)
        is Stage.Hidden -> HiddenStage(stage, state.solved)
        is Stage.Fold -> FoldStage(stage)
        is Stage.Pairs -> PairsStage(stage)
        is Stage.Memory -> MemoryStage(stage, quiz.id, looking, state.solved, onLooked)
        is Stage.Tank -> TankStage(stage, state.solved)
        is Stage.Magnet -> MagnetStage(stage, state.solved)
    }
}

@Composable
private fun SumStage(sum: Stage.Sum, result: Int?) {
    val colors = LocalReelColors.current
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    // Sized to fit the row on one line: "347 + 128 = ?" needs smaller digits than "2 + 3 = ?".
    val chars = sum.a.toString().length + sum.b.toString().length + (result?.toString()?.length ?: 1) + 2
    val big = minOf(60f, maxWidth.value / (chars * 0.62f + 1.6f)).sp
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberWithSticks(sum.a, sum.aids && sum.op != Op.MUL, big)
            T(sum.op.sign, color = colors.ink, size = big, weight = FontWeight.Bold)
            NumberWithSticks(sum.b, sum.aids && sum.op != Op.MUL, big)
            T("=", color = colors.ink, size = big, weight = FontWeight.Bold)
            T(result?.toString() ?: "?", Modifier.popOn(if (result != null) 1 else 0), color = if (result != null) colors.good else colors.ink, size = big, weight = FontWeight.ExtraBold)
        }
        if (sum.aids && sum.op == Op.MUL) Dots(sum.a, sum.b, colors.stick, dot = if (sum.a * sum.b > 20) 12.dp else 16.dp)
    }
    }
}

@Composable
private fun NumberWithSticks(n: Int, sticks: Boolean, size: androidx.compose.ui.unit.TextUnit) {
    val colors = LocalReelColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        T(n.toString(), color = colors.ink, size = size, weight = FontWeight.ExtraBold)
        if (sticks) Sticks(n, colors.stick, Modifier.padding(top = 4.dp), height = 38.dp)
    }
}

/** Things to count. Tapping one marks it and says the count so far: counting along. */
@Composable
private fun CountStage(stage: Stage.Count, id: Long, cheer: Int) {
    val env = LocalReelEnv.current
    val colors = LocalReelColors.current
    val counted = remember(id) { mutableStateListOf<Int>() }
    val emojiSize = when {
        stage.n <= 5 -> 54.sp
        stage.n <= 10 -> 44.sp
        else -> 34.sp
    }
    @Composable
    fun item(i: Int, modifier: Modifier = Modifier) {
        val number = counted.indexOf(i) + 1
        Box(
            modifier.tap {
                if (i !in counted) {
                    counted += i
                    env.sfx(Sfx.TICK)
                    env.say(NumberWords.of(counted.size))
                }
            },
            contentAlignment = Alignment.TopEnd,
        ) {
            // On a right answer they jump one after another, then spin.
            T(
                stage.emoji,
                Modifier.popOn(number).moveOn(cheer, Motion.HOP, delayMillis = i * 70).moveOn(cheer, Motion.SPIN, delayMillis = 520 + i * 70)
                    .graphicsLayer { alpha = if (number > 0) 0.55f else 1f },
                size = emojiSize,
            )
            if (number > 0) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(colors.good), contentAlignment = Alignment.Center) {
                    T(number.toString(), color = Color.White, size = 12.sp, weight = FontWeight.Bold)
                }
            }
        }
    }
    if (stage.scattered) {
        // Scattered, but with fixed positions per reel, so nothing jumps on recomposition.
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val r = Random(id)
            val cell = 64.dp
            val cols = ((maxWidth - cell) / cell).toInt().coerceAtLeast(1) + 1
            val rows = ((maxHeight - cell) / cell).toInt().coerceAtLeast(1) + 1
            val slots = (0 until cols * rows).shuffled(r).take(stage.n)
            slots.forEachIndexed { i, slot ->
                val x = (maxWidth - cell) * ((slot % cols) / (cols - 1f).coerceAtLeast(1f)) + (r.nextInt(-8, 9)).dp
                val y = (maxHeight - cell) * ((slot / cols) / (rows - 1f).coerceAtLeast(1f)) + (r.nextInt(-6, 7)).dp
                // offset, not padding: padding would squeeze the emoji near the right edge.
                item(i, Modifier.offset(x.coerceAtLeast(0.dp), y.coerceAtLeast(0.dp)))
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            (0 until stage.n).chunked(5).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { row.forEach { item(it) } }
            }
        }
    }
}

@Composable
private fun RowStage(stage: Stage.Row, filled: String?, askAgain: () -> Unit) {
    val colors = LocalReelColors.current
    val env = LocalReelEnv.current
    val numeric = stage.tokens.all { it.toIntOrNull() != null }
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    // One row always: a pattern broken over two lines is a different pattern.
    val widthFactor = if (numeric) 1.3f else 1f
    val cell = minOf(56.dp, (maxWidth - 6.dp * stage.tokens.size) / (stage.tokens.size * widthFactor))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
        stage.tokens.forEachIndexed { i, token ->
            val blank = i == stage.blank
            var pop by remember(stage, i) { mutableStateOf(0) }
            Box(
                Modifier.popOn(pop).size(cell * widthFactor, cell)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (blank) colors.card.copy(alpha = if (filled != null) 1f else 0.55f) else colors.card)
                    .border(2.dp, if (blank) (if (filled != null) colors.good else colors.ink.copy(alpha = 0.6f)) else colors.cardLine, RoundedCornerShape(14.dp))
                    .tap {
                        pop++
                        if (blank && filled == null) askAgain() else env.token(token)
                    },
                contentAlignment = Alignment.Center,
            ) {
                val text = if (blank) filled ?: "?" else token
                T(
                    text, if (blank) Modifier.popOn(if (filled != null) 1 else 0) else Modifier,
                    color = colors.cardInk, size = (cell.value * if (numeric) 0.42f else 0.55f).sp, weight = FontWeight.ExtraBold,
                )
            }
        }
    }
    }
}

@Composable
private fun GridStage(grid: Stage.Grid, solved: Boolean, askAgain: () -> Unit) {
    val colors = LocalReelColors.current
    val env = LocalReelEnv.current
    val blankRow = grid.blank / grid.size
    val blankCol = grid.blank % grid.size
    val ink = Color(0xFF2D2440)
    val line = Color(0xFF2D2440)
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val side: Dp = minOf(maxWidth, maxHeight, 420.dp)
        val cell = side / grid.size
        Column(
            Modifier.size(side).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFFFDF7)).border(3.dp, line, RoundedCornerShape(10.dp)),
        ) {
            for (r in 0 until grid.size) {
                Row {
                    for (c in 0 until grid.size) {
                        val i = r * grid.size + c
                        val inBox = r / grid.boxRows == blankRow / grid.boxRows && c / grid.boxCols == blankCol / grid.boxCols
                        val hinted = grid.hint && (r == blankRow || c == blankCol || inBox)
                        val bg = when {
                            i == grid.blank -> if (solved) colors.good.copy(alpha = 0.35f) else Color(0xFFFFE08A)
                            hinted -> Color(0xFFEDE7FF)
                            else -> Color.Transparent
                        }
                        Box(
                            Modifier.size(cell).background(bg).drawBoxLines(r, c, grid, line)
                                .tap { if (i == grid.blank && !solved) askAgain() else env.token(grid.symbols[grid.cells[i]]) },
                            contentAlignment = Alignment.Center,
                        ) {
                            val text = if (i == grid.blank && !solved) "?" else grid.symbols[grid.cells[i]]
                            T(
                                text, if (i == grid.blank) Modifier.popOn(if (solved) 1 else 0) else Modifier,
                                color = ink, size = (cell.value * 0.5f).sp, weight = if (i == grid.blank) FontWeight.ExtraBold else FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Thin lines between cells, thick ones between boxes. */
private fun Modifier.drawBoxLines(r: Int, c: Int, grid: Stage.Grid, line: Color): Modifier = this.then(
    Modifier.drawBehind {
        val w = size.width
        val h = size.height
        val thin = 1.dp.toPx()
        val thick = 3.dp.toPx()
        if (c < grid.size - 1) {
            val t = if ((c + 1) % grid.boxCols == 0) thick else thin
            drawLine(line.copy(alpha = if (t == thick) 1f else 0.35f), Offset(w, 0f), Offset(w, h), t)
        }
        if (r < grid.size - 1) {
            val t = if ((r + 1) % grid.boxRows == 0) thick else thin
            drawLine(line.copy(alpha = if (t == thick) 1f else 0.35f), Offset(0f, h), Offset(w, h), t)
        }
    },
)

@Composable
private fun Choices(quiz: Quiz, state: ReelState, shake: Int, shaking: Int, big: Boolean, choose: (Int) -> Unit) {
    val count = quiz.choices.size
    val columns = if (count == 3) 3 else 2
    val tall = quiz.choices.any { it is Choice.Group || (it is Choice.Number && it.sticks) }
    val height = when {
        big -> 150.dp
        tall -> 120.dp
        else -> 96.dp
    }
    // A figure puzzle asks about a square tile (a figure card, a quarter of the pattern, the
    // folded paper), so its answers are square tiles too, not wide cards: an answer shaped
    // unlike the gap it fills is one more thing to puzzle over.
    val square = quiz.choices.all { it.onPaper }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        quiz.choices.indices.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                row.forEach { i ->
                    ChoiceCard(
                        quiz.choices[i],
                        cheer = if (i == quiz.answer && !LocalReelEnv.current.calm) state.celebrate else 0,
                        right = state.solved && i == quiz.answer,
                        faded = i in state.wrong || (state.solved && i != quiz.answer),
                        shakeKey = if (shaking == i) shake else 0,
                        modifier = if (square) Modifier.size(height) else Modifier.weight(1f).height(height),
                        big = big,
                        onClick = { choose(i) },
                    )
                }
                if (row.size < columns && !square) Spacer(Modifier.weight((columns - row.size).toFloat()))
            }
        }
    }
}

@Composable
private fun ChoiceCard(choice: Choice, cheer: Int, right: Boolean, faded: Boolean, shakeKey: Int, modifier: Modifier, big: Boolean, onClick: () -> Unit) {
    val colors = LocalReelColors.current
    val env = LocalReelEnv.current
    val alpha by animateFloatAsState(if (faded) 0.35f else 1f)
    // A figure answer is a paper tile like the figure cards it is compared with, whatever
    // the look; a paper square inside a coloured card showed as a tile within a tile.
    val paper = choice.onPaper
    val shape = RoundedCornerShape(if (paper) 14.dp else 22.dp)
    Box(
        modifier
            .shakeOn(shakeKey)
            .popOn(if (right) 1 else 0)
            .moveOn(cheer, Motion.HOP, delayMillis = 150, times = 2)
            .graphicsLayer { this.alpha = alpha }
            .clip(shape)
            .background(
                when {
                    paper -> Paper
                    right -> colors.good
                    else -> colors.card
                },
            )
            .border(
                if (right) 3.dp else 1.5.dp,
                when {
                    right -> colors.good
                    paper -> Ink.copy(alpha = 0.35f)
                    else -> colors.cardLine
                },
                shape,
            )
            .pressable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        val ink = if (right) Color.White else colors.cardInk
        when (choice) {
            is Choice.Number -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                T(choice.n.toString(), color = ink, size = if (choice.n >= 1000) 28.sp else 36.sp, weight = FontWeight.ExtraBold)
                if (choice.sticks) Sticks(choice.n, if (right) Color.White else colors.stick, Modifier.padding(top = 4.dp), height = 30.dp)
            }
            is Choice.Token -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Bigger in the big cards a quiz with nothing else on screen gets.
                if (artFor(choice.text) != null) Picture(choice.text, if (big) 64.sp else 46.sp)
                else T(choice.text, color = ink, size = if (big) 64.sp else 46.sp, weight = FontWeight.Bold)
                choice.label?.let { T(it[env.main], color = ink, size = if (big) 18.sp else 13.sp, weight = FontWeight.SemiBold) }
            }
            is Choice.Group -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                (0 until choice.n).chunked(5).forEach { row ->
                    Row { row.forEach { _ -> T(choice.emoji, size = 22.sp) } }
                }
            }
            is Choice.Shape -> ShapeIcon(choice.kind, Color(choice.argb), 64.dp)
            is Choice.Colour -> Box(
                Modifier.size(64.dp).clip(CircleShape).background(Color(choice.argb)).border(3.dp, colors.cardLine, CircleShape),
            )
            is Choice.Sized -> T(choice.emoji, size = (72 * choice.scale).sp)
            is Choice.Time -> T("${choice.hour}:${choice.minute.toString().padStart(2, '0')}", color = ink, size = 32.sp, weight = FontWeight.ExtraBold)
            is Choice.Fig, is Choice.Cut, is Choice.Shapes, is Choice.Holes -> ReasoningChoice(choice)
            // Smaller for long words, so "strawberries" fits on one line rather than breaking.
            is Choice.Word -> MultiText(
                choice.text, env.langs, ink,
                size = when (choice.text[env.main].length) { in 0..7 -> 32.sp; in 8..10 -> 26.sp; else -> 21.sp },
            )
        }
        if (right) {
            val tick = Modifier.align(Alignment.TopEnd).padding(8.dp)
            if (paper) {
                Box(tick.size(26.dp).clip(CircleShape).background(colors.good), contentAlignment = Alignment.Center) {
                    T("✓", color = Color.White, size = 16.sp, weight = FontWeight.Black)
                }
            } else {
                T("✓", tick, color = Color.White, size = 20.sp, weight = FontWeight.Black)
            }
        }
    }
}

/** The figure puzzles' answers, drawn black on paper in every look. */
private val Choice.onPaper: Boolean
    get() = this is Choice.Fig || this is Choice.Cut || this is Choice.Shapes || this is Choice.Holes
