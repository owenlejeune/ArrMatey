package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.navigation.BazarrScreen
import com.dnfapps.arrmatey.navigation.DashboardScreen
import com.dnfapps.arrmatey.navigation.DashboardTabNavigator
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.TracearrScreen
import com.dnfapps.arrmatey.navigation.openArrDashboard
import com.dnfapps.arrmatey.navigation.toArrDetailsOrPreview
import com.dnfapps.arrmatey.navigation.toBookDetails
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.navigation.toEpisodeDetails
import com.dnfapps.arrmatey.navigation.toPersonDetails
import com.dnfapps.arrmatey.ui.components.navigation.TwoPaneMasterDetailNavDisplay
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.components.navigation.tracearrNavEntries
import com.dnfapps.arrmatey.ui.screens.ArrInstanceDashboard
import com.dnfapps.arrmatey.ui.screens.BazarrDetailsScreen
import com.dnfapps.arrmatey.ui.screens.BazarrScreen
import com.dnfapps.arrmatey.ui.screens.dashboard.CombinedDashboard
import org.koin.compose.koinInject

@Composable
fun DashboardTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean = false,
    navigationManager: NavigationManager = koinInject(),
    navigation: DashboardTabNavigator = navigationManager.dashboard,
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val isLargeScreen = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    TwoPaneMasterDetailNavDisplay(
        navigation = navigation,
        isExpanded = isExpanded,
        wideRailIsVisible = wideRailIsVisible,
        isMasterScreen = { it is DashboardScreen.Main },
        entryProvider =
            entryProvider {
                entry<DashboardScreen.Main> {
                    CombinedDashboard(
                        windowSizeClass = windowSizeClass,
                        onNavigateToArrDashboard = { id -> navigation.openArrDashboard(id) },
                        onNavigateToMediaDetails = { id, type -> navigation.toDetails(id = id, type = type) },
                        onNavigateToSeerrMediaDetails = { tmdbId, type -> navigation.toDetails(tmdbId = tmdbId, requestType = type) },
                        onNavigateToSeerrPersonDetails = { personId -> navigation.toPersonDetails(personId) },
                        onNavigateToArrMediaDetailsOrPreview = { media, type -> navigation.toArrDetailsOrPreview(media, type) },
                        onNavigateToSettings = { navigationManager.openSettings() },
                        onNavigateToRequestsTab = { navigationManager.openRequestsTab() },
                        onNavigateToProwlarrTab = { navigation.navigateTo(DashboardScreen.Prowlarr) },
                        onNavigateToDownloadsTab = { navigation.navigateTo(DashboardScreen.Downloads) },
                        onNavigateToActivityTab = { navigation.navigateTo(DashboardScreen.Activity) },
                        onNavigateToScheduleTab = { navigation.navigateTo(DashboardScreen.Calendar) },
                        onNavigateToBazarrTab = { navigation.navigateTo(DashboardScreen.Bazarr) },
                        onNavigateToTracearrTab = { navigation.navigateTo(TracearrScreen.Main) },
                        onNavigateToTracearrHistory = { navigation.navigateTo(TracearrScreen.History) },
                        onNavigateToTracearrUsers = { navigation.navigateTo(TracearrScreen.Users) },
                        onNavigateToTracearrViolations = { navigation.navigateTo(TracearrScreen.Violations) },
                        onNavigateToTracearrActivity = { navigation.navigateTo(TracearrScreen.Activity) },
                        onNavigateToDiscoverTab = { navigationManager.openDiscoverTab() },
                    )
                }
                entry<DashboardScreen.ArrDashboard> {
                    ArrInstanceDashboard(
                        id = it.id,
                        windowSizeClass = windowSizeClass,
                        onBack = { navigation.popBackStack() },
                        onNavigateToEditInstance = { instanceId ->
                            navigationManager.openEditInstanceScreen(instanceId)
                        },
                    )
                }
                entry<DashboardScreen.Activity> {
                    ActivityTab(
                        wideRailIsVisible = wideRailIsVisible,
                        onBack = { navigation.popBackStack() },
                    )
                }
                entry<DashboardScreen.Downloads> {
                    DownloadsTab(
                        wideRailIsVisible = wideRailIsVisible,
                        onBack = { navigation.popBackStack() },
                    )
                }
                entry<DashboardScreen.Prowlarr> {
                    ProwlarrTab(
                        wideRailIsVisible = wideRailIsVisible,
                        onBack = { navigation.popBackStack() },
                    )
                }
                entry<DashboardScreen.Bazarr> {
                    BazarrScreen(
                        wideRailIsVisible = wideRailIsVisible,
                        onBack = { navigation.popBackStack() },
                        onNavigateToDetails = { id, type ->
                            navigation.navigateTo(BazarrScreen.Details(id, type))
                        },
                    )
                }
                entry<BazarrScreen.Details> { details ->
                    BazarrDetailsScreen(
                        id = details.id,
                        type = details.type,
                        onBack = { navigation.popBackStack() },
                        isExpanded = isExpanded,
                        wideRailIsVisible = wideRailIsVisible,
                    )
                }
                entry<DashboardScreen.Calendar> {
                    CalendarHomeScreen(
                        wideRailIsVisible = wideRailIsVisible,
                        isExpanded = isExpanded,
                        onBack = { navigation.popBackStack() },
                        onItemClick = { item, instanceId ->
                            when (item) {
                                is ArrMovie ->
                                    navigation.toDetails(
                                        id = item.id,
                                        tmdbId = item.tmdbId,
                                        type = item.associatedType,
                                        instanceId = instanceId,
                                    )

                                is EpisodeGroup ->
                                    navigation.toDetails(
                                        id = item.first.seriesId,
                                        type = item.associatedType,
                                        instanceId = instanceId,
                                    )

                                is Episode -> {
                                    item.series?.let { series ->
                                        navigation.toDetails(
                                            id = series.id,
                                            tmdbId = series.tmdbId,
                                            type = item.associatedType,
                                            instanceId = instanceId,
                                        )
                                        navigation.toEpisodeDetails(series, item)
                                    }
                                }

                                is ArrAlbum ->
                                    navigation.toDetails(
                                        id = item.id,
                                        type = item.associatedType,
                                        instanceId = instanceId,
                                    )

                                is Book -> {
                                    item.author?.let { author ->
                                        navigation.toDetails(
                                            id = author.id,
                                            type = item.associatedType,
                                            instanceId = instanceId,
                                        )
                                        navigation.toBookDetails(author, item)
                                    }
                                }

                                is Audiobook ->
                                    navigation.toDetails(
                                        id = item.id,
                                        type = item.associatedType,
                                        instanceId = instanceId,
                                    )
                            }
                        },
                    )
                }
                tracearrNavEntries(
                    navigation = navigation,
                    isExpanded = isExpanded,
                    isLargeScreen = isLargeScreen,
                    wideRailIsVisible = wideRailIsVisible,
                    onNavigateBack = { navigation.popBackStack() },
                )
                mediaNavEntries(
                    navigation = navigation,
                    isExpanded = isExpanded,
                    wideRailIsVisible = wideRailIsVisible,
                )
            },
    )
}
