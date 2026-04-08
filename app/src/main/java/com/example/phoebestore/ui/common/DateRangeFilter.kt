package com.example.phoebestore.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.phoebestore.R
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeFilter(
    fromDate: Long,
    toDate: Long,
    formattedFromDate: String,
    formattedToDate: String,
    onFromDateChange: (Long) -> Unit,
    onToDateChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = formattedFromDate,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.sales_list_filter_from)) },
            trailingIcon = {
                TextButton(onClick = { showFromPicker = true }) {
                    Text(stringResource(R.string.record_sale_date_change))
                }
            },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = formattedToDate,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.sales_list_filter_to)) },
            trailingIcon = {
                TextButton(onClick = { showToPicker = true }) {
                    Text(stringResource(R.string.record_sale_date_change))
                }
            },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }

    if (showFromPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = remember(fromDate) { toUtcMidnight(fromDate) }
        )
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onFromDateChange(it) }
                    showFromPicker = false
                }) { Text(stringResource(R.string.sales_list_date_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) {
                    Text(stringResource(R.string.sales_list_date_cancel))
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showToPicker) {
        val fromUtcMidnight = remember(fromDate) { toUtcMidnight(fromDate) }
        val selectableDates = remember(fromUtcMidnight) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= fromUtcMidnight
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = remember(toDate) { toUtcMidnight(toDate) },
            selectableDates = selectableDates
        )
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onToDateChange(it) }
                    showToPicker = false
                }) { Text(stringResource(R.string.sales_list_date_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) {
                    Text(stringResource(R.string.sales_list_date_cancel))
                }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

private fun toUtcMidnight(localEpochMillis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localEpochMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
