package com.dnfapps.arrmatey.notifications

class NotificationCleanupUseCase(
    private val notificationManager: NotificationManager
) {
    /**
     * Cancels notifications for items that are no longer present in the latest fetch.
     * 
     * @param instanceId The ID of the instance being processed.
     * @param currentItems All items currently held in memory across all dates.
     * @param fetchedIds The set of IDs returned by the latest API fetch.
     * @param getId A function to extract the stable notification ID from an item.
     * @param getInstanceId A function to extract the instance ID from an item.
     */
    fun <T> cleanup(
        instanceId: Long,
        currentItems: List<T>,
        fetchedIds: Set<Int>,
        getId: (T) -> Int,
        getInstanceId: (T) -> Long?
    ) {
        currentItems
            .filter { getInstanceId(it) == instanceId }
            .forEach { item ->
                val id = getId(item)
                if (id !in fetchedIds) {
                    notificationManager.cancelNotification(id)
                }
            }
    }
}
