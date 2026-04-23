package com.mobile.travelhub.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.mobile.travelhub.ui.components.OnboardingTripTypeScreenContent
import com.mobile.travelhub.ui.theme.TravelHubTheme

@Composable
fun OnboardingTripTypeScreen(
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onContinue: (String) -> Unit = {}
) {
    var selectedType by remember { mutableStateOf<String?>(null) }

    OnboardingTripTypeScreenContent(
        selectedType = selectedType,
        onBack = onBack,
        onSkip = onSkip,
        onPrevious = onPrevious,
        onSelectType = { selectedType = it },
        onContinue = { selectedType?.let(onContinue) }
    )
}

@Preview(showBackground = true)
@Composable
private fun OnboardingTripTypeScreenPreview() {
    TravelHubTheme {
        OnboardingTripTypeScreen()
    }
}


