package com.dnfapps.arrmatey.downloadclient.state

import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder

data class DownloadQueueSortState(
    val sortBy: SortBy = SortBy.Title,
    val sortOrder: SortOrder = SortOrder.Asc
) {
    constructor(): this(sortBy = SortBy.Title) // ios empty constructor
}