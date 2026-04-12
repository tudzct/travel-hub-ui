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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.travelhub.ui.components.OnboardingDotsIndicator
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
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Hoàn tất rồi!",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Hy vọng bạn sẽ có một trải nghiệm thật tốt cùng Travel Hub.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF383D47)
            )

            Spacer(modifier = Modifier.height(36.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OnboardingDotsIndicator(currentStep = 4, totalSteps = 4)
            }
            Spacer(modifier = Modifier.height(14.dp))

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
        OnboardingFinishScreen(selectedInterests = emptyList())
    }
}
