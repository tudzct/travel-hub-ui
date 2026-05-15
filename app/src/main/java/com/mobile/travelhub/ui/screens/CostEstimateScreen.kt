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


import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color


import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.theme.*
import com.mobile.travelhub.viewmodels.CostEstimateViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostEstimateScreen(
    tripId: Long,
    groupName: String = "Tokyo Trip",
    onBack: () -> Unit,
    viewModel: CostEstimateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddExpense by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(tripId, groupName) {
        viewModel.loadExpenseSummary(tripId, groupName)
    }

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
            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = SunsetOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (uiState.isLoading) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            // Budget Summary Card
            item {
                BudgetSummaryCard(
                    totalSpent = uiState.totalSpent,
                    budgetMax = uiState.budgetMax ?: 0.0,
                    budgetMin = uiState.budgetMin ?: 0.0
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
                    val contributions = uiState.contributions
                    if (contributions.isEmpty()) {
                        Text(
                            text = "Chưa có dữ liệu đóng góp từ BE.",
                            color = OnSurfaceVariant,
                            fontSize = 13.sp
                        )
                    } else {
                        contributions.take(3).forEachIndexed { index, contribution ->
                            MemberExpenseCircle(
                                name = contribution.userName,
                                amount = contribution.amountPaid,
                                color = listOf(PrimaryBlue, SunsetOrange, Color(0xFF4CAF50))[index % 3]
                            )
                        }
                    }
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

            val recentExpenses = uiState.transactions.map {
                ExpenseItemData(
                    title = it.title,
                    paidBy = it.paidByName,
                    amount = it.amount,
                    category = it.category,
                    dateLabel = it.date.orEmpty()
                )
            }

            if (recentExpenses.isEmpty()) {
                item {
                    Text(
                        text = "Chưa có giao dịch chi phí từ BE.",
                        color = OnSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                items(recentExpenses) { expense ->
                    ExpenseRow(expense)
                }
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
                AddExpenseContent(
                    isSaving = uiState.isAddingExpense,
                    onSave = { title, amountText, category ->
                        viewModel.addExpense(title, amountText, category)
                    },
                    onDismiss = { showAddExpense = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseContent(
    isSaving: Boolean,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var expenseTitle by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("FOOD") }
    var expanded by remember { mutableStateOf(false) }

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

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Danh mục") },
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = SurfaceContainerLow
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("FOOD", "STAY", "TRANSPORT", "ENTRY").forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (!isSaving) {
                    onSave(expenseTitle, expenseAmount, selectedCategory)
                    onDismiss()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp),
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(if (isSaving) "Đang lưu..." else "Lưu chi phí", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BudgetSummaryCard(totalSpent: Double, budgetMin: Double, budgetMax: Double) {
    val progress = if (budgetMax > 0.0) (totalSpent / budgetMax).toFloat() else 0f
    
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
            Icon(
                imageVector = expenseCategoryIcon(expense.category),
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(22.dp)
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

data class ExpenseItemData(
    val title: String,
    val paidBy: String,
    val amount: Double,
    val category: String,
    val dateLabel: String = ""
)

private fun expenseCategoryIcon(category: String): ImageVector {
    return when (category.uppercase()) {
        "FOOD" -> Icons.Default.Restaurant
        "STAY" -> Icons.Default.Hotel
        "TRANSPORT" -> Icons.Default.Train
        else -> Icons.Default.ConfirmationNumber

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
