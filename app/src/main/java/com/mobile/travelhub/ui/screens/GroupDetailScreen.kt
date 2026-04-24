package com.mobile.travelhub.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.theme.*

enum class GroupRole { LEADER, NON_MEMBER, PENDING }

@Composable
fun GroupDetailScreen(
    groupName: String,
    onBack: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToItinerary: () -> Unit,
    onNavigateToDiscovery: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToCost: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var userRole by remember { mutableStateOf(GroupRole.LEADER) }

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
                            .clip(RoundedCornerShape(24.dp))
                            .background(SunsetOrange)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Còn 12 ngày nữa", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = groupName,
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
                        Text("Tokyo, Japan", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { FeatureCard(Icons.Default.CalendarMonth, "Lịch trình", PrimaryBlue, onNavigateToItinerary) }
                item { FeatureCard(Icons.Default.Poll, "Bình chọn", SunsetOrange, onNavigateToDiscovery) }
                item { FeatureCard(Icons.Default.Payments, "Chi phí", Color(0xFFE91E63), onNavigateToCost) }
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
                    text = "Tham gia cùng chúng tôi trong hành trình khám phá Tokyo. Khám phá các ngôi đền cổ, thưởng thức văn hóa ẩm thực và ngắm nhìn thành phố không ngủ.",
                    lineHeight = 24.sp,
                    fontSize = 15.sp,
                    color = OnSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        TripDetailRow("Lịch trình", "12 Th10 - 20 Th10, 2024")
                        HorizontalDivider(color = SurfaceContainerLow, modifier = Modifier.padding(vertical = 12.dp))
                        TripDetailRow("Ngân sách dự kiến", "$1,200 - $1,500 / người")
                        HorizontalDivider(color = SurfaceContainerLow, modifier = Modifier.padding(vertical = 12.dp))
                        TripDetailRow("Trạng thái", "Sắp khởi hành (12 Th10)")
                    }
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
                    items(3) { index ->
                        val places = listOf("Senso-ji Temple", "Tsukiji Market", "Shibuya Sky")
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
                                text = places[index],
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
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thành viên tham gia", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = OnSurface)
                    Text("4/10", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(start = (index * 36).dp)
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerLowest)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = "Member",
                                    modifier = Modifier.fillMaxSize().background(SurfaceContainerLow),
                                    contentScale = ContentScale.Crop
                                )
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
                ActivityItem("Alex đã bình chọn cho Ghibli Museum", "2 giờ trước")
                Spacer(modifier = Modifier.height(12.dp))
                ActivityItem("Bạn đã thêm lịch trình: Tsukiji Market", "5 giờ trước")
                Spacer(modifier = Modifier.height(12.dp))
                ActivityItem("Sarah đã thanh toán: Khách sạn", "1 ngày trước")
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (userRole == GroupRole.LEADER) {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "QUẢN LÝ NHÓM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLowest)
                            .clickable { Toast.makeText(context, "Xem yêu cầu tham gia", Toast.LENGTH_SHORT).show() }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonAdd, null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Yêu cầu tham gia", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SunsetOrange.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("2 mới", color = SunsetOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLowest)
                            .clickable { Toast.makeText(context, "Đã sao chép link mời: https://travelhub.com/invite", Toast.LENGTH_SHORT).show() }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Sao chép link mời", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
                        }
                    }
                }
            }

            if (userRole == GroupRole.NON_MEMBER) {
                Button(
                    onClick = { 
                        userRole = GroupRole.PENDING
                        Toast.makeText(context, "Đã gửi yêu cầu tham gia", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Xin tham gia nhóm", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else if (userRole == GroupRole.PENDING) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainerLow)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Đang chờ Trưởng nhóm phê duyệt", color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
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
            IconButton(
                onClick = {},
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.MoreVert, "More", tint = Color.White)
            }
        }
    }
}

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
