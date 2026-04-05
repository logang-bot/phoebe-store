package com.example.phoebestore.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun LoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current
            )
        } else {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            }
            Text(text)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingButtonLightPreview() {
    PhoebeStoreTheme {
        LoadingButton(text = "Record sale", onClick = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoadingButtonDarkPreview() {
    PhoebeStoreTheme {
        LoadingButton(text = "Record sale", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingButtonLoadingLightPreview() {
    PhoebeStoreTheme {
        LoadingButton(text = "Record sale", onClick = {}, isLoading = true)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoadingButtonLoadingDarkPreview() {
    PhoebeStoreTheme {
        LoadingButton(text = "Record sale", onClick = {}, isLoading = true)
    }
}
