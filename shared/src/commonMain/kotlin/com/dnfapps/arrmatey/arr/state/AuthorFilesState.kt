package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.api.model.HistoryItem

data class AuthorFilesState(
    val files: List<BookFile> = emptyList(),
    val history: List<HistoryItem> = emptyList(),
    val isRefreshing: Boolean = false
) {
    constructor(): this(emptyList())
}