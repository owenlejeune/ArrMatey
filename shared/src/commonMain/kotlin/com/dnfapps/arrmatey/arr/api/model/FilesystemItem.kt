package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FilesystemItemType {
    @SerialName("folder")
    Folder,

    @SerialName("directory")
    Directory,

    @SerialName("file")
    File,
}

@Serializable
data class FilesystemItem(
    val path: String,
    val name: String,
    val type: FilesystemItemType = FilesystemItemType.Directory,
    val parent: String? = null,
    val size: Long = 0,
    val lastModified: String? = null,
) {
    val isDirectory: Boolean
        get() = type == FilesystemItemType.Directory || type == FilesystemItemType.Folder
}

@Serializable
data class FilesystemResponse(
    val parent: String? = null,
    val directories: List<FilesystemEntry> = emptyList(),
    val files: List<FilesystemEntry> = emptyList(),
)

@Serializable
data class FilesystemEntry(
    val type: String? = null,
    val name: String,
    val path: String,
    val size: Long = 0,
    val lastModified: String? = null,
) {
    fun toFilesystemItem(parent: String?, isDir: Boolean = true): FilesystemItem {
        val itemType = when (type?.lowercase()) {
            "folder" -> FilesystemItemType.Folder
            "file" -> FilesystemItemType.File
            "directory" -> FilesystemItemType.Directory
            else -> if (isDir) FilesystemItemType.Directory else FilesystemItemType.File
        }
        return FilesystemItem(
            path = path,
            name = name,
            type = itemType,
            parent = parent,
            size = size,
            lastModified = lastModified,
        )
    }
}
