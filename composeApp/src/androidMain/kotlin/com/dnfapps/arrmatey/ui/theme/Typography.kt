package com.dnfapps.arrmatey.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.dnfapps.arrmatey.R

private val GoogleSansFontFamily =
    FontFamily(
        Font(R.font.google_sans_regular, FontWeight.Normal),
        Font(R.font.google_sans_medium, FontWeight.Medium),
        Font(R.font.google_sans_bold, FontWeight.Bold),
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun typography(): Typography {
    val fontFamily = GoogleSansFontFamily
    val typography = Typography()
    return typography.copy(
        displayLarge = typography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = typography.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = typography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = typography.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = typography.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = typography.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = typography.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = typography.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = typography.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = typography.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = typography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = typography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = typography.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = typography.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = typography.labelSmall.copy(fontFamily = fontFamily),
    )
}
