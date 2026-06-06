package com.mobile.travelhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue

private val SearchBarShape = RoundedCornerShape(8.dp)

@Composable
fun SearchBar(
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    onValueChange: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val baseModifier = modifier
        .fillMaxWidth()
        .height(42.dp)
        .clip(SearchBarShape)

    if (onValueChange == null) {
        Row(
            modifier = baseModifier
                .background(containerColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onClick?.invoke() }
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchLeadingIcon()
            SearchPlaceholder(
                text = placeholder,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            )
        }
        return
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = baseModifier.background(containerColor),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = OnSurface,
            fontSize = 14.sp
        ),
        cursorBrush = SolidColor(PrimaryBlue),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        SearchPlaceholder(text = placeholder)
                    }
                    innerTextField()
                }
                trailingContent?.invoke()
            }
        }
    )
}

@Composable
private fun SearchLeadingIcon() {
    Icon(
        imageVector = Icons.Outlined.Search,
        contentDescription = null,
        tint = OnSurfaceVariant,
        modifier = Modifier.size(19.dp)
    )
}

@Composable
private fun SearchPlaceholder(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = OnSurfaceVariant,
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
