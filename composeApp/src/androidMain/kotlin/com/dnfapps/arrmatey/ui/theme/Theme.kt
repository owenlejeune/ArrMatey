package com.dnfapps.arrmatey.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.model.AppColor
import com.dnfapps.arrmatey.model.AppTheme
import kotlinx.coroutines.flow.combine
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArrMateyTheme(content: @Composable () -> Unit) {
    val isPreview = LocalInspectionMode.current
    val isSystemDark = isSystemInDarkTheme()

    if (isPreview) {
        val colorScheme = if (isSystemDark) DarkColorPalette else LightColorPalette
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = typography(),
            shapes = ArrShapes,
            content = {
                Surface(
                    color = colorScheme.background,
                    content = content,
                )
            },
        )
        return
    }

    val preferences = koinInject<PreferencesStore>()

    val themeSettings by remember(preferences) {
        combine(preferences.appTheme, preferences.appColor) { theme, color ->
            theme to color
        }
    }.collectAsStateWithLifecycle(null)

    val (appTheme, appColor) = themeSettings ?: (AppTheme.System to AppColor.ArrMatey)

    val isDarkTheme =
        when (appTheme) {
            AppTheme.System -> isSystemDark
            AppTheme.Light -> false
            AppTheme.Dark -> true
        }

    val context = LocalContext.current
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme =
        when (appColor) {
            AppColor.Dynamic -> {
                if (dynamicColor) {
                    if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                } else {
                    if (isDarkTheme) DarkColorPalette else LightColorPalette
                }
            }
            AppColor.ArrMatey -> {
                if (isDarkTheme) DarkColorPalette else LightColorPalette
            }
            AppColor.Amoled -> AmoledDarkColorPalette
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = typography(),
        shapes = ArrShapes,
        content = {
            Surface(
                color = colorScheme.background,
                content = content,
            )
        },
    )
}
