package com.mobile.travelhub.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mobile.travelhub.ui.components.OnboardingFinishScreenContent
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
    isSyncingPreferences: Boolean = false,
    syncErrorMessage: String? = null,
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    OnboardingFinishScreenContent(
        syncErrorMessage = syncErrorMessage,
        isSyncingPreferences = isSyncingPreferences,
        onBack = onBack,
        onSkip = onSkip,
        onPrevious = onPrevious,
        onContinue = onContinue
    )
}

@Preview(showBackground = true)
@Composable
private fun OnboardingFinishScreenPreview() {
    TravelHubTheme {
        OnboardingFinishScreen(selectedInterests = emptyList())
    }
}
