package com.mobile.travelhub.ui.components.layout

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mobile.travelhub.R

@Composable
fun MainMenuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = stringResource(R.string.ui_197101e9db),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
