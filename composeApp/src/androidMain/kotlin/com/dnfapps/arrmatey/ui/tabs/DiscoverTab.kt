package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.viewmodel.DiscoverViewModel
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.DiscoverScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.toArrDetailsOrPreview
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.navigation.toPersonDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.MediaInstanceFilterRow
import com.dnfapps.arrmatey.ui.components.SearchResultList
import com.dnfapps.arrmatey.ui.components.navigation.TwoPaneMasterDetailNavDisplay
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.screens.DiscoverCategoryScreen
import com.dnfapps.arrmatey.ui.screens.DiscoverHomeScreen
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    viewModel: DiscoverViewModel = koinViewModel(),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<NavKey> = navigationManager.discover,
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    TwoPaneMasterDetailNavDisplay(
        navigation = navigation,
        isExpanded = isExpanded,
        wideRailIsVisible = wideRailIsVisible,
        isMasterScreen = { it is DiscoverScreen.Home },
        entryProvider =
            entryProvider {
                entry<DiscoverScreen.Home> {
                    DiscoverHomeScreen(
                        viewModel = viewModel,
                        wideRailIsVisible = wideRailIsVisible,
                        onSeeMore = { category ->
                            navigation.navigateTo(DiscoverScreen.Category(category))
                        },
                        onItemClick = { result ->
                            when (result) {
                                is SearchResult.ArrMediaResult -> {
                                    navigation.toArrDetailsOrPreview(result.media, result.instanceType)
                                }
                                is SearchResult.SeerrMediaResult -> {
                                    navigation.toDetails(tmdbId = result.result.id, requestType = result.result.mediaType)
                                }
                                is SearchResult.SeerrPersonResult -> {
                                    navigation.toPersonDetails(result.result.id)
                                }
                            }
                        },
                    )
                }
                entry<DiscoverScreen.Category> { entry ->
                    DiscoverCategoryScreen(
                        category = entry.category,
                        viewModel = viewModel,
                        onBack = { navigation.popBackStack() },
                        onItemClick = { result ->
                            when (result) {
                                is SearchResult.ArrMediaResult -> {
                                    navigation.toArrDetailsOrPreview(result.media, result.instanceType)
                                }
                                is SearchResult.SeerrMediaResult -> {
                                    navigation.toDetails(tmdbId = result.result.id, requestType = result.result.mediaType)
                                }
                                is SearchResult.SeerrPersonResult -> {
                                    navigation.toPersonDetails(result.result.id)
                                }
                            }
                        },
                    )
                }
                mediaNavEntries(
                    navigation = navigation,
                    isExpanded = isExpanded,
                    wideRailIsVisible = wideRailIsVisible,
                )
            },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoverSearchOverlay(
    items: List<SearchResult>,
    isLoading: Boolean,
    onItemClick: (SearchResult) -> Unit,
    showBanners: Boolean,
    modifier: Modifier = Modifier,
) {
    var selectedFilter by rememberSaveable { mutableStateOf<InstanceType?>(null) }

    val availableFilters =
        remember(items) {
            items.map { it.instanceType }.distinct()
        }

    val itemCounts =
        remember(items) {
            items.groupingBy { it.instanceType }.eachCount()
        }

    LaunchedEffect(items) {
        if (selectedFilter != null && items.none { it.instanceType == selectedFilter }) {
            selectedFilter = null
        }
    }

    val filteredItems =
        remember(items, selectedFilter) {
            if (selectedFilter == null) items else items.filter { it.instanceType == selectedFilter }
        }

    if (isLoading && items.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator(modifier = Modifier.size(96.dp))
        }
    } else if (items.isNotEmpty()) {
        Column(modifier = modifier.fillMaxSize()) {
            MediaInstanceFilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                availableFilters = availableFilters,
                itemCounts = itemCounts,
                totalCount = items.size,
            )

            if (filteredItems.isEmpty() && selectedFilter != null) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "No ${selectedFilter?.name} results found",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { selectedFilter = null },
                    ) {
                        Text(mokoString(MR.strings.all))
                    }
                }
            } else {
                SearchResultList(
                    items = filteredItems,
                    onItemClick = onItemClick,
                    includeOverview = true,
                    showBanners = showBanners,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
