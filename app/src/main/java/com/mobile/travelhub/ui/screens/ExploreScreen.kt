package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mobile.travelhub.ui.components.FeaturedLocationCard
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg

@Composable
fun ExploreScreen() {
    val featuredLocations = listOf(
        FeaturedLocation(
            country = "INDONESIA",
            city = "Bali",
            imageUrl = "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=700&q=80"
        ),
        FeaturedLocation(
            country = "FRANCE",
            city = "Paris",
            imageUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=700&q=80"
        ),
        FeaturedLocation(
            country = "JAPAN",
            city = "Tokyo",
            imageUrl = "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?auto=format&fit=crop&w=700&q=80"
        )
    )

    val travelers = listOf(
        TopTraveler("Elena R.", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80", true),
        TopTraveler("Marcus T.", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80", false),
        TopTraveler("Sarah J.", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80", false),
        TopTraveler("Daniel K.", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=200&q=80", false)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
            .verticalScroll(rememberScrollState())
            .padding(top = 10.dp, bottom = 24.dp)
    ) {
        Text(
            text = "Explore",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = OnSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )

        SearchField()

        SectionLabel(text = "Recent Searches", topPadding = 18.dp)
        HorizontalChipRow(
            items = listOf("Bali", "Paris", "Tokyo", "New York"),
            leadingIcon = true
        )

        SectionDivider()

        SectionTitle(text = "Trending Now")
        HorizontalChipRow(
            items = listOf("#BeachVibes", "#MountainClimbing", "#CityBreaks", "#FoodTour"),
            leadingIcon = false,
            filled = true
        )

        SectionTitle(text = "Featured Locations", topPadding = 24.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            featuredLocations.forEach { location ->
                FeaturedLocationCard(
                    country = location.country,
                    city = location.city,
                    imageUrl = location.imageUrl,
                    modifier = Modifier
                        .width(220.dp)
                        .height(270.dp)
                )
            }
        }

        SectionTitleRow(
            title = "Top Travelers",
            action = "See All",
            topPadding = 36.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            travelers.forEach { traveler ->
                TravelerItem(traveler = traveler)
            }
        }
    }
}

@Composable
private fun SearchField() {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEFF2FA))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(19.dp)
        )
        Text(
            text = "Search destinations, people, or hashtags",
            modifier = Modifier.padding(start = 10.dp),
            color = OnSurfaceVariant,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionLabel(text: String, topPadding: androidx.compose.ui.unit.Dp = 0.dp) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, top = topPadding, bottom = 10.dp),
        color = OnSurfaceVariant,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SectionTitle(text: String, topPadding: androidx.compose.ui.unit.Dp = 20.dp) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, top = topPadding, bottom = 10.dp),
        color = OnSurface,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun SectionTitleRow(title: String, action: String, topPadding: androidx.compose.ui.unit.Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = OnSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = action,
            color = PrimaryBlue,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HorizontalChipRow(
    items: List<String>,
    leadingIcon: Boolean,
    filled: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            ExploreChip(
                text = item,
                leadingIcon = leadingIcon,
                filled = filled
            )
        }
    }
}

@Composable
private fun ExploreChip(text: String, leadingIcon: Boolean, filled: Boolean) {
    val background = if (filled) Color(0xFFE1E3EA) else Color(0xFFF3F6FC)
    val borderColor = if (filled) Color.Transparent else Color(0xFFD6DAE6)

    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 6.dp),
                color = OnSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                text = text,
                color = OnSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
            .height(1.dp)
            .background(Color(0xFFE4E7EF))
    )
}

@Composable
private fun TravelerItem(traveler: TopTraveler) {
    Column(
        modifier = Modifier.width(66.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = traveler.avatarUrl,
            contentDescription = traveler.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .border(
                    width = if (traveler.isFollowing) 2.dp else 0.dp,
                    color = if (traveler.isFollowing) PrimaryBlue else Color.Transparent,
                    shape = CircleShape
                )
        )
        Text(
            text = traveler.name,
            modifier = Modifier.padding(top = 7.dp),
            color = OnSurface,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Button(
            onClick = {},
            modifier = Modifier
                .padding(top = 6.dp)
                .height(24.dp),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (traveler.isFollowing) PrimaryBlue else Color(0xFFF3F5FA),
                contentColor = if (traveler.isFollowing) Color.White else OnSurfaceVariant
            )
        ) {
            Text(
                text = if (traveler.isFollowing) "Follow" else "Follow",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class FeaturedLocation(
    val country: String,
    val city: String,
    val imageUrl: String
)

private data class TopTraveler(
    val name: String,
    val avatarUrl: String,
    val isFollowing: Boolean
)
