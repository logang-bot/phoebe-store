package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phoebestore.R
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

private val PILL_HEIGHT = 52.dp
private val PILL_CORNER = 50.dp

@Composable
internal fun QuantityField(
    value: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    isError: Boolean,
    errorMessage: String?,
    canIncrement: Boolean,
    modifier: Modifier = Modifier,
    warningMessage: String? = null
) {
    val qty = value.toIntOrNull() ?: 0

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.record_sale_quantity_label),
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        QuantityInput(
            value = value,
            onValueChange = onValueChange,
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        QuantitySteppers(
            onDecrement = onDecrement,
            onIncrement = onIncrement,
            canDecrement = qty > 1,
            canIncrement = canIncrement,
            modifier = Modifier.fillMaxWidth()
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        } else if (warningMessage != null) {
            Text(
                text = warningMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun QuantityInput(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .height(72.dp)
            .border(1.dp, borderColor, MaterialTheme.shapes.extraSmall)
    ) { innerTextField ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            innerTextField()
        }
    }
}

@Composable
private fun QuantitySteppers(
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    canDecrement: Boolean,
    canIncrement: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.height(PILL_HEIGHT)) {
        FilledTonalButton(
            onClick = onDecrement,
            enabled = canDecrement,
            shape = RoundedCornerShape(
                topStart = PILL_CORNER, bottomStart = PILL_CORNER,
                topEnd = 0.dp, bottomEnd = 0.dp
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Icon(painterResource(R.drawable.ic_remove), contentDescription = null)
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        FilledTonalButton(
            onClick = onIncrement,
            enabled = canIncrement,
            shape = RoundedCornerShape(
                topStart = 0.dp, bottomStart = 0.dp,
                topEnd = PILL_CORNER, bottomEnd = PILL_CORNER
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuantityFieldLightPreview() {
    PhoebeStoreTheme {
        QuantityField(
            value = "3",
            onValueChange = {},
            onIncrement = {},
            onDecrement = {},
            isError = false,
            errorMessage = null,
            canIncrement = true,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun QuantityFieldDarkPreview() {
    PhoebeStoreTheme {
        QuantityField(
            value = "1",
            onValueChange = {},
            onIncrement = {},
            onDecrement = {},
            isError = true,
            errorMessage = "Enter a valid quantity greater than 0",
            canIncrement = false,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        )
    }
}
