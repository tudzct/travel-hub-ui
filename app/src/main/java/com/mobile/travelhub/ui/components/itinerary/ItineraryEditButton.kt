package com.mobile.travelhub.ui.components.itinerary

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.PrimaryBlue

@Composable
fun ItineraryEditButton(
    isEditMode: Boolean,
    onToggleEditMode: () -> Unit
) {
    TextButton(onClick = onToggleEditMode) {
        Icon(
            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
            contentDescription = if (isEditMode) "Done editing" else "Edit itinerary",
            modifier = Modifier.size(18.dp),
            tint = if (isEditMode) PrimaryBlue else OnSurface
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isEditMode) "Done" else "Edit",
            fontWeight = FontWeight.ExtraBold,
            color = if (isEditMode) PrimaryBlue else OnSurface
        )
    }
}
