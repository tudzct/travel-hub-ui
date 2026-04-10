package com.mobile.travelhub.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun OnboardingStepProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val normalizedTotal = totalSteps.coerceAtLeast(1)
    val normalizedStep = currentStep.coerceIn(0, normalizedTotal)
    val progress = normalizedStep.toFloat() / normalizedTotal.toFloat()

    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF006B2C),
        trackColor = Color(0xFFDDE5D9)
    )
}
