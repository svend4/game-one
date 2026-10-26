package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.Dictionary
import com.example.game.GameViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val angle: Float,
    val speed: Float,
    val color: Color,
    val radius: Float,
    val isRect: Boolean
)

/**
 * Festive Particle Fireworks + Gentle Haptic Feedback when a Crossword Stencil line vanishes!
 */
@Composable
fun ConfettiCelebrationOverlay(
    triggerTick: Int,
    modifier: Modifier = Modifier
) {
    if (triggerTick <= 0) return
    val haptic = LocalHapticFeedback.current
    val progress = remember(triggerTick) { Animatable(0f) }

    val particles = remember(triggerTick) {
        val palette = listOf(
            Color(0xFFFFD54F), // Warm Gold
            Color(0xFF00E676), // Emerald
            Color(0xFFFF8A65), // Warm Coral
            Color(0xFF4FC3F7), // Sky Blue
            Color(0xFFBA68C8), // Soft Violet
            Color(0xFFFFF176)  // Bright Sun
        )
        List(38) { idx ->
            val angle = (idx * (360f / 38f) + Random.nextFloat() * 12f) * (Math.PI.toFloat() / 180f)
            ConfettiParticle(
                angle = angle,
                speed = Random.nextFloat() * 260f + 90f,
                color = palette[idx % palette.size],
                radius = Random.nextFloat() * 6f + 5f,
                isRect = idx % 2 == 0
            )
        }
    }

    LaunchedEffect(triggerTick) {
        runCatching {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1250, easing = FastOutSlowInEasing)
        )
    }

    val p = progress.value
    if (p in 0.001f..0.999f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height * 0.45f
            val alpha = (1f - p).coerceIn(0f, 1f)

            particles.forEach { pt ->
                val dist = pt.speed * p * 1.4f
                val gravityOffset = 180f * p * p
                val x = centerX + cos(pt.angle) * dist
                val y = centerY + sin(pt.angle) * dist + gravityOffset
                if (pt.isRect) {
                    drawRect(
                        color = pt.color.copy(alpha = alpha),
                        topLeft = Offset(x - pt.radius, y - pt.radius),
                        size = Size(pt.radius * 2.2f, pt.radius * 1.4f)
                    )
                } else {
                    drawCircle(
                        color = pt.color.copy(alpha = alpha),
                        radius = pt.radius,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

/**
 * 📖 Family Encyclopedia Album of Unlocked Words with Educational Facts & Voice Readout
 */
@Composable
fun FamilyWordEncyclopediaDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val isPaper = viewModel.isPaperTheme
    val bgCard = if (isPaper) Color(0xFFFBF7EE) else Color(0xFF2B2930)
    val itemBg = if (isPaper) Color(0xFFFFFDF9) else Color(0xFF1C1B1F)
    val textPrimary = if (isPaper) Color(0xFF2C1E14) else Color.White
    val textSecondary = if (isPaper) Color(0xFF5D4037) else Color(0xFFCAC4D0)
    val accentColor = if (isPaper) Color(0xFFB45309) else Color(0xFFFFD54F)

    val allDefs = remember(viewModel.isRussian, viewModel.unlockedWordsSet) {
        Dictionary.getDefinitionsForLanguage(viewModel.isRussian)
    }
    val unlockedDefs = remember(allDefs, viewModel.unlockedWordsSet) {
        allDefs.filter { viewModel.unlockedWordsSet.contains(it.word.uppercase()) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .border(2.dp, accentColor, RoundedCornerShape(22.dp)),
            colors = CardDefaults.cardColors(containerColor = bgCard),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (viewModel.isRussian) "📖 СЕМЕЙНЫЙ АЛЬБОМ СЛОВ" else "📖 FAMILY WORD ALBUM",
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (viewModel.isRussian) {
                        "Открыто слов с познавательными фактами: ${unlockedDefs.size} из ${allDefs.size}"
                    } else {
                        "Unlocked words with fun facts: ${unlockedDefs.size} of ${allDefs.size}"
                    },
                    color = textSecondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(unlockedDefs) { def ->
                        val fact = def.funFact.ifBlank { Dictionary.getFunFact(def.word, viewModel.isRussian) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = itemBg),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = def.word,
                                            color = accentColor,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(accentColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = def.category.ifBlank { if (viewModel.isRussian) "Семья" else "Family" },
                                                color = accentColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.speakText("${def.word}. ${def.definition}. $fact")
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = accentColor.copy(alpha = 0.18f),
                                            contentColor = accentColor
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = if (viewModel.isRussian) "🔊 Слушать" else "🔊 Listen",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Text(
                                    text = def.definition,
                                    color = textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.10f))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "💡 $fact",
                                        color = textSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = if (isPaper) Color.White else Color(0xFF1C1B1F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = if (viewModel.isRussian) "ЗАКРЫТЬ АЛЬБОМ" else "CLOSE ALBUM",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * ✍️ Family Custom Riddle Creator ("Семейный Конструктор Слов и Загадок для близких")
 */
@Composable
fun CustomFamilyRiddleCreatorDialog(
    viewModel: GameViewModel,
    onNavigateToGame: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isPaper = viewModel.isPaperTheme
    val bgCard = if (isPaper) Color(0xFFFBF7EE) else Color(0xFF2B2930)
    val textPrimary = if (isPaper) Color(0xFF2C1E14) else Color.White
    val textSecondary = if (isPaper) Color(0xFF5D4037) else Color(0xFFCAC4D0)
    val accentColor = if (isPaper) Color(0xFFB45309) else Color(0xFFFFD54F)

    var customWord by remember { mutableStateOf("") }
    var customClue by remember { mutableStateOf("") }
    var customFact by remember { mutableStateOf("") }
    var errorHint by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, accentColor, RoundedCornerShape(22.dp)),
            colors = CardDefaults.cardColors(containerColor = bgCard),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (viewModel.isRussian) "✍️ СЕМЕЙНЫЙ КОНСТРУКТОР ЗАГАДОК" else "✍️ FAMILY RIDDLE CREATOR",
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (viewModel.isRussian) {
                        "Загадайте близким, детям или внукам доброе семейное слово (от 3 до 8 букв) — оно сразу появится на игровом поле!"
                    } else {
                        "Create a warm family word (3 to 8 letters) and riddle for your loved ones — it will appear right on the board!"
                    },
                    color = textSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = customWord,
                    onValueChange = {
                        customWord = it.uppercase().filter { ch -> ch.isLetter() }.take(8)
                        errorHint = null
                    },
                    label = {
                        Text(
                            if (viewModel.isRussian) "Слово (например: БАБУШКА, БАРСИК)"
                            else "Word (e.g., GRANDMA, COOKIE)"
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = textSecondary.copy(alpha = 0.4f),
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_family_word_input")
                )

                OutlinedTextField(
                    value = customClue,
                    onValueChange = {
                        customClue = it
                        errorHint = null
                    },
                    label = {
                        Text(
                            if (viewModel.isRussian) "Добрая подсказка-загадка"
                            else "Warm clue or riddle"
                        )
                    },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = textSecondary.copy(alpha = 0.4f),
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_family_clue_input")
                )

                OutlinedTextField(
                    value = customFact,
                    onValueChange = { customFact = it },
                    label = {
                        Text(
                            if (viewModel.isRussian) "Семейный факт или история (по желанию)"
                            else "Family fun fact or memory (optional)"
                        )
                    },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = textSecondary.copy(alpha = 0.4f),
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                errorHint?.let { err ->
                    Text(
                        text = err,
                        color = Color(0xFFE53935),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, textSecondary.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (viewModel.isRussian) "Отмена" else "Cancel",
                            color = textPrimary
                        )
                    }

                    Button(
                        onClick = {
                            if (customWord.length < 3) {
                                errorHint = if (viewModel.isRussian) "Введите слово от 3 до 8 букв" else "Enter a word of 3–8 letters"
                            } else if (customClue.isBlank()) {
                                errorHint = if (viewModel.isRussian) "Напишите подсказку-загадку" else "Please enter a clue"
                            } else {
                                if (viewModel.createAndPlayCustomFamilyWord(customWord, customClue, customFact)) {
                                    onNavigateToGame?.invoke()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C853),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("custom_family_word_submit")
                    ) {
                        Text(
                            text = if (viewModel.isRussian) "❤️ Загадать на поле!" else "❤️ Place on Board!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * ☀️ Daily Word of the Day & Warm Family Wish Dialog
 */
@Composable
fun DailyWordOfTheDayDialog(
    viewModel: GameViewModel,
    onStartDailyWord: () -> Unit,
    onDismiss: () -> Unit
) {
    val isPaper = viewModel.isPaperTheme
    val bgCard = if (isPaper) Color(0xFFFBF7EE) else Color(0xFF2B2930)
    val textPrimary = if (isPaper) Color(0xFF2C1E14) else Color.White
    val textSecondary = if (isPaper) Color(0xFF5D4037) else Color(0xFFCAC4D0)
    val accentColor = if (isPaper) Color(0xFFD97706) else Color(0xFFFFD54F)

    val dailyDef = remember(viewModel.isRussian) { Dictionary.getDailyWord(viewModel.isRussian) }
    val dailyWish = remember(viewModel.isRussian) { Dictionary.getDailyWish(viewModel.isRussian) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, accentColor, RoundedCornerShape(22.dp)),
            colors = CardDefaults.cardColors(containerColor = bgCard),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (viewModel.isRussian) "☀️ ЕЖЕДНЕВНОЕ СЛОВО ДНЯ" else "☀️ DAILY WORD OF THE DAY",
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.16f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (viewModel.dailyWordCompletedToday) {
                            if (viewModel.isRussian) "✅ Разгадано сегодня: ${dailyDef.word} (+500 очков)"
                            else "✅ Completed Today: ${dailyDef.word} (+500 pts)"
                        } else {
                            if (viewModel.isRussian) "Особая награда: +500 очков • ${dailyDef.word.length} букв(ы)"
                            else "Special Reward: +500 pts • ${dailyDef.word.length} letters"
                        },
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = dailyDef.definition,
                    color = textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                // Warm Family Wish Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00C853).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFF00C853).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (viewModel.isRussian) "💝 Пожелание дня для вашей семьи:" else "💝 Today's Wish for Your Family:",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = dailyWish,
                            color = textPrimary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.speakText("${dailyDef.definition}. $dailyWish")
                        },
                        border = BorderStroke(1.dp, accentColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (viewModel.isRussian) "🔊 Прослушать" else "🔊 Listen",
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onStartDailyWord,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = if (isPaper) Color.White else Color(0xFF1C1B1F)
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("start_daily_word_button")
                    ) {
                        Text(
                            text = if (viewModel.isRussian) "☀️ Разгадать на поле" else "☀️ Solve on Board",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
