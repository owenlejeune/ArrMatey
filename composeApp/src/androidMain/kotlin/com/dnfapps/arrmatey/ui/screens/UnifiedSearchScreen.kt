package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.viewmodel.UnifiedSearchViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.SearchResultList
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.viewmodel.koinViewModel

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun UnifiedSearchScreen(
    initialQuery: String,
    onBack: () -> Unit,
    onItemClick: (SearchResult) -> Unit,
    viewModel: UnifiedSearchViewModel = koinViewModel(),
) {
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val showBanners by viewModel.searchShowBanners.collectAsStateWithLifecycle()

    val textFieldState = rememberTextFieldState(initialQuery)
    val searchBarState = rememberSearchBarState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (initialQuery.isEmpty()) {
            searchBarState.animateToExpanded()
            focusRequester.requestFocus()
        } else {
            viewModel.updateSearchQuery(initialQuery)
        }
    }

    LaunchedEffect(textFieldState.text) {
        viewModel.updateSearchQuery(textFieldState.text.toString())
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearSearch() }
    }

    Scaffold(
        topBar = {
            ArrAppBarWithSearch(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                inputFieldModifier = Modifier.focusRequester(focusRequester),
                searchPlaceholder = mokoString(MR.strings.search),
                navigationIcon = { BackButton(onBack) },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            if (isSearching && searchState.isEmpty()) {
                LoadingIndicator(
                    modifier =
                        Modifier
                            .size(96.dp)
                            .align(Alignment.Center),
                )
            } else if (searchState.isEmpty() && textFieldState.text.isNotEmpty() && !isSearching) {
                Text(
                    text = mokoString(MR.strings.empty_library),
                    modifier = Modifier.align(Alignment.Center),
                )
            } else if (searchState.isNotEmpty()) {
                SearchResultList(
                    items = searchState,
                    onItemClick = onItemClick,
                    includeOverview = true,
                    showBanners = showBanners,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
