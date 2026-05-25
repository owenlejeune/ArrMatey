package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ListenarrRelease(
    @SerialName("id") val idStr: String? = null,
    override val title: String,
    override val size: Long = 0,
    val source: String? = null,
    val seedersInt: Int? = 0,
    val leechersInt: Int? = 0,
    override val indexerId: Int = 0,
    val downloadType: String? = null,
    val magnetLink: String? = null,
    val torrentUrl: String? = null,
    val nzbUrl: String? = null,
    val qualityStr: String? = null,
    val publishedDateStr: String? = null,
    @SerialName("seeders") val seedersVal: Int? = 0,
    @SerialName("leechers") val leechersVal: Int? = 0,
    val audiobookId: Long? = null,
    override var mediaId: Long? = null
): ArrRelease {
    override val id: Int? get() = null
    override val guid: String get() = idStr ?: magnetLink ?: torrentUrl ?: nzbUrl ?: ""
    override val quality: QualityInfo? get() = qualityStr?.let { 
        QualityInfo(
            quality = Quality(id = 0, name = it),
            revision = Revision(version = 1, real = 0, isRepack = false)
        ) 
    }
    override val qualityWeight: Float get() = 0f
    override val age: Float get() = 0f
    override val ageHours: Float get() = 0f
    override val ageMinutes: Float get() = 0f
    override val indexer: String? get() = source
    override val releaseGroup: String? get() = null
    override val subGroup: String? get() = null
    override val releaseHash: String? get() = null
    override val sceneSource: Boolean get() = false
    override val languages: List<Language> get() = emptyList()
    override val approved: Boolean get() = true
    override val temporarilyRejected: Boolean get() = false
    override val rejected: Boolean get() = false
    override val rejections: List<String> get() = emptyList()
    override val publishDate: Instant? get() = null 
    override val commentUrl: String? get() = null
    override val downloadUrl: String? get() = torrentUrl ?: nzbUrl
    override val infoUrl: String? get() = null
    override val downloadAllowed: Boolean get() = true
    override val releaseWeight: Float get() = 0f
    override val customFormats: List<CustomFormat> get() = emptyList()
    override val customFormatScore: Float get() = 0f
    override val magnetUrl: String? get() = magnetLink
    override val infoHash: String? get() = null
    override val seeders: Int get() = seedersVal ?: seedersInt ?: 0
    override val leechers: Int get() = leechersVal ?: leechersInt ?: 0
    override val protocol: ReleaseProtocol get() = when {
        downloadType?.lowercase() == "torrent" || magnetLink != null || torrentUrl != null -> ReleaseProtocol.Torrent
        downloadType?.lowercase() == "usenet" || nzbUrl != null -> ReleaseProtocol.Usenet
        else -> ReleaseProtocol.Unknown
    }
    override val downloadClientId: Int? get() = null
    override val downloadClient: String? get() = null
    override val shouldOverride: Boolean get() = false
}
