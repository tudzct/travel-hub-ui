package com.mobile.travelhub.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.data.ExpenseProofRepository
import com.mobile.travelhub.data.TripRepository
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.data.model.CreateTripExpenseRequest
import com.mobile.travelhub.data.model.ReceiptOcrResult
import com.mobile.travelhub.data.model.SettlementResponse
import com.mobile.travelhub.data.model.TripMemberResponse
import com.mobile.travelhub.data.model.TripExpenseSplitShareRequest
import com.mobile.travelhub.data.model.UpdateTripExpenseRequest
import com.mobile.travelhub.data.model.TripExpenseContributionResponse
import com.mobile.travelhub.data.model.TripExpenseTransactionResponse
import com.mobile.travelhub.data.ocr.ReceiptOcrService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CostEstimateUiState(
    val isLoading: Boolean = true,
    val isAddingExpense: Boolean = false,
    val tripId: Long = -1L,
    val groupName: String = "",
    val totalSpent: Double = 0.0,
    val budgetMin: Double? = null,
    val budgetMax: Double? = null,
    val myBalance: Double = 0.0,
    val contributions: List<TripExpenseContributionUiModel> = emptyList(),
    val transactions: List<ExpenseTransactionUiModel> = emptyList(),
    val members: List<TripExpenseMemberUiModel> = emptyList(),
    val errorMessage: String? = null,
    val isCompleted: Boolean = false,
    val canFinishTrip: Boolean = false,
    val isFinishingTrip: Boolean = false,
    val currentUserId: Long = -1L,
    val settlements: List<SettlementUiModel> = emptyList(),
    val isScanningReceipt: Boolean = false,
    val receiptOcrDraft: ReceiptOcrDraft? = null
)

data class TripExpenseContributionUiModel(
    val userId: Long,
    val userName: String,
    val avatarUrl: String? = null,
    val amountPaid: Double,
    val percentage: Double
)

data class ExpenseTransactionUiModel(
    val id: Long,
    val title: String,
    val category: String,
    val paidByUserId: Long,
    val paidByName: String,
    val amount: Double,
    val date: String? = null,
    val proofImageUrl: String? = null,
    val splitType: String = "EQUAL",
    val splitUserIds: List<Long> = emptyList(),
    val splitShares: List<ExpenseSplitShareUiModel> = emptyList()
)

data class ExpenseSplitShareUiModel(
    val userId: Long,
    val amount: Double
)

data class TripExpenseMemberUiModel(
    val userId: Long,
    val name: String,
    val avatarUrl: String? = null,
    val isCurrentUser: Boolean = false
)

data class ReceiptOcrDraft(
    val imageUri: Uri,
    val result: ReceiptOcrResult
)

data class SettlementUiModel(
    val id: Long,
    val fromUserId: Long,
    val toUserId: Long,
    val amount: Double,
    val status: String,
    val transferContent: String,
    val receiverBankCode: String?,
    val receiverBankName: String?,
    val receiverAccountNumber: String?,
    val receiverAccountName: String?
) {
    fun isPayableBy(userId: Long): Boolean = userId > 0L && fromUserId == userId
}

@HiltViewModel
class CostEstimateViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository,
    private val expenseProofRepository: ExpenseProofRepository,
    private val receiptOcrService: ReceiptOcrService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CostEstimateUiState())
    val uiState: StateFlow<CostEstimateUiState> = _uiState.asStateFlow()

    fun loadExpenseSummary(tripId: Long, groupName: String) {
        if (tripId <= 0L) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    tripId = tripId,
                    groupName = groupName,
                    errorMessage = "Không xác định được chuyến đi"
                )
            }
            return
        }

        val cachedExpenses = tripRepository.getCachedTripExpenses(tripId)
        val cachedDetail = tripRepository.getCachedTripDetail(tripId)
        val cachedSettlements = tripRepository.getCachedTripSettlements(tripId)
        val currentUserId = authRepository.getSavedSession()?.userId?.toLong() ?: -1L

        viewModelScope.launch {
            val isSameTrip = _uiState.value.tripId == tripId
            val hasData = isSameTrip || (cachedExpenses != null)
            
            _uiState.update {
                if (hasData) {
                    val baseState = if (isSameTrip) it else CostEstimateUiState(tripId = tripId, groupName = groupName)
                    val stateWithExpenses = if (cachedExpenses != null) {
                        baseState.copy(
                            totalSpent = cachedExpenses.summary.totalAmount ?: 0.0,
                            myBalance = cachedExpenses.summary.myBalance ?: 0.0,
                            contributions = cachedExpenses.contributions.map { it.toUiModel() },
                            transactions = cachedExpenses.transactions.map { it.toUiModel() },
                            currentUserId = currentUserId
                        )
                    } else baseState.copy(currentUserId = currentUserId)
                    
                    val finalState = if (cachedDetail != null) {
                        val statusCompleted = isBackendCompleted(cachedDetail.tripInfo.status)
                        val isCompleted = statusCompleted || isPastDate(cachedDetail.tripInfo.endDate)
                        stateWithExpenses.copy(
                            budgetMin = cachedDetail.tripInfo.budgetMin,
                            budgetMax = cachedDetail.tripInfo.budgetMax,
                            members = cachedDetail.members.map { it.toUiModel() },
                            isCompleted = isCompleted,
                            canFinishTrip = !statusCompleted && cachedDetail.myRole.equals("LEADER", ignoreCase = true)
                        )
                    } else stateWithExpenses

                    finalState.copy(
                        isLoading = false,
                        errorMessage = null,
                        settlements = cachedSettlements?.map { it.toUiModel() } ?: finalState.settlements
                    )
                } else {
                    CostEstimateUiState(
                        isLoading = true,
                        tripId = tripId,
                        groupName = groupName,
                        currentUserId = currentUserId
                    )
                }
            }

            tripRepository.getTripDetail(tripId)
                .onSuccess { detail ->
                    val statusCompleted = isBackendCompleted(detail.tripInfo.status)
                    val isCompleted = statusCompleted || isPastDate(detail.tripInfo.endDate)
                    _uiState.update { state ->
                        state.copy(
                            budgetMin = detail.tripInfo.budgetMin,
                            budgetMax = detail.tripInfo.budgetMax,
                            groupName = detail.tripInfo.name.ifBlank { state.groupName },
                            members = detail.members.map { it.toUiModel() },
                            isCompleted = isCompleted,
                            canFinishTrip = !statusCompleted && detail.myRole.equals("LEADER", ignoreCase = true),
                            currentUserId = currentUserId
                        )
                    }
                }

            tripRepository.listTripSettlements(tripId)
                .onSuccess { settlements ->
                    _uiState.update { state ->
                        state.copy(settlements = settlements.map { it.toUiModel() })
                    }
                }

            tripRepository.listTripExpenses(tripId)
                .onSuccess { expenseResponse ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            totalSpent = expenseResponse.summary.totalAmount ?: 0.0,
                            myBalance = expenseResponse.summary.myBalance ?: 0.0,
                            contributions = expenseResponse.contributions.map { it.toUiModel() },
                            transactions = expenseResponse.transactions.map { it.toUiModel() },
                            currentUserId = currentUserId
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.userMessage("Không tải được dữ liệu chi phí")
                        )
                    }
                }
        }
    }

    fun finishTrip() {
        val state = _uiState.value
        val tripId = state.tripId
        val groupName = state.groupName

        if (tripId <= 0L) {
            _uiState.update { it.copy(errorMessage = "Không xác định được chuyến đi") }
            return
        }
        if (!state.canFinishTrip) {
            _uiState.update { it.copy(errorMessage = "Chỉ trưởng nhóm có thể kết thúc chuyến đi") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isFinishingTrip = true, errorMessage = null) }
            tripRepository.finishTrip(tripId)
                .onSuccess { settlements ->
                    _uiState.update {
                        it.copy(
                            isFinishingTrip = false,
                            isCompleted = true,
                            canFinishTrip = false,
                            settlements = settlements.map { settlement -> settlement.toUiModel() }
                        )
                    }
                    loadExpenseSummary(tripId, groupName)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isFinishingTrip = false,
                            errorMessage = throwable.userMessage("Không kết thúc được chuyến đi")
                        )
                    }
                }
        }
    }

    fun addExpense(
        title: String,
        amountText: String,
        category: String,
        splitType: String,
        splitUserIds: List<Long>,
        splitShares: List<ExpenseSplitShareUiModel>,
        proofImageUri: Uri? = null,
        onResult: ((Boolean, String?) -> Unit)? = null
    ) {
        val state = _uiState.value
        val tripId = state.tripId
        val groupName = state.groupName
        val parsedAmount = amountText.replace(".", "").trim().toDoubleOrNull()
        val sessionUserId = authRepository.getSavedSession()?.userId?.toLong() ?: -1L

        if (tripId <= 0L) {
            val err = "Không xác định được chuyến đi"
            onResult?.invoke(false, err)
            return
        }
        if (state.isCompleted) {
            val err = "Không thể thêm chi phí cho chuyến đi đã hoàn thành"
            onResult?.invoke(false, err)
            return
        }
        if (title.trim().isBlank() || parsedAmount == null || parsedAmount <= 0.0) {
            val err = "Vui lòng nhập tên và số tiền hợp lệ"
            onResult?.invoke(false, err)
            return
        }
        if (sessionUserId <= 0L) {
            val err = "Bạn cần đăng nhập để thêm chi phí"
            onResult?.invoke(false, err)
            return
        }
        val effectiveSplitUserIds = splitUserIds.distinct().filter { it > 0L }
        if (effectiveSplitUserIds.isEmpty()) {
            val err = "Vui lòng chọn ít nhất một người cùng chia"
            onResult?.invoke(false, err)
            return
        }
        val (effectiveSplitShares, splitSharesError) = normalizeSplitShares(splitType, parsedAmount, effectiveSplitUserIds, splitShares)
        if (effectiveSplitShares == null) {
            val err = splitSharesError ?: "Cấu hình chia tiền không hợp lệ"
            onResult?.invoke(false, err)
            return
        }

        viewModelScope.launch {
            val pendingId = -System.currentTimeMillis()
            val pendingExpense = buildPendingExpense(
                id = pendingId,
                title = title.trim(),
                category = category,
                paidByUserId = sessionUserId,
                amount = parsedAmount,
                proofImageUrl = proofImageUri?.toString(),
                splitUserIds = effectiveSplitUserIds,
                splitType = normalizeSplitType(splitType),
                splitShares = effectiveSplitShares
            )
            addPendingExpense(pendingExpense)
            val proofObjectName = if (proofImageUri != null) {
                expenseProofRepository.uploadProof(proofImageUri)
                    .getOrElse { throwable ->
                        val err = throwable.userMessage("Không tải được ảnh minh chứng")
                        removePendingExpense(
                            pendingId = pendingId,
                            amount = parsedAmount,
                            errorMessage = err,
                            updateGlobalError = false
                        )
                        onResult?.invoke(false, err)
                        return@launch
                    }
            } else {
                null
            }
            tripRepository.addTripExpense(
                tripId = tripId,
                request = CreateTripExpenseRequest(
                    title = title.trim(),
                    amount = parsedAmount,
                    totalAmount = parsedAmount,
                    category = category,
                    paidByUserId = sessionUserId,
                    proofImageUrl = proofObjectName,
                    splitType = normalizeSplitType(splitType),
                    splitShares = effectiveSplitShares.map { TripExpenseSplitShareRequest(it.userId, it.amount) },
                    splitUserIds = effectiveSplitUserIds
                )
            ).onSuccess { savedExpense ->
                replacePendingExpense(pendingId, savedExpense.toUiModel())
                loadExpenseSummary(tripId, groupName)
                onResult?.invoke(true, null)
            }.onFailure { throwable ->
                val err = throwable.userMessage("Không thêm được chi phí")
                removePendingExpense(
                    pendingId = pendingId,
                    amount = parsedAmount,
                    errorMessage = err,
                    updateGlobalError = false
                )
                onResult?.invoke(false, err)
            }
        }
    }

    fun editExpense(
        expenseId: Long,
        title: String,
        amountText: String,
        category: String,
        paidByUserId: Long,
        existingProofImageUrl: String?,
        splitType: String,
        splitUserIds: List<Long>,
        splitShares: List<ExpenseSplitShareUiModel>,
        proofImageUri: Uri? = null,
        onResult: ((Boolean, String?) -> Unit)? = null
    ) {
        val state = _uiState.value
        val tripId = state.tripId
        val groupName = state.groupName
        val parsedAmount = amountText.replace(".", "").trim().toDoubleOrNull()

        if (tripId <= 0L) {
            val err = "Không xác định được chuyến đi"
            onResult?.invoke(false, err)
            return
        }
        if (state.isCompleted) {
            val err = "Không thể chỉnh sửa chi phí cho chuyến đi đã hoàn thành"
            onResult?.invoke(false, err)
            return
        }
        if (title.trim().isBlank() || parsedAmount == null || parsedAmount <= 0.0) {
            val err = "Vui lòng nhập tên và số tiền hợp lệ"
            onResult?.invoke(false, err)
            return
        }
        if (paidByUserId <= 0L) {
            val err = "Không xác định được người thanh toán"
            onResult?.invoke(false, err)
            return
        }
        val effectiveSplitUserIds = splitUserIds.distinct().filter { it > 0L }
        if (effectiveSplitUserIds.isEmpty()) {
            val err = "Vui lòng chọn ít nhất một người cùng chia"
            onResult?.invoke(false, err)
            return
        }
        val (effectiveSplitShares, splitSharesError) = normalizeSplitShares(splitType, parsedAmount, effectiveSplitUserIds, splitShares)
        if (effectiveSplitShares == null) {
            val err = splitSharesError ?: "Cấu hình chia tiền không hợp lệ"
            onResult?.invoke(false, err)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingExpense = true, errorMessage = null) }
            val proofImageValue = if (proofImageUri != null) {
                expenseProofRepository.uploadProof(proofImageUri)
                    .getOrElse { throwable ->
                        val err = throwable.userMessage("Không tải được ảnh minh chứng")
                        _uiState.update {
                            it.copy(
                                isAddingExpense = false
                            )
                        }
                        onResult?.invoke(false, err)
                        return@launch
                    }
            } else {
                existingProofImageUrl
            }
            tripRepository.updateTripExpense(
                tripId = tripId,
                expenseId = expenseId,
                request = UpdateTripExpenseRequest(
                    title = title.trim(),
                    amount = parsedAmount,
                    totalAmount = parsedAmount,
                    category = category,
                    paidByUserId = paidByUserId,
                    proofImageUrl = proofImageValue,
                    splitType = normalizeSplitType(splitType),
                    splitShares = effectiveSplitShares.map { TripExpenseSplitShareRequest(it.userId, it.amount) },
                    splitUserIds = effectiveSplitUserIds
                )
            ).onSuccess {
                _uiState.update { it.copy(isAddingExpense = false) }
                loadExpenseSummary(tripId, groupName)
                onResult?.invoke(true, null)
            }.onFailure { throwable ->
                val err = throwable.userMessage("Không cập nhật được chi phí")
                _uiState.update {
                    it.copy(
                        isAddingExpense = false
                    )
                }
                onResult?.invoke(false, err)
            }
        }
    }

    fun scanReceipt(imageUri: Uri) {
        val state = _uiState.value
        if (state.isCompleted) {
            _uiState.update { it.copy(errorMessage = "Không thể thêm chi phí cho chuyến đi đã hoàn thành") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isScanningReceipt = true, errorMessage = null) }
            runCatching { receiptOcrService.scanReceipt(imageUri) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isScanningReceipt = false,
                            receiptOcrDraft = ReceiptOcrDraft(imageUri = imageUri, result = result)
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isScanningReceipt = false,
                            errorMessage = throwable.userMessage("Không đọc được hóa đơn")
                        )
                    }
                }
        }
    }

    fun dismissReceiptOcrDraft() {
        _uiState.update { it.copy(receiptOcrDraft = null, isScanningReceipt = false) }
    }

    fun submitReceiptOcrExpense(
        title: String,
        amountText: String,
        category: String,
        expenseDate: String,
        note: String?,
        paidByUserId: Long,
        splitType: String,
        splitUserIds: List<Long>,
        splitShares: List<ExpenseSplitShareUiModel>
    ) {
        val state = _uiState.value
        val draft = state.receiptOcrDraft
        val tripId = state.tripId
        val groupName = state.groupName
        val parsedAmount = amountText.replace(".", "").trim().toDoubleOrNull()

        if (draft == null) {
            _uiState.update { it.copy(errorMessage = "Không có dữ liệu OCR để lưu") }
            return
        }
        if (tripId <= 0L) {
            _uiState.update { it.copy(errorMessage = "Không xác định được chuyến đi") }
            return
        }
        if (state.isCompleted) {
            _uiState.update { it.copy(errorMessage = "Không thể thêm chi phí cho chuyến đi đã hoàn thành") }
            return
        }
        if (title.trim().isBlank() || parsedAmount == null || parsedAmount <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên và số tiền hợp lệ") }
            return
        }
        if (paidByUserId <= 0L) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn người đã trả") }
            return
        }
        val effectiveSplitUserIds = splitUserIds.distinct().filter { it > 0L }
        if (effectiveSplitUserIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn ít nhất một người cùng chia") }
            return
        }
        val (effectiveSplitShares, splitSharesError) = normalizeSplitShares(splitType, parsedAmount, effectiveSplitUserIds, splitShares)
        if (effectiveSplitShares == null) {
            _uiState.update { it.copy(errorMessage = splitSharesError ?: "Cấu hình chia tiền không hợp lệ") }
            return
        }

        viewModelScope.launch {
            val pendingId = -System.currentTimeMillis()
            val effectiveExpenseDate = expenseDate.ifBlank { LocalDate.now().toString() }
            val pendingExpense = buildPendingExpense(
                id = pendingId,
                title = title.trim(),
                category = category,
                paidByUserId = paidByUserId,
                amount = parsedAmount,
                proofImageUrl = draft.imageUri.toString(),
                splitUserIds = effectiveSplitUserIds,
                splitType = normalizeSplitType(splitType),
                splitShares = effectiveSplitShares,
                date = effectiveExpenseDate
            )
            addPendingExpense(pendingExpense, clearReceiptDraft = true)
            val proofObjectName = expenseProofRepository.uploadProof(draft.imageUri)
                .getOrElse { throwable ->
                    removePendingExpense(
                        pendingId = pendingId,
                        amount = parsedAmount,
                        errorMessage = throwable.userMessage("Không tải được ảnh hóa đơn")
                    )
                    return@launch
                }
            tripRepository.addTripExpense(
                tripId = tripId,
                request = CreateTripExpenseRequest(
                    title = title.trim(),
                    amount = parsedAmount,
                    totalAmount = parsedAmount,
                    category = category,
                    paidByUserId = paidByUserId,
                    expenseDate = effectiveExpenseDate,
                    note = note?.trim()?.ifBlank { null },
                    source = "OCR",
                    rawOcrText = draft.result.rawText,
                    proofImageUrl = proofObjectName,
                    splitType = normalizeSplitType(splitType),
                    splitShares = effectiveSplitShares.map { TripExpenseSplitShareRequest(it.userId, it.amount) },
                    splitUserIds = effectiveSplitUserIds
                )
            ).onSuccess { savedExpense ->
                replacePendingExpense(pendingId, savedExpense.toUiModel())
                loadExpenseSummary(tripId, groupName)
            }.onFailure { throwable ->
                removePendingExpense(
                    pendingId = pendingId,
                    amount = parsedAmount,
                    errorMessage = throwable.userMessage("Không lưu được chi phí từ hóa đơn")
                )
            }
        }
    }

    fun deleteExpense(expenseId: Long) {
        val state = _uiState.value
        val tripId = state.tripId
        val groupName = state.groupName

        if (tripId <= 0L) {
            _uiState.update { it.copy(errorMessage = "Không xác định được chuyến đi") }
            return
        }
        if (state.isCompleted) {
            _uiState.update { it.copy(errorMessage = "Không thể xóa chi phí cho chuyến đi đã hoàn thành") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            tripRepository.deleteTripExpense(tripId, expenseId)
                .onSuccess {
                    loadExpenseSummary(tripId, groupName)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.userMessage("Không xóa được chi phí")
                        )
                    }
                }
        }
    }

    private fun TripExpenseContributionResponse.toUiModel(): TripExpenseContributionUiModel {
        return TripExpenseContributionUiModel(
            userId = userId ?: -1L,
            userName = userName.orEmpty(),
            avatarUrl = avatarUrl,
            amountPaid = amountPaid ?: 0.0,
            percentage = percentage ?: 0.0
        )
    }

    private fun TripMemberResponse.toUiModel(): TripExpenseMemberUiModel {
        val currentUserId = authRepository.getSavedSession()?.userId?.toLong()
        return TripExpenseMemberUiModel(
            userId = userId,
            name = name,
            avatarUrl = avatarUrl,
            isCurrentUser = currentUserId == userId
        )
    }

    private fun SettlementResponse.toUiModel(): SettlementUiModel {
        val receiverInfo = receiver
        return SettlementUiModel(
            id = id ?: -1L,
            fromUserId = fromUserId ?: -1L,
            toUserId = toUserId ?: -1L,
            amount = amount ?: 0.0,
            status = status.orEmpty(),
            transferContent = transferContent.orEmpty(),
            receiverBankCode = receiverInfo?.bankCode,
            receiverBankName = receiverInfo?.bankName,
            receiverAccountNumber = receiverInfo?.accountNumber,
            receiverAccountName = receiverInfo?.accountName
        )
    }

    private fun isBackendCompleted(status: String?): Boolean {
        return status.equals("COMPLETED", ignoreCase = true) ||
                status?.contains("hoàn thành", ignoreCase = true) == true
    }

    private fun isPastDate(dateText: String?): Boolean {
        if (dateText.isNullOrBlank()) return false
        val normalized = dateText.substringBefore("T")
        val date = runCatching { LocalDate.parse(normalized) }
            .recoverCatching {
                LocalDate.parse(
                    normalized,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                )
            }
            .getOrNull() ?: return false
        return date.isBefore(LocalDate.now())
    }

    private fun TripExpenseTransactionResponse.toUiModel(): ExpenseTransactionUiModel {
        return ExpenseTransactionUiModel(
            id = id ?: -1L,
            title = title.orEmpty(),
            category = category ?: "ENTRY",
            paidByUserId = paidByUserId ?: -1L,
            paidByName = paidByName.orEmpty(),
            amount = amount ?: 0.0,
            date = date,
            proofImageUrl = proofImageUrl,
            splitType = splitType ?: "EQUAL",
            splitUserIds = splitUserIds,
            splitShares = splitShares.map {
                ExpenseSplitShareUiModel(
                    userId = it.userId ?: -1L,
                    amount = it.amount ?: 0.0
                )
            }.filter { it.userId > 0L && it.amount > 0.0 }
        )
    }

    private fun buildPendingExpense(
        id: Long,
        title: String,
        category: String,
        paidByUserId: Long,
        amount: Double,
        proofImageUrl: String?,
        splitUserIds: List<Long>,
        splitType: String = "EQUAL",
        splitShares: List<ExpenseSplitShareUiModel> = emptyList(),
        date: String = LocalDate.now().toString()
    ): ExpenseTransactionUiModel {
        val paidByName = _uiState.value.members
            .firstOrNull { it.userId == paidByUserId }
            ?.name
            .orEmpty()
            .ifBlank { "Bạn" }
        return ExpenseTransactionUiModel(
            id = id,
            title = title,
            category = category,
            paidByUserId = paidByUserId,
            paidByName = paidByName,
            amount = amount,
            date = date,
            proofImageUrl = proofImageUrl,
            splitType = splitType,
            splitUserIds = splitUserIds,
            splitShares = splitShares
        )
    }

    private fun normalizeSplitType(splitType: String): String {
        return if (splitType.equals("CUSTOM", ignoreCase = true)) "CUSTOM" else "EQUAL"
    }

    private fun normalizeSplitShares(
        splitType: String,
        totalAmount: Double,
        splitUserIds: List<Long>,
        splitShares: List<ExpenseSplitShareUiModel>
    ): Pair<List<ExpenseSplitShareUiModel>?, String?> {
        if (!splitType.equals("CUSTOM", ignoreCase = true)) {
            return Pair(emptyList(), null)
        }
        val selectedUserIds = splitUserIds.toSet()
        val effectiveShares = splitShares
            .filter { it.userId in selectedUserIds && it.amount > 0.0 }
            .groupBy { it.userId }
            .map { (userId, shares) ->
                ExpenseSplitShareUiModel(userId = userId, amount = shares.sumOf { it.amount })
            }
        if (effectiveShares.size != selectedUserIds.size) {
            return Pair(null, "Vui lòng nhập số tiền cho từng người cùng chia")
        }
        val totalSplitAmount = effectiveShares.sumOf { it.amount }
        if (kotlin.math.abs(totalSplitAmount - totalAmount) > 0.5) {
            return Pair(null, "Tổng tiền chia phải bằng tổng tiền hóa đơn")
        }
        if (effectiveShares.any { it.amount > totalAmount }) {
            return Pair(null, "Số tiền chia của một người không được vượt quá tổng tiền")
        }
        return Pair(effectiveShares, null)
    }

    private fun addPendingExpense(
        expense: ExpenseTransactionUiModel,
        clearReceiptDraft: Boolean = false
    ) {
        _uiState.update { state ->
            state.copy(
                isAddingExpense = true,
                errorMessage = null,
                receiptOcrDraft = if (clearReceiptDraft) null else state.receiptOcrDraft,
                totalSpent = state.totalSpent + expense.amount,
                transactions = listOf(expense) + state.transactions
            )
        }
    }

    private fun replacePendingExpense(pendingId: Long, savedExpense: ExpenseTransactionUiModel) {
        _uiState.update { state ->
            state.copy(
                isAddingExpense = false,
                transactions = state.transactions.map { expense ->
                    if (expense.id == pendingId) savedExpense else expense
                }
            )
        }
    }

    private fun removePendingExpense(
        pendingId: Long,
        amount: Double,
        errorMessage: String,
        updateGlobalError: Boolean = true
    ) {
        _uiState.update { state ->
            state.copy(
                isAddingExpense = false,
                errorMessage = if (updateGlobalError) errorMessage else state.errorMessage,
                totalSpent = (state.totalSpent - amount).coerceAtLeast(0.0),
                transactions = state.transactions.filterNot { it.id == pendingId }
            )
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
