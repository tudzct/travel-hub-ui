package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.travelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Bản đồ Lộ trình",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceBg.copy(alpha = 0.8f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Map Placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Google Maps Integration",
                    color = OnSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            // Floating elements
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = { },
                    containerColor = SurfaceContainerLowest,
                    contentColor = PrimaryBlue,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.MyLocation, null)
                }
                FloatingActionButton(
                    onClick = { },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.NearMe, null)
                }
            }

            // Bottom Navigation Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚀", fontSize = 24.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Đang di chuyển tới", fontSize = 12.sp, color = OnSurfaceVariant)
                        Text("Shinjuku Gyoen", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = OnSurface)
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text("15 min", fontWeight = FontWeight.Bold, color = SunsetOrange)
                        Text("2.4 km", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }
            }
        }
    }
}
