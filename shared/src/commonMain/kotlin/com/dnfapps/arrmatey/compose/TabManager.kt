package com.dnfapps.arrmatey.compose

import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.datastore.TabPreferences
import com.dnfapps.arrmatey.webpage.model.CustomWebpage
import com.dnfapps.arrmatey.webpage.repository.CustomWebpageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TabManager(
    preferencesStore: PreferencesStore,
    customWebpageRepository: CustomWebpageRepository
) {
    private val tabPreferencesFlow = preferencesStore.tabPreferences
    private val customWebpagesFlow = customWebpageRepository.getAllWebpages()

    data class TabConfiguration(
        val visibleTabs: List<TabItem>,
        val drawerTabs: List<TabItem>
    )

    val tabConfiguration: Flow<TabConfiguration> = combine(
        tabPreferencesFlow,
        customWebpagesFlow
    ) { prefs, webpages ->
        TabConfiguration(
            visibleTabs = buildVisibleTabs(prefs, webpages),
            drawerTabs = buildDrawerTabs(prefs, webpages)
        )
    }

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
        // Map of all possible items
        val standardItems = TabItem.Standard.entries.associateBy { it.key }
        val webpageItems = webpages.associate { "webpage_${it.id}" to TabItem.CustomWebpage(it.id, it.name, it.url, it.headers) }
        val allItems = standardItems + webpageItems

        // Resolve keys in the exact order saved in preferences
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
            // 1. Add explicitly hidden items in order
            prefs.orderedHiddenKeys.mapNotNull { key -> allItems[key] }.forEach { add(it) }

            // 2. Safety: Catch webpages that exist in DB but aren't in preferences yet
            val tracked = (prefs.orderedVisibleKeys + prefs.orderedHiddenKeys).toSet()
            webpages.filter { "webpage_${it.id}" !in tracked }.forEach {
                add(TabItem.CustomWebpage(it.id, it.name, it.url, it.headers))
            }

            // 3. Force Settings to be at the bottom if missing
            if (this.none { it == TabItem.Standard.SETTINGS }) {
                add(TabItem.Standard.SETTINGS)
            }
        }
    }
}