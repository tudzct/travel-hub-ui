package com.mobile.travelhub.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class InterestTag(
    val name: String,
    val icon: String,
    val tone: Color
)

enum class InterestLevel {
    Neutral,
    Like,
    Strong,
    Hidden
}

val introTags = listOf(
    InterestTag(name = "Beach", icon = "~", tone = Color(0xFFD8DADF)),
    InterestTag(name = "Mountain", icon = "^", tone = Color(0xFF0A5C77)),
    InterestTag(name = "City", icon = "#", tone = Color(0xFFAFDAB3)),
    InterestTag(name = "Culture", icon = "M", tone = Color(0xFFD8DADF)),
    InterestTag(name = "Food", icon = "F", tone = Color(0xFFD8DADF)),
    InterestTag(name = "Adventure", icon = "A", tone = Color(0xFFF1D8DB)),
    InterestTag(name = "Nature", icon = "T", tone = Color(0xFFAFDAB3)),
    InterestTag(name = "Nightlife", icon = "N", tone = Color(0xFFD8DADF))
)

@Composable
fun OnboardingIntroScreenContent(
    tagLevels: Map<String, InterestLevel>,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onPrevious: () -> Unit,
    onContinue: () -> Unit,
    onTapTag: (InterestTag, InterestLevel) -> Unit,
    onDoubleTapTag: (InterestTag) -> Unit,
    onLongPressTag: (InterestTag, InterestLevel) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF4F5F8)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            IntroHeader(onBack = onBack, onSkip = onSkip)
            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "What sparks your curiosity?",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                lineHeight = MaterialTheme.typography.displaySmall.lineHeight
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tap to like, double-tap for strong interest, or long-press to hide. We'll curate your horizon accordingly.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF343944)
            )

            Spacer(modifier = Modifier.height(22.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                introTags.chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowItems.forEach { tag ->
                            val level = tagLevels[tag.name] ?: InterestLevel.Neutral
                            IntroTagChip(
                                tag = tag,
                                level = level,
                                onTap = { onTapTag(tag, level) },
                                onDoubleTap = { onDoubleTapTag(tag) },
                                onLongPress = { onLongPressTag(tag, level) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OnboardingDotsIndicator(currentStep = 3, totalSteps = 4)
            }
            Spacer(modifier = Modifier.height(14.dp))

            IntroBottomActions(
                onPrevious = onPrevious,
                onContinue = onContinue
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun IntroHeader(onBack: () -> Unit, onSkip: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "<",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onBack)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Skip",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF0A4D66),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onSkip)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroTagChip(
    tag: InterestTag,
    level: InterestLevel,
    onTap: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = when (level) {
        InterestLevel.Neutral -> tag.tone
        InterestLevel.Like -> Color(0xFFAFDAB3)
        InterestLevel.Strong -> Color(0xFF0A5C77)
        InterestLevel.Hidden -> Color(0xFFF1D8DB)
    }
    val textColor = when (level) {
        InterestLevel.Strong -> Color.White
        InterestLevel.Hidden -> Color(0xFFB34A55)
        else -> Color(0xFF101318)
    }
    val trailing = when (level) {
        InterestLevel.Like -> "+"
        InterestLevel.Strong -> "++"
        InterestLevel.Hidden -> "x"
        InterestLevel.Neutral -> ""
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .border(width = 1.dp, color = Color(0x10FFFFFF), shape = RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = onTap,
                onDoubleClick = onDoubleTap,
                onLongClick = onLongPress
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = tag.icon, color = textColor, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = tag.name,
            color = textColor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = if (level == InterestLevel.Strong) FontWeight.Bold else FontWeight.Medium
        )
        if (trailing.isNotEmpty()) {
            Spacer(modifier = Modifier.weight(1f))
            Text(text = trailing, color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun IntroBottomActions(onPrevious: () -> Unit, onContinue: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 2.dp,
        color = Color(0xFFEFF1F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "< Previous",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9AA1AD),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onPrevious)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onContinue,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A5C77))
            ) {
                Text(
                    text = "Continue ->",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }
    }
}
