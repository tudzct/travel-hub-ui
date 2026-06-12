package com.mobile.travelhub.ui.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.components.modifiers.shimmerEffect
import com.mobile.travelhub.ui.theme.*
import com.mobile.travelhub.viewmodels.GroupDetailUiState
import com.mobile.travelhub.viewmodels.GroupJoinRequestUiModel
import com.mobile.travelhub.viewmodels.GroupMemberUiModel

@Composable
fun FeatureCard(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = if (label == "Chi phí") "Ước tính chi tiết" else "Xem chi tiết",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TripDetailRow(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color = PrimaryBlue,
    trailingText: String? = null,
    pillText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint.copy(alpha = 0.9f),
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        if (pillText != null) {
            Text(
                text = pillText,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp
            )
            trailingText?.let {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            }
        } else {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TripMemberCard(
    member: GroupMemberUiModel,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TravelHubAvatar(
            avatarUrl = member.avatarUrl,
            contentDescription = member.name,
            fallbackName = member.name,
            modifier = Modifier.size(66.dp)
        )

        Spacer(modifier = Modifier.width(18.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (member.role.equals("LEADER", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "♛",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Trưởng nhóm",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun GroupDetailInitialErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.ui_28a4aafc8b),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message.ifBlank { "Vui lòng thử lại sau." },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        RetryButton(onClick = onRetry, filled = true)
    }
}

@Composable
fun JoinRequestActionItem(
    request: GroupJoinRequestUiModel,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onProfileClick: (Long) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .clickable { onProfileClick(request.userId) },
            contentAlignment = Alignment.Center
        ) {
            if (!request.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = request.avatarUrl,
                    contentDescription = request.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = request.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        }

        Column(modifier = Modifier.weight(1f).clickable { onProfileClick(request.userId) }) {
            Text(
                text = request.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onReject() },
                    contentAlignment = Alignment.Center
                ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.ui_63bbfd75f6),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .clickable { onApprove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.ui_ca41be9306),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun JoinRequestActionItemSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SkeletonBlock(
            modifier = Modifier.size(44.dp),
            shape = CircleShape
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBlock(
                modifier = Modifier
                    .width(156.dp)
                    .height(16.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBlock(
                modifier = Modifier.size(40.dp),
                shape = CircleShape
            )
            SkeletonBlock(
                modifier = Modifier.size(40.dp),
                shape = CircleShape
            )
        }
    }
}

@Composable
fun MemberAvatarItem(
    member: GroupMemberUiModel,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
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
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ManageMembersDialog(
    visible: Boolean,
    members: List<GroupMemberUiModel>,
    isLeader: Boolean,
    isCompleted: Boolean,
    isRemovingMember: Boolean,
    onDismiss: () -> Unit,
    onRemoveMember: (GroupMemberUiModel) -> Unit,
    onMemberClick: (Long) -> Unit
) {
    var memberToDelete by remember { mutableStateOf<GroupMemberUiModel?>(null) }
    val context = LocalContext.current

    if (visible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.ui_e91fb7a715),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.ui_d2b73ab2ad))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(members) { member ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .clickable { onMemberClick(member.userId) },
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

                                Column(modifier = Modifier.weight(1f).clickable { onMemberClick(member.userId) }) {
                                    Text(
                                        text = member.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val roleText = if (member.role.equals("LEADER", ignoreCase = true)) "Trưởng nhóm" else "Thành viên"
                                    val roleColor = if (member.role.equals("LEADER", ignoreCase = true)) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                    Text(
                                        text = roleText,
                                        fontSize = 12.sp,
                                        color = roleColor,
                                        fontWeight = if (member.role.equals("LEADER", ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                val isMemberLeader = member.role.equals("LEADER", ignoreCase = true)
                                if (isLeader && !isMemberLeader && !isCompleted) {
                                    IconButton(
                                        onClick = { memberToDelete = member },
                                        enabled = !isRemovingMember
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.ui_035849d3f3),
                                            tint = if (isRemovingMember) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { if (!isRemovingMember) memberToDelete = null },
            title = { Text(stringResource(R.string.ui_035849d3f3)) },
            text = {
                if (isRemovingMember) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text("Đang xóa thành viên...", color = MaterialTheme.colorScheme.onSurface)
                    }
                } else {
                    Text(
                        stringResource(
                            R.string.remove_member_confirmation,
                            memberToDelete?.name.orEmpty()
                        )
                    )
                }
            },
            confirmButton = {
                if (!isRemovingMember) {
                    TextButton(
                        onClick = {
                            val member = memberToDelete
                            if (member != null) {
                                onRemoveMember(member)
                                memberToDelete = null
                            }
                        }
                    ) {
                        Text(stringResource(R.string.ui_aa1d94fc16), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!isRemovingMember) {
                    TextButton(onClick = { memberToDelete = null }) {
                        Text(stringResource(R.string.ui_34ca764caf))
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun JoinRequestsDialog(
    visible: Boolean,
    requests: List<GroupJoinRequestUiModel>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onApproveJoinRequest: (Long, String) -> Unit,
    onRejectJoinRequest: (Long, String) -> Unit,
    onNavigateToProfile: (Long) -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(360.dp)
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 20.dp, end = 12.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.join_request_approval_title),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!isLoading && requests.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(
                                    R.string.pending_join_request_count,
                                    requests.size
                                ),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.ui_d2b73ab2ad)
                        )
                    }
                }

                HorizontalDivider(color = SurfaceContainerLow)

                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            repeat(3) {
                                JoinRequestActionItemSkeleton()
                            }
                        }
                    }

                    requests.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.ui_f193b502d6),
                            modifier = Modifier.padding(24.dp),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 480.dp),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = requests,
                                key = { request -> request.userId }
                            ) { request ->
                                JoinRequestActionItem(
                                    request = request,
                                    onApprove = {
                                        onApproveJoinRequest(request.userId, request.name)
                                    },
                                    onReject = {
                                        onRejectJoinRequest(request.userId, request.name)
                                    },
                                    onProfileClick = onNavigateToProfile
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupDetailMoreMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    uiState: GroupDetailUiState,
    isLeader: Boolean,
    isCompleted: Boolean,
    pendingRequestCount: Int,
    onOpenJoinRequests: () -> Unit,
    onLeaveGroupClick: () -> Unit,
    onDeleteGroupClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = 260.dp, max = 340.dp),
        offset = DpOffset((-8).dp, 4.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        val inviteCode = uiState.inviteCode?.takeIf { it.isNotBlank() }
        val inviteCodeText = if (uiState.isInviteCodeLoading) {
            "Đang tải..."
        } else if (inviteCode != null) {
            inviteCode
        } else {
            "Chưa có mã"
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = stringResource(R.string.ui_02ad5216fa),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = if (isCompleted) 0.38f else 1.0f },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = inviteCodeText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        val currentInviteCode = inviteCode.orEmpty()
                        if (currentInviteCode.isNotBlank()) {
                            clipboardManager.setText(AnnotatedString(currentInviteCode))
                            Toast.makeText(
                                context,
                                context.getString(R.string.join_code_copied),
                                Toast.LENGTH_SHORT
                            ).show()
                            onDismiss()
                        }
                    },
                    enabled = inviteCode != null && !uiState.isInviteCodeLoading && !isCompleted
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.ui_35accf0b7c))
                }
            }

            if (isLeader) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SurfaceContainerLow)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.join_request_approval_title),
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (pendingRequestCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text(text = pendingRequestCount.toString())
                            }
                        }
                    },
                    onClick = {
                        onDismiss()
                        onOpenJoinRequests()
                    }
                )
                HorizontalDivider(color = SurfaceContainerLow)
            }

            val role = uiState.myRole.orEmpty().uppercase()
            val canLeave = !isLeader && role.isNotBlank() && role != "NON_MEMBER" && role != "PENDING"
            if (canLeave) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ui_4bb91c8b42), color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error) },
                    enabled = !isCompleted,
                    onClick = {
                        onDismiss()
                        onLeaveGroupClick()
                    }
                )
            }

            if (isLeader) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ui_a4564eb2e2), color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error) },
                    enabled = !isCompleted,
                    onClick = {
                        onDismiss()
                        onDeleteGroupClick()
                    }
                )
            }
        }
    }
}
