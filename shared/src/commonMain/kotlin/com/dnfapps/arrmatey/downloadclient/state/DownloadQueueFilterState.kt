package com.dnfapps.arrmatey.downloadclient.state

import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus

data class DownloadQueueFilterState(
    val query: String = "",
    val clientIds: List<Long> = emptyList(),
    val selectedStatuses: Set<DownloadItemStatus> = emptySet(),
    val selectedTags: Set<String> = emptySet(),
    val activeOnly: Boolean = false,
    val completedOnly: Boolean = false,
    val excludeTags: Boolean = false,
    val excludeStatuses: Boolean = false
) {
    constructor(): this("") // empty ios constructor
}
