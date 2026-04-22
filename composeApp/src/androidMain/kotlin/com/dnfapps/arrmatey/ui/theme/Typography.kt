package com.dnfapps.arrmatey.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.dnfapps.arrmatey.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val GoogleSansFont = GoogleFont("Google Sans")
private val GoogleSansFontFamily = FontFamily(
    Font(googleFont = GoogleSansFont, fontProvider = provider)
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
        labelSmall = typography.labelSmall.copy(fontFamily = fontFamily)
    )
}