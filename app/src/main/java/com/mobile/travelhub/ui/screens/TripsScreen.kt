package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    onNavigateToCreateGroup: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {}
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
                    viewModel.refreshTripsScreen()
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
        viewModel.refreshTripsScreen(isSilent = false)
        while (true) {
            kotlinx.coroutines.delay(10000L)
            viewModel.refreshDashboard(isSilent = true)
        }
    }
    val listState = rememberLazyListState()
    val shouldLoadMorePastTrips by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            totalItems > 0 && lastVisibleIndex >= totalItems - 2
        }
    }
    LaunchedEffect(
        shouldLoadMorePastTrips,
        state.pastTrips.size,
        state.pastTripsHasMore,
        state.isPastTripsLoading,
        state.isPastTripsLoadingMore
    ) {
        if (
            shouldLoadMorePastTrips &&
            state.pastTripsHasMore &&
            !state.isPastTripsLoading &&
            !state.isPastTripsLoadingMore
        ) {
            viewModel.loadMorePastTrips()
        }
    }
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
    var bankAccountPromptMessage by remember { mutableStateOf<String?>(null) }
    var joinSheetErrorMessage by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = SurfaceBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTripSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                ),
                modifier = Modifier
                    .height(76.dp)
                    .padding(end = 4.dp, bottom = 20.dp)
            ) {
                Icon(
                    Icons.Default.FlightTakeoff,
                    contentDescription = stringResource(R.string.ui_77c36e7640),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    stringResource(R.string.ui_ab38c564dc),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
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
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                    Text(
                        text = stringResource(R.string.ui_8f23352824),
                        color = OnSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.ui_5a3d42db37),
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurface,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "✦",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            // Current Active Trip (More immersive)
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        stringResource(R.string.ui_0811963e56),
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface,
                        style = MaterialTheme.typography.titleMedium
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
                Spacer(modifier = Modifier.height(32.dp))
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
                            color = OnSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            modifier = Modifier.clickable(onClick = onNavigateToUpcomingTrips),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.ui_7e04025452),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    if (state.isLoading && state.upcomingTrips.isEmpty()) {
                        UpcomingTripsSkeleton()
                    } else if (state.upcomingTrips.isEmpty()) {
                        EmptyUpcomingTripCard(onClick = { showAddTripSheet = true })
                    } else {
                        UpcomingTripPreviewList(
                            trips = nearestUpcomingTrips(state.upcomingTrips),
                            onTripClick = { trip ->
                                onNavigateToGroupDetail(trip.tripId, trip.name)
                            }
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = stringResource(R.string.ui_f593bb6075),
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            if ((state.isLoading || state.isPastTripsLoading) && state.pastTrips.isEmpty()) {
                item {
                    PastMemoriesSkeleton()
                }
            } else if (state.pastTrips.isEmpty()) {
                item {
                    EmptyJourneyJournalCard(
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                itemsIndexed(
                    items = state.pastTrips,
                    key = { _, trip -> trip.tripId }
                ) { index, trip ->
                    JourneyJournalCardItem(
                        trip = trip,
                        modifier = Modifier.padding(
                            start = 24.dp,
                            end = 24.dp,
                            bottom = if (index == state.pastTrips.lastIndex) 0.dp else 10.dp
                        ),
                        onClick = {
                            onNavigateToGroupDetail(trip.tripId, trip.locationName)
                        }
                    )
                }
                if (state.isPastTripsLoadingMore) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Bottom Sheet for adding trips
        if (showAddTripSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddTripSheet = false
                    joinSheetErrorMessage = null
                },
                sheetState = sheetState,
                containerColor = SurfaceContainerLowest,
                dragHandle = { BottomSheetDefaults.DragHandle(color = SurfaceContainerLow) }
            ) {
                AddTripOptionsContent(
                    onDismiss = {
                        showAddTripSheet = false
                        joinSheetErrorMessage = null
                    },
                    onCreateNew = {
                        showAddTripSheet = false
                        onNavigateToCreateGroup()
                    },
                    onJoinTrip = { joinCode, onDone ->
                        joinSheetErrorMessage = null
                        viewModel.joinTrip(joinCode) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                joinSheetErrorMessage = null
                                onDone(true)
                                showAddTripSheet = false
                            } else if (message.requiresBankAccountUpdate()) {
                                joinSheetErrorMessage = null
                                bankAccountPromptMessage = message
                                onDone(true)
                                showAddTripSheet = false
                            } else {
                                joinSheetErrorMessage = message
                                onDone(false)
                            }
                        }
                    },
                    isJoining = state.isJoining,
                    joinErrorMessage = joinSheetErrorMessage,
                    onJoinErrorClear = { joinSheetErrorMessage = null }
                )
            }
        }

        if (bankAccountPromptMessage != null) {
            AlertDialog(
                onDismissRequest = { bankAccountPromptMessage = null },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = OnSurface,
                textContentColor = OnSurfaceVariant,
                title = {
                    Text(
                        text = "Cập nhật tài khoản ngân hàng",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    Text(
                        text = bankAccountPromptMessage
                            ?: "Bạn cần cập nhật tài khoản ngân hàng trước khi tham gia chuyến đi."
                    )
                },
                dismissButton = {
                    TextButton(onClick = { bankAccountPromptMessage = null }) {
                        Text("Hủy")
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            bankAccountPromptMessage = null
                            onNavigateToEditProfile()
                        }
                    ) {
                        Text(
                            text = "Cập nhật",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}


@Composable
fun ActiveJourneyCardV2(
    trip: com.mobile.travelhub.viewmodels.UpcomingTripUiModel?,
    onNavigateToGroupDetail: (Long, String) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val progress = remember(trip?.startDate, trip?.endDate) {
        calculateTripProgress(trip?.startDate, trip?.endDate)
    }
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = primary),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = trip != null) {
                trip?.let { onNavigateToGroupDetail(it.tripId, it.name) }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(primary)
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip?.name ?: "Chưa có chuyến đi diễn ra",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    val subtitle = trip?.location?.takeIf { it.isNotBlank() }
                        ?: "Hãy lên kế hoạch cho chuyến đi tiếp theo!"
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = subtitle,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = progress.dayLabel,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = progress.fraction,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "${(progress.fraction * 100).toInt()}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (trip != null) {
                    Surface(
                        modifier = Modifier
                            .height(48.dp)
                            .clickable {
                                onNavigateToGroupDetail(trip.tripId, trip.name)
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceContainerLowest
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Xem chi tiết",
                                color = primary,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyUpcomingTripCard(onClick: () -> Unit) {
    EmptyTripSectionCard(
        title = "Chưa có chuyến đi nào sắp tới.",
        description = "Hãy lên kế hoạch cho chuyến đi tiếp theo của bạn!",
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    )
}

@Composable
private fun EmptyTripSectionCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    ElevatedCard(
        modifier = cardModifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    color = OnSurface,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
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
    ElevatedCard(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(126.dp)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = place,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface,
                style = MaterialTheme.typography.titleSmall
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cleanDate,
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PrimaryMountainIllustration(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    primary.copy(alpha = 0.62f),
                    primary.copy(alpha = 0.38f),
                    primary.copy(alpha = 0.92f)
                )
            )
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawCircle(
                color = Color.White.copy(alpha = 0.58f),
                radius = w * 0.07f,
                center = androidx.compose.ui.geometry.Offset(w * 0.88f, h * 0.2f)
            )

            fun ridge(vararg points: Pair<Float, Float>) = Path().apply {
                moveTo(0f, h)
                points.forEachIndexed { index, point ->
                    val x = point.first * w
                    val y = point.second * h
                    if (index == 0) lineTo(x, y) else lineTo(x, y)
                }
                lineTo(w, h)
                close()
            }

            drawPath(
                path = ridge(
                    0f to 0.56f,
                    0.12f to 0.5f,
                    0.25f to 0.6f,
                    0.42f to 0.54f,
                    0.62f to 0.64f,
                    0.82f to 0.48f,
                    1f to 0.62f
                ),
                color = Color.White.copy(alpha = 0.2f)
            )
            drawPath(
                path = ridge(
                    0f to 0.72f,
                    0.2f to 0.58f,
                    0.38f to 0.72f,
                    0.56f to 0.6f,
                    0.75f to 0.72f,
                    0.92f to 0.58f,
                    1f to 0.66f
                ),
                color = primary.copy(alpha = 0.45f)
            )
            drawPath(
                path = ridge(
                    0f to 0.86f,
                    0.16f to 0.72f,
                    0.36f to 0.78f,
                    0.52f to 0.9f,
                    0.7f to 0.76f,
                    0.9f to 0.7f,
                    1f to 0.76f
                ),
                color = primary.copy(alpha = 0.86f)
            )
        }
    }
}

@Composable
fun AddTripOptionsContent(
    onDismiss: () -> Unit,
    onCreateNew: () -> Unit,
    onJoinTrip: (String, (Boolean) -> Unit) -> Unit,
    isJoining: Boolean,
    joinErrorMessage: String?,
    onJoinErrorClear: () -> Unit = {}
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

            if (!joinErrorMessage.isNullOrBlank()) {
                Text(
                    text = joinErrorMessage,
                    color = SunsetOrange,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            SimpleFormTextField(
                value = joinCode,
                onValueChange = {
                    joinCode = it.uppercase().take(8)
                    onJoinErrorClear()
                },
                placeholder = stringResource(R.string.ui_63d05307ca),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    onJoinTrip(joinCode) { shouldClearCode ->
                        if (shouldClearCode) {
                            joinCode = ""
                        }
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
            .padding(12.dp),
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
            .padding(horizontal = 12.dp, vertical = 12.dp),
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
                    style = MaterialTheme.typography.bodySmall
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
private fun UpcomingTripPreviewList(
    trips: List<com.mobile.travelhub.viewmodels.UpcomingTripUiModel>,
    onTripClick: (com.mobile.travelhub.viewmodels.UpcomingTripUiModel) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            trips.forEachIndexed { index, trip ->
                UpcomingTripCompactItem(
                    trip = trip,
                    onClick = { onTripClick(trip) }
                )
                if (index < trips.lastIndex) {
                    HorizontalDivider(color = SurfaceContainerLow)
                }
            }
        }
    }
}

@Composable
private fun UpcomingTripCompactItem(
    trip: com.mobile.travelhub.viewmodels.UpcomingTripUiModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateTile(dateText = trip.startDate)
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = trip.name,
                color = OnSurface,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleSmall
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (trip.daysLeft > 0) {
                        "Còn ${trip.daysLeft} ngày"
                    } else {
                        trip.startDate?.let { "Bắt đầu ${formatShortDate(it)}" }
                            ?: "Đang chờ ngày khởi hành"
                    },
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun DateTile(dateText: String?) {
    val date = remember(dateText) { parseLocalDate(dateText) }
    Box(
        modifier = Modifier
            .width(116.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        ?: "--/--/----",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text(
                text = date?.let { "(${weekdayLabel(it)})" } ?: "",
                color = OnSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun JourneyJournalList(
    trips: List<com.mobile.travelhub.viewmodels.PastTripUiModel>,
    modifier: Modifier = Modifier,
    onTripClick: (com.mobile.travelhub.viewmodels.PastTripUiModel) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            trips.forEachIndexed { index, trip ->
                JourneyJournalItem(
                    trip = trip,
                    onClick = { onTripClick(trip) }
                )
                if (index < trips.lastIndex) {
                    HorizontalDivider(
                        color = SurfaceContainerLow,
                        modifier = Modifier.padding(start = 116.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyJournalItem(
    trip: com.mobile.travelhub.viewmodels.PastTripUiModel,
    onClick: () -> Unit
) {
    val storageService = stringResource(R.string.storage_service)
        .trim()
        .trim('"')
        .trimEnd('/')
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageUrl = trip.imageUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { resolveTripImageUrl(it, storageService) }
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 88.dp, height = 70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerLow)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 88.dp, height = 70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerLow)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = trip.locationName,
                    color = OnSurface,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatPastTripDateRange(trip.dateString),
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun JourneyJournalCardItem(
    trip: com.mobile.travelhub.viewmodels.PastTripUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        JourneyJournalItem(
            trip = trip,
            onClick = onClick
        )
    }
}

@Composable
private fun EmptyJourneyJournalCard(modifier: Modifier = Modifier) {
    EmptyTripSectionCard(
        title = "Chưa có nhật ký hành trình.",
        description = "Các chuyến đi đã hoàn thành sẽ xuất hiện ở đây.",
        modifier = modifier
            .fillMaxWidth()
    )
}

private data class TripProgress(
    val dayLabel: String,
    val fraction: Float
)

private fun calculateTripProgress(startText: String?, endText: String?): TripProgress {
    val start = parseLocalDate(startText)
    val end = parseLocalDate(endText)
    if (start == null || end == null || end.isBefore(start)) {
        return TripProgress(dayLabel = "Chưa có lịch", fraction = 0f)
    }

    val totalDays = (ChronoUnit.DAYS.between(start, end) + 1).toInt().coerceAtLeast(1)
    val currentDay = (ChronoUnit.DAYS.between(start, LocalDate.now()) + 1)
        .toInt()
        .coerceIn(1, totalDays)
    return TripProgress(
        dayLabel = "Ngày $currentDay / $totalDays",
        fraction = currentDay.toFloat() / totalDays.toFloat()
    )
}

private fun parseLocalDate(dateText: String?): LocalDate? {
    if (dateText.isNullOrBlank()) return null
    val normalized = dateText.substringBefore("T").trim()
    val patterns = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()),
        DateTimeFormatter.ofPattern("d/M/yyyy", Locale.getDefault())
    )
    return patterns.firstNotNullOfOrNull { formatter ->
        runCatching { LocalDate.parse(normalized, formatter) }.getOrNull()
    }
}

private fun formatShortDate(dateText: String): String {
    return parseLocalDate(dateText)?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ?: dateText.substringBefore("T")
}

private fun weekdayLabel(date: LocalDate): String {
    return when (date.dayOfWeek.value) {
        1 -> "Thứ 2"
        2 -> "Thứ 3"
        3 -> "Thứ 4"
        4 -> "Thứ 5"
        5 -> "Thứ 6"
        6 -> "Thứ 7"
        else -> "CN"
    }
}

private fun formatPastTripDateRange(dateString: String): String {
    val parts = dateString
        .split(" - ", " – ", " to ")
        .map { it.trim() }
        .filter { it.isNotBlank() }
    if (parts.isEmpty()) {
        return dateString
    }
    return parts
        .map { part ->
            parseLocalDate(part)?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                ?: part.substringBefore("T")
        }
        .distinct()
        .joinToString(" - ")
}

private fun resolveTripImageUrl(rawUrl: String, storageService: String): String {
    val value = rawUrl.trim()
    if (value.startsWith("http://", ignoreCase = true) ||
        value.startsWith("https://", ignoreCase = true)
    ) {
        return value
    }
    return "$storageService/${value.trimStart('/')}"
}

private fun nearestUpcomingTrips(
    trips: List<com.mobile.travelhub.viewmodels.UpcomingTripUiModel>
): List<com.mobile.travelhub.viewmodels.UpcomingTripUiModel> {
    return trips
        .sortedWith(
            compareBy<com.mobile.travelhub.viewmodels.UpcomingTripUiModel> {
                parseLocalDate(it.startDate) ?: LocalDate.MAX
            }.thenBy { it.tripId }
        )
        .take(3)
}

private fun String.requiresBankAccountUpdate(): Boolean {
    return contains("ngân hàng", ignoreCase = true) ||
        contains("số tài khoản", ignoreCase = true) ||
        contains("bank account", ignoreCase = true)
}
