package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class MovieFile(
    override val id: Long,
    override val relativePath: String,
    override val path: String? = null,
    override val size: Long,
    @Contextual override val dateAdded: Instant? = null,
    override val sceneName: String? = null,
    override val releaseGroup: String? = null,
    override val languages: List<Language> = emptyList(),
    override val quality: QualityInfo? = null,
    override val customFormats: List<CustomFormat> = emptyList(),
    override val customFormatScore: Int? = null,
    override val indexerFlags: Int? = null,
    override val mediaInfo: MediaInfo? = null,
    override val originalFilePath: String? = null,
    override val qualityCutoffNotMet: Boolean,
    val movieId: Int,
    val edition: String? = null,
) : MediaFile
