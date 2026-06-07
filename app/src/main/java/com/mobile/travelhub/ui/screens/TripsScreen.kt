package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import coil.compose.AsyncImage
import com.mobile.travelhub.ui.components.modifiers.shimmerEffect
import com.mobile.travelhub.ui.components.SimpleFormTextField
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.theme.*
import com.mobile.travelhub.viewmodels.TripsViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    createdTripId: Long? = null,
    createdGroupName: String? = null,
    onNavigateToGroupDetail: (Long, String) -> Unit = { _, _ -> },
    onNavigateToUpcomingTrips: () -> Unit = {},
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
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(15000L)
            viewModel.refreshDashboard(isSilent = true)
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
                Icon(Icons.Default.FlightTakeoff, contentDescription = stringResource(R.string.ui_77c36e7640))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.ui_ab38c564dc), fontWeight = FontWeight.Bold)
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
                        text = stringResource(R.string.ui_8f23352824),
                        fontSize = 16.sp,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.ui_5a3d42db37),
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
                        stringResource(R.string.ui_0811963e56),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (state.isLoading && state.activeTrip == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .shimmerEffect()
                        )
                    } else {
                        ActiveJourneyCardV2(
                            trip = state.activeTrip,
                            onNavigateToGroupDetail = onNavigateToGroupDetail
                        )
                    }
                }
                Spacer(modifier = Modifier.height(36.dp))
            }
            
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.ui_9183bdeb31),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = OnSurface
                        )
                        Text(
                            text = stringResource(R.string.ui_7e04025452),
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable(onClick = onNavigateToUpcomingTrips)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    if (state.isLoading && state.upcomingTrips.isEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            repeat(2) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(SurfaceContainerLowest)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .shimmerEffect()
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .width(140.dp)
                                                .height(14.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .shimmerEffect()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(80.dp)
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .shimmerEffect()
                                        )
                                    }
                                }
                            }
                        }
                    } else if (state.upcomingTrips.isEmpty()) {
                        Text(
                            text = stringResource(R.string.ui_a5bb058cec),
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
                        text = stringResource(R.string.ui_f593bb6075),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = OnSurface,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (state.isLoading && state.pastTrips.isEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(3) {
                                Column(modifier = Modifier.width(130.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(130.dp)
                                            .clip(RoundedCornerShape(24.dp))
                                            .shimmerEffect()
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .shimmerEffect()
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .shimmerEffect()
                                    )
                                }
                            }
                        }
                    } else if (state.pastTrips.isEmpty()) {
                        Text(
                            text = stringResource(R.string.ui_8a9ea817b6),
                            color = OnSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = state.pastTrips,
                                key = { it.tripId }
                            ) { trip ->
                                PastMemoryCard(
                                    place = trip.locationName,
                                    date = trip.dateString,
                                    imageUrl = trip.imageUrl,
                                    onClick = { onNavigateToGroupDetail(trip.tripId, trip.locationName) }
                                )
                            }
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
                            onDone()
                            showAddTripSheet = false
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
            val coverImageUrl = trip?.coverImageUrl?.takeIf { it.isNotBlank() }
            if (coverImageUrl != null) {
                AsyncImage(
                    model = coverImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

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
                        trip?.location ?: "",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PastMemoryCard(place: String, date: String, imageUrl: String? = null, onClick: () -> Unit = {}) {
    val cleanDate = remember(date) {
        val rawDate = date.split(" - ", " – ", " to ").firstOrNull()?.trim() ?: date
        val normalized = rawDate.substringBefore("T")
        runCatching {
            val localDate = LocalDate.parse(normalized)
            localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }.getOrDefault(rawDate)
    }
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            val resolvedImageUrl = imageUrl?.takeIf { it.isNotBlank() }
            if (resolvedImageUrl != null) {
                AsyncImage(
                    model = resolvedImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceContainerLow)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceContainerLow)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(place, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = OnSurface)
        Text(cleanDate, fontSize = 12.sp, color = OnSurfaceVariant)
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
                text = stringResource(R.string.ui_cac2fa7e3c),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            TripOptionItem(
                icon = Icons.Default.Add,
                title = stringResource(R.string.ui_a7b24cbdc8),
                desc = stringResource(R.string.ui_e935e26423),
                color = PrimaryBlue,
                onClick = onCreateNew
            )
            Spacer(modifier = Modifier.height(16.dp))
            TripOptionItem(
                icon = Icons.Default.GroupAdd,
                title = stringResource(R.string.ui_d7e500da55),
                desc = stringResource(R.string.ui_0d27dc79d6),
                color = SunsetOrange,
                onClick = {
                    showJoinInput = true
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Text(
                text = stringResource(R.string.ui_4732924c6b),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.ui_ab465c45bd),
                fontSize = 14.sp,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            SimpleFormTextField(
                value = joinCode,
                onValueChange = { joinCode = it.uppercase().take(8) },
                placeholder = stringResource(R.string.ui_63d05307ca),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    onJoinTrip(joinCode) {
                        joinCode = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                enabled = !isJoining && joinCode.length == 8,
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
            val coverImageUrl = trip.coverImageUrl?.takeIf { it.isNotBlank() }
            if (coverImageUrl != null) {
                AsyncImage(
                    model = coverImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
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
