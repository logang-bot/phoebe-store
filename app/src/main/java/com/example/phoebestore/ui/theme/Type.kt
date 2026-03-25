package com.example.phoebestore.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.phoebestore.R

val bodyFontFamily = FontFamily(
    Font(R.font.nunito_sans_variable, weight = FontWeight.Light),
    Font(R.font.nunito_sans_variable, weight = FontWeight.Normal),
    Font(R.font.nunito_sans_variable, weight = FontWeight.Medium),
    Font(R.font.nunito_sans_variable, weight = FontWeight.SemiBold),
    Font(R.font.nunito_sans_variable, weight = FontWeight.Bold),
    Font(R.font.nunito_sans_italic_variable, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(R.font.nunito_sans_italic_variable, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.nunito_sans_italic_variable, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.nunito_sans_italic_variable, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    Font(R.font.nunito_sans_italic_variable, weight = FontWeight.Bold, style = FontStyle.Italic),
)

val displayFontFamily = FontFamily(
    Font(R.font.funnel_display_variable, weight = FontWeight.Light),
    Font(R.font.funnel_display_variable, weight = FontWeight.Normal),
    Font(R.font.funnel_display_variable, weight = FontWeight.Medium),
    Font(R.font.funnel_display_variable, weight = FontWeight.SemiBold),
    Font(R.font.funnel_display_variable, weight = FontWeight.Bold),
    Font(R.font.funnel_display_variable, weight = FontWeight.ExtraBold),
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
)
