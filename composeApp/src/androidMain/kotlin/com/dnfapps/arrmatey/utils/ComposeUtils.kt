package com.dnfapps.arrmatey.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.invoke

@Composable
fun navigationBarBottomInset() = WindowInsets.navigationBars.getBottom(LocalDensity.current).dp()

@Composable
fun Int.dp(): Dp {
    val density = LocalDensity.current.density
    return (this / density).dp
}