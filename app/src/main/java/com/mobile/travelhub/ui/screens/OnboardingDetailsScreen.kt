package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

private val budgetOptions = listOf("Economy", "Mid-Range", "Luxury")

@Composable
fun OnboardingDetailsScreen(
    initialStartDate: String = "Oct 12, 2024",
    initialEndDate: String = "Oct 24, 2024",
    initialTravelers: Int = 2,
    initialBudgetLevel: String = "Mid-Range",
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onContinue: (String, String, Int, String) -> Unit = { _, _, _, _ -> }
) {
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }
    var travelers by remember { mutableIntStateOf(initialTravelers.coerceAtLeast(1)) }
    var budgetLevel by remember { mutableStateOf(initialBudgetLevel) }

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
            DetailsHeader(onBack = onBack, onSkip = onSkip)
            Spacer(modifier = Modifier.height(10.dp))
            OnboardingStepProgress(currentStep = 4, totalSteps = 5)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Trip details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add timeline, travelers and budget to personalize suggestions.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4B4F59)
            )
            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                singleLine = true,
                label = { Text("Start date") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                singleLine = true,
                label = { Text("End date") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Travelers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEFF1F5))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CounterButton(symbol = "-", onClick = { if (travelers > 1) travelers -= 1 })
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = travelers.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                CounterButton(symbol = "+", onClick = { travelers += 1 })
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Budget level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                budgetOptions.forEach { option ->
                    val isSelected = option == budgetLevel
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) Color.White else Color(0xFF3E4A3D),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF006B2C) else Color(0xFFE7EEE2))
                            .clickable { budgetLevel = option }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
            DetailsBottomActions(
                canContinue = startDate.isNotBlank() && endDate.isNotBlank(),
                onPrevious = onPrevious,
                onContinue = {
                    onContinue(startDate.trim(), endDate.trim(), travelers, budgetLevel)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DetailsHeader(onBack: () -> Unit, onSkip: () -> Unit) {
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
            text = "Step 4 of 5",
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
private fun CounterButton(symbol: String, onClick: () -> Unit) {
    Text(
        text = symbol,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFD4EAF8))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
private fun DetailsBottomActions(
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
private fun OnboardingDetailsScreenPreview() {
    TravelHubTheme {
        OnboardingDetailsScreen()
    }
}
