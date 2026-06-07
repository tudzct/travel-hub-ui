package com.mobile.travelhub.ui.screens

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
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
import com.mobile.travelhub.viewmodels.GroupDetailViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage

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
    var showItinerarySheet by remember { mutableStateOf(false) }
    var showManageMembersDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<com.mobile.travelhub.viewmodels.GroupMemberUiModel?>(null) }
    val isLeader = uiState.myRole.equals("LEADER", ignoreCase = true)
    val isCompleted = uiState.isCompleted
    val pendingRequestCount = uiState.joinRequests.size
    val isInitialLoading = uiState.isLoading && uiState.groupName.isBlank()
    val showInitialError = !uiState.isLoading && uiState.groupName.isBlank() && uiState.errorMessage != null

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
            .background(SurfaceBg)
    ) {
        if (!showInitialError) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            if (isInitialLoading) {
                GroupDetailHeaderSkeleton()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    val coverImageUrl = uiState.coverImageUrl?.takeIf { it.isNotBlank() }
                    val images = remember(coverImageUrl, uiState.placeImages) {
                        val list = mutableListOf<String>()
                        if (coverImageUrl != null) {
                            list.add(coverImageUrl)
                        }
                        uiState.placeImages.forEach { img ->
                            if (img != coverImageUrl && img.isNotBlank()) {
                                list.add(img)
                            }
                        }
                        list.distinct()
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
                                    .padding(bottom = 16.dp)
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
                            .padding(24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.groupName.ifBlank { groupName },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp,
                            color = Color.White,
                            lineHeight = 40.sp,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = Color.White.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.location.ifBlank { "Đang tải điểm đến" },
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isInitialLoading) {
                    item { FeatureCardSkeleton() }
                    item { FeatureCardSkeleton() }
                } else {
                    item { FeatureCard(Icons.Default.CalendarMonth, "Lịch trình", PrimaryBlue) { showItinerarySheet = true } }
                    item { FeatureCard(Icons.Default.Payments, "Chi phí", Color(0xFFE91E63), { onNavigateToCost(tripId) }) }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = stringResource(R.string.ui_c39635df84),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        if (isInitialLoading) {
                            TripDetailRowSkeleton()
                            HorizontalDivider(color = SurfaceContainerLow, modifier = Modifier.padding(vertical = 12.dp))
                            TripDetailRowSkeleton()
                            HorizontalDivider(color = SurfaceContainerLow, modifier = Modifier.padding(vertical = 12.dp))
                            TripDetailRowSkeleton()
                        } else {
                            TripDetailRow("Lịch trình", listOf(uiState.startDate, uiState.endDate).filter { it.isNotBlank() }.joinToString(" - ").ifBlank { "Chưa có từ API" })
                            HorizontalDivider(color = SurfaceContainerLow, modifier = Modifier.padding(vertical = 12.dp))
                            TripDetailRow("Số điểm dừng", uiState.totalStops.toString())
                            HorizontalDivider(color = SurfaceContainerLow, modifier = Modifier.padding(vertical = 12.dp))
                            TripDetailRow("Trạng thái", uiState.statusLabel.ifBlank { "Chưa xác định" })
                        }
                    }
                }



            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
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
                            fontSize = 20.sp,
                            color = OnSurface
                        )
                        if (!isInitialLoading) {
                            Text(
                                text = "(${uiState.members.size})",
                                color = OnSurfaceVariant,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.ui_b773dc5ed8),
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { showManageMembersDialog = true }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (isInitialLoading) {
                    GroupMembersSkeleton()
                } else if (uiState.members.isEmpty()) {
                    Text(
                        text = stringResource(R.string.ui_14518e1450),
                        color = OnSurfaceVariant,
                        fontSize = 13.sp
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.members) { member ->
                            MemberAvatarItem(
                                member = member,
                                onClick = { onNavigateToProfile(member.userId) }
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }

            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(
                    onClick = { showInviteMenu = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.MoreVert, "More", tint = Color.White)
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
                                Text(text = stringResource(R.string.ui_aa1d94fc16), color = SunsetOrange)
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
                            ) { Text(text = stringResource(R.string.ui_d354717258), color = SunsetOrange) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLeaveConfirm = false }) { Text(text = stringResource(R.string.ui_34ca764caf)) }
                        }
                    )
                }

                GroupDetailMoreMenu(
                    expanded = showInviteMenu,
                    onDismiss = { showInviteMenu = false },
                    uiState = uiState,
                    isLeader = isLeader,
                    isCompleted = isCompleted,
                    pendingRequestCount = pendingRequestCount,
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
                    onNavigateToProfile = onNavigateToProfile,
                    onLeaveGroupClick = { showLeaveConfirm = true },
                    onDeleteGroupClick = { showDeleteConfirm = true }
                )
            }
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
    }
}

