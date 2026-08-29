package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.compose.utils.QueueSortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder

data class ActivityQueueUiState(
    val instanceId: Long? = null,
    val sortBy: QueueSortBy = QueueSortBy.Added,
    val sortOrder: SortOrder = SortOrder.Asc,
) {
    constructor() : this(null, QueueSortBy.Added, SortOrder.Asc)
}
