package com.dnfapps.arrmatey.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayTopAppBar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    title: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    OverlayTopAppBar(
        scrollValueProvider = { scrollState.value },
        modifier = modifier,
        navigationIcon = navigationIcon,
        title = title,
        actions = actions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayTopAppBar(
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    title: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    OverlayTopAppBar(
        scrollValueProvider = {
            if (gridState.firstVisibleItemIndex == 0) gridState.firstVisibleItemScrollOffset else 500
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        title = title,
        actions = actions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayTopAppBar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    title: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    OverlayTopAppBar(
        scrollValueProvider = {
            if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset else 500
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        title = title,
        actions = actions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverlayTopAppBar(
    scrollValueProvider: () -> Int,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    title: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    val headerBackgroundAlpha by remember {
        derivedStateOf {
            // Fade in over 200 pixels of scroll
            val fadeDistance = 200f
            (scrollValueProvider() / fadeDistance).coerceIn(0f, 1f)
        }
    }

    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val isScrolled = headerBackgroundAlpha > 0.5f

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val insetsController = remember(window, view) {
                WindowCompat.getInsetsController(window, view)
            }
            DisposableEffect(insetsController, isScrolled, isDarkTheme) {
                insetsController.isAppearanceLightStatusBars = if (isScrolled) !isDarkTheme else false
                onDispose {
                    insetsController.isAppearanceLightStatusBars = !isDarkTheme
                }
            }
        }
    }

    TopAppBar(
        title = {
            AnimatedVisibility(
                visible = headerBackgroundAlpha > 0.9f,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                title()
            }
        },
        navigationIcon = navigationIcon,
        actions = actions,
        modifier = modifier,
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor =
                    MaterialTheme.colorScheme.surface.copy(
                        alpha = headerBackgroundAlpha,
                    ),
            ),
    )
}
