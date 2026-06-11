package com.mobile.travelhub.ui.components.itinerary

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mobile.travelhub.data.model.ItineraryEvent
import com.mobile.travelhub.ui.components.SimpleFormTextField
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.OutlineVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceContainerLow
import com.mobile.travelhub.ui.theme.SurfaceContainerLowest
import com.mobile.travelhub.ui.theme.SunsetOrange
import com.mobile.travelhub.viewmodels.ItineraryDayOption
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.mobile.travelhub.R

@OptIn(ExperimentalMaterial3Api::class)
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
    val context = LocalContext.current
    var selectedDay by remember(event.eventId) { mutableStateOf(event.dayIndex) }
    var isDayMenuExpanded by remember { mutableStateOf(false) }
    var expandedTimeTarget by remember { mutableStateOf<TimePickerTarget?>(null) }
    val effectiveDayOptions = remember(dayOptions, dayCount, event.dayIndex) {
        dayOptions.ifEmpty {
            val effectiveDayCount = maxOf(dayCount, event.dayIndex)
            (1..effectiveDayCount).map { day ->
                ItineraryDayOption(
                    dayIndex = day,
                    label = context.getString(R.string.day_number, day),
                    dateLabel = "",
                    epochDay = null
                )
            }
        }
    }
    val selectedDayOption = effectiveDayOptions.firstOrNull { it.dayIndex == selectedDay }
        ?: effectiveDayOptions.firstOrNull()

    var startTime by remember(event.eventId) { mutableStateOf(event.startTime) }
    var endTime by remember(event.eventId) { mutableStateOf(event.endTime) }
    var title by remember(event.eventId) { mutableStateOf(event.title) }
    var placeName by remember(event.eventId) { mutableStateOf(event.placeName) }
    var note by remember(event.eventId) { mutableStateOf(event.note) }
    var transport by remember(event.eventId) { mutableStateOf(event.transportToNext) }
    val timeOptions = remember { buildTimeOptions() }
    val canSave = title.isNotBlank() && selectedDayOption != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(28.dp),
            color = SurfaceContainerLowest
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = if (isCreating) "Thêm activity" else "Sửa activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface
                )

                Box {
                    PickerAnchorField(
                        label = stringResource(R.string.ui_0ab997e2e2),
                        value = selectedDayOption?.dateLabel?.toPickerDateLabel()
                            ?: selectedDayOption?.label
                            ?: "Chọn ngày",
                        icon = Icons.Default.CalendarMonth,
                        expanded = isDayMenuExpanded,
                        onClick = { isDayMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = isDayMenuExpanded,
                        onDismissRequest = { isDayMenuExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.86f)
                            .heightIn(max = 280.dp)
                            .background(SurfaceContainerLowest)
                    ) {
                        effectiveDayOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = OnSurfaceVariant
                                        )
                                        Text(
                                            text = option.dateLabel.toPickerDateLabel()
                                                .ifBlank { "Chưa có ngày" },
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurface
                                        )
                                    }
                                },
                                onClick = {
                                    selectedDay = option.dayIndex
                                    isDayMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        PickerAnchorField(
                            value = startTime.toPickerTimeLabel(),
                            label = stringResource(R.string.ui_8dc3261b77),
                            icon = Icons.Default.Schedule,
                            expanded = expandedTimeTarget == TimePickerTarget.START,
                            onClick = { expandedTimeTarget = TimePickerTarget.START }
                        )
                        TimeDropdownMenu(
                            expanded = expandedTimeTarget == TimePickerTarget.START,
                            options = timeOptions,
                            selected = startTime,
                            onDismiss = { expandedTimeTarget = null },
                            onSelect = { selected ->
                                startTime = selected
                                if (endTime.isBlank() || endTime <= selected) {
                                    endTime = nextTimeOption(timeOptions, selected)
                                }
                                expandedTimeTarget = null
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PickerAnchorField(
                            value = endTime.toPickerTimeLabel(),
                            label = stringResource(R.string.ui_4923a13d6c),
                            icon = Icons.Default.Schedule,
                            expanded = expandedTimeTarget == TimePickerTarget.END,
                            onClick = { expandedTimeTarget = TimePickerTarget.END }
                        )
                        TimeDropdownMenu(
                            expanded = expandedTimeTarget == TimePickerTarget.END,
                            options = timeOptions,
                            selected = endTime,
                            onDismiss = { expandedTimeTarget = null },
                            onSelect = { selected ->
                                endTime = selected
                                expandedTimeTarget = null
                            }
                        )
                    }
                }

                ItineraryEditorField(
                    value = title,
                    onValueChange = { title = it },
                    label = stringResource(R.string.ui_f91b97aefb)
                )

                ItineraryEditorField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    label = stringResource(R.string.ui_c9805922f7)
                )

                ItineraryEditorField(
                    value = transport,
                    onValueChange = { transport = it },
                    label = stringResource(R.string.ui_e6c5c414f2)
                )

                ItineraryEditorField(
                    value = note,
                    onValueChange = { note = it },
                    label = stringResource(R.string.ui_02f03f68e7),
                    minLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isCreating) {
                        Button(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceContainerLow,
                                contentColor = SunsetOrange
                            )
                        ) {
                            Text(stringResource(R.string.ui_aa1d94fc16))
                        }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.ui_34ca764caf))
                        }
                    }
                    Button(
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
                                    colorHex = event.colorHex,
                                    iconName = event.iconName
                                )
                            )
                        },
                        enabled = canSave,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (isCreating) "Thêm" else "Lưu")
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
private fun ItineraryEditorField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1
) {
    SimpleFormTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = label,
        modifier = modifier.fillMaxWidth(),
        singleLine = minLines == 1,
        minLines = minLines,
        maxLines = if (minLines == 1) 1 else 6
    )
}

@Composable
private fun PickerAnchorField(
    value: String,
    label: String,
    icon: ImageVector,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.4.dp,
                color = if (expanded) PrimaryBlue else OutlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        color = SurfaceContainerLowest,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (expanded) PrimaryBlue else OnSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = OnSurface
            )
        }
    }
}

@Composable
private fun TimeDropdownMenu(
    expanded: Boolean,
    options: List<String>,
    selected: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .heightIn(max = 280.dp)
            .background(SurfaceContainerLowest)
    ) {
        options.forEachIndexed { index, option ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = option.toPickerTimeLabel(),
                        color = if (option == selected) PrimaryBlue else OnSurface,
                        fontWeight = if (option == selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                onClick = { onSelect(option) }
            )
            if (index < options.lastIndex) {
                HorizontalDivider(color = SurfaceContainerLow)
            }
        }
    }
}

private fun buildTimeOptions(): List<String> {
    return (0 until 24).flatMap { hour ->
        listOf(0, 30).map { minute -> "%02d:%02d".format(hour, minute) }
    }
}

private fun nextTimeOption(options: List<String>, selected: String): String {
    val index = options.indexOf(selected)
    return options.getOrElse(index + 1) { selected }
}

private fun String.toPickerTimeLabel(): String {
    return ifBlank { "--:--" }
}

private fun String.toPickerDateLabel(): String {
    return runCatching {
        LocalDate.parse(this, displayDateFormatter).format(pickerDateFormatter)
    }.getOrDefault(this)
}

private val displayDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

private val pickerDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())
