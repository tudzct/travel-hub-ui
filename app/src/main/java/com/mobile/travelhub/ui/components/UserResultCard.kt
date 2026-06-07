package com.mobile.travelhub.ui.components

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceContainer
import com.mobile.travelhub.R

@Composable
fun UserResultCard(
    name: String,
    username: String,
    avatarUrl: String?,
    followersCount: Int,
    isFollowing: Boolean,
    isFollowLoading: Boolean,
    isCurrentUser: Boolean = false,
    onFollowClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = name.takeIf { it.isNotBlank() } ?: username
    val metadata = buildString {
        append(formatFollowerCount(followersCount))
        append(" follower")
        if (followersCount != 1) append("s")
    }

    Column(
        modifier = modifier
            .width(156.dp)
            .height(188.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserResultAvatar(
            avatarUrl = avatarUrl,
            name = title
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            color = OnSurface,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = metadata,
            color = OnSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.weight(1f))
        if (isCurrentUser) {
            Surface(
                modifier = Modifier
                    .height(38.dp)
                    .widthIn(min = 104.dp),
                color = SurfaceContainer,
                shape = RoundedCornerShape(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.ui_905cb326c7),
                        color = OnSurface,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            UserResultFollowButton(
                isFollowing = isFollowing,
                isLoading = isFollowLoading,
                onClick = onFollowClick
            )
        }
    }
}

@Composable
fun UserResultCardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(156.dp)
            .height(188.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkeletonBlock(
            modifier = Modifier.size(66.dp),
            shape = CircleShape
        )
        Spacer(modifier = Modifier.height(12.dp))
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(16.dp),
            shape = RoundedCornerShape(6.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth(0.58f)
                .height(13.dp),
            shape = RoundedCornerShape(6.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun UserResultAvatar(
    avatarUrl: String?,
    name: String
) {
    Box(
        modifier = Modifier
            .size(66.dp)
            .clip(CircleShape)
            .border(2.dp, PrimaryBlue, CircleShape)
            .padding(3.dp)
            .clip(CircleShape)
            .background(Color(0xFFEFF2FA)),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(34.dp)
            )
        } else {
            AsyncImage(
                model = avatarUrl,
                contentDescription = stringResource(R.string.avatar_description, name),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun UserResultFollowButton(
    isFollowing: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowing) SurfaceContainer else PrimaryBlue,
            contentColor = if (isFollowing) OnSurface else Color.White,
            disabledContainerColor = if (isFollowing) {
                SurfaceContainer
            } else {
                PrimaryBlue.copy(alpha = 0.62f)
            },
            disabledContentColor = if (isFollowing) {
                OnSurfaceVariant
            } else {
                Color.White.copy(alpha = 0.82f)
            }
        ),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
        modifier = Modifier
            .height(38.dp)
            .widthIn(min = 104.dp)
    ) {
        Text(
            text = when {
                isLoading -> "..."
                isFollowing -> "Following"
                else -> "Follow"
            },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

private fun formatFollowerCount(count: Int): String {
    val safeCount = count.coerceAtLeast(0)
    return when {
        safeCount >= 1_000_000 -> {
            val millions = safeCount / 1_000_000f
            "${"%.1f".format(millions).trimEnd('0').trimEnd('.')}M"
        }
        safeCount >= 1_000 -> {
            val thousands = safeCount / 1_000f
            "${"%.1f".format(thousands).trimEnd('0').trimEnd('.')}K"
        }
        else -> safeCount.toString()
    }
}
