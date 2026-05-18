package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class TripDateField {
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
    var activeDateField by remember { mutableStateOf<TripDateField?>(null) }
    val displayDateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val zoneId = remember { ZoneId.systemDefault() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CREATE NEW TRIP",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                label = "Trip Name",
                value = uiState.name,
                onValueChange = viewModel::updateName,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            EditProfileField(
                label = "Destination",
                value = uiState.destination,
                onValueChange = viewModel::updateDestination,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            TripDateFieldInput(
                label = "Start Date",
                value = uiState.startDate,
                placeholder = "DD/MM/YYYY",
                onClick = {
                    focusManager.clearFocus()
                    activeDateField = TripDateField.START
                },
                onFocused = {
                    if (activeDateField == null) {
                        activeDateField = TripDateField.START
                    }
                }
            )

            TripDateFieldInput(
                label = "End Date",
                value = uiState.endDate,
                placeholder = "DD/MM/YYYY",
                onClick = {
                    focusManager.clearFocus()
                    activeDateField = TripDateField.END
                },
                onFocused = {
                    if (activeDateField == null) {
                        activeDateField = TripDateField.END
                    }
                }
            )

            EditProfileField(
                label = "Budget Min",
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
                label = "Budget Max",
                value = uiState.budgetMax,
                onValueChange = viewModel::updateBudgetMax,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            PrimaryProfileButton(
                text = if (uiState.isSaving) "Creating..." else "Create Trip",
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

    if (activeDateField != null) {
        key(activeDateField, uiState.startDate, uiState.endDate) {
            val selectedField = activeDateField
            val initialDate = when (selectedField) {
                TripDateField.START -> parseDisplayDate(uiState.startDate, displayDateFormatter)
                TripDateField.END -> parseDisplayDate(uiState.endDate, displayDateFormatter)
                null -> null
            }
            val initialMillis = initialDate
                ?.atStartOfDay(zoneId)
                ?.toInstant()
                ?.toEpochMilli()

            // Calculate date constraints based on field type
            val today = LocalDate.now()
            val startDateObj = parseDisplayDate(uiState.startDate, displayDateFormatter)
            var datePickerErrorMessage by remember { mutableStateOf<String?>(null) }

            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialMillis,
                yearRange = when (selectedField) {
                    TripDateField.START -> today.year..today.year + 1
                    TripDateField.END -> today.year..today.year + 1
                    null -> today.year..today.year + 1
                }
            )

            DatePickerDialog(
                onDismissRequest = { activeDateField = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                val selectedDate = Instant.ofEpochMilli(selectedMillis)
                                    .atZone(zoneId)
                                    .toLocalDate()

                                // Validate selected date
                                val isValid = when (selectedField) {
                                    TripDateField.START -> !selectedDate.isBefore(today)
                                    TripDateField.END -> {
                                        if (startDateObj == null) true
                                        else !selectedDate.isBefore(startDateObj) && !selectedDate.isAfter(startDateObj.plusDays(60))
                                    }
                                    null -> true
                                }

                                if (isValid) {
                                    val formattedDate = selectedDate.format(displayDateFormatter)
                                    when (selectedField) {
                                        TripDateField.START -> viewModel.updateStartDate(formattedDate)
                                        TripDateField.END -> viewModel.updateEndDate(formattedDate)
                                        null -> Unit
                                    }
                                    activeDateField = null
                                    focusManager.clearFocus()
                                    datePickerErrorMessage = null
                                } else {
                                    // Show error - keep dialog open
                                    datePickerErrorMessage = when (selectedField) {
                                        TripDateField.START -> "❌ Ngày đi phải từ hôm nay trở đi"
                                        TripDateField.END -> "❌ Ngày kết thúc: từ ngày đi đến 60 ngày sau"
                                        null -> "❌ Ngày không hợp lệ"
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Chọn")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeDateField = null }) {
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
}

@Composable
private fun TripDateFieldInput(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    onFocused: () -> Unit
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (value.isBlank()) placeholder else value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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