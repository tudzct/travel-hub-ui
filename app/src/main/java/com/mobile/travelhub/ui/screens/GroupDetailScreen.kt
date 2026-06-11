package com.mobile.travelhub.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer


import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.components.*
import com.mobile.travelhub.ui.theme.*
import com.mobile.travelhub.viewmodels.GroupActivityLogUiModel
import com.mobile.travelhub.viewmodels.GroupDetailViewModel
import com.mobile.travelhub.viewmodels.TripPhotoUiModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class GroupRole { LEADER, NON_MEMBER, PENDING }

@Composable
fun GroupDetailScreen(
    tripId: Long,
    groupName: String,
    onBack: () -> Unit,
    onNavigateToCost: (Long) -> Unit,
    onNavigateToAssistant: (Long, String) -> Unit,
    onNavigateToProfile: (Long) -> Unit = {},
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isKickedOut) {
        if (uiState.isKickedOut) {
            Toast.makeText(
                context,
                context.getString(R.string.removed_from_group),
                Toast.LENGTH_LONG
            ).show()
            onBack()
        }
    }

    var showInviteMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showLeaveConfirm by remember { mutableStateOf(false) }
    var showFinishTripConfirm by remember { mutableStateOf(false) }
    var showItinerarySheet by remember { mutableStateOf(false) }
    var showManageMembersDialog by remember { mutableStateOf(false) }
    var showPublishPostDialog by remember { mutableStateOf(false) }
    var tripPostDescription by remember { mutableStateOf("") }
    var selectedTripPhotoUrl by remember { mutableStateOf<String?>(null) }
    var showJoinRequestsDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<com.mobile.travelhub.viewmodels.GroupMemberUiModel?>(null) }
    val isLeader = uiState.myRole.equals("LEADER", ignoreCase = true)
    val isCompleted = uiState.isCompleted
    val pendingRequestCount = uiState.joinRequests.size
    val isInitialLoading = uiState.isLoading && uiState.groupName.isBlank()
    val showInitialError = !uiState.isLoading && uiState.groupName.isBlank() && uiState.errorMessage != null
    val tripPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.uploadTripPhotos(uris) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(tripId, groupName) {
        viewModel.loadGroup(tripId = tripId, groupName = groupName, isSilent = false)
        while (true) {
            kotlinx.coroutines.delay(10000L)
            viewModel.loadGroup(tripId = tripId, groupName = groupName, isSilent = true)
        }
    }

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
                    viewModel.loadGroup(tripId = tripId, groupName = groupName)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!showInitialError) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            if (isInitialLoading) {
                GroupDetailHeaderSkeleton()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    val coverImageUrl = uiState.coverImageUrl?.trim()?.takeIf { it.isNotBlank() }
                    val images = remember(coverImageUrl, uiState.placeImages) {
                        buildList {
                            coverImageUrl?.let(::add)
                            uiState.placeImages
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .forEach(::add)
                        }.distinct()
                    }


                    if (images.isNotEmpty()) {
                        val topPagerState = rememberPagerState(
                            initialPage = 0,
                            pageCount = { images.size }
                        )
                        HorizontalPager(
                            state = topPagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = images[page],
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (images.size > 1) {
                            Row(
                                Modifier
                                    .wrapContentSize()
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 24.dp)
                                    .zIndex(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(images.size) { iteration ->
                                    val color = if (topPagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                }
                            }
                        }
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_background),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 54.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.groupName.ifBlank { groupName },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 23.sp,
                            color = Color.White,
                            lineHeight = 28.sp,
                            letterSpacing = 0.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = Color.White.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.location.ifBlank { "Đang tải điểm đến" },
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .offset(y = (-18).dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp)
                    .padding(top = 28.dp, bottom = 28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isInitialLoading) {
                        FeatureCardSkeleton(modifier = Modifier.weight(1f))
                        FeatureCardSkeleton(modifier = Modifier.weight(1f))
                    } else {
                        FeatureCard(
                            icon = Icons.Default.CalendarMonth,
                            label = "Lịch trình",
                            color = PrimaryBlue,
                            modifier = Modifier.weight(1f),
                            onClick = { showItinerarySheet = true }
                        )
                        FeatureCard(
                            icon = Icons.Default.Payments,
                            label = "Chi phí",
                            color = Color(0xFFE91E63),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToCost(tripId) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = stringResource(R.string.ui_c39635df84),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )

                Spacer(modifier = Modifier.height(18.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isInitialLoading) {
                        TripDetailRowSkeleton()
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                        TripDetailRowSkeleton()
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                        TripDetailRowSkeleton()
                    } else {
                        TripDetailRow(
                            label = "Lịch trình",
                            value = displayTripDateRange(uiState.startDate, uiState.endDate),
                            icon = Icons.Default.CalendarMonth
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                        TripDetailRow(
                            label = "Số điểm dừng",
                            value = uiState.totalStops.toString(),
                            icon = Icons.Default.Star
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                        TripDetailRow(
                            label = "Trạng thái",
                            value = uiState.statusLabel.ifBlank { "Chưa xác định" },
                            icon = Icons.Default.CardTravel,
                            pillText = displayTripStatus(uiState.statusLabel),
                            trailingText = tripProgressLabel(uiState.startDate, uiState.endDate)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "NHẬT KÝ HÀNH TRÌNH",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isInitialLoading) {
                    TimelineLogSectionSkeleton()
                } else {
                    JourneyLogTimeline(
                        activities = uiState.recentActivities,
                        progressLabel = ongoingTripDayLabel(uiState.startDate, uiState.endDate)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.ui_448ae61e1f),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.4.sp
                        )
                        if (!isInitialLoading) {
                            Text(
                                text = "(${uiState.members.size})",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.ui_b773dc5ed8),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { showManageMembersDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (isInitialLoading) {
                    GroupMembersSkeleton()
                } else if (uiState.members.isEmpty()) {
                    Text(
                        text = stringResource(R.string.ui_14518e1450),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                } else {
                    TripMemberCard(
                        member = uiState.members.first(),
                        onClick = { onNavigateToProfile(uiState.members.first().userId) }
                    )
                }

                if (!isInitialLoading) {
                    Spacer(modifier = Modifier.height(28.dp))
                    TripPhotosSection(
                        photos = uiState.tripPhotos,
                        isUploading = uiState.isUploadingTripPhotos,
                        onUploadClick = {
                            tripPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onPhotoClick = { imageUrl -> selectedTripPhotoUrl = imageUrl }
                    )
                }

                if (!isInitialLoading && isLeader && isCompleted) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            tripPostDescription = defaultTripPostDescription(uiState.groupName, uiState.location)
                            showPublishPostDialog = true
                        },
                        enabled = !uiState.isPublishingTripPost && uiState.tripPhotos.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                        )
                    ) {
                        if (uiState.isPublishingTripPost) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Article,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isPublishingTripPost) "Đang đăng bài..." else "Đăng bài về chuyến đi",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if (!isInitialLoading && isLeader && !isCompleted) {
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = { showFinishTripConfirm = true },
                        enabled = !uiState.isFinishingTrip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.42f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (uiState.isFinishingTrip) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (uiState.isFinishingTrip) "Đang kết thúc..." else "Kết thúc chuyến đi",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(21.dp)
                )
            }

            Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = { showInviteMenu = true },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            "More",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    if (isLeader && pendingRequestCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                                .align(Alignment.TopEnd)
                        )
                    }

                    GroupDetailMoreMenu(
                        expanded = showInviteMenu,
                        onDismiss = { showInviteMenu = false },
                        uiState = uiState,
                        isLeader = isLeader,
                        isCompleted = isCompleted,
                        pendingRequestCount = pendingRequestCount,
                        onOpenJoinRequests = { showJoinRequestsDialog = true },
                        onLeaveGroupClick = { showLeaveConfirm = true },
                        onDeleteGroupClick = { showDeleteConfirm = true }
                    )
                }
        }

                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = {
                            Text(text = stringResource(R.string.ui_a4564eb2e2))
                        },
                        text = {
                            Text(text = stringResource(R.string.ui_41518ec6bf))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (!isLeader) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.group_delete_forbidden),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        showDeleteConfirm = false
                                    } else {
                                        viewModel.deleteGroup(tripId) { success, message ->
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            if (success) {
                                                showDeleteConfirm = false
                                                onBack()
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text(text = stringResource(R.string.ui_aa1d94fc16), color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDeleteConfirm = false }
                            ) {
                                Text(text = stringResource(R.string.ui_34ca764caf))
                            }
                        }
                    )
                }

                if (showLeaveConfirm) {
                    AlertDialog(
                        onDismissRequest = { showLeaveConfirm = false },
                        title = { Text(text = stringResource(R.string.ui_4bb91c8b42)) },
                        text = { Text(text = stringResource(R.string.ui_8bab2c310d)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.leaveGroup { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            showLeaveConfirm = false
                                            onBack()
                                        }
                                    }
                                }
                            ) { Text(text = stringResource(R.string.ui_d354717258), color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLeaveConfirm = false }) { Text(text = stringResource(R.string.ui_34ca764caf)) }
                        }
                    )
                }

                if (showFinishTripConfirm) {
                    AlertDialog(
                        onDismissRequest = {
                            if (!uiState.isFinishingTrip) {
                                showFinishTripConfirm = false
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = { Text(text = "Kết thúc chuyến đi?") },
                        text = {
                            Text(
                                text = "Sau khi kết thúc, hệ thống sẽ tính các khoản cần thanh toán và chuyến đi không thể tiếp tục chỉnh sửa chi phí."
                            )
                        },
                        confirmButton = {
                            TextButton(
                                enabled = !uiState.isFinishingTrip,
                                onClick = {
                                    viewModel.finishTrip { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            showFinishTripConfirm = false
                                        }
                                    }
                                }
                            ) {
                                Text(text = "Kết thúc", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                enabled = !uiState.isFinishingTrip,
                                onClick = { showFinishTripConfirm = false }
                            ) {
                                Text(text = stringResource(R.string.ui_34ca764caf))
                            }
                        }
                    )
                }

        }

        if (showInitialError) {
            GroupDetailInitialErrorState(
                message = uiState.errorMessage.orEmpty(),
                onRetry = { viewModel.loadGroup(tripId = tripId, groupName = groupName) }
            )
        }

        if (showItinerarySheet) {
            ItineraryPopupSheet(
                tripId = tripId,
                groupName = groupName,
                onDismiss = { showItinerarySheet = false },
            )
        }

        ManageMembersDialog(
            visible = showManageMembersDialog,
            members = uiState.members,
            isLeader = isLeader,
            isCompleted = isCompleted,
            isRemovingMember = uiState.isRemovingMember,
            onDismiss = { showManageMembersDialog = false },
            onRemoveMember = { member ->
                viewModel.removeMember(member.userId) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            },
            onMemberClick = { userId -> onNavigateToProfile(userId) }
        )

        selectedTripPhotoUrl?.let { imageUrl ->
            FullTripPhotoDialog(
                imageUrl = imageUrl,
                onDismiss = { selectedTripPhotoUrl = null }
            )
        }

        if (showPublishPostDialog) {
            PublishTripPostDialog(
                description = tripPostDescription,
                isPublishing = uiState.isPublishingTripPost,
                onDescriptionChange = { tripPostDescription = it },
                onDismiss = {
                    if (!uiState.isPublishingTripPost) {
                        showPublishPostDialog = false
                    }
                },
                onConfirm = {
                    viewModel.publishTripPost(tripPostDescription) { success, message ->
                        if (success) {
                            showPublishPostDialog = false
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
        JoinRequestsDialog(
            visible = showJoinRequestsDialog,
            requests = uiState.joinRequests,
            isLoading = uiState.isJoinRequestsLoading,
            onDismiss = { showJoinRequestsDialog = false },
            onApproveJoinRequest = { userId, name ->
                viewModel.approveJoinRequest(userId)
                Toast.makeText(
                    context,
                    context.getString(R.string.join_request_accepted, name),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onRejectJoinRequest = { userId, name ->
                viewModel.rejectJoinRequest(userId)
                Toast.makeText(
                    context,
                    context.getString(R.string.join_request_rejected, name),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onNavigateToProfile = { userId ->
                showJoinRequestsDialog = false
                onNavigateToProfile(userId)
            }
        )
    }
}

private fun displayTripDateRange(startDate: String, endDate: String): String {
    val start = startDate.toVietnameseDate()
    val end = endDate.toVietnameseDate()
    return listOf(start, end).filter { it.isNotBlank() }.joinToString(" - ")
        .ifBlank { "Chưa có từ API" }
}

private fun daysUntilStartLabel(startDate: String): String? {
    val start = runCatching { LocalDate.parse(startDate) }.getOrNull() ?: return null
    val days = ChronoUnit.DAYS.between(LocalDate.now(), start)
    return when {
        days > 0 -> "Còn $days ngày"
        days == 0L -> "Hôm nay"
        else -> null
    }
}

private fun tripProgressLabel(startDate: String, endDate: String): String? {
    return ongoingTripDayLabel(startDate, endDate) ?: daysUntilStartLabel(startDate)
}

private fun ongoingTripDayLabel(startDate: String, endDate: String): String? {
    val start = runCatching { LocalDate.parse(startDate.substringBefore("T")) }.getOrNull() ?: return null
    val end = runCatching { LocalDate.parse(endDate.substringBefore("T")) }.getOrNull() ?: return null
    val today = LocalDate.now()
    if (today.isBefore(start) || today.isAfter(end)) {
        return null
    }
    val currentDay = ChronoUnit.DAYS.between(start, today) + 1
    val totalDays = ChronoUnit.DAYS.between(start, end) + 1
    return "Hôm nay là ngày $currentDay/$totalDays"
}

private fun displayTripStatus(statusLabel: String): String {
    return statusLabel
        .substringBefore("·")
        .trim()
        .ifBlank { "Chưa xác định" }
}

@Composable
private fun TripPhotosSection(
    photos: List<TripPhotoUiModel>,
    isUploading: Boolean,
    onUploadClick: () -> Unit,
    onPhotoClick: (String) -> Unit
) {
    val storageService = stringResource(R.string.storage_service)
        .trim()
        .trim('"')
        .trimEnd('/')

    fun toDisplayUrl(rawUrl: String): String {
        val value = rawUrl.trim()
        if (value.startsWith("http://", true) || value.startsWith("https://", true)) {
            return value
        }
        return "$storageService/${value.trimStart('/')}"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Ảnh chuyến đi",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "(${photos.size})",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            TextButton(
                onClick = onUploadClick,
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = if (isUploading) "Đang tải..." else "Thêm ảnh",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (photos.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Mọi thành viên có thể thêm ảnh kỷ niệm cho chuyến đi.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = photos,
                    key = { it.id }
                ) { photo ->
                    val resolvedUrl = toDisplayUrl(photo.imageUrl)
                    AsyncImage(
                        model = resolvedUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 112.dp, height = 88.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onPhotoClick(resolvedUrl) }
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun FullTripPhotoDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun PublishTripPostDialog(
    description: String,
    isPublishing: Boolean,
    onDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Article,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Đăng bài về chuyến đi",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Mô tả sẽ hiển thị cùng album ảnh chuyến đi.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isPublishing,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 132.dp),
                    enabled = !isPublishing,
                    minLines = 5,
                    maxLines = 8,
                    shape = RoundedCornerShape(18.dp),
                    placeholder = {
                        Text(
                            text = "Bạn muốn kể gì về chuyến đi này?",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isPublishing,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Hủy", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = !isPublishing && description.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                        )
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isPublishing) "Đang đăng" else "Đăng bài",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

private fun defaultTripPostDescription(groupName: String, location: String): String {
    return listOf(
        "Nhìn lại chuyến đi",
        groupName.takeIf { it.isNotBlank() },
        location.takeIf { it.isNotBlank() }?.let { "tại $it" }
    ).filterNotNull().joinToString(" ") + "."
}

private fun String.toVietnameseDate(): String {
    val date = runCatching { LocalDate.parse(this) }.getOrNull() ?: return this
    return "%02d/%02d/%04d".format(date.dayOfMonth, date.monthValue, date.year)
}

@Composable
private fun JourneyLogTimeline(
    activities: List<GroupActivityLogUiModel>,
    progressLabel: String?
) {
    if (activities.isEmpty()) {
        Text(
            text = "Chưa có nhật ký hành trình.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        progressLabel?.let { label ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PrimaryBlue.copy(alpha = 0.10f)
            ) {
                Text(
                    text = label,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }

        activities.forEachIndexed { index, activity ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.width(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    if (index < activities.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(64.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceContainerLowest
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = activity.actorName.ifBlank { "Thành viên trong chuyến đi" },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = activity.description.ifBlank { "Có cập nhật mới trong hành trình" },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = activity.createdAt.toTimelineTimestamp(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineLogSectionSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(14.dp)
                        .background(PrimaryBlue.copy(alpha = 0.25f), CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainerLow)
                )
            }
        }
    }
}

private fun String.toTimelineTimestamp(): String {
    val normalized = substringBeforeLast("Z").ifBlank { this }
    val parsed = runCatching { java.time.Instant.parse(this) }.getOrNull()
        ?: runCatching { java.time.LocalDateTime.parse(normalized) }.getOrNull()?.atZone(java.time.ZoneId.systemDefault())?.toInstant()
        ?: return this
    val dateTime = parsed.atZone(java.time.ZoneId.systemDefault())
    return "%02d/%02d/%04d %02d:%02d".format(
        dateTime.dayOfMonth,
        dateTime.monthValue,
        dateTime.year,
        dateTime.hour,
        dateTime.minute
    )
}
