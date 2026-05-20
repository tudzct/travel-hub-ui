package com.mobile.travelhub.ui.components.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mobile.travelhub.data.model.ItineraryEvent
import com.mobile.travelhub.data.model.ItineraryEventColors
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceContainerLow
import com.mobile.travelhub.ui.theme.SurfaceContainerLowest
import com.mobile.travelhub.ui.theme.SunsetOrange
import com.mobile.travelhub.data.model.ItineraryIcons
import com.mobile.travelhub.data.model.getItineraryIcon
import com.mobile.travelhub.viewmodels.ItineraryDayOption
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItineraryEventEditorDialog(
    event: ItineraryEvent,
    dayCount: Int,
    dayOptions: List<ItineraryDayOption> = emptyList(),
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onSave: (ItineraryEvent) -> Unit,
    onDelete: () -> Unit
) {
    var selectedDay by remember(event.eventId) { mutableStateOf(event.dayIndex) }
    val effectiveDayOptions = remember(dayOptions, dayCount, event.dayIndex) {
        dayOptions.ifEmpty {
            val effectiveDayCount = maxOf(dayCount, event.dayIndex)
            (1..effectiveDayCount).map { day ->
                ItineraryDayOption(
                    dayIndex = day,
                    label = "Day $day",
                    dateLabel = "",
                    epochDay = null
                )
            }
        }
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var timePickerTarget by remember { mutableStateOf<TimePickerTarget?>(null) }
    var datePickerErrorMessage by remember { mutableStateOf<String?>(null) }
    val selectedDayOption = effectiveDayOptions.firstOrNull { it.dayIndex == selectedDay }
        ?: effectiveDayOptions.firstOrNull()
    val zoneId = remember { ZoneId.systemDefault() }
    val selectedDateMillis = selectedDayOption?.epochDay
        ?.let { LocalDate.ofEpochDay(it).atStartOfDay(zoneId).toInstant().toEpochMilli() }
    val selectableEpochDays = remember(effectiveDayOptions) {
        effectiveDayOptions.mapNotNull { it.epochDay }.toSet()
    }
    var startTime by remember(event.eventId) { mutableStateOf(event.startTime) }
    var endTime by remember(event.eventId) { mutableStateOf(event.endTime) }
    var title by remember(event.eventId) { mutableStateOf(event.title) }
    var placeName by remember(event.eventId) { mutableStateOf(event.placeName) }
    var note by remember(event.eventId) { mutableStateOf(event.note) }
    var transport by remember(event.eventId) { mutableStateOf(event.transportToNext) }
    var cost by remember(event.eventId) { mutableStateOf(event.estimatedCost) }
    var colorHex by remember(event.eventId) { mutableStateOf(event.colorHex) }
    var iconName by remember(event.eventId) { mutableStateOf(event.iconName) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(32.dp),
            color = SurfaceContainerLowest
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                Text(
                    text = if (isCreating) "Add itinerary" else "Edit stop",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ngày",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = {
                            datePickerErrorMessage = null
                            showDatePicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = selectedDayOption?.let { option ->
                                listOf(option.label, option.dateLabel)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" - ")
                            } ?: "Chọn ngày"
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimePickerField(
                        value = startTime,
                        label = "Start",
                        modifier = Modifier.weight(1f),
                        onClick = { timePickerTarget = TimePickerTarget.START }
                    )
                    TimePickerField(
                        value = endTime,
                        label = "End",
                        modifier = Modifier.weight(1f),
                        onClick = { timePickerTarget = TimePickerTarget.END }
                    )
                }

                ItineraryEditorField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Title"
                )
                ItineraryEditorField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    label = "Địa điểm"
                )
                ItineraryEditorField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = "Estimate cost"
                )

                if (!isCreating) {
                    ItineraryEditorField(
                        value = note,
                        onValueChange = { note = it },
                        label = "Note",
                        minLines = 3
                    )
                    ItineraryEditorField(
                        value = transport,
                        onValueChange = { transport = it },
                        label = "Transport to next"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Color",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ItineraryEventColors.Palette.forEach { option ->
                                EventColorSwatch(
                                    colorHex = option,
                                    selected = colorHex == option,
                                    onClick = { colorHex = option }
                                )
                            }
                        }
                    }

                    @OptIn(ExperimentalLayoutApi::class)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Icon",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ItineraryIcons.Palette.forEach { option ->
                                EventIconSwatch(
                                    iconName = option,
                                    selected = iconName == option,
                                    onClick = { iconName = option }
                                )
                            }
                        }
                    }
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!isCreating) {
                        TextButton(onClick = onDelete) {
                            Text("Delete stop", color = SunsetOrange)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            onSave(
                                event.copy(
                                    dayIndex = selectedDay,
                                    startTime = startTime.trim(),
                                    endTime = endTime.trim(),
                                    title = title.trim(),
                                    placeName = placeName.trim(),
                                    note = note.trim(),
                                    transportToNext = transport.trim(),
                                    estimatedCost = cost.trim(),
                                    colorHex = colorHex,
                                    iconName = iconName
                                )
                            )
                        }
                    ) {
                        Text(if (isCreating) "Add" else "Save changes")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val yearRange = remember(effectiveDayOptions) {
            val dates = effectiveDayOptions.mapNotNull { it.epochDay?.let(LocalDate::ofEpochDay) }
            val first = dates.minOrNull()
            val last = dates.maxOrNull()
            if (first != null && last != null) first.year..last.year else 1900..2100
        }
        val selectableDates = remember(selectableEpochDays, yearRange, zoneId) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    if (selectableEpochDays.isEmpty()) return true
                    val epochDay = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(zoneId)
                        .toLocalDate()
                        .toEpochDay()
                    return epochDay in selectableEpochDays
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year in yearRange
                }
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            yearRange = yearRange,
            selectableDates = selectableDates
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        val selectedDate = selectedMillis
                            ?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }
                        val option = selectedDate?.let { date ->
                            effectiveDayOptions.firstOrNull { it.epochDay == date.toEpochDay() }
                        }
                        if (option != null) {
                            selectedDay = option.dayIndex
                            datePickerErrorMessage = null
                            showDatePicker = false
                        } else {
                            datePickerErrorMessage = "Vui lòng chọn ngày trong khoảng thời gian của trip"
                        }
                    }
                ) {
                    Text("Chọn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            Column {
                DatePicker(state = datePickerState)
                datePickerErrorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    timePickerTarget?.let { target ->
        val initialTime = parseEditorTime(if (target == TimePickerTarget.START) startTime else endTime)
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.first,
            initialMinute = initialTime.second,
            is24Hour = true
        )
        Dialog(onDismissRequest = { timePickerTarget = null }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = SurfaceContainerLowest
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (target == TimePickerTarget.START) "Chọn giờ bắt đầu" else "Chọn giờ kết thúc",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { timePickerTarget = null }) {
                            Text("Hủy")
                        }
                        TextButton(
                            onClick = {
                                val selected = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                                if (target == TimePickerTarget.START) {
                                    startTime = selected
                                } else {
                                    endTime = selected
                                }
                                timePickerTarget = null
                            }
                        ) {
                            Text("Chọn")
                        }
                    }
                }
            }
        }
    }
}

private enum class TimePickerTarget {
    START,
    END
}

@Composable
private fun EventColorSwatch(
    colorHex: Long,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = colorHex.toItineraryColor()
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) OnSurface else color.copy(alpha = 0.22f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ItineraryEditorField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        minLines = minLines
    )
}

@Composable
private fun TimePickerField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(value.ifBlank { "--:--" })
        }
    }
}

private fun parseEditorTime(value: String): Pair<Int, Int> {
    val parts = value.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 9
    val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    return hour to minute
}

@Composable
private fun EventIconSwatch(
    iconName: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) PrimaryBlue.copy(alpha = 0.12f) else SurfaceContainerLow)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) PrimaryBlue else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getItineraryIcon(iconName),
            contentDescription = null,
            tint = if (selected) PrimaryBlue else OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
