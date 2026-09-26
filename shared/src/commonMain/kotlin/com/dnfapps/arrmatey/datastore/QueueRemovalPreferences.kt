package com.dnfapps.arrmatey.datastore

import kotlinx.serialization.Serializable

@Serializable
data class QueueRemovalPreferences(
    val removeFromClient: Boolean = PreferenceDefaults.QUEUE_REMOVE_FROM_CLIENT,
    val addToBlocklist: Boolean = PreferenceDefaults.QUEUE_ADD_TO_BLOCKLIST,
    val skipRedownload: Boolean = PreferenceDefaults.QUEUE_SKIP_REDOWNLOAD,
)
