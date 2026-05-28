package com.dnfapps.arrmatey.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AndroidPreferencesStore(
    dataStoreFactory: DataStoreFactory
) {
    private val dataStore: DataStore<Preferences> = dataStoreFactory.providePlatformDataStore()

    companion object {
        private val KEY_SHORTCUTS_ORDER = stringPreferencesKey("shortcuts_order")
        private val KEY_SHORTCUTS_DISABLED = stringSetPreferencesKey("shortcuts_disabled")
    }

    val shortcutsOrder: Flow<List<String>> = dataStore.data.map {
        it[KEY_SHORTCUTS_ORDER]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    val disabledShortcuts: Flow<Set<String>> = dataStore.data.map {
        it[KEY_SHORTCUTS_DISABLED] ?: emptySet()
    }

    suspend fun saveShortcutsOrder(order: List<String>) {
        dataStore.edit { it[KEY_SHORTCUTS_ORDER] = order.joinToString(",") }
    }

    suspend fun saveDisabledShortcuts(disabled: Set<String>) {
        dataStore.edit { it[KEY_SHORTCUTS_DISABLED] = disabled }
    }
}
