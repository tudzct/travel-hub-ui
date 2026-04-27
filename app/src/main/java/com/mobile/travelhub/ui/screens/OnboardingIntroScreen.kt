package com.mobile.travelhub.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.mobile.travelhub.ui.components.InterestLevel
import com.mobile.travelhub.ui.components.OnboardingIntroScreenContent
import com.mobile.travelhub.ui.theme.TravelHubTheme

@Composable
fun OnboardingIntroScreen(
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    val tagLevels = remember {
        mutableStateMapOf<String, InterestLevel>().apply {
            put("Mountain", InterestLevel.Strong)
            put("Adventure", InterestLevel.Hidden)
            put("City", InterestLevel.Like)
            put("Nature", InterestLevel.Like)
        }
    }

    OnboardingIntroScreenContent(
        tagLevels = tagLevels,
        onBack = onBack,
        onSkip = onSkip,
        onPrevious = onPrevious,
        onContinue = onContinue,
        onTapTag = { tag, level ->
            tagLevels[tag.name] = when (level) {
                InterestLevel.Neutral -> InterestLevel.Like
                InterestLevel.Like -> InterestLevel.Neutral
                InterestLevel.Strong -> InterestLevel.Like
                InterestLevel.Hidden -> InterestLevel.Neutral
            }
        },
        onDoubleTapTag = { tag -> tagLevels[tag.name] = InterestLevel.Strong },
        onLongPressTag = { tag, level ->
            tagLevels[tag.name] = if (level == InterestLevel.Hidden) {
                InterestLevel.Neutral
            } else {
                InterestLevel.Hidden
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun OnboardingIntroScreenPreview() {
    TravelHubTheme {
        OnboardingIntroScreen()
    }
}



