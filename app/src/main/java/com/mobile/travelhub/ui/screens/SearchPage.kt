package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.OutlineVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg

@Composable
fun SearchPage(
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val recentSearches = listOf("Bali", "Paris", "Tokyo", "New York")
    val trendingSearches = listOf("#BeachVibes", "#MountainClimbing", "#CityBreaks", "#FoodTour")
    val results = remember(query) {
        val candidates = listOf(
            SearchResult("Bali", "Indonesia", SearchResultType.Place),
            SearchResult("Paris", "France", SearchResultType.Place),
            SearchResult("Tokyo", "Japan", SearchResultType.Place),
            SearchResult("Da Nang", "Vietnam", SearchResultType.Place),
            SearchResult("#BeachVibes", "Trending hashtag", SearchResultType.Tag),
            SearchResult("#FoodTour", "Trending hashtag", SearchResultType.Tag)
        )
        if (query.isBlank()) {
            emptyList()
        } else {
            candidates.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.subtitle.contains(query, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = OnSurface
                )
            }
            SearchInput(
                query = query,
                onQueryChange = { query = it },
                onClear = { query = "" },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (query.isBlank()) {
                item {
                    SearchSectionTitle("Recent Searches")
                }
                items(recentSearches) { text ->
                    SearchSuggestionRow(
                        text = text,
                        subtitle = "Search again",
                        leadingIcon = SearchLeadingIcon.History,
                        onClick = { query = text }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    SearchSectionTitle("Trending Now")
                }
                items(trendingSearches) { text ->
                    SearchSuggestionRow(
                        text = text,
                        subtitle = "Explore posts and trips",
                        leadingIcon = SearchLeadingIcon.Tag,
                        onClick = { query = text }
                    )
                }
            } else if (results.isEmpty()) {
                item {
                    EmptySearchState(query = query)
                }
            } else {
                item {
                    SearchSectionTitle("Results")
                }
                items(results) { result ->
                    SearchResultRow(result = result)
                }
            }
        }
    }
}

@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = OnSurface,
            fontSize = 14.sp
        ),
        cursorBrush = SolidColor(PrimaryBlue),
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEFF2FA)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search destinations, people, or hashtags",
                            color = OnSurfaceVariant,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear search",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun SearchSectionTitle(text: String) {
    Text(
        text = text,
        color = OnSurfaceVariant,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SearchSuggestionRow(
    text: String,
    subtitle: String,
    leadingIcon: SearchLeadingIcon,
    onClick: () -> Unit
) {
    SearchRowContainer(onClick = onClick) {
        SearchIconBubble(type = leadingIcon)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = text,
                color = OnSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = OnSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchResultRow(result: SearchResult) {
    SearchRowContainer(onClick = {}) {
        SearchIconBubble(
            type = if (result.type == SearchResultType.Tag) SearchLeadingIcon.Tag else SearchLeadingIcon.Place
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = result.title,
                color = OnSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = result.subtitle,
                color = OnSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchRowContainer(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE6E9F0), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun SearchIconBubble(type: SearchLeadingIcon) {
    val icon = when (type) {
        SearchLeadingIcon.History -> Icons.Outlined.History
        SearchLeadingIcon.Place -> Icons.Outlined.LocationOn
        SearchLeadingIcon.Tag -> Icons.Outlined.Tag
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFFEFF2FA)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun EmptySearchState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF2FA))
                .border(1.dp, OutlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "No results for \"$query\"",
            modifier = Modifier.padding(top = 14.dp),
            color = OnSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Try another destination, person, or hashtag.",
            modifier = Modifier.padding(top = 4.dp),
            color = OnSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

private data class SearchResult(
    val title: String,
    val subtitle: String,
    val type: SearchResultType
)

private enum class SearchResultType {
    Place,
    Tag
}

private enum class SearchLeadingIcon {
    History,
    Place,
    Tag
}
