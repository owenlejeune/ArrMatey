package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ListenarrDiskSpace(
    val usedBytes: Long,
    val totalBytes: Long,
    val freeBytes: Long,
    val usedPercentage: Float,
    val usedFormatted: String,
    val totalFormatted: String,
    val freeFormatted: String,
    val driveName: String? = null,
    val status: String? = null,
) {
    fun toArrDiskSpace(): ArrDiskSpace =
        ArrDiskSpace(
            freeSpace = freeBytes,
            totalSpace = totalBytes,
            path = driveName,
        )
}
