package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.ui.components.DestinationPlacePicker
import com.mobile.travelhub.ui.components.EditProfileField
import com.mobile.travelhub.ui.components.PrimaryProfileButton
import com.mobile.travelhub.viewmodels.CreateGroupViewModel
import com.mobile.travelhub.utils.NumberUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private enum class TripDateSelectionStep {
    START,
    END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onBack: () -> Unit,
    onCreate: (Long, String) -> Unit,
    viewModel: CreateGroupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var dateSelectionStep by remember { mutableStateOf<TripDateSelectionStep?>(null) }
    val displayDateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    var budgetMaxFieldVal by remember {
        val initialText = uiState.budgetMax
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }

    LaunchedEffect(uiState.budgetMax) {
        if (uiState.budgetMax != budgetMaxFieldVal.text) {
            budgetMaxFieldVal = TextFieldValue(
                text = uiState.budgetMax,
                selection = TextRange(uiState.budgetMax.length)
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "TẠO CHUYẾN ĐI MỚI",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    TextButton(onClick = onBack) {
                        Text("Hủy")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            EditProfileField(
                label = "Tên chuyến đi",
                value = uiState.name,
                onValueChange = viewModel::updateName,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            DestinationPlacePicker(
                label = "Điểm đến",
                selectedProvince = uiState.selectedProvince,
                selectedPlace = uiState.selectedPlace,
                provinces = uiState.provinces,
                places = uiState.places,
                isLoading = uiState.isLoadingLocations,
                enabled = !uiState.isSaving && uiState.provinces.isNotEmpty(),
                placeholder = "Chọn địa điểm",
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                onProvinceSelected = { viewModel.selectProvince(it) },
                onPlaceSelected = { viewModel.selectPlace(it) }
            )

            TripDateRangeFieldInput(
                label = "Ngày đi và ngày về",
                value = when {
                    uiState.startDate.isNotBlank() && uiState.endDate.isNotBlank() -> {
                        "${uiState.startDate} - ${uiState.endDate}"
                    }
                    uiState.startDate.isNotBlank() -> {
                        "${uiState.startDate} - Chọn ngày về"
                    }
                    else -> ""
                },
                placeholder = "",
                onClick = {
                    focusManager.clearFocus()
                    dateSelectionStep = TripDateSelectionStep.START
                }
            )

            EditProfileField(
                label = "Ngân sách dự kiến",
                value = budgetMaxFieldVal,
                onValueChange = { newValue ->
                    val formatted = NumberUtils.formatTextFieldValue(newValue)
                    budgetMaxFieldVal = formatted
                    viewModel.updateBudgetMax(formatted.text)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        if (!uiState.isSaving) {
                            viewModel.createTrip { id -> onCreate(id, uiState.name) }
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            PrimaryProfileButton(
                text = if (uiState.isSaving) "Đang tạo..." else "Tạo chuyến đi",
                onClick = {
                    if (!uiState.isSaving) {
                        focusManager.clearFocus()
                        viewModel.createTrip { id -> onCreate(id, uiState.name) }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (dateSelectionStep != null) {
        val selectedStep = dateSelectionStep
        val startDateObj = parseDisplayDate(uiState.startDate, displayDateFormatter)
        val today = LocalDate.now()
        val currentStep by rememberUpdatedState(selectedStep)
        val currentStartDate by rememberUpdatedState(startDateObj)

        val initialMillis = when (selectedStep) {
            TripDateSelectionStep.START -> {
                startDateObj?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis()
            }
            TripDateSelectionStep.END -> {
                startDateObj?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis()
            }
            null -> System.currentTimeMillis()
        }

        val selectableDates = remember {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val candidateDate = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()

                    return when (currentStep) {
                        TripDateSelectionStep.START -> viewModel.canSelectStartDate(candidateDate)
                        TripDateSelectionStep.END -> {
                            val startDate = currentStartDate ?: return false
                            viewModel.canSelectEndDate(startDate, candidateDate)
                        }
                        null -> false
                    }
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year in today.year..(today.year + 1)
                }
            }
        }

        var selectedDateMillisState by remember { mutableStateOf<Long?>(null) }
        var datePickerErrorMessage by remember { mutableStateOf<String?>(null) }

        DatePickerDialog(
            onDismissRequest = { dateSelectionStep = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = selectedDateMillisState ?: return@TextButton
                        val selectedDate = Instant.ofEpochMilli(selectedMillis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()

                        val isValid = when (currentStep) {
                            TripDateSelectionStep.START -> viewModel.canSelectStartDate(selectedDate)
                            TripDateSelectionStep.END -> {
                                val startDate = startDateObj
                                startDate != null && viewModel.canSelectEndDate(startDate, selectedDate)
                            }
                            null -> false
                        }

                        if (!isValid) {
                            datePickerErrorMessage = when (currentStep) {
                                TripDateSelectionStep.START -> "❌ Ngày đi phải từ hôm nay trở đi"
                                TripDateSelectionStep.END -> "❌ Ngày về phải sau ngày đi ít nhất 1 ngày"
                                null -> "❌ Ngày không hợp lệ"
                            }
                            return@TextButton
                        }

                        val formattedDate = selectedDate.format(displayDateFormatter)
                        when (currentStep) {
                            TripDateSelectionStep.START -> {
                                viewModel.updateStartDate(formattedDate)
                                viewModel.updateEndDate("")
                                dateSelectionStep = TripDateSelectionStep.END
                                datePickerErrorMessage = null
                            }
                            TripDateSelectionStep.END -> {
                                viewModel.updateEndDate(formattedDate)
                                dateSelectionStep = null
                                focusManager.clearFocus()
                                datePickerErrorMessage = null
                            }
                            null -> Unit
                        }
                    }
                ) {
                    Text(if (selectedStep == TripDateSelectionStep.START) "Tiếp tục" else "Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { dateSelectionStep = null }) {
                    Text("Hủy")
                }
            }
        ) {
            Column {
                // Hoisted Custom Header with absolute layout/spacing control
                Column(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = if (currentStep == TripDateSelectionStep.START) "Chọn ngày bắt đầu" else "Chọn ngày kết thúc",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val formattedSelectedDate = selectedDateMillisState?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().format(displayDateFormatter)
                    } ?: ""
                    Text(
                        text = if (formattedSelectedDate.isNotBlank()) formattedSelectedDate else "Chọn ngày",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    thickness = 1.dp
                )

                key(selectedStep, startDateObj) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = initialMillis,
                        yearRange = today.year..today.year + 1,
                        selectableDates = selectableDates
                    )

                    LaunchedEffect(datePickerState.selectedDateMillis) {
                        selectedDateMillisState = datePickerState.selectedDateMillis
                    }

                    DatePicker(
                        state = datePickerState,
                        title = null,
                        headline = null,
                        showModeToggle = false,
                        colors = DatePickerDefaults.colors(
                            todayDateBorderColor = Color.Transparent,
                            todayContentColor = if (currentStep == TripDateSelectionStep.START) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    )
                }
                if (datePickerErrorMessage != null) {
                    Text(
                        text = datePickerErrorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripDateRangeFieldInput(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true
        )
    }
}

private fun parseDisplayDate(value: String, formatter: DateTimeFormatter): LocalDate? {
    if (value.isBlank()) {
        return null
    }

    return runCatching {
        LocalDate.parse(value, formatter)
    }.getOrNull()
}

