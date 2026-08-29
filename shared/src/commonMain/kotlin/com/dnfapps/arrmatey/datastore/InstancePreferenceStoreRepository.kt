package com.dnfapps.arrmatey.datastore

class InstancePreferenceStoreRepository(
    private val dataStoreFactory: DataStoreFactory,
) {
    private val dataStoreMap = mutableMapOf<Long, InstancePreferenceStore>()

    fun getInstancePreferences(instanceId: Long) =
        dataStoreMap.getOrPut(instanceId) {
            InstancePreferenceStore(instanceId, dataStoreFactory)
        }
}
