package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.AudiobookFile
import com.dnfapps.arrmatey.arr.api.model.HistoryItem

data class AudiobookFilesState(
    val files: List<AudiobookFile> = emptyList(),
    val history: List<HistoryItem> = emptyList(),
    val isRefreshing: Boolean = false
) {
    constructor(): this(emptyList())
}