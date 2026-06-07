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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
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
import com.mobile.travelhub.ui.components.ActiveTripSkeleton
import com.mobile.travelhub.ui.components.UpcomingTripsSkeleton
import com.mobile.travelhub.ui.components.PastMemoriesSkeleton
import com.mobile.travelhub.ui.components.SimpleFormTextField
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.theme.*
import com.mobile.travelhub.viewmodels.TripsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter



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
    var hasReachedInitialResume by remember(lifecycleOwner) {
        mutableStateOf(
            lifecycleOwner.lifecycle.currentState.isAtLeast(
                androidx.lifecycle.Lifecycle.State.RESUMED
            )
        )
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (hasReachedInitialResume) {
                    viewModel.refreshDashboard()
                } else {
                    hasReachedInitialResume = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.refreshDashboard(isSilent = false)
        while (true) {
            kotlinx.coroutines.delay(10000L)
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
                shape = RoundedCornerShape(30.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .height(64.dp)
                    .padding(end = 4.dp)
            ) {
                Icon(
                    Icons.Default.FlightTakeoff,
                    contentDescription = stringResource(R.string.ui_77c36e7640),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    stringResource(R.string.ui_ab38c564dc),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = 100.dp 
            )
        ) {
            if (state.errorMessage != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(
                            text = state.errorMessage.orEmpty(),
                            color = SunsetOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Personalized Header
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp)) {
                    Text(
                        text = stringResource(R.string.ui_8f23352824),
                        color = OnSurfaceVariant,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.ui_5a3d42db37),
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface,
                        fontSize = 38.sp,
                        lineHeight = 44.sp
                    )
                }
            }

            // Current Active Trip (More immersive)
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        stringResource(R.string.ui_0811963e56),
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    if (state.isLoading && state.activeTrip == null) {
                        ActiveTripSkeleton()
                    } else {
                        ActiveJourneyCardV2(
                            trip = state.activeTrip,
                            onNavigateToGroupDetail = onNavigateToGroupDetail
                        )
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
            
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.ui_9183bdeb31),
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurface,
                            fontSize = 22.sp
                        )
                        Text(
                            text = stringResource(R.string.ui_7e04025452),
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onNavigateToUpcomingTrips)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    if (state.isLoading && state.upcomingTrips.isEmpty()) {
                        UpcomingTripsSkeleton()
                    } else if (state.upcomingTrips.isEmpty()) {
                        Text(
                            text = stringResource(R.string.ui_a5bb058cec),
                            color = OnSurfaceVariant
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
            item { Spacer(modifier = Modifier.height(32.dp)) }

            // Past Memories Section
            item {
                Column {
                    Text(
                        text = stringResource(R.string.ui_f593bb6075),
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (state.isLoading && state.pastTrips.isEmpty()) {
                        PastMemoriesSkeleton()
                    } else if (state.pastTrips.isEmpty()) {
                        EmptyJourneyJournalCard(
                            modifier = Modifier.padding(horizontal = 20.dp)
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
        shape = RoundedCornerShape(20.dp),
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
                .height(248.dp)
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
                val imageRes = if (trip == null) {
                    R.drawable.img_no_trip
                } else {
                    R.drawable.ic_launcher_background
                }
                Image(
                    painter = painterResource(id = imageRes),
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
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.76f)
                            )
                        )
                    )
            )

            // Info bottom left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 18.dp, end = 18.dp, bottom = 22.dp)
            ) {
                Text(
                    text = trip?.name ?: "Chưa có chuyến đi diễn ra",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        trip?.location?.takeIf { it.isNotBlank() }
                            ?: "Hãy lên kế hoạch cho chuyến đi tiếp theo của bạn!",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
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
        Text(place, fontWeight = FontWeight.ExtraBold, color = OnSurface)
        Text(cleanDate, color = OnSurfaceVariant)
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
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.ui_ab465c45bd),
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                )
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
            Text(title, fontWeight = FontWeight.Bold, color = OnSurface)
            Text(desc, color = OnSurfaceVariant)
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
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
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
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = trip.name,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (trip.daysLeft > 0) {
                        "Còn ${trip.daysLeft} ngày nữa"
                    } else {
                        trip.startDate?.let { "Bắt đầu $it" } ?: "Đang chờ ngày khởi hành"
                    },
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
private fun EmptyJourneyJournalCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceContainerLowest)
            .padding(horizontal = 24.dp, vertical = 26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_no_trip),
            contentDescription = null,
            modifier = Modifier.size(width = 136.dp, height = 88.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Chưa có chuyến đi",
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface
            )
        }
    }
}
