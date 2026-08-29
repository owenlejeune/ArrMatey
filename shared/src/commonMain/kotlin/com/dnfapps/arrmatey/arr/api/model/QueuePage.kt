package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class QueuePage(
    val page: Int,
    val pageSize: Int,
    val totalRecords: Int,
    val records: List<QueueItem>,
) {
    fun setInstance(
        id: Long,
        name: String,
    ) = copy(
        records =
            records.apply {
                forEach { r ->
                    r.instanceId = id
                    r.instanceName = name
                }
            },
    )
}
