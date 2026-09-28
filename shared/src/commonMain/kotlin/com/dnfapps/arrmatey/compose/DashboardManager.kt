package com.dnfapps.arrmatey.compose

import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR.strings
import com.dnfapps.arrmatey.shared.dashboard_activity_queue_overview
import com.dnfapps.arrmatey.shared.dashboard_arr_overview
import com.dnfapps.arrmatey.shared.dashboard_bazarr_overview
import com.dnfapps.arrmatey.shared.dashboard_discover_feed
import com.dnfapps.arrmatey.shared.dashboard_discover_quick_pick
import com.dnfapps.arrmatey.shared.dashboard_discover_spotlight
import com.dnfapps.arrmatey.shared.dashboard_download_clients_overview
import com.dnfapps.arrmatey.shared.dashboard_instance_dashboards
import com.dnfapps.arrmatey.shared.dashboard_network_monitor
import com.dnfapps.arrmatey.shared.dashboard_prowlarr_overview
import com.dnfapps.arrmatey.shared.dashboard_recently_added
import com.dnfapps.arrmatey.shared.dashboard_seerr_overview
import com.dnfapps.arrmatey.shared.dashboard_todays_releases
import com.dnfapps.arrmatey.shared.dashboard_tracearr_active_streams
import com.dnfapps.arrmatey.shared.dashboard_tracearr_overview
import com.dnfapps.arrmatey.shared.dashboard_upcoming_releases
import com.dnfapps.arrmatey.shared.issues
import com.dnfapps.arrmatey.shared.requests
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class DashboardManager(
    private val preferencesStore: PreferencesStore,
) {
    private val _cardsOrder = MutableStateFlow<List<DashboardCards>>(emptyList())
    val cardsOrder: StateFlow<List<DashboardCards>> = _cardsOrder.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch {
            preferencesStore.dashboardCardsOrder
                .take(1)
                .collect { savedOrder ->
                    _cardsOrder.value = savedOrder
                }
        }
    }

    fun saveCardOrder(cards: List<DashboardCards>) {
        _cardsOrder.value = cards
        scope.launch {
            preferencesStore.updateDashboardCardsOrder(cards)
        }
    }

    fun removeCard(card: DashboardCards) {
        val newCards =
            _cardsOrder.value.toMutableList().apply {
                remove(card)
            }
        saveCardOrder(newCards)
    }

    fun addCard(card: DashboardCards) {
        val newCards =
            _cardsOrder.value.toMutableList().apply {
                if (card !in this) {
                    add(card)
                }
            }
        saveCardOrder(newCards)
    }

    fun reset() {
        saveCardOrder(DashboardCards.defaultEntries)
    }
}

@kotlinx.serialization.Serializable
enum class DashboardCards(
    val title: StringResource,
) {
    ArrOverview(strings.dashboard_arr_overview),
    SeerrOverview(strings.dashboard_seerr_overview),
    PendingRequests(strings.requests),
    PendingIssues(strings.issues),
    ProwlarrOverview(strings.dashboard_prowlarr_overview),
    Network(strings.dashboard_network_monitor),
    RecentlyAdded(strings.dashboard_recently_added),
    DownloadClients(strings.dashboard_download_clients_overview),
    ActivityQueue(strings.dashboard_activity_queue_overview),
    OnToday(strings.dashboard_todays_releases),
    UpcomingReleases(strings.dashboard_upcoming_releases),
    InstanceDashboard(strings.dashboard_instance_dashboards),
    BazarrOverview(strings.dashboard_bazarr_overview),
    TracearrOverview(strings.dashboard_tracearr_overview),
    TracearrActiveStreams(strings.dashboard_tracearr_active_streams),
    DiscoverFeed(strings.dashboard_discover_feed),
    DiscoverSpotlight(strings.dashboard_discover_spotlight),
    DiscoverQuickPick(strings.dashboard_discover_quick_pick),
    ;

    companion object {
        val defaultEntries: List<DashboardCards>
            get() =
                listOf(
                    ArrOverview,
                    SeerrOverview,
                    PendingRequests,
                    PendingIssues,
                    ProwlarrOverview,
                    BazarrOverview,
                    TracearrOverview,
                    TracearrActiveStreams,
                    ActivityQueue,
                    RecentlyAdded,
                    OnToday,
                    UpcomingReleases,
                    Network,
                    InstanceDashboard,
                )
    }
}
