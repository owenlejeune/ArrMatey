package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookFileBulkDeleteBody(
    val bookFilesIds: List<Long>
)
