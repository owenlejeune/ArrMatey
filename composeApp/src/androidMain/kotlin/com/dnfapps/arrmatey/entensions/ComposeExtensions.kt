package com.dnfapps.arrmatey.entensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

fun PaddingValues.copy(
    start: Dp = this.calculateLeftPadding(LayoutDirection.Ltr),
    end: Dp = this.calculateRightPadding(LayoutDirection.Ltr),
    top: Dp = this.calculateTopPadding(),
    bottom: Dp = this.calculateBottomPadding()
) = PaddingValues(start = start, end = end, top = top, bottom = bottom)

suspend fun SnackbarHostState.showSnackbarImmediately(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
): SnackbarResult {
    currentSnackbarData?.dismiss()
    return showSnackbar(message, actionLabel, withDismissAction, duration)
}

suspend fun SnackbarHostState.showErrorImmediately(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
): SnackbarResult {
    currentSnackbarData?.dismiss()
    val visuals = ErrorVisuals(message, actionLabel, withDismissAction, duration)
    return showSnackbar(visuals)
}

private class ErrorVisuals(
    override val message: String,
    override val actionLabel: String?,
    override val withDismissAction: Boolean,
    override val duration: SnackbarDuration
): SnackbarVisuals

@Composable
fun SafeSnackbar(snackbarData: SnackbarData) {
    val (containerColor, contentColor) = when (snackbarData.visuals) {
        is ErrorVisuals -> with(MaterialTheme.colorScheme) { errorContainer to onErrorContainer }
        else -> with(MaterialTheme.colorScheme) { primaryContainer to onPrimaryContainer }
    }
    Snackbar(
        snackbarData = snackbarData,
        containerColor = containerColor,
        contentColor = contentColor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
fun SearchBarState.isExpanded() =
    currentValue == SearchBarValue.Expanded

@OptIn(ExperimentalMaterial3Api::class)
fun SearchBarState.isCollapsed() =
    currentValue == SearchBarValue.Collapsed

fun PaddingValues(
    all: Dp,
    start: Dp? = null,
    end: Dp? = null,
    top: Dp? = null,
    bottom: Dp? = null
): PaddingValues = PaddingValues(
    start = start ?: all,
    end = end ?: all,
    top = top ?: all,
    bottom = bottom ?: all
)