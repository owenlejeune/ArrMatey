package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ArrDiskSpace(
    val freeSpace: Long,
    val totalSpace: Long,
    val label: String? = null,
    val path: String? = null,
) {
    val usedPercentage: Float
        get() {
            if (totalSpace <= 0f) return 0f
            val usedSpace = totalSpace - freeSpace
            return usedSpace.toFloat() / totalSpace.toFloat()
        }
}
