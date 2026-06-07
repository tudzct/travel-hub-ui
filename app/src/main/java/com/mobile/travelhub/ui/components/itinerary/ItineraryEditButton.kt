package com.mobile.travelhub.ui.components.itinerary

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.R

@Composable
fun ItineraryAddButton(
    onAddItinerary: () -> Unit
) {
    TextButton(onClick = onAddItinerary) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.ui_ac5cf15d1d),
            modifier = Modifier.size(18.dp),
            tint = OnSurface
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.ui_61cc55aa04),
            fontWeight = FontWeight.ExtraBold,
            color = OnSurface
        )
    }
}
