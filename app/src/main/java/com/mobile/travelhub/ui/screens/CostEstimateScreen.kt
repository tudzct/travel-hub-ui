package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import com.mobile.travelhub.viewmodels.ExpenseTransactionUiModel


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
import com.mobile.travelhub.utils.NumberUtils
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostEstimateScreen(
    tripId: Long,
    groupName: String = "Tokyo Trip",
    onBack: () -> Unit,
    onNavigateToProfile: (Long) -> Unit = {},
    viewModel: CostEstimateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddExpense by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseTransactionUiModel?>(null) }
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
            if (!uiState.isCompleted) {
                FloatingActionButton(
                    onClick = { showAddExpense = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
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
                    budgetMax = uiState.budgetMax ?: 0.0
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
                            text = "Chưa có khoản chi nào cho chuyến đi này.",
                            color = OnSurfaceVariant,
                            fontSize = 13.sp
                        )
                    } else {
                        contributions.take(3).forEachIndexed { index, contribution ->
                            MemberExpenseCircle(
                                name = contribution.userName,
                                amount = contribution.amountPaid,
                                avatarUrl = contribution.avatarUrl,
                                color = listOf(PrimaryBlue, SunsetOrange, Color(0xFF4CAF50))[index % 3],
                                onClick = { onNavigateToProfile(contribution.userId) }
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

            val recentExpenses = uiState.transactions

            if (recentExpenses.isEmpty()) {
                item {
                    Text(
                        text = "Chuyến đi này chưa có giao dịch nào.",
                        color = OnSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                items(recentExpenses) { expense ->
                    ExpenseRow(
                        expense = expense,
                        onClick = {
                            if (!uiState.isCompleted) {
                                editingExpense = expense
                            }
                        }
                    )
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

        if (editingExpense != null) {
            ModalBottomSheet(
                onDismissRequest = { editingExpense = null },
                sheetState = sheetState,
                containerColor = SurfaceContainerLowest,
                dragHandle = { BottomSheetDefaults.DragHandle(color = SurfaceContainerLow) }
            ) {
                val expense = editingExpense!!
                EditExpenseContent(
                    expense = expense,
                    isSaving = uiState.isAddingExpense,
                    onSave = { title, amountText, category ->
                        viewModel.editExpense(expense.id, title, amountText, category, expense.paidByUserId)
                    },
                    onDelete = {
                        viewModel.deleteExpense(expense.id)
                    },
                    onDismiss = { editingExpense = null }
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
    var expenseAmountValue by remember { mutableStateOf(TextFieldValue("")) }
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
            value = expenseAmountValue,
            onValueChange = { newValue ->
                val formatted = NumberUtils.formatTextFieldValue(newValue)
                expenseAmountValue = formatted
            },
            label = { Text("Số tiền (VND)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = SurfaceContainerLow
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                    onSave(expenseTitle, expenseAmountValue.text, selectedCategory)
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
fun BudgetSummaryCard(totalSpent: Double, budgetMax: Double) {
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
                    Text(NumberUtils.formatVnd(totalSpent), fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = OnSurface)
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
                text = "Còn lại ${NumberUtils.formatVnd(budgetMax - totalSpent)} trước khi vượt ngân sách",
                fontSize = 12.sp,
                color = OnSurfaceVariant
            )
        }
    }
}

@Composable
fun MemberExpenseCircle(
    name: String,
    amount: Double,
    avatarUrl: String? = null,
    color: Color,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(painterResource(id = R.drawable.ic_launcher_foreground), null, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(NumberUtils.formatVnd(amount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
    }
}

@Composable
fun ExpenseRow(
    expense: ExpenseTransactionUiModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLowest)
            .clickable(onClick = onClick)
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
            Text("Trả bởi ${expense.paidByName}", fontSize = 12.sp, color = OnSurfaceVariant)
        }
        
        Text(NumberUtils.formatVnd(expense.amount), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = PrimaryBlue)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseContent(
    expense: ExpenseTransactionUiModel,
    isSaving: Boolean,
    onSave: (String, String, String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var expenseTitle by remember(expense.id) { mutableStateOf(expense.title) }
    var expenseAmountValue by remember(expense.id) {
        val initialText = NumberUtils.formatInputString(expense.amount.toInt().toString())
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }
    var selectedCategory by remember(expense.id) { mutableStateOf(expense.category) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xóa khoản chi", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa khoản chi '${expense.title}' không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = SunsetOrange)
                ) {
                    Text("Xóa", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy")
                }
            },
            containerColor = SurfaceContainerLowest
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chỉnh sửa khoản chi",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = OnSurface
            )
            IconButton(
                onClick = { showDeleteConfirm = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Expense",
                    tint = SunsetOrange
                )
            }
        }
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
            value = expenseAmountValue,
            onValueChange = { newValue ->
                val formatted = NumberUtils.formatTextFieldValue(newValue)
                expenseAmountValue = formatted
            },
            label = { Text("Số tiền (VND)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = SurfaceContainerLow
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                    onSave(expenseTitle, expenseAmountValue.text, selectedCategory)
                    onDismiss()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp),
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(if (isSaving) "Đang lưu..." else "Cập nhật chi phí", fontWeight = FontWeight.Bold)
        }
    }
}
