package com.mobile.travelhub.ui.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
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
import coil.compose.AsyncImage
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.components.modifiers.shimmerEffect
import com.mobile.travelhub.ui.theme.*
import com.mobile.travelhub.viewmodels.GroupDetailUiState
import com.mobile.travelhub.viewmodels.GroupJoinRequestUiModel
import com.mobile.travelhub.viewmodels.GroupMemberUiModel

@Composable
fun FeatureCard(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
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
fun GroupDetailInitialErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.ui_28a4aafc8b),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message.ifBlank { "Vui lòng thử lại sau." },
            fontSize = 14.sp,
            color = OnSurfaceVariant,
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
                .background(PrimaryBlue.copy(alpha = 0.12f))
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
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerLowest)
                    .clickable { onReject() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.ui_63bbfd75f6),
                    tint = SunsetOrange,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.12f))
                    .clickable { onApprove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.ui_ca41be9306),
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
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
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
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
                            color = OnSurface
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
                                        .background(PrimaryBlue.copy(alpha = 0.12f))
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
                                        color = OnSurface
                                    )
                                    val roleText = if (member.role.equals("LEADER", ignoreCase = true)) "Trưởng nhóm" else "Thành viên"
                                    val roleColor = if (member.role.equals("LEADER", ignoreCase = true)) SunsetOrange else OnSurfaceVariant
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
                                            tint = if (isRemovingMember) SunsetOrange.copy(alpha = 0.5f) else SunsetOrange
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
                            color = SunsetOrange
                        )
                        Text("Đang xóa thành viên...", color = OnSurface)
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
                        Text(stringResource(R.string.ui_aa1d94fc16), color = SunsetOrange, fontWeight = FontWeight.Bold)
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
            containerColor = SurfaceContainerLowest
        )
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
    onApproveJoinRequest: (Long, String) -> Unit,
    onRejectJoinRequest: (Long, String) -> Unit,
    onNavigateToProfile: (Long) -> Unit,
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
        containerColor = Color.White,
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
                color = OnSurfaceVariant,
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
                    color = OnSurface,
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
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.ui_7b208e75d2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                when {
                    uiState.isJoinRequestsLoading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InlineLoadingSkeleton(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.ui_438eeb013a),
                                fontSize = 14.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    uiState.joinRequests.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.ui_f193b502d6),
                            fontSize = 14.sp,
                            color = OnSurfaceVariant
                        )
                    }

                    else -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            uiState.joinRequests.forEach { request ->
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
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SurfaceContainerLow)
            }

            val role = uiState.myRole.orEmpty().uppercase()
            val canLeave = !isLeader && role.isNotBlank() && role != "NON_MEMBER" && role != "PENDING"
            if (canLeave) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ui_4bb91c8b42), color = SunsetOrange) },
                    leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = SunsetOrange) },
                    onClick = {
                        onDismiss()
                        onLeaveGroupClick()
                    }
                )
            }

            if (isLeader) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ui_a4564eb2e2), color = if (isCompleted) Color.Gray.copy(alpha = 0.5f) else SunsetOrange) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = if (isCompleted) Color.Gray.copy(alpha = 0.5f) else SunsetOrange) },
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
