package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder

data class IndexersSortingState(
    val sortOrder: SortOrder = SortOrder.Asc,
    val sortBy: SortBy = SortBy.Name
) {
    constructor(): this(SortOrder.Asc, SortBy.Name)
}