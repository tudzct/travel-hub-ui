package com.mobile.travelhub.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.theme.*
import com.mobile.travelhub.viewmodels.GroupDetailViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage

enum class GroupRole { LEADER, NON_MEMBER, PENDING }

@Composable
fun GroupDetailScreen(
    tripId: Long,
    groupName: String,
    onBack: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToItinerary: () -> Unit,
    onNavigateToDiscovery: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToCost: (Long) -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsState()
    var showInviteMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val isLeader = uiState.myRole == "LEADER"
    val pendingRequestCount = uiState.joinRequests.size

    LaunchedEffect(tripId, groupName) {
        viewModel.loadGroup(tripId = tripId, groupName = groupName)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

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
                    Box(
                        modifier = Modifier

                            .clip(RoundedCornerShape(8.dp))

                            .clip(RoundedCornerShape(24.dp))

                            .background(SunsetOrange)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = uiState.statusLabel.ifBlank { "Đang tải" },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
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
                            text = uiState.location.ifBlank { "Dashboard BE chưa có location" },
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { FeatureCard(Icons.Default.CalendarMonth, "Lịch trình", PrimaryBlue, onNavigateToItinerary) }
                item { FeatureCard(Icons.Default.Poll, "Bình chọn", SunsetOrange, onNavigateToDiscovery) }
                item { FeatureCard(Icons.Default.Payments, "Chi phí", Color(0xFFE91E63), { onNavigateToCost(tripId) }) }
                item { FeatureCard(Icons.Default.Map, "Bản đồ", Color(0xFF4CAF50), onNavigateToMap) }
                item { FeatureCard(Icons.AutoMirrored.Filled.Chat, "Chat nhóm", PrimaryContainer, onNavigateToChat) }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "VỀ CHUYẾN ĐI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (uiState.startDate.isNotBlank() || uiState.endDate.isNotBlank()) {
                        "Ngày đi: ${uiState.startDate.ifBlank { "?" }} - ${uiState.endDate.ifBlank { "?" }}"
                    } else {
                        "Backend hiện chưa trả mô tả trip chi tiết, nên màn này đang dùng dữ liệu dashboard + itinerary thật."
                    },
                    lineHeight = 24.sp,
                    fontSize = 15.sp,
                    color = OnSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        TripDetailRow("Lịch trình", listOf(uiState.startDate, uiState.endDate).filter { it.isNotBlank() }.joinToString(" - ").ifBlank { "Chưa có từ API" })
                        HorizontalDivider(color = SurfaceContainerLow, modifier = Modifier.padding(vertical = 12.dp))
                        TripDetailRow("Số chặng", uiState.totalStops.toString())
                        HorizontalDivider(color = SurfaceContainerLow, modifier = Modifier.padding(vertical = 12.dp))
                        TripDetailRow("Trạng thái", uiState.statusLabel.ifBlank { "Chưa xác định" })
                    }
                }

            Spacer(modifier = Modifier.height(32.dp))

            Column {
                Text(
                    text = "ĐIỂM ĐẾN NỔI BẬT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.days) { day ->
                        Box(
                            modifier = Modifier
                                .size(width = 140.dp, height = 180.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceContainerLow)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_background),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                        )
                                    )
                            )
                            Text(
                                text = day.firstStopTitles.firstOrNull() ?: day.label,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            )
                        }
                    }
                }
                if (uiState.days.isEmpty()) {
                    Text(
                        text = "Chưa có itinerary từ BE cho group này.",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thành viên tham gia", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = OnSurface)
                    Text(uiState.memberInfoLabel, color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "${uiState.members.size} thành viên",
                            color = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (uiState.members.isEmpty()) {
                            Text(
                                text = "Chưa có dữ liệu thành viên từ BE.",
                                color = OnSurfaceVariant,
                                fontSize = 13.sp
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uiState.members) { member ->
                                    MemberAvatarItem(member)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "HOẠT ĐỘNG GẦN ĐÂY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.recentActivities.isEmpty()) {
                    ActivityItem(uiState.activityLabel, "BE chưa có event feed")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        uiState.recentActivities.take(3).forEach { activity ->
                            ActivityItem(
                                activity.title,
                                activity.timestamp ?: activity.description ?: activity.actorName.orEmpty()
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
                            Text(text = "Xóa nhóm")
                        },
                        text = {
                            Text(text = "Bạn có chắc muốn xóa nhóm này không? Thao tác này không thể hoàn tác.")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteGroup(tripId) { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            showDeleteConfirm = false
                                            onBack()
                                        }
                                    }
                                }
                            ) {
                                Text(text = "Xóa", color = SunsetOrange)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDeleteConfirm = false }
                            ) {
                                Text(text = "Hủy")
                            }
                        }
                    )
                }

                DropdownMenu(
                    expanded = showInviteMenu,
                    onDismissRequest = { showInviteMenu = false },
                    modifier = Modifier.widthIn(min = 260.dp, max = 340.dp),
                    offset = DpOffset((-8).dp, 4.dp)
                ) {
                    val inviteCode = if (uiState.isInviteCodeLoading) {
                        "Đang tải..."
                    } else {
                        uiState.inviteCode ?: "Chưa có mã"
                    }

                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text = "Mã tham gia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = OnSurfaceVariant,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = inviteCode,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = OnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val currentInviteCode = uiState.inviteCode.orEmpty()
                                    if (currentInviteCode.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(currentInviteCode))
                                        Toast.makeText(context, "Đã sao chép mã tham gia", Toast.LENGTH_SHORT).show()
                                        showInviteMenu = false
                                    }
                                },
                                enabled = uiState.inviteCode != null && !uiState.isInviteCodeLoading
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy mã tham gia")
                            }
                        }

                        if (isLeader) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = SurfaceContainerLow)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Yêu cầu tham gia",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = OnSurfaceVariant,
                                letterSpacing = 0.6.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            when {
                                uiState.isJoinRequestsLoading -> {
                                    Text(
                                        text = "Đang tải yêu cầu...",
                                        fontSize = 14.sp,
                                        color = OnSurfaceVariant
                                    )
                                }
                                uiState.joinRequests.isEmpty() -> {
                                    Text(
                                        text = "Không có yêu cầu nào",
                                        fontSize = 14.sp,
                                        color = OnSurfaceVariant
                                    )
                                }
                                else -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        uiState.joinRequests.take(5).forEach { request ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(SurfaceContainerLow),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = request.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                                                        fontWeight = FontWeight.Bold,
                                                        color = OnSurface
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = request.name,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp,
                                                        color = OnSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = request.requestedAt?.takeIf { it.isNotBlank() } ?: "Đang chờ duyệt",
                                                        fontSize = 12.sp,
                                                        color = OnSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = SurfaceContainerLow)
                            Spacer(modifier = Modifier.height(8.dp))

                            DropdownMenuItem(
                                text = { Text("Xóa nhóm", color = SunsetOrange) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = SunsetOrange) },
                                onClick = {
                                    showInviteMenu = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable

fun FeatureCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(90.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = label,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = OnSurface,
            maxLines = 2

        )
    }
}

@Preview
@Composable
fun FeatureCardPreview(){
    FeatureCard(Icons.Default.CalendarMonth, "Lịch trình", PrimaryBlue, {})

}

@Composable
fun TripDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, color = OnSurface, fontSize = 14.sp)
    }
}

@Composable
fun ActivityItem(text: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(PrimaryBlue)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(time, fontSize = 12.sp, color = OnSurfaceVariant)
        }
    }
}

@Composable
fun MemberAvatarItem(member: com.mobile.travelhub.viewmodels.GroupMemberUiModel) {
    Column(
        modifier = Modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            if (!member.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = member.avatarUrl,
                    contentDescription = member.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = member.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = member.name,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = OnSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
