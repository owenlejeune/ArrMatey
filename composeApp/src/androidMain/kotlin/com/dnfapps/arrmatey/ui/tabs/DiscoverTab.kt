package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.viewmodel.DiscoverViewModel
import com.dnfapps.arrmatey.navigation.DiscoverScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.toArrDetailsOrPreview
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.navigation.toPersonDetails
import com.dnfapps.arrmatey.ui.components.SearchResultList
import com.dnfapps.arrmatey.ui.components.navigation.TwoPaneMasterDetailNavDisplay
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.screens.DiscoverCategoryScreen
import com.dnfapps.arrmatey.ui.screens.DiscoverHomeScreen
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
    showInstanceIndicatorShadow: Boolean,
) {
    if (isLoading && items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator(modifier = Modifier.size(96.dp))
        }
    } else if (items.isNotEmpty()) {
        SearchResultList(
            items = items,
            onItemClick = onItemClick,
            includeOverview = true,
            showBanners = showBanners,
            showInstanceIndicatorShadow = showInstanceIndicatorShadow,
        )
    }
}
