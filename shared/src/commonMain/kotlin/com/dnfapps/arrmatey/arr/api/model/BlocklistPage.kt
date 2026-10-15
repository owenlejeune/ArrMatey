package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.Serializable

@Serializable
data class BlocklistPage(
    val page: Int,
    val pageSize: Int,
    val totalRecords: Int,
    val records: List<BlocklistItem>,
) {
    fun setInstance(
        id: Long,
        name: String,
        type: InstanceType? = null,
    ) = copy(
        records =
            records.apply {
                forEach { r ->
                    // records instance setting if needed
                }
            },
    )
}
