package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class KapowarrRootFolderResponse(
    val error: String? = null,
    val result: List<KapowarrRootFolder>? = emptyList(),
)

@Serializable
data class KapowarrRootFolder(
    val id: Int,
    val folder: String,
    val size: KapowarrRootFolderSize? = null,
) {
    fun toRootFolder(): RootFolder = RootFolder(
        id = id,
        path = folder,
        freeSpace = size?.free ?: 0L,
        totalSpace = size?.total ?: 0L,
    )
}

@Serializable
data class KapowarrRootFolderSize(
    val total: Long = 0L,
    val used: Long = 0L,
    val free: Long = 0L,
)
