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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
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
