package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.phoebestore.R
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun SaleResultDialog(
    result: SaleResult,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn()
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.inversePrimary
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isSuccess = result is SaleResult.Success

                    // Icon
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier
                                .padding(16.dp)
                                .size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = stringResource(
                            if (isSuccess) R.string.record_sale_result_success_title
                            else R.string.record_sale_result_error_title
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.inverseSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Body
                    Text(
                        text = if (result is SaleResult.Success) {
                            stringResource(R.string.record_sale_result_success_body, result.productName, result.quantity)
                        } else {
                            stringResource(R.string.record_sale_result_error_body)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )

                    if (result is SaleResult.Success) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.record_sale_result_success_total, result.formattedTotal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.inverseSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // OK button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.inverseSurface,
                            contentColor = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    ) {
                        Text(stringResource(R.string.record_sale_result_ok))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SaleResultDialogSuccessLightPreview() {
    PhoebeStoreTheme {
        SaleResultDialog(
            result = SaleResult.Success(productName = "Summer Dress", quantity = 3, formattedTotal = "USD 89.97"),
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SaleResultDialogSuccessDarkPreview() {
    PhoebeStoreTheme {
        SaleResultDialog(
            result = SaleResult.Success(productName = "Summer Dress", quantity = 3, formattedTotal = "USD 89.97"),
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SaleResultDialogErrorPreview() {
    PhoebeStoreTheme {
        SaleResultDialog(
            result = SaleResult.Error,
            onDismiss = {}
        )
    }
}
