package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.phoebestore.R
import com.example.phoebestore.ui.theme.PhoebeStoreTheme
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    epochMillis: Long,
    formattedDate: String,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDateMillis by remember { mutableLongStateOf(epochMillis) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = epochMillis)

    val initialCalendar = remember(epochMillis) {
        Calendar.getInstance().apply { timeInMillis = epochMillis }
    }
    val timePickerState = rememberTimePickerState(
        initialHour = initialCalendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCalendar.get(Calendar.MINUTE),
        is24Hour = false
    )

    OutlinedTextField(
        value = formattedDate,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.record_sale_date_label)) },
        modifier = modifier,
        trailingIcon = {
            TextButton(onClick = { showDatePicker = true }) {
                Text(
                    text = stringResource(R.string.record_sale_date_change),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingDateMillis = datePickerState.selectedDateMillis ?: epochMillis
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text(stringResource(R.string.record_sale_date_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.record_sale_date_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = pendingDateMillis
                    }
                    val combined = Calendar.getInstance().apply {
                        set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                        set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onDateSelected(combined.timeInMillis)
                    showTimePicker = false
                }) {
                    Text(stringResource(R.string.record_sale_date_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.record_sale_date_cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DateFieldLightPreview() {
    PhoebeStoreTheme {
        DateField(
            epochMillis = System.currentTimeMillis(),
            formattedDate = "Mar 28, 2026 - 3:45 PM",
            onDateSelected = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DateFieldDarkPreview() {
    PhoebeStoreTheme {
        DateField(
            epochMillis = System.currentTimeMillis(),
            formattedDate = "Mar 28, 2026 - 3:45 PM",
            onDateSelected = {}
        )
    }
}
