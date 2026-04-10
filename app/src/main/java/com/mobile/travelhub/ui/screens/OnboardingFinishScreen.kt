package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.travelhub.ui.components.OnboardingStepProgress
import com.mobile.travelhub.ui.theme.TravelHubTheme

@Composable
fun OnboardingFinishScreen(
    selectedInterests: List<String>,
    selectedTripType: String? = null,
    selectedDestination: String? = null,
    startDate: String = "",
    endDate: String = "",
    travelers: Int = 1,
    budgetLevel: String = "",
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onContinue: () -> Unit = {}
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
            FinishHeader(onBack = onBack, onSkip = onSkip)
            Spacer(modifier = Modifier.height(10.dp))
            OnboardingStepProgress(currentStep = 5, totalSteps = 5)
            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Your journey is taking shape",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Final step helps us finalize your feed with travel styles you picked.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF383D47)
            )

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFFEFF2F6)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    if (!selectedTripType.isNullOrBlank()) {
                        Text(
                            text = "Trip type",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedTripType,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF0A4F69),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFD2E9F8))
                                .border(1.dp, Color(0xFFB4D7EC), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (!selectedDestination.isNullOrBlank()) {
                        Text(
                            text = "Destination",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedDestination,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF0A4F69),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFD2E9F8))
                                .border(1.dp, Color(0xFFB4D7EC), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (startDate.isNotBlank() || endDate.isNotBlank()) {
                        Text(
                            text = "Timeline",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$startDate - $endDate",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF383D47)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Travelers: $travelers · Budget: $budgetLevel",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5E6470)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = "Selected interests",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    if (selectedInterests.isEmpty()) {
                        Text(
                            text = "No interest selected yet. You can still continue.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF616874)
                        )
                    } else {
                        VibeWrap(selectedInterests)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFFE4E8EC)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2A769B), Color(0xFF133146))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Ready for personalized trips",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "We will prioritize destinations, hotels, and activities based on your vibe profile.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFE2ECF4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            FinishActions(
                onPrevious = onPrevious,
                onContinue = onContinue
            )
        }
    }
}

@Composable
private fun FinishHeader(onBack: () -> Unit, onSkip: () -> Unit) {
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
        Spacer(modifier = Modifier.padding(4.dp))
        Text(
            text = "Step 5 of 5",
            style = MaterialTheme.typography.titleLarge,
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
private fun VibeWrap(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0A4F69),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFD2E9F8))
                            .border(1.dp, Color(0xFFB4D7EC), RoundedCornerShape(24.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
                if (rowItems.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FinishActions(onPrevious: () -> Unit, onContinue: () -> Unit) {
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
                onClick = onContinue,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A5C77))
            ) {
                Text(
                    text = "Start Exploring ->",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingFinishScreenPreview() {
    TravelHubTheme {
        OnboardingFinishScreen(
            selectedInterests = listOf("Great Food", "Hidden Gems", "Museums"),
            selectedDestination = "Hanoi",
            startDate = "Oct 12, 2024",
            endDate = "Oct 24, 2024",
            travelers = 2,
            budgetLevel = "Mid-Range"
        )
    }
}

