package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
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
        type: InstanceType? = null,
    ) = copy(
        records =
        records.apply {
            forEach { r ->
                r.instanceId = id
                r.instanceName = name
                r.instanceType = type
            }
        },
    )
}
