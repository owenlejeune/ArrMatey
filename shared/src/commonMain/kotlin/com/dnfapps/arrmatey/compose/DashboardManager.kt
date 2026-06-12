package com.dnfapps.arrmatey.compose

import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.StringResolver
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardManager(
    private val preferencesStore: PreferencesStore
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
        val newCards = _cardsOrder.value.toMutableList().apply {
            remove(card)
        }
        saveCardOrder(newCards)
    }

    fun addCard(card: DashboardCards) {
        val newCards = _cardsOrder.value.toMutableList().apply {
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

enum class DashboardCards(val title: StringResource) {
    ArrOverview(MR.strings.dashboard_arr_overview),
    SeerrOverview(MR.strings.dashboard_seerr_overview),
    ProwlarrOverview(MR.strings.dashboard_prowlarr_overview),
    Network(MR.strings.dashboard_network_monitor),
    RecentlyAdded(MR.strings.dashboard_recently_added),
    DownloadClients(MR.strings.dashboard_download_clients_overview),
    ActivityQueue(MR.strings.dashboard_activity_queue_overview),
    OnToday(MR.strings.dashboard_todays_releases),
    UpcomingReleases(MR.strings.dashboard_upcoming_releases),
    InstanceDashboard(MR.strings.dashboard_instance_dashboards);

    companion object {
        val defaultEntries: List<DashboardCards>
            get() = listOf(
                ArrOverview, SeerrOverview, ProwlarrOverview, ActivityQueue,
                RecentlyAdded, OnToday, UpcomingReleases, Network, InstanceDashboard
            )
    }
}