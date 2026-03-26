package com.example.phoebestore.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Composable
fun Modifier.themedShadow(
    elevation: Dp,
    shape: Shape = MaterialTheme.shapes.medium
): Modifier {
    val shadowColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    return shadow(elevation = elevation, shape = shape, ambientColor = shadowColor, spotColor = shadowColor)
}
