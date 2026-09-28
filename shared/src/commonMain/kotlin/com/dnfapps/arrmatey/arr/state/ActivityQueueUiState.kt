package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.HistoryStateFilter
import com.dnfapps.arrmatey.compose.utils.QueueSortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder

data class ActivityQueueUiState(
    val instanceId: Long? = null,
    val sortBy: QueueSortBy = QueueSortBy.Added,
    val sortOrder: SortOrder = SortOrder.Asc,
    val selectedTab: ActivityTabSegment = ActivityTabSegment.Activity,
    val historyStateFilter: HistoryStateFilter = HistoryStateFilter.All,
    val historyInstanceId: Long? = null,
) {
    constructor() : this(
        null,
        QueueSortBy.Added,
        SortOrder.Asc,
        ActivityTabSegment.Activity,
        HistoryStateFilter.All,
        null
    )
}
