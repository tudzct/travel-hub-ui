package com.mobile.travelhub.ui.components.itinerary

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

@Composable
fun ItineraryAddButton(
    onAddItinerary: () -> Unit
) {
    TextButton(onClick = onAddItinerary) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add itinerary",
            modifier = Modifier.size(18.dp),
            tint = OnSurface
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Add",
            fontWeight = FontWeight.ExtraBold,
            color = OnSurface
        )
    }
}
