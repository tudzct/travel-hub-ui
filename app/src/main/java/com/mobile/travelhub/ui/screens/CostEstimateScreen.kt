package com.mobile.travelhub.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.components.SimpleFormTextField
import com.mobile.travelhub.ui.components.costEstimateLoadingSkeleton
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.ui.theme.SurfaceContainerLow
import com.mobile.travelhub.ui.theme.SurfaceContainerLowest
import com.mobile.travelhub.ui.theme.SunsetOrange
import com.mobile.travelhub.ui.theme.isDarkTheme
import com.mobile.travelhub.utils.NumberUtils
import com.mobile.travelhub.viewmodels.CostEstimateViewModel
import com.mobile.travelhub.viewmodels.ExpenseTransactionUiModel
import com.mobile.travelhub.viewmodels.ExpenseSplitShareUiModel
import com.mobile.travelhub.viewmodels.ReceiptOcrDraft
import com.mobile.travelhub.viewmodels.SettlementUiModel
import com.mobile.travelhub.viewmodels.TripExpenseMemberUiModel
import java.net.URLEncoder
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ExpenseBlue = Color(0xFF0D7CFF)
private val ExpenseOrange = Color(0xFFFF941A)
private val ExpenseGreen = Color(0xFF38C779)
private val ExpensePurple = Color(0xFF9458E8)
private val ExpenseSlate = Color(0xFF98A4BC)
private val ExpenseInk: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurface

private val ExpenseMuted: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

private val ExpenseBorder: Color
    @Composable
    get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

private val ExpenseSoftBlue: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

private val ExpenseItemBg: Color
    @Composable
    get() = if (isDarkTheme) Color(0xFF22252A) else Color(0xFFF7F9FC)
private const val SPLIT_EQUAL = "EQUAL"
private const val SPLIT_CUSTOM = "CUSTOM"

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
    var selectedTab by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categoryBreakdown = remember(uiState.transactions) {
        buildCategoryBreakdown(uiState.transactions)
    }
    val receiptPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.scanReceipt(uri)
        }
    }

    LaunchedEffect(tripId, groupName) {
        viewModel.loadExpenseSummary(tripId, groupName)
    }

    Scaffold(
        containerColor = SurfaceBg,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                ExpenseHeader(onBack = onBack)
            }

            item {
                ExpenseTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

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
            } else if (selectedTab == 0) {
                item {
                    ExpenseOverviewCard(
                        totalSpent = uiState.totalSpent,
                        transactionCount = uiState.transactions.size,
                        categories = categoryBreakdown
                    )
                }

                item {
                    TripSettlementPanel(
                        canFinishTrip = uiState.canFinishTrip,
                        isCompleted = uiState.isCompleted,
                        currentUserId = uiState.currentUserId,
                        settlements = uiState.settlements
                    )
                }

                item {
                    InvoiceOcrBanner(
                        enabled = !uiState.isCompleted,
                        isScanning = uiState.isScanningReceipt,
                        onScanClick = {
                            receiptPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onManualClick = { showAddExpense = true }
                    )
                }

                item {
                    SectionHeader(
                        title = "Chi tiêu gần đây",
                        action = if (uiState.transactions.isNotEmpty()) "Xem tất cả" else null,
                        onActionClick = { selectedTab = 1 }
                    )
                }

                if (uiState.transactions.isEmpty()) {
                    item {
                        EmptyExpensesCard()
                    }
                } else {
                    items(uiState.transactions.take(6), key = { it.id }) { expense ->
                    ModernExpenseRow(
                        expense = expense,
                        onClick = {
                            editingExpense = expense
                        }
                    )
                    }
                }
            } else {
                item {
                    SectionHeader(title = "Hóa đơn", action = null)
                }

                if (uiState.transactions.isEmpty()) {
                    item { EmptyExpensesCard() }
                } else {
                    items(uiState.transactions, key = { it.id }) { expense ->
                        ModernExpenseRow(
                            expense = expense,
                            onClick = {
                                editingExpense = expense
                            }
                        )
                    }
                }
            }
        }

        if (showAddExpense) {
            ModalBottomSheet(
                onDismissRequest = { showAddExpense = false },
                sheetState = sheetState,
                containerColor = SurfaceContainerLowest,
                dragHandle = { BottomSheetDefaults.DragHandle(color = SurfaceContainerLow) }
            ) {
                AddExpenseContent(
                    members = uiState.members,
                    isSaving = uiState.isAddingExpense,
                    onSave = { title, amountText, category, splitType, splitUserIds, splitShares, proofImageUri ->
                        viewModel.addExpense(title, amountText, category, splitType, splitUserIds, splitShares, proofImageUri)
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
                    members = uiState.members,
                    canEdit = !uiState.isCompleted,
                    isSaving = uiState.isAddingExpense,
                    onSave = { title, amountText, category, splitType, splitUserIds, splitShares, proofImageUri ->
                        viewModel.editExpense(
                            expense.id,
                            title,
                            amountText,
                            category,
                            expense.paidByUserId,
                            expense.proofImageUrl,
                            splitType,
                            splitUserIds,
                            splitShares,
                            proofImageUri
                        )
                    },
                    onDelete = {
                        viewModel.deleteExpense(expense.id)
                    },
                    onDismiss = { editingExpense = null }
                )
            }
        }

        if (uiState.receiptOcrDraft != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissReceiptOcrDraft() },
                sheetState = sheetState,
                containerColor = SurfaceContainerLowest,
                dragHandle = { BottomSheetDefaults.DragHandle(color = SurfaceContainerLow) }
            ) {
                ReceiptOcrConfirmContent(
                    draft = uiState.receiptOcrDraft!!,
                    members = uiState.members,
                    isSaving = uiState.isAddingExpense,
                    onConfirm = { title, amountText, category, expenseDate, note, paidByUserId, splitType, splitUserIds, splitShares ->
                        viewModel.submitReceiptOcrExpense(
                            title = title,
                            amountText = amountText,
                            category = category,
                            expenseDate = expenseDate,
                            note = note,
                            paidByUserId = paidByUserId,
                            splitType = splitType,
                            splitUserIds = splitUserIds,
                            splitShares = splitShares
                        )
                    },
                    onDismiss = { viewModel.dismissReceiptOcrDraft() }
                )
            }
        }
    }
}

@Composable
private fun ExpenseHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.ui_b52b36b726),
                tint = ExpenseInk,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Chi phí",
            color = ExpenseInk,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Tổng quan", "Hóa đơn")
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = SurfaceBg,
        contentColor = PrimaryBlue,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                height = 3.dp,
                color = PrimaryBlue
            )
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = if (selectedTab == index) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (selectedTab == index) PrimaryBlue else ExpenseMuted
                    )
                }
            )
        }
    }
}

@Composable
private fun ExpenseOverviewCard(
    totalSpent: Double,
    transactionCount: Int,
    categories: List<CategoryAmount>
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                            text = "Tổng chi tiêu",
                            color = ExpenseMuted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = NumberUtils.formatVnd(totalSpent),
                        color = ExpenseInk,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$transactionCount chi phí",
                        color = ExpenseMuted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(144.dp)
                        .padding(top = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ExpenseDonutChart(categories = categories)
                    Text(
                        text = "Theo\ndanh mục",
                        color = ExpenseInk,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            CategoryLegend(categories = categories)
        }
    }
}

@Composable
private fun TripSettlementPanel(
    canFinishTrip: Boolean,
    isCompleted: Boolean,
    currentUserId: Long,
    settlements: List<SettlementUiModel>
) {
    val payableSettlements = remember(settlements, currentUserId) {
        settlements
            .filter { it.isPayableBy(currentUserId) && it.amount > 0.0 }
            .filterNot { it.status.equals("CONFIRMED", ignoreCase = true) }
    }
    val totalPayable = payableSettlements.sumOf { it.amount }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(alpha = 0.04f), spotColor = Color.Black.copy(alpha = 0.06f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Thanh toán sau chuyến đi",
                        color = ExpenseInk,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (settlements.isEmpty()) {
                            if (isCompleted) {
                                "Chưa có khoản thanh toán nào cần xử lý"
                            } else if (canFinishTrip) {
                                "Kết thúc chuyến đi ở trang chính để tính khoản cần chuyển"
                            } else {
                                "Trưởng nhóm sẽ kết thúc chuyến đi để tính thanh toán"
                            }
                        } else {
                            "Bạn cần thanh toán ${NumberUtils.formatVnd(totalPayable)}"
                        },
                        color = ExpenseMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                if (settlements.isNotEmpty() && totalPayable <= 0.0) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ExpenseGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (settlements.isNotEmpty()) {
                HorizontalDivider(color = ExpenseBorder)

                if (payableSettlements.isEmpty()) {
                    Text(
                        text = "Bạn không có khoản cần thanh toán.",
                        color = ExpenseMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    payableSettlements.forEach { settlement ->
                        PayableSettlementItem(settlement = settlement)
                    }
                }
            }
        }
    }
}

@Composable
private fun PayableSettlementItem(settlement: SettlementUiModel) {
    val qrUrl = remember(settlement) { buildVietQrUrl(settlement) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ExpenseItemBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = settlement.receiverAccountName?.takeIf { it.isNotBlank() } ?: "Người nhận",
                    color = ExpenseInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(
                        settlement.receiverBankName?.takeIf { it.isNotBlank() },
                        settlement.receiverAccountNumber?.takeIf { it.isNotBlank() }
                    ).joinToString(" - ").ifBlank { "Chưa có thông tin ngân hàng" },
                    color = ExpenseMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            Text(
                text = NumberUtils.formatVnd(settlement.amount),
                color = PrimaryBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.End
            )
        }

        Text(
            text = "Nội dung: ${settlement.transferContent.ifBlank { "Chuyenkhoannganhang" }}",
            color = ExpenseMuted,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )

        if (qrUrl != null) {
            AsyncImage(
                model = qrUrl,
                contentDescription = "QR chuyển khoản",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = "Người nhận chưa cấu hình đủ tài khoản ngân hàng để tạo QR.",
                color = SunsetOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ExpenseDonutChart(categories: List<CategoryAmount>) {
    val total = categories.sumOf { it.amount }.toFloat()
    val borderColor = ExpenseBorder
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 28.dp.toPx()
        val chartSize = Size(size.minDimension - strokeWidth, size.minDimension - strokeWidth)
        val topLeft = androidx.compose.ui.geometry.Offset(
            x = (size.width - chartSize.width) / 2f,
            y = (size.height - chartSize.height) / 2f
        )

        if (total <= 0f) {
            drawArc(
                color = borderColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = chartSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            return@Canvas
        }

        var startAngle = -90f
        categories.forEach { category ->
            if (category.amount > 0.0) {
                val sweep = ((category.amount / total) * 360f).toFloat()
                drawArc(
                    color = category.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = chartSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
    }
}

@Composable
private fun CategoryLegend(categories: List<CategoryAmount>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.forEach { category ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(category.color)
                    )
                    Text(
                        text = category.label,
                        color = ExpenseInk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 14.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = NumberUtils.formatVnd(category.amount),
                    color = ExpenseInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun ExpenseFilterPanel() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(alpha = 0.04f), spotColor = Color.Black.copy(alpha = 0.06f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterPill(
                    text = "Tháng này",
                    icon = Icons.Default.CalendarMonth,
                    selected = true,
                    modifier = Modifier.weight(1f)
                )
                FilterPill(
                    text = "Tất cả chuyến đi",
                    icon = null,
                    selected = false,
                    modifier = Modifier.weight(1.18f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterPill(
                    text = "Danh mục",
                    icon = null,
                    selected = false,
                    modifier = Modifier.widthIn(min = 136.dp)
                )
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Bộ lọc",
                        tint = ExpenseMuted,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    icon: ImageVector?,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) ExpenseSoftBlue else SurfaceContainerLowest,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, ExpenseBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(21.dp)
                )
                Spacer(Modifier.width(7.dp))
            }
            Text(
                text = text,
                color = if (selected) PrimaryBlue else ExpenseMuted,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = if (selected) PrimaryBlue else ExpenseMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun InvoiceOcrBanner(
    enabled: Boolean,
    isScanning: Boolean,
    onScanClick: () -> Unit,
    onManualClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ExpenseItemBg)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onScanClick,
            enabled = enabled && !isScanning,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                disabledContentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(6.dp))
            Column {
                Text(
                    if (isScanning) "Đang quét" else "Quét hóa đơn",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "OCR tự động",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Button(
            onClick = onManualClick,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDarkTheme) Color(0xFF1B3D2B) else Color(0xFFEAF8F0),
                contentColor = if (isDarkTheme) Color(0xFF5EDAA0) else Color(0xFF145C3C),
                disabledContainerColor = (if (isDarkTheme) Color(0xFF1B3D2B) else Color(0xFFEAF8F0)).copy(alpha = 0.5f),
                disabledContentColor = (if (isDarkTheme) Color(0xFF5EDAA0) else Color(0xFF145C3C)).copy(alpha = 0.5f)
            ),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(6.dp))
            Column {
                Text(
                    "Nhập thủ công",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Nhập tay từng khoản",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = (if (isDarkTheme) Color(0xFF5EDAA0) else Color(0xFF145C3C)).copy(alpha = 0.76f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String?,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = ExpenseInk,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        if (action != null) {
            Text(
                text = action,
                color = PrimaryBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable(
                    enabled = onActionClick != null,
                    onClick = { onActionClick?.invoke() }
                )
            )
        }
    }
}

@Composable
private fun EmptyExpensesCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Chưa có chi phí nào trong chuyến đi này",
            color = ExpenseMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(18.dp)
        )
    }
}

@Composable
private fun ModernExpenseRow(
    expense: ExpenseTransactionUiModel,
    onClick: () -> Unit
) {
    val category = categoryVisual(expense.category)
    val isPending = expense.id < 0L
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceContainerLowest)
            .clickable(enabled = !isPending, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = expense.title,
                color = ExpenseInk,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Người trả: ${expense.paidByName}",
                color = ExpenseMuted,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (isPending) "Đang lưu..." else formatExpenseDate(expense.date),
                color = if (isPending) PrimaryBlue else ExpenseMuted,
                fontSize = 13.sp
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = category.color.copy(alpha = 0.12f)
            ) {
                Text(
                    text = category.label,
                    color = category.color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    maxLines = 1
                )
            }
        }

        Text(
            text = NumberUtils.formatVnd(expense.amount),
            color = ExpenseInk,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .padding(start = 4.dp)
                .widthIn(min = 82.dp),
            textAlign = TextAlign.End,
            maxLines = 1
        )
        if (!isPending) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = ExpenseMuted,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptOcrConfirmContent(
    draft: ReceiptOcrDraft,
    members: List<TripExpenseMemberUiModel>,
    isSaving: Boolean,
    onConfirm: (
        title: String,
        amountText: String,
        category: String,
        expenseDate: String,
        note: String?,
        paidByUserId: Long,
        splitType: String,
        splitUserIds: List<Long>,
        splitShares: List<ExpenseSplitShareUiModel>
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val result = draft.result
    val defaultTitle = result.merchantName?.takeIf { it.isNotBlank() } ?: "Chi phí từ hóa đơn"
    val defaultAmount = result.totalAmount?.let { NumberUtils.formatInputString(it.toLong().toString()) }.orEmpty()
    val effectiveMembers = members.ifEmpty {
        listOf(TripExpenseMemberUiModel(userId = -1L, name = "Tôi", isCurrentUser = true))
    }
    var title by remember(draft.imageUri) { mutableStateOf(defaultTitle) }
    var amountValue by remember(draft.imageUri) {
        mutableStateOf(TextFieldValue(defaultAmount, selection = TextRange(defaultAmount.length)))
    }
    var expenseDate by remember(draft.imageUri) { mutableStateOf(result.expenseDate.orEmpty()) }
    var note by remember(draft.imageUri) { mutableStateOf("") }
    var selectedCategory by remember(draft.imageUri) { mutableStateOf("FOOD") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var paidByExpanded by remember { mutableStateOf(false) }
    var showReceiptImageFullScreen by remember(draft.imageUri) { mutableStateOf(false) }
    var paidByUserId by remember(draft.imageUri, effectiveMembers) {
        mutableStateOf(effectiveMembers.firstOrNull { it.isCurrentUser }?.userId ?: effectiveMembers.first().userId)
    }
    var splitUserIds by remember(draft.imageUri, effectiveMembers) {
        mutableStateOf(effectiveMembers.map { it.userId }.filter { it > 0L }.toSet())
    }
    var splitType by remember(draft.imageUri) { mutableStateOf(SPLIT_EQUAL) }
    var customSplitValues by remember(draft.imageUri) { mutableStateOf<Map<Long, TextFieldValue>>(emptyMap()) }

    if (showReceiptImageFullScreen) {
        FullScreenProofImageDialog(
            imageModel = draft.imageUri,
            onDismiss = { showReceiptImageFullScreen = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Xác nhận hóa đơn",
            color = ExpenseInk,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "OCR chỉ tự điền thông tin. Kiểm tra và chỉnh sửa trước khi lưu chi phí.",
            color = ExpenseMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        AsyncImage(
            model = draft.imageUri,
            contentDescription = "Ảnh hóa đơn",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable { showReceiptImageFullScreen = true }
                .background(ExpenseItemBg),
            contentScale = ContentScale.Crop
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Tên cửa hàng / tiêu đề") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = amountValue,
            onValueChange = { amountValue = NumberUtils.formatTextFieldValue(it) },
            label = { Text("Tổng tiền") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = expenseDate,
            onValueChange = { expenseDate = it },
            label = { Text("Ngày hóa đơn (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        ExpenseCategoryDropdown(
            selectedCategory = selectedCategory,
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it },
            onCategorySelected = { selectedCategory = it }
        )

        ExposedDropdownMenuBox(
            expanded = paidByExpanded,
            onExpandedChange = { paidByExpanded = !paidByExpanded }
        ) {
            SimpleFormTextField(
                value = effectiveMembers.firstOrNull { it.userId == paidByUserId }?.name.orEmpty(),
                onValueChange = {},
                readOnly = true,
                placeholder = "Người đã trả",
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = paidByExpanded,
                onDismissRequest = { paidByExpanded = false },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                effectiveMembers.forEach { member ->
                    DropdownMenuItem(
                        text = { Text(member.name) },
                        onClick = {
                            paidByUserId = member.userId
                            paidByExpanded = false
                        }
                    )
                }
            }
        }

        SplitOptionEditor(
            members = effectiveMembers,
            totalAmountText = amountValue.text,
            splitType = splitType,
            onSplitTypeChange = { splitType = it },
            selectedUserIds = splitUserIds,
            customSplitValues = customSplitValues,
            onCustomSplitValuesChange = { customSplitValues = it },
            enabled = !isSaving,
            onSelectedUserIdsChange = { splitUserIds = it }
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Ghi chú") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(14.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Hủy", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    if (!isSaving) {
                        onConfirm(
                            title,
                            amountValue.text,
                            selectedCategory,
                            expenseDate,
                            note,
                            paidByUserId,
                            splitType,
                            splitUserIds.toList(),
                            buildSplitShares(splitUserIds, customSplitValues)
                        )
                    }
                },
                modifier = Modifier
                    .weight(1.4f)
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                )
            ) {
                Text(if (isSaving) "Đang lưu..." else "Lưu chi phí", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun SplitOptionEditor(
    members: List<TripExpenseMemberUiModel>,
    totalAmountText: String,
    splitType: String,
    onSplitTypeChange: (String) -> Unit,
    selectedUserIds: Set<Long>,
    customSplitValues: Map<Long, TextFieldValue>,
    onCustomSplitValuesChange: (Map<Long, TextFieldValue>) -> Unit,
    enabled: Boolean,
    onSelectedUserIdsChange: (Set<Long>) -> Unit
) {
    val totalAmount = remember(totalAmountText) { parseMoneyInputToLong(totalAmountText) ?: 0L }
    val selectedMembers = remember(members, selectedUserIds) {
        members.filter { it.userId in selectedUserIds && it.userId > 0L }
    }
    val assignedAmount = remember(customSplitValues, selectedUserIds) {
        selectedUserIds.sumOf { userId -> parseMoneyInputToLong(customSplitValues[userId]?.text.orEmpty()) ?: 0L }
    }
    val remainingAmount = totalAmount - assignedAmount

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Cách chia",
            color = ExpenseInk,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SplitTypeChip(
                title = "Chia đều",
                subtitle = "Tự chia theo số người",
                selected = splitType == SPLIT_EQUAL,
                enabled = enabled,
                onClick = { onSplitTypeChange(SPLIT_EQUAL) },
                modifier = Modifier.weight(1f)
            )
            SplitTypeChip(
                title = "Chia cụ thể",
                subtitle = "Nhập số tiền từng người",
                selected = splitType == SPLIT_CUSTOM,
                enabled = enabled,
                onClick = {
                    onSplitTypeChange(SPLIT_CUSTOM)
                    if (totalAmount > 0L && selectedMembers.isNotEmpty() && customSplitValues.isEmpty()) {
                        onCustomSplitValuesChange(
                            autoFillCustomSplitValues(
                                totalAmount = totalAmount,
                                selectedMembers = selectedMembers,
                                currentValues = customSplitValues
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        SplitMemberSelector(
            members = members,
            selectedUserIds = selectedUserIds,
            customAmounts = if (splitType == SPLIT_CUSTOM) customSplitValues.toMoneyMap() else emptyMap(),
            enabled = enabled,
            onSelectedUserIdsChange = { newSelection ->
                onSelectedUserIdsChange(newSelection)
                val retainedValues = customSplitValues.filterKeys { it in newSelection }
                val nextSelectedMembers = members.filter { it.userId in newSelection && it.userId > 0L }
                val hasEmptyCustomAmount = nextSelectedMembers.any { member ->
                    parseMoneyInputToLong(retainedValues[member.userId]?.text.orEmpty()) == null
                }
                val nextCustomValues = if (splitType == SPLIT_CUSTOM && totalAmount > 0L && hasEmptyCustomAmount) {
                    autoFillCustomSplitValues(
                        totalAmount = totalAmount,
                        selectedMembers = nextSelectedMembers,
                        currentValues = retainedValues
                    )
                } else {
                    retainedValues
                }
                onCustomSplitValuesChange(nextCustomValues)
            }
        )

        if (splitType == SPLIT_CUSTOM) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ExpenseItemBg)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Đã nhập ${NumberUtils.formatVnd(assignedAmount.toDouble())} • Còn lại ${NumberUtils.formatVnd(remainingAmount.coerceAtLeast(0L).toDouble())}",
                    color = if (remainingAmount < 0L) SunsetOrange else ExpenseMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                TextButton(
                    enabled = enabled && totalAmount > 0L && selectedMembers.isNotEmpty(),
                    onClick = {
                        onCustomSplitValuesChange(
                            autoFillCustomSplitValues(
                                totalAmount = totalAmount,
                                selectedMembers = selectedMembers,
                                currentValues = customSplitValues
                            )
                        )
                    }
                ) {
                    Text("Tự chia phần còn lại", fontWeight = FontWeight.Bold)
                }

                selectedMembers.forEach { member ->
                    CustomSplitAmountRow(
                        member = member,
                        value = customSplitValues[member.userId] ?: TextFieldValue(""),
                        enabled = enabled,
                        onValueChange = { value ->
                            onCustomSplitValuesChange(
                                customSplitValues + (member.userId to NumberUtils.formatTextFieldValue(value))
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitTypeChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) ExpenseSoftBlue else ExpenseItemBg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            enabled = enabled,
            onClick = onClick
        )
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(title, color = ExpenseInk, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = ExpenseMuted, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun CustomSplitAmountRow(
    member: TripExpenseMemberUiModel,
    value: TextFieldValue,
    enabled: Boolean,
    onValueChange: (TextFieldValue) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLowest)
            .padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = member.name,
                color = ExpenseInk,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (value.text.isBlank()) "Chưa nhập" else "${value.text} đ",
                color = if (value.text.isBlank()) ExpenseMuted else PrimaryBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.width(150.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            trailingIcon = {
                Text(
                    text = "đ",
                    color = ExpenseMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

private fun buildSplitShares(
    selectedUserIds: Set<Long>,
    customSplitValues: Map<Long, TextFieldValue>
): List<ExpenseSplitShareUiModel> {
    return selectedUserIds.mapNotNull { userId ->
        val amount = parseMoneyInputToLong(customSplitValues[userId]?.text.orEmpty()) ?: return@mapNotNull null
        if (amount <= 0L) return@mapNotNull null
        ExpenseSplitShareUiModel(userId = userId, amount = amount.toDouble())
    }
}

private fun autoFillCustomSplitValues(
    totalAmount: Long,
    selectedMembers: List<TripExpenseMemberUiModel>,
    currentValues: Map<Long, TextFieldValue>
): Map<Long, TextFieldValue> {
    val emptyMembers = selectedMembers.filter { member ->
        parseMoneyInputToLong(currentValues[member.userId]?.text.orEmpty()) == null
    }
    val targetMembers = emptyMembers.ifEmpty { selectedMembers }
    val lockedAmount = if (emptyMembers.isEmpty()) {
        0L
    } else {
        selectedMembers
            .filterNot { it in targetMembers }
            .sumOf { parseMoneyInputToLong(currentValues[it.userId]?.text.orEmpty()) ?: 0L }
    }
    val amountToSplit = (totalAmount - lockedAmount).coerceAtLeast(0L)
    val distributedAmounts = splitAmountEvenly(amountToSplit, targetMembers.map { it.userId })
    return currentValues + distributedAmounts.mapValues { (_, amount) ->
        val text = NumberUtils.formatInputString(amount.toString())
        TextFieldValue(text = text, selection = TextRange(text.length))
    }
}

private fun splitAmountEvenly(totalAmount: Long, userIds: List<Long>): Map<Long, Long> {
    if (userIds.isEmpty()) return emptyMap()
    val baseAmount = totalAmount / userIds.size
    val remainder = (totalAmount % userIds.size).toInt()
    return userIds.mapIndexed { index, userId ->
        userId to (baseAmount + if (index >= userIds.size - remainder) 1L else 0L)
    }.toMap()
}

private fun parseMoneyInputToLong(value: String): Long? {
    return value.replace(".", "").trim().takeIf { it.isNotBlank() }?.toLongOrNull()
}

private fun Map<Long, TextFieldValue>.toMoneyMap(): Map<Long, Long> {
    return mapNotNull { (userId, value) ->
        val amount = parseMoneyInputToLong(value.text) ?: return@mapNotNull null
        userId to amount
    }.toMap()
}

@Composable
private fun SplitMemberSelector(
    members: List<TripExpenseMemberUiModel>,
    selectedUserIds: Set<Long>,
    customAmounts: Map<Long, Long>,
    enabled: Boolean,
    onSelectedUserIdsChange: (Set<Long>) -> Unit
) {
    var searchQuery by remember(members) { mutableStateOf("") }
    val selectableMembers = remember(members) {
        members.filter { it.userId > 0L }
    }
    val filteredMembers = remember(selectableMembers, searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            selectableMembers
        } else {
            selectableMembers.filter { member ->
                member.name.contains(query, ignoreCase = true)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Người cùng chia",
            color = ExpenseInk,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            enabled = enabled,
            label = { Text("Tìm thành viên trong chuyến đi") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        enabled = enabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa tìm kiếm"
                        )
                    }
                }
            }
        )

        Text(
            text = "Đã chọn ${selectedUserIds.size}/${selectableMembers.size} người",
            color = ExpenseMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        if (filteredMembers.isEmpty()) {
            Text(
                text = "Không tìm thấy thành viên phù hợp",
                color = ExpenseMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ExpenseItemBg)
                    .padding(14.dp)
            )
        } else {
            filteredMembers.forEach { member ->
                val checked = selectedUserIds.contains(member.userId)
                val customAmount = customAmounts[member.userId]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ExpenseItemBg)
                        .clickable(enabled = enabled) {
                            onSelectedUserIdsChange(
                                if (checked) {
                                    selectedUserIds - member.userId
                                } else {
                                    selectedUserIds + member.userId
                                }
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked,
                        enabled = enabled,
                        onCheckedChange = { isChecked ->
                            onSelectedUserIdsChange(
                                if (isChecked) {
                                    selectedUserIds + member.userId
                                } else {
                                    selectedUserIds - member.userId
                                }
                            )
                        }
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.name,
                            color = ExpenseInk,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (checked && customAmount != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ExpenseSoftBlue
                            ) {
                                Text(
                                    text = NumberUtils.formatVnd(customAmount.toDouble()),
                                    color = PrimaryBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseContent(
    members: List<TripExpenseMemberUiModel>,
    isSaving: Boolean,
    onSave: (String, String, String, String, List<Long>, List<ExpenseSplitShareUiModel>, android.net.Uri?) -> Unit,
    onDismiss: () -> Unit
) {
    val initialSplitUserIds = remember(members) {
        members.map { it.userId }.filter { it > 0L }.toSet()
    }
    var expenseTitle by remember { mutableStateOf("") }
    var expenseAmountValue by remember { mutableStateOf(TextFieldValue("")) }
    var selectedCategory by remember { mutableStateOf("FOOD") }
    var expanded by remember { mutableStateOf(false) }
    var proofImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var splitUserIds by remember(initialSplitUserIds) { mutableStateOf(initialSplitUserIds) }
    var splitType by remember { mutableStateOf(SPLIT_EQUAL) }
    var customSplitValues by remember { mutableStateOf<Map<Long, TextFieldValue>>(emptyMap()) }
    val proofPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        proofImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .verticalScroll(rememberScrollState())
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
                expenseAmountValue = NumberUtils.formatTextFieldValue(newValue)
            },
            placeholder = stringResource(R.string.ui_33eb57284b),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExpenseCategoryDropdown(
            selectedCategory = selectedCategory,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SplitOptionEditor(
            members = members,
            totalAmountText = expenseAmountValue.text,
            splitType = splitType,
            onSplitTypeChange = { splitType = it },
            selectedUserIds = splitUserIds,
            customSplitValues = customSplitValues,
            onCustomSplitValuesChange = { customSplitValues = it },
            enabled = !isSaving,
            onSelectedUserIdsChange = { splitUserIds = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExpenseProofImagePicker(
            imageModel = proofImageUri,
            enabled = !isSaving,
            emptyText = "Chưa chọn ảnh minh chứng",
            actionText = "Thêm ảnh minh chứng",
            onPickImage = {
                proofPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onClearImage = { proofImageUri = null }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isSaving) {
                    onSave(
                        expenseTitle,
                        expenseAmountValue.text,
                        selectedCategory,
                        splitType,
                        splitUserIds.toList(),
                        buildSplitShares(splitUserIds, customSplitValues),
                        proofImageUri
                    )
                    onDismiss()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseContent(
    expense: ExpenseTransactionUiModel,
    members: List<TripExpenseMemberUiModel>,
    canEdit: Boolean,
    isSaving: Boolean,
    onSave: (String, String, String, String, List<Long>, List<ExpenseSplitShareUiModel>, android.net.Uri?) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val fallbackSplitUserIds = remember(expense.id, members) {
        members.map { it.userId }.filter { it > 0L }.toSet()
    }
    val initialSplitUserIds = remember(expense.id, expense.splitUserIds, fallbackSplitUserIds) {
        expense.splitUserIds.filter { it > 0L }.toSet().ifEmpty { fallbackSplitUserIds }
    }
    var expenseTitle by remember(expense.id) { mutableStateOf(expense.title) }
    var expenseAmountValue by remember(expense.id) {
        val initialText = NumberUtils.formatInputString(expense.amount.toLong().toString())
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }
    var selectedCategory by remember(expense.id) { mutableStateOf(expense.category) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var replacementProofImageUri by remember(expense.id) { mutableStateOf<android.net.Uri?>(null) }
    var splitUserIds by remember(expense.id, initialSplitUserIds) { mutableStateOf(initialSplitUserIds) }
    var splitType by remember(expense.id) { mutableStateOf(if (expense.splitType.equals(SPLIT_CUSTOM, ignoreCase = true)) SPLIT_CUSTOM else SPLIT_EQUAL) }
    var customSplitValues by remember(expense.id, expense.splitShares) {
        mutableStateOf(
            expense.splitShares.associate { share ->
                val text = NumberUtils.formatInputString(share.amount.toLong().toString())
                share.userId to TextFieldValue(text = text, selection = TextRange(text.length))
            }
        )
    }
    val proofPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        replacementProofImageUri = uri
    }

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
            .fillMaxHeight(0.9f)
            .verticalScroll(rememberScrollState())
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
            if (canEdit) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.ui_1816483d8a),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        SimpleFormTextField(
            value = expenseTitle,
            onValueChange = { expenseTitle = it },
            enabled = canEdit && !isSaving,
            placeholder = stringResource(R.string.ui_5365fbceb0),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        SimpleFormTextField(
            value = expenseAmountValue,
            onValueChange = { newValue ->
                expenseAmountValue = NumberUtils.formatTextFieldValue(newValue)
            },
            enabled = canEdit && !isSaving,
            placeholder = stringResource(R.string.ui_33eb57284b),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExpenseCategoryDropdown(
            selectedCategory = selectedCategory,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SplitOptionEditor(
            members = members,
            totalAmountText = expenseAmountValue.text,
            splitType = splitType,
            onSplitTypeChange = { splitType = it },
            selectedUserIds = splitUserIds,
            customSplitValues = customSplitValues,
            onCustomSplitValuesChange = { customSplitValues = it },
            enabled = canEdit && !isSaving,
            onSelectedUserIdsChange = { splitUserIds = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExpenseProofImagePicker(
            imageModel = replacementProofImageUri ?: expense.proofImageUrl,
            enabled = canEdit && !isSaving,
            emptyText = "Khoản chi này chưa có ảnh minh chứng",
            actionText = if (expense.proofImageUrl.isNullOrBlank()) "Thêm ảnh minh chứng" else "Thay ảnh minh chứng",
            onPickImage = {
                proofPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onClearImage = { replacementProofImageUri = null },
            showClear = replacementProofImageUri != null
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (canEdit) {
            Button(
                onClick = {
                    if (!isSaving) {
                        onSave(
                            expenseTitle,
                            expenseAmountValue.text,
                            selectedCategory,
                            splitType,
                            splitUserIds.toList(),
                            buildSplitShares(splitUserIds, customSplitValues),
                            replacementProofImageUri
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
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
}

@Composable
private fun ExpenseProofImagePicker(
    imageModel: Any?,
    enabled: Boolean,
    emptyText: String,
    actionText: String,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
    showClear: Boolean = true
) {
    var fullScreenImageModel by remember(imageModel) { mutableStateOf<Any?>(null) }

    if (fullScreenImageModel != null) {
        FullScreenProofImageDialog(
            imageModel = fullScreenImageModel!!,
            onDismiss = { fullScreenImageModel = null }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Ảnh minh chứng",
            color = ExpenseInk,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
        if (imageModel != null && imageModel.toString().isNotBlank()) {
            AsyncImage(
                model = imageModel,
                contentDescription = "Ảnh minh chứng chi tiêu",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { fullScreenImageModel = imageModel }
                    .background(ExpenseItemBg),
                contentScale = ContentScale.Crop
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ExpenseItemBg)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = null,
                    tint = ExpenseMuted,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = emptyText,
                    color = ExpenseMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }

        if (enabled) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onPickImage) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(actionText, fontWeight = FontWeight.Bold)
                }
                if (showClear && imageModel != null) {
                    TextButton(onClick = onClearImage) {
                        Text("Bỏ ảnh vừa chọn", fontWeight = FontWeight.Bold, color = SunsetOrange)
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenProofImageDialog(
    imageModel: Any,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = "Ảnh minh chứng chi tiêu",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clickable(enabled = false) {},
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng ảnh",
                    tint = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseCategoryDropdown(
    selectedCategory: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) }
    ) {
        SimpleFormTextField(
            value = categoryVisual(selectedCategory).label,
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
            onDismissRequest = { onExpandedChange(false) },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            listOf("FOOD", "STAY", "TRANSPORT", "ENTRY").forEach { category ->
                DropdownMenuItem(
                    text = { Text(categoryVisual(category).label) },
                    onClick = {
                        onCategorySelected(category)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

private data class CategoryAmount(
    val key: String,
    val label: String,
    val color: Color,
    val icon: ImageVector,
    val amount: Double
)

private data class CategoryVisual(
    val label: String,
    val color: Color,
    val icon: ImageVector
)

private fun buildCategoryBreakdown(transactions: List<ExpenseTransactionUiModel>): List<CategoryAmount> {
    val grouped = transactions.groupBy { normalizeCategoryKey(it.category) }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    return listOf("TRANSPORT", "STAY", "FOOD", "ENTRY", "OTHER").map { key ->
        val visual = categoryVisual(key)
        CategoryAmount(
            key = key,
            label = visual.label,
            color = visual.color,
            icon = visual.icon,
            amount = grouped[key] ?: 0.0
        )
    }
}

private fun categoryVisual(category: String): CategoryVisual {
    return when (normalizeCategoryKey(category)) {
        "TRANSPORT" -> CategoryVisual("Di chuyển", ExpenseBlue, Icons.Default.DirectionsCar)
        "STAY" -> CategoryVisual("Lưu trú", ExpenseOrange, Icons.Default.Hotel)
        "FOOD" -> CategoryVisual("Ăn uống", ExpenseGreen, Icons.Default.Restaurant)
        "ENTRY" -> CategoryVisual("Vé tham quan", ExpensePurple, Icons.Default.ConfirmationNumber)
        else -> CategoryVisual("Khác", ExpenseSlate, Icons.Default.MoreHoriz)
    }
}

private fun normalizeCategoryKey(category: String): String {
    return when (category.uppercase(Locale.ROOT)) {
        "TRANSPORT", "TRANSPORTATION", "MOVE" -> "TRANSPORT"
        "STAY", "HOTEL", "LODGING" -> "STAY"
        "FOOD", "RESTAURANT" -> "FOOD"
        "ENTRY", "TICKET" -> "ENTRY"
        else -> "OTHER"
    }
}

private fun formatExpenseDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return runCatching {
        OffsetDateTime.parse(raw).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.getOrElse {
        raw.take(10).split("-").let { parts ->
            if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else raw
        }
    }
}

private fun buildVietQrUrl(settlement: SettlementUiModel): String? {
    val bankCode = settlement.receiverBankCode?.trim().orEmpty()
    val accountNumber = settlement.receiverAccountNumber?.trim().orEmpty()
    val accountName = settlement.receiverAccountName?.trim().orEmpty()
//    if (bankCode.isBlank() || accountNumber.isBlank() || accountName.isBlank() || settlement.amount <= 0.0) {
//        return null
//    }

    val amount = settlement.amount.toLong().toString()
    val transferContent = settlement.transferContent.ifBlank { "Chuyen%20khoan%20chuyen%20di" }
    return "https://img.vietqr.io/image/970422-0362507370-compact2.png?amount=${amount}&addInfo=Chuyenkhoannganhang&accountName=Bui%20Minh%20Quang"
}

private fun urlEncode(value: String): String {
    return URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
}
