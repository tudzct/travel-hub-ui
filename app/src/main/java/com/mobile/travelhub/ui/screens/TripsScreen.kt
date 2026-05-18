package com.mobile.travelhub.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.theme.*
import com.mobile.travelhub.viewmodels.TripsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    createdTripId: Long? = null,
    createdGroupName: String? = null,
    onNavigateToGroupDetail: (Long, String) -> Unit = { _, _ -> },
    onNavigateToCreateGroup: () -> Unit = {}
) {
    val viewModel: TripsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshDashboard()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val listState = rememberLazyListState()
    LaunchedEffect(state.upcomingTrips, state.activeTrip, createdTripId) {
        if (createdTripId != null) {
            val tripsList = state.upcomingTrips.ifEmpty { listOfNotNull(state.activeTrip) }
            val indexInTrips = tripsList.indexOfFirst { it.tripId == createdTripId }
            if (indexInTrips >= 0) {
                val baseOffset = (if (state.errorMessage != null) 1 else 0) + 1 + 1 + 1
                val targetIndex = baseOffset + indexInTrips
                try {
                    listState.animateScrollToItem(targetIndex)
                } catch (_: Exception) {
                }
            }
        }
    }
    var showAddTripSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = SurfaceBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTripSheet = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.FlightTakeoff, contentDescription = "New Trip")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chuyến đi mới", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp) // Space for FAB
        ) {
            if (state.errorMessage != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(
                            text = state.errorMessage.orEmpty(),
                            color = SunsetOrange,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Personalized Header
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                    Text(
                        text = "Sẵn sàng lên đường nào,",
                        fontSize = 16.sp,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nhà thám hiểm! \uD83C\uDF0D",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = OnSurface,
                        letterSpacing = (-1).sp
                    )
                }
            }

            // Current Active Trip (More immersive)
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        "Đang diễn ra",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (state.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else {
                        ActiveJourneyCardV2(
                            trip = state.activeTrip,
                            onNavigateToGroupDetail = onNavigateToGroupDetail
                        )
                    }
                }
                Spacer(modifier = Modifier.height(36.dp))
            }
            
            // Upcoming Trips
            // Upcoming Trips header
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sắp khởi hành",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = OnSurface
                        )
                        Text(
                            text = "Xem tất cả",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    if (state.upcomingTrips.isEmpty()) {
                        Text(
                            text = "Chưa có chuyến đi sắp tới từ BE.",
                            color = OnSurfaceVariant,
                            fontSize = 14.sp
                        )
                    } else {
                        state.upcomingTrips.forEachIndexed { index, trip ->
                            UpcomingTripItem(
                                trip = trip,
                                onClick = { onNavigateToGroupDetail(trip.tripId, trip.name) }
                            )
                            if (index < state.upcomingTrips.lastIndex) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(36.dp)) }

            // Past Memories Section
            item {
                Column {
                    Text(
                        text = "Nhật ký hành trình",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = OnSurface,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(4) { index ->
                            val places = listOf("Đà Lạt", "Đà Nẵng", "Sapa", "Phú Quốc")
                            val dates = listOf(
                                "Tháng 8, 2023",
                                "Tháng 5, 2023",
                                "Tháng 12, 2022",
                                "Tháng 7, 2022"
                            )
                            PastMemoryCard(places[index], dates[index])
                        }
                    }
                }
            }
        }

        // Bottom Sheet for adding trips
        if (showAddTripSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddTripSheet = false },
                sheetState = sheetState,
                containerColor = SurfaceContainerLowest,
                dragHandle = { BottomSheetDefaults.DragHandle(color = SurfaceContainerLow) }
            ) {
                AddTripOptionsContent(
                    onDismiss = { showAddTripSheet = false },
                    onCreateNew = {
                        showAddTripSheet = false
                        onNavigateToCreateGroup()
                    },
                    onJoinTrip = { joinCode, onDone ->
                        viewModel.joinTrip(joinCode) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) {
                                onDone()
                                showAddTripSheet = false
                            }
                        }
                    },
                    isJoining = state.isJoining,
                    joinErrorMessage = state.errorMessage
                )
            }
        }
    }
}

@Composable
fun StatChip(value: String, label: String, emoji: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
        }
    }
}

@Composable
fun ActiveJourneyCardV2(
    trip: com.mobile.travelhub.viewmodels.UpcomingTripUiModel?,
    onNavigateToGroupDetail: (Long, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = trip != null) {
                trip?.let { onNavigateToGroupDetail(it.tripId, it.name) }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background), // Mock image
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient from bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            // Info bottom left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SunsetOrange)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Ngày 3 / 8", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = trip?.name ?: "Chưa có chuyến đi đang diễn ra",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        trip?.location ?: "Dashboard BE chưa trả location",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PastMemoryCard(place: String, date: String) {
    Column(modifier = Modifier.width(130.dp)) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(SurfaceContainerLow)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(place, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = OnSurface)
        Text(date, fontSize = 12.sp, color = OnSurfaceVariant)
    }
}

@Composable
fun AddTripOptionsContent(
    onDismiss: () -> Unit,
    onCreateNew: () -> Unit,
    onJoinTrip: (String, () -> Unit) -> Unit,
    isJoining: Boolean,
    joinErrorMessage: String?
) {
    var showJoinInput by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        if (!showJoinInput) {
            Text(
                text = "Bắt đầu hành trình mới",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            TripOptionItem(
                icon = Icons.Default.Add,
                title = "Tạo chuyến đi mới",
                desc = "Lên kế hoạch từ đầu với bạn bè",
                color = PrimaryBlue,
                onClick = onCreateNew
            )
            Spacer(modifier = Modifier.height(16.dp))
            TripOptionItem(
                icon = Icons.Default.GroupAdd,
                title = "Tham gia bằng mã",
                desc = "Nhập mã để tham gia nhóm có sẵn",
                color = SunsetOrange,
                onClick = {
                    showJoinInput = true
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TripOptionItem(
                icon = Icons.Default.Map,
                title = "Khám phá địa điểm",
                desc = "Tìm cảm hứng từ các hành trình mẫu",
                color = Color(0xFF4CAF50),
                onClick = {}
            )
        } else {
            Text(
                text = "Tham gia chuyến đi",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nhập mã gồm 8 ký tự được chia sẻ bởi Trưởng nhóm.",
                fontSize = 14.sp,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = joinCode,
                onValueChange = { joinCode = it.uppercase().take(8) },
                label = { Text("Mã chuyến đi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = SurfaceContainerLow
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                enabled = !isJoining,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(if (isJoining) "Đang gửi..." else "Gửi yêu cầu tham gia", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TripOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    title: String, 
    desc: String, 
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurface)
            Text(desc, fontSize = 12.sp, color = OnSurfaceVariant)
        }
    }
}

@Composable
fun UpcomingTripItem(
    trip: com.mobile.travelhub.viewmodels.UpcomingTripUiModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerLow)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = trip.name,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, null, modifier = Modifier.size(14.dp), tint = SunsetOrange)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (trip.daysLeft > 0) {
                        "Còn ${trip.daysLeft} ngày nữa"
                    } else {
                        trip.startDate?.let { "Bắt đầu $it" } ?: "Đang chờ ngày khởi hành"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SunsetOrange
                )
            }
        }
    }
}
