package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ArrowBack


import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsRailway

import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.travelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(onBack: () -> Unit) {
    var selectedDay by remember { mutableIntStateOf(1) }
    
    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch trình Chi tiết",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Day Selector
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) { index ->
                    val dayNum = index + 1
                    val isSelected = selectedDay == dayNum
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) PrimaryBlue else SurfaceContainerLowest)
                            .clickable { selectedDay = dayNum }
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Ngày $dayNum",
                            color = if (isSelected) Color.White else OnSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Timeline
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp)
            ) {
                item {
                    TimelineNode(
                        time = "09:00 AM",
                        title = "Senso-ji Temple",
                        desc = "Thăm quan ngôi đền cổ nhất Tokyo",
                        duration = "Ở lại khoảng 2.5 giờ",
                        cost = "Free",

                        transportToNext = "🚇 Tàu điện ngầm Ginza Line (15 phút)",

                        transportToNext = "Tàu điện ngầm Ginza Line (15 phút)",
                        transportIcon = Icons.Default.DirectionsRailway,

                        isFirst = true
                    )
                }
                item {
                    TimelineNode(
                        time = "11:45 AM",
                        title = "Sushi Dai",
                        desc = "Ăn trưa tại khu vực chợ cá Tsukiji. Cần lấy số trước.",
                        duration = "Ăn trưa 1.5 giờ",
                        cost = "Dự kiến: $45.00/người",

                        transportToNext = "🚶 Đi bộ dọc khu phố (10 phút)",

                        transportToNext = "Đi bộ dọc khu phố (10 phút)",
                        transportIcon = Icons.AutoMirrored.Filled.DirectionsWalk,

                        isHighlight = true
                    )
                }
                item {
                    TimelineNode(
                        time = "01:30 PM",
                        title = "Akihabara",
                        desc = "Khám phá trung tâm điện tử và văn hóa anime",
                        duration = "Tham quan tự do 3 giờ",
                        cost = "Variable",

                        transportToNext = "🚌 Xe buýt trung tâm (20 phút)"

                        transportToNext = "Xe buýt trung tâm (20 phút)",
                        transportIcon = Icons.Default.DirectionsBus

                    )
                }
                item {
                    TimelineNode(
                        time = "05:00 PM",
                        title = "Shibuya Sky",
                        desc = "Ngắm hoàng hôn từ đỉnh tòa nhà. Đã đặt vé trước.",
                        duration = "Tham quan & Chụp ảnh 2 giờ",
                        cost = "Đã thanh toán: $15.00",
                        isLast = true
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
fun TimelineNode(
    time: String,
    title: String,
    desc: String,
    duration: String = "",
    cost: String = "",
    transportToNext: String = "",


    transportIcon: ImageVector = Icons.Default.DirectionsTransit,

    isFirst: Boolean = false,
    isLast: Boolean = false,
    isHighlight: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Left Column: Time & Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            Text(
                time.split(" ")[0],
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = OnSurface
            )
            Text(
                time.split(" ")[1],
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OnSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isHighlight) SunsetOrange else PrimaryBlue)
            )
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f) // Fill remaining height
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    if (isHighlight) SunsetOrange else PrimaryBlue,
                                    SurfaceContainerLow
                                )
                            )
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Right Column: Card & Transport
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 24.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isHighlight) PrimaryBlue.copy(alpha = 0.05f) else SurfaceContainerLowest
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = OnSurface)
                            if (isHighlight) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp), tint = SunsetOrange)
                            }
                        }
                        
                        if (cost.isNotEmpty()) {
                            Text(
                                text = cost,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PrimaryBlue
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (duration.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(duration, fontSize = 12.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(desc, fontSize = 13.sp, color = OnSurface, lineHeight = 20.sp)
                    
                    if (isHighlight) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SunsetOrange.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Must Visit", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SunsetOrange)
                        }
                    }
                }
            }

            if (transportToNext.isNotEmpty() && !isLast) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(Icons.Default.DirectionsTransit, null, modifier = Modifier.size(18.dp), tint = OnSurfaceVariant)

                    Icon(transportIcon, null, modifier = Modifier.size(18.dp), tint = OnSurfaceVariant)

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = transportToNext,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
