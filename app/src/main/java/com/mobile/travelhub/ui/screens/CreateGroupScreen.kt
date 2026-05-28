package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.ui.components.EditProfileField
import com.mobile.travelhub.ui.components.PrimaryProfileButton
import com.mobile.travelhub.viewmodels.CreateGroupViewModel
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
            Text(
                text = "TỈNH",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            CompactSelectionDropdown(
                selectedValue = uiState.selectedProvince?.name.orEmpty(),
                options = uiState.provinces,
                optionLabel = { it.name },
                onOptionSelected = { viewModel.selectProvince(it.id) },
                enabled = !uiState.isSaving && uiState.provinces.isNotEmpty(),
                placeholder = ""
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "ĐIỂM ĐẾN",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            CompactSelectionDropdown(
                selectedValue = uiState.selectedPlace?.let { "${it.name} • ${it.province.name}" }.orEmpty(),
                options = uiState.places,
                optionLabel = { "${it.name} • ${it.province.name}" },
                onOptionSelected = { viewModel.selectPlace(it.id) },
                enabled = !uiState.isSaving && uiState.selectedProvinceId != null && uiState.places.isNotEmpty(),
                placeholder = ""
            )

            if (uiState.isLoadingLocations) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Đang tải danh sách điểm đến...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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
                placeholder = "dd/mm/yyyy - dd/mm/yyyy",
                onClick = {
                    focusManager.clearFocus()
                    dateSelectionStep = TripDateSelectionStep.START
                }
            )

            EditProfileField(
                label = "Ngân sách tối thiểu",
                value = uiState.budgetMin,
                onValueChange = viewModel::updateBudgetMin,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            EditProfileField(
                label = "Ngân sách tối đa",
                value = uiState.budgetMax,
                onValueChange = viewModel::updateBudgetMax,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
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

        val initialDate = when (selectedStep) {
            TripDateSelectionStep.START -> startDateObj ?: today
            TripDateSelectionStep.END -> startDateObj?.plusDays(1) ?: today
            null -> today
        }
        val initialMillis = initialDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

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

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            yearRange = today.year..today.year + 1,
            selectableDates = selectableDates
        )
        var datePickerErrorMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(selectedStep, uiState.startDate, uiState.endDate) {
            datePickerErrorMessage = null
            datePickerState.selectedDateMillis = initialMillis
        }

        DatePickerDialog(
            onDismissRequest = { dateSelectionStep = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis ?: return@TextButton
                        val selectedDate = Instant.ofEpochMilli(selectedMillis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()

                        val isValid = when (currentStep) {
                            TripDateSelectionStep.START -> viewModel.canSelectStartDate(selectedDate)
                            TripDateSelectionStep.END -> {
                                val startDate = currentStartDate
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
                DatePicker(state = datePickerState)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> CompactSelectionDropdown(
    selectedValue: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    enabled: Boolean,
    placeholder: String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true
        )

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 320.dp)
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    }
                )
            }
        }
    }
}
