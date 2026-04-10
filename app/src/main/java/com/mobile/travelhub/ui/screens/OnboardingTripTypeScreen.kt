package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.travelhub.ui.components.OnboardingStepProgress
import com.mobile.travelhub.ui.theme.TravelHubTheme

private data class TripTypeOption(
    val title: String,
    val subtitle: String
)

private val tripTypeOptions = listOf(
    TripTypeOption("Relax", "Slow pace and chill days"),
    TripTypeOption("Adventure", "Outdoor and active plans"),
    TripTypeOption("Culture", "Museums, history, local stories"),
    TripTypeOption("Food", "Cafe hopping and local dishes"),
    TripTypeOption("Nature", "Mountains, beaches, green spaces"),
    TripTypeOption("City Break", "Short urban escape")
)

@Composable
fun OnboardingTripTypeScreen(
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onContinue: (String) -> Unit = {}
) {
    var selectedType by remember { mutableStateOf<String?>(null) }

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
            TripTypeHeader(onBack = onBack, onSkip = onSkip)
            Spacer(modifier = Modifier.height(10.dp))
            OnboardingStepProgress(currentStep = 1, totalSteps = 5)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "What kind of trip do you want?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pick one to start. You can change this later anytime.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4B4F59)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tripTypeOptions.forEach { option ->
                    TripTypeCard(
                        option = option,
                        isSelected = selectedType == option.title,
                        onClick = { selectedType = option.title }
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            TripTypeBottomActions(
                canContinue = selectedType != null,
                onPrevious = onPrevious,
                onContinue = { onContinue(selectedType ?: return@TripTypeBottomActions) }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TripTypeHeader(onBack: () -> Unit, onSkip: () -> Unit) {
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
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Step 1 of 5",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
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
private fun TripTypeCard(
    option: TripTypeOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) Color(0xFFD4EAF8) else Color(0xFFEFF1F5)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF0A4F69),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = option.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4E5563)
            )
        }
    }
}

@Composable
private fun TripTypeBottomActions(canContinue: Boolean, onPrevious: () -> Unit, onContinue: () -> Unit) {
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
private fun OnboardingTripTypeScreenPreview() {
    TravelHubTheme {
        OnboardingTripTypeScreen()
    }
}


