package com.dnfapps.arrmatey.ui.helpers

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Represents the physical posture of a foldable device.
 */
@Immutable
sealed interface FoldablePosture {
    data object Normal : FoldablePosture
    data class Tabletop(val isHalfOpened: Boolean = true) : FoldablePosture
    data class Book(val isHalfOpened: Boolean = true) : FoldablePosture
}

/**
 * Local CompositionLocal providing the current [FoldablePosture] to composable subtrees.
 */
val LocalFoldablePosture = staticCompositionLocalOf<FoldablePosture> { FoldablePosture.Normal }

/**
 * Returns true if the current window size supports multi-pane layouts (unfolded/tablet/desktop).
 */
val WindowSizeClass.isMultiPaneSupported: Boolean
    get() = widthSizeClass != WindowWidthSizeClass.Compact

/**
 * Returns true if the window size is expanded (unfolded main screen in landscape, desktop, large tablet).
 */
val WindowSizeClass.isExpandedWidth: Boolean
    get() = widthSizeClass == WindowWidthSizeClass.Expanded
