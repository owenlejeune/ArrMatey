package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrIndexersViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrSearchViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.appbar.FullScreenSearchAppBar
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.menu.IndexersSortMenu
import com.dnfapps.arrmatey.ui.screens.ProwlarrIndexersContent
import com.dnfapps.arrmatey.ui.screens.ProwlarrSearchContent
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProwlarrTab(
    indexersViewModel: ProwlarrIndexersViewModel = koinInject(),
    searchViewModel: ProwlarrSearchViewModel = koinInject()
) {
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState.text) {
        searchViewModel.performSearch(textFieldState.text.toString())
    }

    val sortingState by indexersViewModel.indexerSortState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            FullScreenSearchAppBar(
                textFieldState = textFieldState,
                searchPlaceholder = mokoString(MR.strings.prowlarr_search_hint),
                navigationIcon = { NavigationDrawerButton() },
                actions = {
                    IndexersSortMenu(
                        sortBy = sortingState.sortBy,
                        onSortByChanged = { indexersViewModel.updateSortBy(it) },
                        sortOrder = sortingState.sortOrder,
                        onSortOrderChanged = { indexersViewModel.updateSortOrder(it) }
                    )
                }
            ) {
                ProwlarrSearchContent(
                    viewModel = searchViewModel
                )
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        ProwlarrIndexersContent(
            viewModel = indexersViewModel,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}
