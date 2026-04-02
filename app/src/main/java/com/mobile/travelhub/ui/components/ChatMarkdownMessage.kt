package com.mobile.travelhub.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ChatMarkdownMessage(markdown: String) {
    MarkdownText(
        markdown = markdown,
        style = MaterialTheme.typography.bodyMedium
    )
}
