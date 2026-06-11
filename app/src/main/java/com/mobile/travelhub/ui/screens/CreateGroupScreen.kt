package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.ui.components.DestinationPlacePicker
import com.mobile.travelhub.viewmodels.CreateGroupViewModel
import com.mobile.travelhub.utils.NumberUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.theme.PrimaryBlue

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

    val submitTrip = {
        if (!uiState.isSaving) {
            focusManager.clearFocus()
            keyboardController?.hide()
            viewModel.createTrip { id -> onCreate(id, uiState.name) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tạo chuyến đi mới",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ui_8a09e03d20))
                    }
                },
                actions = {
                    TextButton(
                        onClick = submitTrip,
                        enabled = !uiState.isSaving
                    ) {
                        Text(if (uiState.isSaving) "Đang tạo..." else "Tạo")
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
            CreateTripTextField(
                label = stringResource(R.string.ui_ea611ea5b8),
                value = uiState.name,
                onValueChange = viewModel::updateName,
                placeholder = "Nhập tên chuyến đi",
                leadingIcon = Icons.Default.CardTravel,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            DestinationPlacePicker(
                label = stringResource(R.string.ui_8dc001232f),
                selectedProvince = uiState.selectedProvince,
                selectedPlace = uiState.selectedPlace,
                provinces = uiState.provinces,
                places = uiState.places,
                isLoading = uiState.isLoadingLocations,
                enabled = !uiState.isSaving &&
                    (uiState.provinces.isNotEmpty() || uiState.provinceErrorMessage != null),
                placeholder = stringResource(R.string.ui_9433146e77),
                modifier = Modifier.padding(top = 10.dp, bottom = 14.dp),
                uppercaseLabel = false,
                compactAnchor = true,
                anchorContainerColor = MaterialTheme.colorScheme.surface,
                anchorBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f),
                anchorTrailingIcon = Icons.Rounded.KeyboardArrowDown,
                showCompactIconBackground = false,
                onProvinceSelected = { viewModel.selectProvince(it) },
                onPlaceSelected = { viewModel.selectPlace(it) },
                provinceErrorMessage = uiState.provinceErrorMessage,
                placesErrorMessage = uiState.placesErrorMessage,
                onRetryProvinces = viewModel::retryLoadProvinces,
                onRetryPlaces = viewModel::retryLoadPlaces
            )

            TripDateFieldInput(
                label = "Ngày đi",
                value = uiState.startDate,
                placeholder = "Chọn ngày đi",
                onClick = {
                    focusManager.clearFocus()
                    dateSelectionStep = TripDateSelectionStep.START
                }
            )

            TripDateFieldInput(
                label = "Ngày về",
                value = uiState.endDate,
                placeholder = "Chọn ngày về",
                onClick = {
                    focusManager.clearFocus()
                    dateSelectionStep = if (uiState.startDate.isBlank()) {
                        TripDateSelectionStep.START
                    } else {
                        TripDateSelectionStep.END
                    }
                }
            )

            CreateTripTextField(
                label = "Ngân sách dự kiến (tùy chọn)",
                value = budgetMaxFieldVal,
                onValueChange = { newValue ->
                    val formatted = NumberUtils.formatTextFieldValue(newValue)
                    budgetMaxFieldVal = formatted
                    viewModel.updateBudgetMax(formatted.text)
                },
                placeholder = "Nhập ngân sách dự kiến",
                leadingIcon = Icons.Default.AccountBalanceWallet,
                trailingText = "VND",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        submitTrip()
                    }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
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
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                navigationContentColor = MaterialTheme.colorScheme.onSurface,
                yearContentColor = MaterialTheme.colorScheme.onSurface,
                disabledYearContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                currentYearContentColor = PrimaryBlue,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                disabledSelectedYearContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
                selectedYearContainerColor = PrimaryBlue,
                disabledSelectedYearContainerColor = PrimaryBlue.copy(alpha = 0.28f),
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                disabledSelectedDayContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
                selectedDayContainerColor = PrimaryBlue,
                disabledSelectedDayContainerColor = PrimaryBlue.copy(alpha = 0.28f),
                todayContentColor = PrimaryBlue,
                todayDateBorderColor = PrimaryBlue,
                dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onSurface,
                dayInSelectionRangeContainerColor = PrimaryBlue.copy(alpha = 0.12f)
            ),
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
                                dateSelectionStep = null
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
                    Text("Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { dateSelectionStep = null }) {
                    Text(stringResource(R.string.ui_34ca764caf))
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
                    val datePickerState = rememberVietnameseDatePickerState(
                        initialMillis = initialMillis,
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
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            headlineContentColor = MaterialTheme.colorScheme.onSurface,
                            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            navigationContentColor = MaterialTheme.colorScheme.onSurface,
                            dayContentColor = MaterialTheme.colorScheme.onSurface,
                            disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                            selectedDayContainerColor = PrimaryBlue,
                            disabledSelectedDayContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
                            disabledSelectedDayContainerColor = PrimaryBlue.copy(alpha = 0.28f),
                            todayDateBorderColor = if (currentStep == TripDateSelectionStep.START) PrimaryBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                            todayContentColor = if (currentStep == TripDateSelectionStep.START) PrimaryBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            yearContentColor = MaterialTheme.colorScheme.onSurface,
                            disabledYearContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            currentYearContentColor = PrimaryBlue,
                            selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledSelectedYearContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
                            selectedYearContainerColor = PrimaryBlue,
                            disabledSelectedYearContainerColor = PrimaryBlue.copy(alpha = 0.28f),
                            dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onSurface,
                            dayInSelectionRangeContainerColor = PrimaryBlue.copy(alpha = 0.12f)
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
private fun rememberVietnameseDatePickerState(
    initialMillis: Long,
    yearRange: IntRange,
    selectableDates: SelectableDates
): DatePickerState {
    return remember {
        DatePickerState(
            locale = Locale("vi", "VN"),
            initialSelectedDateMillis = initialMillis,
            initialDisplayedMonthMillis = initialMillis,
            yearRange = yearRange,
            selectableDates = selectableDates
        )
    }
}


@Composable
private fun CreateTripTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .padding(vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        CreateTripFieldSurface {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CreateTripFieldIcon(leadingIcon)
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.text.isBlank()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.46f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (trailingText != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = trailingText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateTripTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    CreateTripTextField(
        label = label,
        value = fieldValue,
        onValueChange = {
            fieldValue = it
            onValueChange(it.text)
        },
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

@Composable
private fun TripDateFieldInput(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        val interactionSource = remember { MutableInteractionSource() }
        CreateTripFieldSurface(
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CreateTripFieldIcon(Icons.Default.CalendarMonth)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = value.ifBlank { placeholder },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.46f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CreateTripFieldSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f),
                shape = RoundedCornerShape(22.dp)
            )
            .then(modifier),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = content
    )
}

@Composable
private fun CreateTripFieldIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
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
