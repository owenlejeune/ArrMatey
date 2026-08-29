package com.dnfapps.arrmatey.entensions

import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun IconButtonDefaults.headerBarColors(
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
) = iconButtonColors(
    containerColor = containerColor.copy(alpha = .8f),
    contentColor = contentColor,
)
