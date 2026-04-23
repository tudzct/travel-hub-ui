package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDiscoveryScreen(
    onBack: () -> Unit
) {
    var showAddOptions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cộng tác & Bình chọn",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBg)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    "Đang đề xuất",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }

            items(3) { index ->
                val places = listOf("Ghibli Museum", "Mt. Fuji Day Tour", "Shinjuku Gyoen")
                val votes = listOf(10, 8, 4)
                val total = 12
                VotingCard(
                    title = places[index],
                    votes = votes[index],
                    total = total,
                    isWinning = index == 0
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showAddOptions = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Thêm Đề xuất", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showAddOptions) {
            ModalBottomSheet(
                onDismissRequest = { showAddOptions = false },
                sheetState = sheetState,
                containerColor = SurfaceContainerLowest,
                dragHandle = { BottomSheetDefaults.DragHandle(color = SurfaceContainerLow) }
            ) {
                AddSuggestionContent(onDismiss = { showAddOptions = false })
            }
        }
    }
}

@Composable
fun AddSuggestionContent(onDismiss: () -> Unit) {
    var suggestionText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        Text(
            text = "Thêm đề xuất mới",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OptionItem(
                icon = Icons.Default.Place,
                label = "Địa điểm",
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            OptionItem(
                icon = Icons.Default.Restaurant,
                label = "Ăn uống",
                color = SunsetOrange,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = suggestionText,
            onValueChange = { suggestionText = it },
            label = { Text("Tên địa điểm / hoạt động") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = SurfaceContainerLow
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Gửi đề xuất", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OptionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLow)
            .clickable { }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnSurface)
    }
}

@Composable
fun VotingCard(title: String, votes: Int, total: Int, isWinning: Boolean) {
    val progress = votes.toFloat() / total.toFloat()
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = OnSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$votes bình chọn", fontSize = 13.sp, color = OnSurfaceVariant)
                }
                
                if (isWinning) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.1f))
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerLow)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(if (isWinning) PrimaryBlue else PrimaryBlue.copy(alpha = 0.4f))
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerLow)
                        ) {
                            Image(painterResource(id = R.drawable.ic_launcher_foreground), null)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Đã bình chọn", fontSize = 11.sp, color = OnSurfaceVariant)
            }
        }
    }
}
