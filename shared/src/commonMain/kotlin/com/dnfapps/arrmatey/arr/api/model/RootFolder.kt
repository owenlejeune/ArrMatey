package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import kotlinx.serialization.Serializable

@Serializable
data class RootFolder(
    val id: Int,
    val path: String,
    val accessible: Boolean = true,
    val freeSpace: Long = 0L,
    val isDefault: Boolean = false,
    val unmappedFolders: List<UnmappedFolder> = emptyList()
) {
    val freeSpaceString: String
        get() = freeSpace.bytesAsFileSizeString()
}
