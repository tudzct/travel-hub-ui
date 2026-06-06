package com.mobile.travelhub.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationPlacePicker(
    label: String,
    selectedProvince: AdminProvinceResponse?,
    selectedPlace: TravelPlaceListItemResponse?,
    provinces: List<AdminProvinceResponse>,
    places: List<TravelPlaceListItemResponse>,
    isLoading: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String = "Chọn địa điểm",
    onProvinceSelected: (Long) -> Unit,
    onPlaceSelected: (Long) -> Unit
) {
    var isSheetOpen by remember { mutableStateOf(false) }
    var isChoosingProvince by remember(selectedProvince?.id) { mutableStateOf(selectedProvince == null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        DestinationPickerAnchor(
            selectedProvince = selectedProvince,
            selectedPlace = selectedPlace,
            placeholder = placeholder,
            enabled = enabled,
            isLoading = isLoading,
            onClick = {
                isChoosingProvince = selectedProvince == null
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
                isChoosingProvince = isChoosingProvince || selectedProvince == null,
                onChooseProvince = { isChoosingProvince = true },
                onProvinceSelected = { provinceId ->
                    onProvinceSelected(provinceId)
                    isChoosingProvince = false
                },
                onPlaceSelected = { placeId ->
                    onPlaceSelected(placeId)
                    isSheetOpen = false
                }
            )
        }
    }
}

@Composable
private fun DestinationPickerAnchor(
    selectedProvince: AdminProvinceResponse?,
    selectedPlace: TravelPlaceListItemResponse?,
    placeholder: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectedPlaceThumb(selectedPlace = selectedPlace)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedPlace?.name ?: placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selectedPlace != null) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val title = selectedPlace?.province?.name
                    ?: selectedProvince?.let { "Đang xem ${it.name}" }

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

            Spacer(modifier = Modifier.width(10.dp))

            if (isLoading) {
                InlineLoadingSkeleton(modifier = Modifier.size(18.dp))
            } else {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
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
    isChoosingProvince: Boolean,
    onChooseProvince: () -> Unit,
    onProvinceSelected: (Long) -> Unit,
    onPlaceSelected: (Long) -> Unit
) {
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
                    text = if (isChoosingProvince) "Chọn tỉnh" else "Chọn địa điểm",
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

            if (!isChoosingProvince && selectedProvince != null) {
                TextButton(onClick = onChooseProvince) {
                    Text("Đổi tỉnh")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        Spacer(modifier = Modifier.height(8.dp))

        if (isChoosingProvince) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(provinces, key = { it.id }) { province ->
                    ProvinceOptionRow(
                        province = province,
                        selected = province.id == selectedProvince?.id,
                        onClick = { onProvinceSelected(province.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        } else {
            when {
                isLoading -> LoadingPlaceList()
                places.isEmpty() -> EmptyPlaceList()
                else -> LazyColumn(
                    modifier = Modifier.heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(places, key = { it.id }) { place ->
                        PlaceOptionRow(
                            place = place,
                            selected = place.id == selectedPlace?.id,
                            onClick = { onPlaceSelected(place.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
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
            text = "Chưa có địa điểm cho tỉnh này",
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
                label = "Điểm đến",
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
