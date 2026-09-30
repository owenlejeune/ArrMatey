package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import kotlinx.serialization.Serializable

@Serializable
data class ManualImportFile(
    val id: Long? = null,
    val path: String? = null,
    val relativePath: String? = null,
    val name: String? = null,
    val size: Long = 0L,
    val quality: QualityInfo? = null,
    val languages: List<Language> = emptyList(),
    val releaseGroup: String? = null,
    val qualityWeight: Int = 0,
    val downloadId: String? = null,
    val customFormats: List<CustomFormat> = emptyList(),
    val customFormatScore: Int = 0,
    val rejections: List<ManualImportRejection> = emptyList(),
    val seriesId: Long? = null,
    val episodeIds: List<Long> = emptyList(),
    val movieId: Long? = null,
    val artistId: Long? = null,
    val albumId: Long? = null,
    val authorId: Long? = null,
    val bookId: Long? = null,
) {
    val reasons: List<String>
        get() = rejections.mapNotNull { it.reason }

    val isAcceptable: Boolean
        get() = rejections.isEmpty()

    val qualityLabel: String
        get() = quality?.qualityLabel ?: "Unknown"

    val sizeLabel: String
        get() = size.bytesAsFileSizeString()

    val languageLabel: String
        get() = languages.takeUnless { it.isEmpty() }?.mapNotNull { it.name }?.joinToString(", ") ?: "Unknown"

    val displayName: String
        get() = relativePath ?: name ?: path ?: "Unknown"

    val stableId: String
        get() = id?.toString() ?: path ?: relativePath ?: name ?: ""
}

@Serializable
data class ManualImportRejection(
    val reason: String? = null,
    val type: String? = null,
)
