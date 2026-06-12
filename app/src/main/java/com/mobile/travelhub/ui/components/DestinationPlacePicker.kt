package com.mobile.travelhub.ui.components

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mobile.travelhub.data.model.AdminProvinceResponse
import com.mobile.travelhub.data.model.ProvinceResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.ui.theme.TravelHubTheme
import com.mobile.travelhub.R
import java.text.Normalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationPlacePicker(
    label: String,
    selectedProvince: AdminProvinceResponse?,
    selectedPlace: TravelPlaceListItemResponse?,
    provinces: List<AdminProvinceResponse>,
    places: List<TravelPlaceListItemResponse>,
    isLoading: Boolean,
    isLoadingMorePlaces: Boolean = false,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String = "Chọn địa điểm",
    allowPlaceSelection: Boolean = true,
    labelLeadingIcon: ImageVector? = null,
    uppercaseLabel: Boolean = true,
    compactAnchor: Boolean = false,
    anchorContainerColor: Color? = null,
    anchorBorderColor: Color? = null,
    anchorTitleOverride: String? = null,
    compactSupportingText: String? = null,
    anchorTrailingIcon: ImageVector = Icons.Rounded.KeyboardArrowDown,
    showCompactIconBackground: Boolean = true,
    onProvinceSelected: (Long) -> Unit,
    onPlaceSelected: (Long) -> Unit = {},
    placeQuery: String = "",
    onPlaceQueryChange: (String) -> Unit = {},
    onLoadMorePlaces: () -> Unit = {},
    provinceErrorMessage: String? = null,
    placesErrorMessage: String? = null,
    placesLoadMoreErrorMessage: String? = null,
    onRetryProvinces: () -> Unit = {},
    onRetryPlaces: () -> Unit = {}
) {
    var isSheetOpen by remember { mutableStateOf(false) }
    var isChoosingProvince by remember(selectedProvince?.id, allowPlaceSelection) {
        mutableStateOf(!allowPlaceSelection || selectedProvince == null)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotBlank()) {
            Row(
                modifier = Modifier.padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                labelLeadingIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = if (uppercaseLabel) label.uppercase() else label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = if (uppercaseLabel) 1.sp else 0.sp
                )
            }
        }

        DestinationPickerAnchor(
            selectedProvince = selectedProvince,
            selectedPlace = selectedPlace,
            placeholder = placeholder,
            allowPlaceSelection = allowPlaceSelection,
            enabled = enabled,
            isLoading = isLoading,
            compact = compactAnchor,
            containerColor = anchorContainerColor,
            borderColor = anchorBorderColor,
            titleOverride = anchorTitleOverride,
            compactSupportingText = compactSupportingText,
            trailingIcon = anchorTrailingIcon,
            showCompactIconBackground = showCompactIconBackground,
            onClick = {
                isChoosingProvince = !allowPlaceSelection || selectedProvince == null
                isSheetOpen = true
            }
        )
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DestinationPickerSheet(
                selectedProvince = selectedProvince,
                selectedPlace = selectedPlace,
                provinces = provinces,
                places = places,
                isLoading = isLoading,
                isLoadingMorePlaces = isLoadingMorePlaces,
                isChoosingProvince = !allowPlaceSelection || isChoosingProvince || selectedProvince == null,
                allowPlaceSelection = allowPlaceSelection,
                onChooseProvince = { isChoosingProvince = true },
                onProvinceSelected = { provinceId ->
                    onProvinceSelected(provinceId)
                    if (allowPlaceSelection) {
                        isChoosingProvince = false
                    } else {
                        isSheetOpen = false
                    }
                },
                onPlaceSelected = { placeId ->
                    onPlaceSelected(placeId)
                    isSheetOpen = false
                },
                placeQuery = placeQuery,
                onPlaceQueryChange = onPlaceQueryChange,
                onLoadMorePlaces = onLoadMorePlaces,
                provinceErrorMessage = provinceErrorMessage,
                placesErrorMessage = placesErrorMessage,
                placesLoadMoreErrorMessage = placesLoadMoreErrorMessage,
                onRetryProvinces = onRetryProvinces,
                onRetryPlaces = onRetryPlaces
            )
        }
    }
}

@Composable
private fun DestinationPickerAnchor(
    selectedProvince: AdminProvinceResponse?,
    selectedPlace: TravelPlaceListItemResponse?,
    placeholder: String,
    allowPlaceSelection: Boolean,
    enabled: Boolean,
    isLoading: Boolean,
    compact: Boolean,
    containerColor: Color?,
    borderColor: Color?,
    titleOverride: String?,
    compactSupportingText: String?,
    trailingIcon: ImageVector,
    showCompactIconBackground: Boolean,
    onClick: () -> Unit
) {
    val anchorShape = RoundedCornerShape(18.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(anchorShape)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, anchorShape)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled, onClick = onClick),
        shape = anchorShape,
        color = containerColor ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = if (compact) 16.dp else 14.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (compact) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (showCompactIconBackground) {
                                Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                SelectedPlaceThumb(selectedPlace = selectedPlace)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleOverride
                        ?: selectedPlace?.name
                        ?: selectedProvince?.name
                        ?: placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selectedPlace != null || selectedProvince != null) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    },
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (compact && compactSupportingText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = compactSupportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val title = selectedPlace?.province?.name
                    ?: selectedProvince?.let { if (allowPlaceSelection) "Đang xem ${it.name}" else it.name }
                if (!compact && title != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isLoading) {
                InlineLoadingSkeleton(modifier = Modifier.size(18.dp))
            } else {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SelectedPlaceThumb(selectedPlace: TravelPlaceListItemResponse?) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        if (selectedPlace?.mainImage.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Rounded.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            AsyncImage(
                model = selectedPlace?.mainImage,
                contentDescription = selectedPlace?.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun DestinationPickerSheet(
    selectedProvince: AdminProvinceResponse?,
    selectedPlace: TravelPlaceListItemResponse?,
    provinces: List<AdminProvinceResponse>,
    places: List<TravelPlaceListItemResponse>,
    isLoading: Boolean,
    isLoadingMorePlaces: Boolean = false,
    isChoosingProvince: Boolean,
    allowPlaceSelection: Boolean = true,
    onChooseProvince: () -> Unit,
    onProvinceSelected: (Long) -> Unit,
    onPlaceSelected: (Long) -> Unit,
    placeQuery: String = "",
    onPlaceQueryChange: (String) -> Unit = {},
    onLoadMorePlaces: () -> Unit = {},
    provinceErrorMessage: String? = null,
    placesErrorMessage: String? = null,
    placesLoadMoreErrorMessage: String? = null,
    onRetryProvinces: () -> Unit = {},
    onRetryPlaces: () -> Unit = {}
) {
    var provinceQuery by remember { mutableStateOf("") }
    val normalizedProvinceQuery = remember(provinceQuery) {
        provinceQuery.normalizedSearchText()
    }
    val filteredProvinces = remember(provinces, normalizedProvinceQuery) {
        if (normalizedProvinceQuery.isBlank()) {
            provinces
        } else {
            provinces.filter { province ->
                province.name.normalizedSearchText().contains(normalizedProvinceQuery)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 620.dp)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isChoosingProvince || !allowPlaceSelection) "Chọn tỉnh" else "Chọn địa điểm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = selectedProvince?.name ?: "Bắt đầu bằng tỉnh/thành phố",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (allowPlaceSelection && !isChoosingProvince && selectedProvince != null) {
                TextButton(onClick = onChooseProvince) {
                    Text(stringResource(R.string.ui_c969efe0e5))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        Spacer(modifier = Modifier.height(8.dp))

        DestinationSearchBar(
            value = if (isChoosingProvince || !allowPlaceSelection) provinceQuery else placeQuery,
            placeholder = stringResource(
                if (isChoosingProvince || !allowPlaceSelection) {
                    R.string.search_province_hint
                } else {
                    R.string.search_place_hint
                }
            ),
            onValueChange = if (isChoosingProvince || !allowPlaceSelection) {
                { value -> provinceQuery = value }
            } else {
                onPlaceQueryChange
            }
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (isChoosingProvince || !allowPlaceSelection) {
            when {
                isLoading && provinces.isEmpty() -> LoadingPlaceList()
                provinceErrorMessage != null && provinces.isEmpty() -> DestinationLoadError(
                    title = stringResource(R.string.ui_86143251a3),
                    message = provinceErrorMessage,
                    onRetry = onRetryProvinces
                )
                filteredProvinces.isEmpty() -> EmptySearchResults(
                    message = stringResource(R.string.no_province_search_results)
                )
                else -> LazyColumn(
                    modifier = Modifier.heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredProvinces, key = { it.id }) { province ->
                        ProvinceOptionRow(
                            province = province,
                            selected = province.id == selectedProvince?.id,
                            onClick = { onProvinceSelected(province.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        } else {
            when {
                isLoading && places.isEmpty() -> LoadingPlaceList()
                placesErrorMessage != null && places.isEmpty() -> DestinationLoadError(
                    title = stringResource(R.string.ui_230aa1c612),
                    message = placesErrorMessage,
                    onRetry = onRetryPlaces
                )
                places.isEmpty() && placeQuery.isNotBlank() -> EmptySearchResults(
                    message = stringResource(R.string.no_place_search_results)
                )
                places.isEmpty() -> EmptyPlaceList()
                else -> PlaceOptionsList(
                    places = places,
                    selectedPlaceId = selectedPlace?.id,
                    isLoadingMore = isLoadingMorePlaces,
                    loadMoreErrorMessage = placesLoadMoreErrorMessage,
                    onPlaceSelected = onPlaceSelected,
                    onLoadMore = onLoadMorePlaces
                )
            }
        }
    }
}

@Composable
private fun DestinationSearchBar(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    SearchBar(
        value = value,
        placeholder = placeholder,
        onValueChange = onValueChange,
        trailingContent = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Composable
private fun PlaceOptionsList(
    places: List<TravelPlaceListItemResponse>,
    selectedPlaceId: Long?,
    isLoadingMore: Boolean,
    loadMoreErrorMessage: String?,
    onPlaceSelected: (Long) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember(places) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            places.isNotEmpty() && lastVisibleIndex >= places.lastIndex - 3
        }
    }

    LaunchedEffect(shouldLoadMore, places.size) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.heightIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(places, key = { it.id }) { place ->
            PlaceOptionRow(
                place = place,
                selected = place.id == selectedPlaceId,
                onClick = { onPlaceSelected(place.id) }
            )
        }
        when {
            isLoadingMore -> item(key = "places-loading-more") {
                PlaceOptionRowSkeleton()
            }
            loadMoreErrorMessage != null -> item(key = "places-load-more-error") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = loadMoreErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = onLoadMore) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
            else -> item(key = "places-list-bottom") {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun PlaceOptionRowSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonBlock(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(18.dp),
                shape = RoundedCornerShape(6.dp)
            )
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.42f)
                    .height(14.dp),
                shape = RoundedCornerShape(6.dp)
            )
        }
    }
}

@Composable
private fun EmptySearchResults(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.size(34.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun String.normalizedSearchText(): String {
    return Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .replace('đ', 'd')
        .replace('Đ', 'D')
        .lowercase()
}

@Composable
private fun ProvinceOptionRow(
    province: AdminProvinceResponse,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = province.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun PlaceOptionRow(
    place: TravelPlaceListItemResponse,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (place.mainImage.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            } else {
                AsyncImage(
                    model = place.mainImage,
                    contentDescription = place.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = place.province.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LoadingPlaceList() {
    LoadingListSkeleton(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        itemCount = 2
    )
}

@Composable
private fun DestinationLoadError(
    title: String,
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.WarningAmber,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(34.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        RetryButton(onClick = onRetry)
    }
}

@Composable
private fun EmptyPlaceList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.size(34.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.ui_4d8857d1a8),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Destination picker - selected", showBackground = true)
@Composable
private fun DestinationPlacePickerSelectedPreview() {
    val provinces = previewProvinces()
    val places = previewPlaces()

    TravelHubTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            DestinationPlacePicker(
                label = stringResource(R.string.ui_8dc001232f),
                selectedProvince = provinces.first(),
                selectedPlace = places.first(),
                provinces = provinces,
                places = places,
                isLoading = false,
                enabled = true,
                onProvinceSelected = {},
                onPlaceSelected = {}
            )
        }
    }
}

@Preview(name = "Destination picker - provinces", showBackground = true, heightDp = 620)
@Composable
private fun DestinationPickerProvinceSheetPreview() {
    TravelHubTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 20.dp)
        ) {
            DestinationPickerSheet(
                selectedProvince = null,
                selectedPlace = null,
                provinces = previewProvinces(),
                places = emptyList(),
                isLoading = false,
                isChoosingProvince = true,
                onChooseProvince = {},
                onProvinceSelected = {},
                onPlaceSelected = {}
            )
        }
    }
}

@Preview(name = "Destination picker - places", showBackground = true, heightDp = 620)
@Composable
private fun DestinationPickerPlaceSheetPreview() {
    val provinces = previewProvinces()
    val places = previewPlaces()

    TravelHubTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 20.dp)
        ) {
            DestinationPickerSheet(
                selectedProvince = provinces.first(),
                selectedPlace = places.first(),
                provinces = provinces,
                places = places,
                isLoading = false,
                isChoosingProvince = false,
                onChooseProvince = {},
                onProvinceSelected = {},
                onPlaceSelected = {}
            )
        }
    }
}

private fun previewProvinces(): List<AdminProvinceResponse> = listOf(
    AdminProvinceResponse(
        id = 1,
        name = "Đà Nẵng",
        codename = "da_nang",
        divisionType = "thanh_pho_trung_uong",
        phoneCode = 236,
        image = null
    ),
    AdminProvinceResponse(
        id = 2,
        name = "Lâm Đồng",
        codename = "lam_dong",
        divisionType = "tinh",
        phoneCode = 263,
        image = null
    ),
    AdminProvinceResponse(
        id = 3,
        name = "Thành phố Hồ Chí Minh",
        codename = "ho_chi_minh",
        divisionType = "thanh_pho_trung_uong",
        phoneCode = 28,
        image = null
    )
)

private fun previewPlaces(): List<TravelPlaceListItemResponse> {
    val province = ProvinceResponse(id = 1, name = "Đà Nẵng", image = null)
    return listOf(
        TravelPlaceListItemResponse(
            id = 101,
            name = "Bà Nà Hills",
            description = "Khu du lịch trên núi với cáp treo và cầu Vàng.",
            province = province,
            mainImage = "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=800&q=80",
            views = 1200,
            openingTime = "08:00 - 17:00",
            averageRating = 4.7,
            reviewCount = 248
        ),
        TravelPlaceListItemResponse(
            id = 102,
            name = "Cầu Rồng",
            description = "Biểu tượng bên sông Hàn.",
            province = province,
            mainImage = null,
            views = 820,
            openingTime = null,
            averageRating = 4.5,
            reviewCount = 96
        ),
        TravelPlaceListItemResponse(
            id = 103,
            name = "Bán đảo Sơn Trà",
            description = "Rừng, biển và các điểm ngắm cảnh.",
            province = province,
            mainImage = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
            views = 980,
            openingTime = null,
            averageRating = 4.8,
            reviewCount = 174
        )
    )
}
