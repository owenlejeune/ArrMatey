package com.dnfapps.arrmatey.compose

import com.dnfapps.arrmatey.datastore.PreferencesStore

class DashboardManager(
    private val preferencesStore: PreferencesStore
) {
    val cardsOrder = preferencesStore.dashboardCardsOrder

    fun saveCardsOrder(cards: List<DashboardCards>) {
        preferencesStore.updateDashboardCardsOrder(cards)
    }
}

enum class DashboardCards {
    ArrOverview,
    SeerrOverview,
    ProwlarrOverview,
    Network,
    RecentlyAdded,
    DownloadClients,
    RecentActivity,
    OnToday,
    UpcomingReleases,
    InstanceDashboard
}