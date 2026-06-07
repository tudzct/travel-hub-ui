package com.mobile.travelhub.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.mobile.travelhub.ui.theme.SurfaceContainerLowest
import com.mobile.travelhub.ui.components.modifiers.shimmerEffect

@Composable
fun SkeletonBlock(
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmerEffect()
    )
}

@Composable
fun InlineLoadingSkeleton(
    modifier: Modifier = Modifier.size(24.dp)
) {
    SkeletonBlock(
        modifier = modifier,
        shape = CircleShape
    )
}

@Composable
fun LoadingListSkeleton(
    modifier: Modifier = Modifier.fillMaxWidth(),
    itemCount: Int = 5
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        repeat(itemCount) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBlock(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.58f)
                            .height(14.dp)
                    )
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.86f)
                            .height(11.dp)
                    )
                }
                SkeletonBlock(
                    modifier = Modifier
                        .width(56.dp)
                        .height(28.dp),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    }
}

@Composable
fun LoadingContentSkeleton(
    modifier: Modifier = Modifier.fillMaxSize()
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(18.dp)
        )
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth(0.68f)
                .height(22.dp)
        )
        repeat(3) {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(if (it == 2) 0.72f else 1f)
                    .height(14.dp)
            )
        }
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun EditProfileLoadingSkeleton(
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar circle skeleton
        Box(
            modifier = Modifier
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            SkeletonBlock(
                modifier = Modifier.size(100.dp),
                shape = CircleShape
            )
        }
        
        // 5 field skeletons (Name, Username, Bio, Location, Email)
        repeat(5) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // Label skeleton
                SkeletonBlock(
                    modifier = Modifier
                        .width(80.dp)
                        .height(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Text field skeleton
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

// --- Atomic Skeletons ---

@Composable
fun ImageTextRowSkeleton(
    modifier: Modifier = Modifier,
    imageSize: Dp = 48.dp,
    imageShape: Shape = RoundedCornerShape(8.dp),
    textLines: List<Dp> = listOf(120.dp, 80.dp),
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonBlock(modifier = Modifier.size(imageSize), shape = imageShape)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            textLines.forEach { width ->
                SkeletonBlock(modifier = Modifier.width(width).height(12.dp))
            }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailingContent()
        }
    }
}

@Composable
fun AvatarWithTextsSkeleton(
    modifier: Modifier = Modifier,
    avatarSize: Dp = 44.dp,
    textLines: List<Dp> = emptyList()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        SkeletonBlock(modifier = Modifier.size(avatarSize), shape = CircleShape)
        textLines.forEach { width ->
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBlock(modifier = Modifier.width(width).height(10.dp), shape = RoundedCornerShape(4.dp))
        }
    }
}

@Composable
fun CardWithTextsSkeleton(
    modifier: Modifier = Modifier,
    cardSize: Dp = 130.dp,
    cardShape: Shape = RoundedCornerShape(24.dp),
    textLines: List<Dp> = listOf(90.dp, 60.dp)
) {
    Column(modifier = modifier) {
        SkeletonBlock(modifier = Modifier.size(cardSize), shape = cardShape)
        textLines.forEach { width ->
            Spacer(modifier = Modifier.height(12.dp))
            SkeletonBlock(modifier = Modifier.width(width).height(12.dp), shape = RoundedCornerShape(4.dp))
        }
    }
}

// --- Screen Specific Skeletons ---

fun LazyListScope.costEstimateLoadingSkeleton() {
    // Skeleton Budget Card
    item {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Skeleton Contributions Label
    item {
        SkeletonBlock(
            modifier = Modifier
                .width(100.dp)
                .height(16.dp),
            shape = RoundedCornerShape(4.dp)
        )
    }

    // Skeleton Contributions list
    item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                AvatarWithTextsSkeleton(
                    avatarSize = 40.dp,
                    textLines = listOf(48.dp, 56.dp),
                    modifier = Modifier
                        .width(90.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainerLowest)
                        .padding(vertical = 12.dp)
                )
            }
        }
    }

    // Skeleton Recent Expenses Label
    item {
        SkeletonBlock(
            modifier = Modifier
                .width(140.dp)
                .height(16.dp),
            shape = RoundedCornerShape(4.dp)
        )
    }

    // Skeleton Transactions list
    items(3) {
        ImageTextRowSkeleton(
            imageSize = 44.dp,
            imageShape = RoundedCornerShape(12.dp),
            textLines = listOf(120.dp, 80.dp),
            trailingContent = {
                SkeletonBlock(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceContainerLowest)
                .padding(16.dp)
        )
    }
}

@Composable
fun ActiveTripSkeleton(
    modifier: Modifier = Modifier
) {
    SkeletonBlock(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(32.dp)
    )
}

@Composable
fun UpcomingTripsSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(2) {
            ImageTextRowSkeleton(
                imageSize = 70.dp,
                imageShape = RoundedCornerShape(16.dp),
                textLines = listOf(140.dp, 80.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainerLowest)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
fun PastMemoriesSkeleton(
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(3) {
            CardWithTextsSkeleton(
                cardSize = 130.dp,
                textLines = listOf(90.dp, 60.dp),
                modifier = Modifier.width(130.dp)
            )
        }
    }
}

@Composable
fun GroupDetailHeaderSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
    ) {
        // Image background shimmer
        SkeletonBlock(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(0.dp)
        )
        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )
        // Info shimmers at bottom start
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            SkeletonBlock(
                modifier = Modifier
                    .width(96.dp)
                    .height(24.dp),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            SkeletonBlock(
                modifier = Modifier
                    .width(260.dp)
                    .height(42.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBlock(
                    modifier = Modifier.size(16.dp),
                    shape = CircleShape
                )
                Spacer(modifier = Modifier.width(6.dp))
                SkeletonBlock(
                    modifier = Modifier
                        .width(180.dp)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }
    }
}

@Composable
fun GroupMembersSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) {
            AvatarWithTextsSkeleton(
                avatarSize = 44.dp
            )
        }
    }
}

@Composable
fun FeatureCardSkeleton() {
    Column(
        modifier = Modifier
            .width(90.dp)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )

        Box(
            modifier = Modifier
                .width(60.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}

@Composable
fun TripDetailRowSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(88.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}
