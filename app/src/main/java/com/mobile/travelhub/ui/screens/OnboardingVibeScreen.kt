package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.travelhub.ui.theme.TravelHubTheme

private data class VibeOption(
    val title: String,
    val subtitle: String,
    val gradient: List<Color>
)

private val vibeOptions = listOf(
    VibeOption("Alpine Serenity", "Misty mountain air", listOf(Color(0xFF5A7B8C), Color(0xFF1C2A35))),
    VibeOption("Heritage Wonders", "Culture and timeless art", listOf(Color(0xFF98846E), Color(0xFF2C2420))),
    VibeOption("Island Minimal", "Calm sea and white stone", listOf(Color(0xFF5784B8), Color(0xFF1E2F45))),
    VibeOption("Crystal Shores", "Turquoise and sunlight", listOf(Color(0xFF33A7C7), Color(0xFF0D3556))),
    VibeOption("Eco Escapes", "Forest hideaways", listOf(Color(0xFF3F7258), Color(0xFF18251E))),
    VibeOption("Neon Pulse", "City nights and energy", listOf(Color(0xFF5D4A8E), Color(0xFF1B1A31)))
)

@Composable
fun OnboardingVibeScreen(
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onContinue: (List<String>) -> Unit = {}
) {
    val selected = remember { mutableStateListOf<String>() }

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
            HeaderRow(onBack = onBack, onSkip = onSkip)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Refine Your Vibe",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Swipe or tap to let us know what catches your eye. Your personalized horizon begins here.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4B4F59)
            )
            Spacer(modifier = Modifier.height(18.dp))

            HeroCard()
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                RoundActionButton(label = "X", background = Color(0xFFE6E7EA), content = Color(0xFF8F95A1))
                Spacer(modifier = Modifier.width(20.dp))
                RoundActionButton(label = "LIKE", background = Color(0xFFBFD2DB), content = Color(0xFF0D4B63))
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Pick 3 favorites",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Select environments that resonate with you.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5E6470)
                    )
                }

                SelectionPill(selectedCount = selected.size)
            }

            Spacer(modifier = Modifier.height(18.dp))

            VibeGrid(
                selected = selected,
                onToggle = { option ->
                    if (option.title in selected) {
                        selected.remove(option.title)
                    } else if (selected.size < 3) {
                        selected.add(option.title)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            BottomActions(
                canContinue = selected.isNotEmpty(),
                onPrevious = onPrevious,
                onContinue = { onContinue(selected.toList()) }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HeaderRow(onBack: () -> Unit, onSkip: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "<",
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onBack)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Step 3 of 4",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Skip",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF164B63),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onSkip)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun HeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.76f)
            .clip(RoundedCornerShape(34.dp))
            .background(brush = Brush.verticalGradient(listOf(Color(0xFF6BA1D0), Color(0xFF111822))))
            .padding(20.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Text(
                text = "MEDITERRANEAN LUXE",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF395434),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFD8ECD0))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Amalfi Coast, Italy",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cliffs, lemon groves, and azure waters.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE3E8EE)
            )
        }
    }
}

@Composable
private fun RoundActionButton(label: String, background: Color, content: Color) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(background)
            .clickable {}
            .border(width = 1.dp, color = Color(0x22FFFFFF), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = content,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SelectionPill(selectedCount: Int) {
    Text(
        text = "$selectedCount / 3\nSELECTED",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFF0F4F6A),
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFCFE8FA))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun VibeGrid(selected: List<String>, onToggle: (VibeOption) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        vibeOptions.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowItems.forEach { option ->
                    VibeSmallCard(
                        option = option,
                        isSelected = option.title in selected,
                        onClick = { onToggle(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun VibeSmallCard(
    option: VibeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(option.gradient))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color(0xFF70B6E4) else Color(0x22000000),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Text(
                text = option.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = option.subtitle,
                color = Color(0xFFE4E9F0),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun BottomActions(canContinue: Boolean, onPrevious: () -> Unit, onContinue: () -> Unit) {
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
                color = Color(0xFF6A7280),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onPrevious)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                enabled = canContinue,
                onClick = onContinue,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A5C77),
                    disabledContainerColor = Color(0xFF8AA3AF)
                )
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

@Preview(showBackground = true)
@Composable
private fun OnboardingVibeScreenPreview() {
    TravelHubTheme {
        OnboardingVibeScreen()
    }
}

