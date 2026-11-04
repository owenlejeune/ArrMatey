package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.compose.utils.SortBy

class InstancePreferenceStoreRepository(
    private val dataStoreFactory: DataStoreFactory,
) {
    private val dataStoreMap = mutableMapOf<Long, InstancePreferenceStore>()

    fun getInstancePreferences(instanceId: Long, defaultSortBy: SortBy = SortBy.Title) = dataStoreMap.getOrPut(instanceId) {
        InstancePreferenceStore(instanceId, dataStoreFactory, defaultSortBy)
    }

    fun getMediaServerPreferences(serverId: Long, defaultSortBy: SortBy = SortBy.Title): InstancePreferenceStore = getInstancePreferences(mediaServerPrefId(serverId), defaultSortBy)

    companion object {
        fun mediaServerPrefId(serverId: Long): Long = -10_000L - serverId
    }
}
