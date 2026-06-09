package com.mobile.travelhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.theme.PrimaryBlue

@Composable
fun CommentSubmitAction(
    isSubmitting: Boolean,
    canSubmit: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onSubmit,
            enabled = canSubmit && !isSubmitting,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (canSubmit && !isSubmitting) PrimaryBlue
                    else PrimaryBlue.copy(alpha = 0.5f)
                )
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.paper_plane_right),
                    contentDescription = stringResource(R.string.ui_591e0e89f0),
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
            }
        }


    }
}
