package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.travelhub.ui.components.OnboardingDotsIndicator
import com.mobile.travelhub.ui.theme.TravelHubTheme

private val interestOptions = listOf(
    "Must-see Attractions",
    "Great Food",
    "Hidden Gems",
    "Tours & Experiences",
    "Street Food",
    "Historical Landmarks",
    "Museums",
    "Architecture",
    "Nature Trails"
)

@Composable
fun OnboardingInterestsScreen(
    initialSelected: List<String> = emptyList(),
    syncErrorMessage: String? = null,
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onContinue: (List<String>) -> Unit = {}
) {
    val selected = remember { mutableStateListOf<String>().apply { addAll(initialSelected) } }

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
            InterestsHeader(onBack = onBack, onSkip = onSkip)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tell us what you're interested in",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select all that apply.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4B4F59)
            )
            syncErrorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(18.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                interestOptions.forEach { option ->
                    val isSelected = option in selected
                    Text(
                        text = option,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isSelected) Color.White else Color(0xFF3E4A3D),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF006B2C) else Color(0xFFE7EEE2))
                            .clickable {
                                if (isSelected) {
                                    selected.remove(option)
                                } else {
                                    selected.add(option)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OnboardingDotsIndicator(currentStep = 2, totalSteps = 4)
            }
            Spacer(modifier = Modifier.height(14.dp))

            InterestsBottomActions(
                canContinue = selected.isNotEmpty(),
                onPrevious = onPrevious,
                onContinue = { onContinue(selected.toList()) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun InterestsHeader(onBack: () -> Unit, onSkip: () -> Unit) {
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

@Composable
private fun InterestsBottomActions(
    canContinue: Boolean,
    onPrevious: () -> Unit,
    onContinue: () -> Unit
) {
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
private fun OnboardingInterestsScreenPreview() {
    TravelHubTheme {
        OnboardingInterestsScreen()
    }
}
