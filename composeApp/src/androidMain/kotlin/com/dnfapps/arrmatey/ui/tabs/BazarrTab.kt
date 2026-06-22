package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.ui.screens.BazarrLibraryScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazarrTab(
    wideRailIsVisible: Boolean
) {
    BazarrLibraryScreen(wideRailIsVisible = wideRailIsVisible)
}
