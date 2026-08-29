package com.dnfapps.arrmatey.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val preferences = koinInject<PreferencesStore>()

    val themeSettings by remember(preferences) {
        combine(preferences.appTheme, preferences.appColor) { theme, color ->
            theme to color
        }
    }.collectAsStateWithLifecycle(null)

    val isSystemDark = isSystemInDarkTheme()

    if (themeSettings == null) {
        MaterialExpressiveTheme(
            colorScheme = if (isSystemDark) DarkColorPalette else LightColorPalette,
            content = {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isSystemDark) Color.Black else Color.White,
                ) {}
            },
            typography = typography(),
        )
        return
    }

    val (appTheme, appColor) = themeSettings!!

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
        content = content,
        typography = typography(),
    )
}
