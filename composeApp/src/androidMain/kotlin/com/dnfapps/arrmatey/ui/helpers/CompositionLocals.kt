package com.dnfapps.arrmatey.ui.helpers

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalIsInTwoPane = staticCompositionLocalOf { false }
val LocalFloatingBarBottomPadding = compositionLocalOf { 0.dp }
val LocalIsTabActive = staticCompositionLocalOf { true }
