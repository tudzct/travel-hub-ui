package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
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
import com.mobile.travelhub.ui.components.modifiers.shimmerEffect
import com.mobile.travelhub.ui.components.SimpleFormTextField


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
import com.mobile.travelhub.ui.components.costEstimateLoadingSkeleton
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
                        text = stringResource(R.string.ui_4ce4734973),
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ui_b52b36b726), tint = OnSurface)
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
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.ui_f53d122b20))
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

            if (uiState.isLoading && uiState.transactions.isEmpty()) {
                costEstimateLoadingSkeleton()
            } else {
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
                        text = stringResource(R.string.ui_573c15f137),
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
                                text = stringResource(R.string.ui_b034ef2e0c),
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
                        text = stringResource(R.string.ui_d5c5cb14cc),
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
                            text = stringResource(R.string.ui_1a5dca7037),
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
            text = stringResource(R.string.ui_74c24b11c3),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        SimpleFormTextField(
            value = expenseTitle,
            onValueChange = { expenseTitle = it },
            placeholder = stringResource(R.string.ui_5365fbceb0),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        SimpleFormTextField(
            value = expenseAmountValue,
            onValueChange = { newValue ->
                val formatted = NumberUtils.formatTextFieldValue(newValue)
                expenseAmountValue = formatted
            },
            placeholder = stringResource(R.string.ui_33eb57284b),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            SimpleFormTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                placeholder = stringResource(R.string.ui_fc7b5ce028),
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
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
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White
            )
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
                    Text(stringResource(R.string.ui_7817b94196), fontSize = 13.sp, color = OnSurfaceVariant)
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
            
            val exceeded = totalSpent > budgetMax
            Text(
                text = if (exceeded) {
                    stringResource(
                        R.string.budget_exceeded,
                        NumberUtils.formatVnd(totalSpent - budgetMax)
                    )
                } else {
                    stringResource(
                        R.string.budget_remaining,
                        NumberUtils.formatVnd(budgetMax - totalSpent)
                    )
                },
                fontSize = 12.sp,
                color = if (exceeded) SunsetOrange else OnSurfaceVariant
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
            Text(
                stringResource(R.string.paid_by, expense.paidByName),
                fontSize = 12.sp,
                color = OnSurfaceVariant
            )
        }
        
        Text(NumberUtils.formatVnd(expense.amount), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = PrimaryBlue)
    }
}

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
            title = { Text(stringResource(R.string.ui_9d47b1985c), fontWeight = FontWeight.Bold) },
            text = {
                Text(stringResource(R.string.delete_expense_confirmation, expense.title))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = SunsetOrange)
                ) {
                    Text(stringResource(R.string.ui_aa1d94fc16), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.ui_34ca764caf))
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
                text = stringResource(R.string.ui_c8eff87563),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = OnSurface
            )
            IconButton(
                onClick = { showDeleteConfirm = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.ui_1816483d8a),
                    tint = SunsetOrange
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        SimpleFormTextField(
            value = expenseTitle,
            onValueChange = { expenseTitle = it },
            placeholder = stringResource(R.string.ui_5365fbceb0),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        SimpleFormTextField(
            value = expenseAmountValue,
            onValueChange = { newValue ->
                val formatted = NumberUtils.formatTextFieldValue(newValue)
                expenseAmountValue = formatted
            },
            placeholder = stringResource(R.string.ui_33eb57284b),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            SimpleFormTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                placeholder = stringResource(R.string.ui_fc7b5ce028),
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
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
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White
            )
        ) {
            Text(if (isSaving) "Đang lưu..." else "Cập nhật chi phí", fontWeight = FontWeight.Bold)
        }
    }
}
