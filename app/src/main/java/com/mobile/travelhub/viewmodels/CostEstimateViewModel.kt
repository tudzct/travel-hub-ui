package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.data.TripRepository
import com.mobile.travelhub.data.model.CreateTripExpenseRequest
import com.mobile.travelhub.data.model.UpdateTripExpenseRequest
import com.mobile.travelhub.data.model.TripExpenseContributionResponse
import com.mobile.travelhub.data.model.TripExpenseTransactionResponse
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
    val errorMessage: String? = null,
    val isCompleted: Boolean = false
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
    val date: String? = null
)

@HiltViewModel
class CostEstimateViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val authRepository: AuthRepository
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

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    tripId = tripId,
                    groupName = groupName,
                    errorMessage = null
                )
            }

            tripRepository.getTripDetail(tripId)
                .onSuccess { detail ->
                    val isCompleted = detail.tripInfo.status.equals("COMPLETED", ignoreCase = true) ||
                            detail.tripInfo.status?.contains("hoàn thành", ignoreCase = true) == true ||
                            isPastDate(detail.tripInfo.endDate)
                    _uiState.update { state ->
                        state.copy(
                            budgetMin = detail.tripInfo.budgetMin,
                            budgetMax = detail.tripInfo.budgetMax,
                            groupName = detail.tripInfo.name.ifBlank { state.groupName },
                            isCompleted = isCompleted
                        )
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
                            transactions = expenseResponse.transactions.map { it.toUiModel() }
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Không tải được dữ liệu chi phí"
                        )
                    }
                }
        }
    }

    fun addExpense(title: String, amountText: String, category: String) {
        val state = _uiState.value
        val tripId = state.tripId
        val groupName = state.groupName
        val parsedAmount = amountText.replace(".", "").trim().toDoubleOrNull()
        val sessionUserId = authRepository.getSavedSession()?.userId?.toLong() ?: -1L

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
        if (sessionUserId <= 0L) {
            _uiState.update { it.copy(errorMessage = "Bạn cần đăng nhập để thêm chi phí") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingExpense = true, errorMessage = null) }
            tripRepository.addTripExpense(
                tripId = tripId,
                request = CreateTripExpenseRequest(
                    title = title.trim(),
                    amount = parsedAmount,
                    category = category,
                    paidByUserId = sessionUserId
                )
            ).onSuccess {
                _uiState.update { it.copy(isAddingExpense = false) }
                loadExpenseSummary(tripId, groupName)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isAddingExpense = false,
                        errorMessage = throwable.message ?: "Không thêm được chi phí"
                    )
                }
            }
        }
    }

    fun editExpense(expenseId: Long, title: String, amountText: String, category: String, paidByUserId: Long) {
        val state = _uiState.value
        val tripId = state.tripId
        val groupName = state.groupName
        val parsedAmount = amountText.replace(".", "").trim().toDoubleOrNull()

        if (tripId <= 0L) {
            _uiState.update { it.copy(errorMessage = "Không xác định được chuyến đi") }
            return
        }
        if (state.isCompleted) {
            _uiState.update { it.copy(errorMessage = "Không thể chỉnh sửa chi phí cho chuyến đi đã hoàn thành") }
            return
        }
        if (title.trim().isBlank() || parsedAmount == null || parsedAmount <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên và số tiền hợp lệ") }
            return
        }
        if (paidByUserId <= 0L) {
            _uiState.update { it.copy(errorMessage = "Không xác định được người thanh toán") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingExpense = true, errorMessage = null) }
            tripRepository.updateTripExpense(
                tripId = tripId,
                expenseId = expenseId,
                request = UpdateTripExpenseRequest(
                    title = title.trim(),
                    amount = parsedAmount,
                    category = category,
                    paidByUserId = paidByUserId
                )
            ).onSuccess {
                _uiState.update { it.copy(isAddingExpense = false) }
                loadExpenseSummary(tripId, groupName)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isAddingExpense = false,
                        errorMessage = throwable.message ?: "Không cập nhật được chi phí"
                    )
                }
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
                            errorMessage = throwable.message ?: "Không xóa được chi phí"
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
            date = date
        )
    }
}