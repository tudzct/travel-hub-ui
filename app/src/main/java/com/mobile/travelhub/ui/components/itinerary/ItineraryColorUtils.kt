package com.mobile.travelhub.ui.components.itinerary

import androidx.compose.ui.graphics.Color

internal fun Long.toItineraryColor(): Color {
    return Color(toInt())
}
