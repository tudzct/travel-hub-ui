package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.models.UiState
import com.mobile.travelhub.ui.components.ChatMarkdownMessage
import com.mobile.travelhub.viewmodels.ItineraryBotViewModel

@Composable
fun ItineraryBotScreen(vm: ItineraryBotViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.start()
    }

    ItineraryBotContent(state = state)
}

@Composable
fun ItineraryBotContent(state: UiState) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        if (state.thinking.isNotEmpty()) {
            Text(
                text = state.thinking,
                color = Color.Gray,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (state.answer.isNotEmpty()) {
            ChatMarkdownMessage(markdown = state.answer)
        }

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (state.isStreaming) {
            CircularProgressIndicator()
        }
    }
}
