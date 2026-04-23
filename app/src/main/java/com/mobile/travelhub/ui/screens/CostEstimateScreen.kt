package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
fun CostEstimateScreen(
    groupName: String = "Tokyo Trip",
    onBack: () -> Unit
) {
    var showAddExpense by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quản lý Chi phí",
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExpense = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Budget Summary Card
            item {
                BudgetSummaryCard(
                    totalSpent = 850.0,
                    budgetMax = 1500.0,
                    budgetMin = 1200.0
                )
            }

            // Individual Contributions
            item {
                Text(
                    text = "Đã chi trả bởi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = OnSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MemberExpenseCircle(name = "Me", amount = 450.0, color = PrimaryBlue)
                    MemberExpenseCircle(name = "Alex", amount = 200.0, color = SunsetOrange)
                    MemberExpenseCircle(name = "Sarah", amount = 200.0, color = Color(0xFF4CAF50))
                }
            }

            // Recent Expenses List
            item {
                Text(
                    text = "Giao dịch gần đây",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = OnSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            val recentExpenses = listOf(
                ExpenseItemData("Sushi Lunch", "Sarah", 120.0, "Food"),
                ExpenseItemData("Hotel Deposit", "Me", 400.0, "Stay"),
                ExpenseItemData("Train Tickets", "Alex", 200.0, "Transport"),
                ExpenseItemData("Museum Entry", "Me", 50.0, "Entry")
            )

            items(recentExpenses) { expense ->
                ExpenseRow(expense)
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        if (showAddExpense) {
            ModalBottomSheet(
                onDismissRequest = { showAddExpense = false },
                sheetState = sheetState,
                containerColor = SurfaceContainerLowest,
                dragHandle = { BottomSheetDefaults.DragHandle(color = SurfaceContainerLow) }
            ) {
                AddExpenseContent(onDismiss = { showAddExpense = false })
            }
        }
    }
}

@Composable
fun AddExpenseContent(onDismiss: () -> Unit) {
    var expenseTitle by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        Text(
            text = "Thêm khoản chi",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = expenseTitle,
            onValueChange = { expenseTitle = it },
            label = { Text("Tên khoản chi (VD: Ăn trưa, Vé tàu...)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = SurfaceContainerLow
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = expenseAmount,
            onValueChange = { expenseAmount = it },
            label = { Text("Số tiền ($)") },
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
            Text("Lưu chi phí", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BudgetSummaryCard(totalSpent: Double, budgetMin: Double, budgetMax: Double) {
    val progress = (totalSpent / budgetMax).toFloat()
    
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tổng chi tiêu", fontSize = 13.sp, color = OnSurfaceVariant)
                    Text("$${totalSpent.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = OnSurface)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SunsetOrange.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Ngân sách: $${budgetMin.toInt()}-$${budgetMax.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SunsetOrange)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = PrimaryBlue,
                trackColor = SurfaceContainerLow,
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Còn lại $${(budgetMax - totalSpent).toInt()} trước khi vượt mức tối đa",
                fontSize = 12.sp,
                color = OnSurfaceVariant
            )
        }
    }
}

@Composable
fun MemberExpenseCircle(name: String, amount: Double, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLowest)
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Image(painterResource(id = R.drawable.ic_launcher_foreground), null, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnSurface)
        Text("$${amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
    }
}

@Composable
fun ExpenseRow(expense: ExpenseItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLowest)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when(expense.category) {
                    "Food" -> "🍱"
                    "Stay" -> "🏨"
                    "Transport" -> "🚄"
                    else -> "🎟️"
                },
                fontSize = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(expense.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
            Text("Trả bởi ${expense.paidBy}", fontSize = 12.sp, color = OnSurfaceVariant)
        }
        
        Text("$${expense.amount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = PrimaryBlue)
    }
}

data class ExpenseItemData(val title: String, val paidBy: String, val amount: Double, val category: String)
