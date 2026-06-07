package com.mobile.travelhub.ui.components.itinerary

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mobile.travelhub.data.model.ItineraryDay
import com.mobile.travelhub.ui.components.SimpleFormTextField
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceContainerLowest
import com.mobile.travelhub.ui.theme.SunsetOrange
import com.mobile.travelhub.R

@Composable
fun ItineraryDayEditorDialog(
    day: ItineraryDay,
    onDismiss: () -> Unit,
    onSave: (ItineraryDay) -> Unit,
    onDelete: () -> Unit
) {
    var label by remember(day.dayIndex) { mutableStateOf(day.label) }
    var dateLabel by remember(day.dayIndex) { mutableStateOf(day.dateLabel) }

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PrimaryBlue.copy(alpha = 0.18f),
                                    SunsetOrange.copy(alpha = 0.12f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.ui_f02c7f33ec),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurface
                        )
                        Text(
                            text = stringResource(R.string.ui_4dea4a7b33),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }

                SimpleFormTextField(
                    value = label,
                    onValueChange = { label = it },
                    placeholder = stringResource(R.string.ui_474b496f72),
                    modifier = Modifier.fillMaxWidth()
                )
                SimpleFormTextField(
                    value = dateLabel,
                    onValueChange = { dateLabel = it },
                    placeholder = stringResource(R.string.ui_913d65e5d6),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDelete) {
                        Text(stringResource(R.string.ui_87ba27fac0), color = SunsetOrange)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.ui_77dfd2135f))
                    }
                    TextButton(
                        onClick = {
                            onSave(
                                day.copy(
                                    label = label.trim().ifBlank { day.label },
                                    dateLabel = dateLabel.trim().ifBlank { day.dateLabel }
                                )
                            )
                        }
                    ) {
                        Text(stringResource(R.string.ui_179359b39e))
                    }
                }
            }
        }
    }
}
