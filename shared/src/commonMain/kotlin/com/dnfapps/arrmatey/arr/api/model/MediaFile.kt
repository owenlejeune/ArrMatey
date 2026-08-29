package com.dnfapps.arrmatey.arr.api.model

import kotlin.time.Instant

interface MediaFile {
    val id: Long
    val relativePath: String
    val path: String?
    val size: Long
    val dateAdded: Instant?
    val sceneName: String?
    val releaseGroup: String?
    val languages: List<Language>
    val quality: QualityInfo?
    val customFormats: List<CustomFormat>
    val customFormatScore: Int?
    val indexerFlags: Int?
    val mediaInfo: MediaInfo?
    val originalFilePath: String?
    val qualityCutoffNotMet: Boolean

    val qualityName: String?
        get() = quality?.quality?.name
}
