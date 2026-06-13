package com.mobile.travelhub.ui.components

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.travelhub.R

@Composable
fun CommentItem(
    name: String,
    comment: String,
    time: String,
    avatarUrl: String?,
    onAuthorClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 24.dp)
    ) {
        TravelHubAvatar(
            avatarUrl = avatarUrl,
            contentDescription = stringResource(R.string.ui_7631b26ea8),
            fallbackName = name,
            modifier = Modifier
                .size(32.dp)
                .then(
                    if (onAuthorClick != null) {
                        Modifier.clickable(onClick = onAuthorClick)
                    } else {
                        Modifier
                    }
                )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.then(
                        if (onAuthorClick != null) {
                            Modifier.clickable(onClick = onAuthorClick)
                        } else {
                            Modifier
                        }
                    )
                )
                Text(
                    text = stringResource(R.string.comment_time_format, time),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = comment,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
