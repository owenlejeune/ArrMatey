package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ExtraFile
import com.dnfapps.arrmatey.arr.api.model.HistoryItem

data class MovieFilesState(
    val extraFiles: List<ExtraFile> = emptyList(),
    val history: List<HistoryItem> = emptyList(),
    val isRefreshing: Boolean = false,
) {
    constructor() : this(emptyList(), emptyList(), false)
}
