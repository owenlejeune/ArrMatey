package com.dnfapps.arrmatey.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal to provide NavigationManager throughout the UI tree.
 */
val LocalNavigationManager =
    staticCompositionLocalOf<NavigationManager> {
        error("No NavigationManager provided")
    }

/**
 * Composable helper for NavigationManager
 */
val navigationManager: NavigationManager
    @Composable
    get() = LocalNavigationManager.current
