package com.mobile.travelhub.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.mobile.travelhub.ui.components.OnboardingInterestsScreenContent
import com.mobile.travelhub.ui.theme.TravelHubTheme

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

    OnboardingInterestsScreenContent(
        selected = selected,
        syncErrorMessage = syncErrorMessage,
        onBack = onBack,
        onSkip = onSkip,
        onPrevious = onPrevious,
        onToggleInterest = { option ->
            if (option in selected) {
                selected.remove(option)
            } else {
                selected.add(option)
            }
        },
        onContinue = { onContinue(selected.toList()) }
    )
}

@Preview(showBackground = true)
@Composable
private fun OnboardingInterestsScreenPreview() {
    TravelHubTheme {
        OnboardingInterestsScreen()
    }
}
