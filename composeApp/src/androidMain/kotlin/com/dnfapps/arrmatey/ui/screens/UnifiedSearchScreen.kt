package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.viewmodel.UnifiedSearchViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.MediaInstanceFilterRow
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
    val filteredSearchState by viewModel.filteredSearchState.collectAsStateWithLifecycle()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val availableTypeFilters by viewModel.availableTypeFilters.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val showBanners by viewModel.searchShowBanners.collectAsStateWithLifecycle()

    val itemCounts =
        remember(searchState) {
            searchState.groupingBy { it.instanceType }.eachCount()
        }

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
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = mokoString(MR.strings.empty_library),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            } else if (searchState.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    MediaInstanceFilterRow(
                        selectedFilter = selectedTypeFilter,
                        onFilterSelected = { viewModel.selectTypeFilter(it) },
                        availableFilters = availableTypeFilters,
                        itemCounts = itemCounts,
                        totalCount = searchState.size,
                    )

                    if (filteredSearchState.isEmpty() && selectedTypeFilter != null) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "No ${selectedTypeFilter?.name} results found",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.selectTypeFilter(null) },
                            ) {
                                Text(mokoString(MR.strings.all))
                            }
                        }
                    } else {
                        SearchResultList(
                            items = filteredSearchState,
                            onItemClick = onItemClick,
                            includeOverview = true,
                            showBanners = showBanners,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
