package com.mobile.travelhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingDotsIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val normalizedTotal = totalSteps.coerceAtLeast(1)
    val selectedIndex = currentStep.coerceIn(1, normalizedTotal) - 1

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(normalizedTotal) { index ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(if (index == selectedIndex) 9.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == selectedIndex) Color(0xFF0A5C77) else Color(0xFFC7CED8)
                    )
            )
        }
    }
}
