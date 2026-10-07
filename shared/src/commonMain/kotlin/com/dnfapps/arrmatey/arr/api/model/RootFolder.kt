package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import kotlinx.serialization.Serializable

@Serializable
enum class RootFolderType(val value: Int) {
    Audiobook(1),
    Ebook(2),
    None(0),
    ;

    companion object {
        fun fromValue(value: Int?): RootFolderType? = entries.firstOrNull { it.value == value }
    }
}

@Serializable
data class RootFolder(
    val id: Int,
    val path: String,
    val name: String? = null,
    val accessible: Boolean = true,
    val freeSpace: Long = 0L,
    val totalSpace: Long = 0L,
    val isDefault: Boolean = false,
    val unmappedFolders: List<UnmappedFolder> = emptyList(),
    val folderType: Int? = null,
    val isEffectiveDefaultAudiobook: Boolean = false,
    val isEffectiveDefaultEbook: Boolean = false,
    val audiobookQualityProfileId: Int? = null,
    val audiobookMetadataProfileId: Int? = null,
    val ebookQualityProfileId: Int? = null,
    val ebookMetadataProfileId: Int? = null,
) {
    val freeSpaceString: String
        get() = freeSpace.bytesAsFileSizeString()

    val totalSpaceString: String
        get() = totalSpace.bytesAsFileSizeString()

    val type: RootFolderType?
        get() = RootFolderType.fromValue(folderType)

    val isAudiobook: Boolean
        get() = type == RootFolderType.Audiobook || isEffectiveDefaultAudiobook

    val isEbook: Boolean
        get() = type == RootFolderType.Ebook || isEffectiveDefaultEbook
}

fun List<RootFolder>.filterForAudiobook(): List<RootFolder> {
    val filtered = filter { it.isAudiobook || (!it.isEbook && it.folderType == null) }
    return if (filtered.isEmpty()) this else filtered
}

fun List<RootFolder>.filterForEbook(): List<RootFolder> {
    val filtered = filter { it.isEbook || (!it.isAudiobook && it.folderType == null) }
    return if (filtered.isEmpty()) this else filtered
}

fun List<RootFolder>.defaultForAudiobook(preferredPath: String? = null): RootFolder? = firstOrNull { it.path == preferredPath }
    ?: firstOrNull { it.isEffectiveDefaultAudiobook }
    ?: filterForAudiobook().firstOrNull { it.isAudiobook }
    ?: filterForAudiobook().firstOrNull()

fun List<RootFolder>.defaultForEbook(preferredPath: String? = null): RootFolder? = firstOrNull { it.path == preferredPath }
    ?: firstOrNull { it.isEffectiveDefaultEbook }
    ?: filterForEbook().firstOrNull { it.isEbook }
    ?: filterForEbook().firstOrNull()
