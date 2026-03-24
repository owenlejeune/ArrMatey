package com.dnfapps.arrmatey.compose

import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.datastore.TabPreferences
import com.dnfapps.arrmatey.webpage.model.CustomWebpage
import com.dnfapps.arrmatey.webpage.repository.CustomWebpageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TabManager(
    preferencesStore: PreferencesStore,
    customWebpageRepository: CustomWebpageRepository
) {
    private val tabPreferencesFlow = preferencesStore.tabPreferences
    private val customWebpagesFlow = customWebpageRepository.getAllWebpages()

    data class TabConfiguration(
        val visibleTabs: List<TabItem> = TabItem.defaultStandardEntries(),
        val drawerTabs: List<TabItem> = TabItem.defaultHiddenStandard()
    ) {
        constructor(): this(TabItem.defaultStandardEntries()) // empty ios constructor
    }

    val tabConfiguration: StateFlow<TabConfiguration> = combine(
        tabPreferencesFlow,
        customWebpagesFlow
    ) { prefs, webpages ->
        TabConfiguration(
            visibleTabs = buildVisibleTabs(prefs, webpages),
            drawerTabs = buildDrawerTabs(prefs, webpages)
        )
    }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TabConfiguration()
        )

    fun getVisibleTabs(): Flow<List<TabItem>> {
        return combine(tabPreferencesFlow, customWebpagesFlow) { prefs, webpages ->
            buildVisibleTabs(prefs, webpages)
        }
    }

    fun getHiddenTabs(): Flow<List<TabItem>> {
        return combine(tabPreferencesFlow, customWebpagesFlow) { prefs, webpages ->
            buildDrawerTabs(prefs, webpages)
        }
    }

    fun getAllTabs(): Flow<List<TabItem>> {
        return combine(getVisibleTabs(), getHiddenTabs()) { visible, hidden ->
            visible + hidden
        }
    }

    private fun buildVisibleTabs(
        prefs: TabPreferences,
        webpages: List<CustomWebpage>
    ): List<TabItem> {
        val standardItems = TabItem.Standard.entries.associateBy { it.key }
        val webpageItems = webpages.associate { "webpage_${it.id}" to TabItem.CustomWebpage(it.id, it.name, it.url, it.headers) }
        val allItems = standardItems + webpageItems

        return prefs.orderedVisibleKeys.mapNotNull { key -> allItems[key] }
    }

    private fun buildDrawerTabs(
        prefs: TabPreferences,
        webpages: List<CustomWebpage>
    ): List<TabItem> {
        val standardItems = TabItem.Standard.entries.associateBy { it.key }
        val webpageItems = webpages.associate { "webpage_${it.id}" to TabItem.CustomWebpage(it.id, it.name, it.url, it.headers) }
        val allItems = standardItems + webpageItems

        return buildList {
            prefs.orderedHiddenKeys.mapNotNull { key -> allItems[key] }.forEach { add(it) }

            val tracked = (prefs.orderedVisibleKeys + prefs.orderedHiddenKeys).toSet()
            webpages.filter { "webpage_${it.id}" !in tracked }.forEach {
                add(TabItem.CustomWebpage(it.id, it.name, it.url, it.headers))
            }
        }
    }
}