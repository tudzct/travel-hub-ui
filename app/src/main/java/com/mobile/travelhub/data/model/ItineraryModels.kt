package com.mobile.travelhub.data.model
 
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable

enum class ItineraryUserRole {
    LEADER,
    MEMBER
}

data class ItineraryWorkspace(
    val groupName: String,
    val version: Int,
    val role: ItineraryUserRole,
    val days: List<ItineraryDay>
)

data class ItineraryDay(
    val dayIndex: Int,
    val label: String,
    val dateLabel: String,
    val events: List<ItineraryEvent>,
    val dayId: Long? = null
)

data class ItineraryEvent(
    val eventId: String,
    val dayIndex: Int,
    val startTime: String,
    val endTime: String,
    val title: String,
    val placeName: String,
    val note: String,
    val transportToNext: String,
    val estimatedCost: String,
    val colorHex: Long = ItineraryEventColors.Default,
    val iconName: String = "Place",
    val dayId: Long? = null,
    val stopId: Long? = null
)

object ItineraryEventColors {
    const val Default: Long = 0xFF3E6AE1
    val Palette: List<Long> = listOf(
        Default,
        0xFF0D8A4B,
        0xFFCC5F00,
        0xFFB3261E,
        0xFF7A4DFF,
        0xFF00838F,
        0xFFAF3E6A,
        0xFF6B7280
    )
}

@Composable
fun getItineraryIcon(name: String) = when (name) {
    "Restaurant" -> Icons.Default.Restaurant
    "Flight" -> Icons.Default.Flight
    "Hotel" -> Icons.Default.Hotel
    "PhotoCamera" -> Icons.Default.PhotoCamera
    "ShoppingBag" -> Icons.Default.ShoppingBag
    "Museum" -> Icons.Default.Museum
    "DirectionsBus" -> Icons.Default.DirectionsBus
    "DirectionsWalk" -> Icons.Default.DirectionsWalk
    "DirectionsCar" -> Icons.Default.DirectionsCar
    "Train" -> Icons.Default.Train
    "LocalDrink" -> Icons.Default.LocalDrink
    "LocalPark" -> Icons.Default.Park
    "BeachAccess" -> Icons.Default.BeachAccess
    "Nightlife" -> Icons.Default.Nightlife
    else -> Icons.Default.Place
}

object ItineraryIcons {
    val Palette: List<String> = listOf(
        "Place",
        "Restaurant",
        "Flight",
        "Hotel",
        "PhotoCamera",
        "ShoppingBag",
        "Museum",
        "DirectionsBus",
        "DirectionsWalk",
        "DirectionsCar",
        "Train",
        "LocalDrink",
        "LocalPark",
        "BeachAccess",
        "Nightlife"
    )
}
