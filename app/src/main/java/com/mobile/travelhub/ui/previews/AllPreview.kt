package com.mobile.travelhub.ui.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mobile.travelhub.ui.screens.TravelHubScreen
import com.mobile.travelhub.ui.theme.TravelHubTheme

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TravelHubTheme {
        TravelHubScreen()
    }
}
