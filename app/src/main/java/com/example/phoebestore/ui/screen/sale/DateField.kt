package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.phoebestore.R
import com.example.phoebestore.ui.theme.PhoebeStoreTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    epochMillis: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = epochMillis)
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    OutlinedTextField(
        value = dateFormat.format(Date(epochMillis)),
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.record_sale_date_label)) },
        modifier = modifier,
        trailingIcon = {
            TextButton(onClick = { showPicker = true }) {
                Text(
                    text = stringResource(R.string.record_sale_date_change),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    showPicker = false
                }) {
                    Text(stringResource(R.string.record_sale_date_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.record_sale_date_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DateFieldLightPreview() {
    PhoebeStoreTheme {
        DateField(
            epochMillis = System.currentTimeMillis(),
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
            onDateSelected = {}
        )
    }
}
