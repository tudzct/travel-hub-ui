package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.viewmodels.ItineraryBotViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ItineraryBotScreen(vm: ItineraryBotViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (state.thinking.isNotEmpty()) {
            Text(
                text = state.thinking,
                color = Color.Gray,
                fontStyle = FontStyle.Italic
            )
        }

        if (state.isStreaming) {
            Text(state.answer) // nhẹ hơn khi stream
        } else {
            MarkdownText(markdown = state.answer)
        }
    }
}
