package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.bazarr.viewmodel.BazarrViewModel
import com.dnfapps.arrmatey.ui.screens.BazarrScreen
import org.koin.compose.koinInject

@Composable
fun BazarrTab(
    wideRailIsVisible: Boolean,
    viewModel: BazarrViewModel = koinInject()
) {
    BazarrScreen(
        viewModel = viewModel,
        wideRailIsVisible = wideRailIsVisible
    )
}
