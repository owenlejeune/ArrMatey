package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class KapowarrRelease(
    val series: String? = null,
    val year: Int? = null,
    @Serializable(with = NullableIntOrArraySerializer::class)
    @SerialName("volume_number") val volumeNumber: Int? = null,
    @SerialName("special_version") val specialVersion: String? = null,
    val annual: Boolean = false,
    val link: String? = null,
    @SerialName("display_title") val displayTitle: String? = null,
    override val size: Long = 0,
    @SerialName("indexer_id") val indexerIdRaw: Int? = null,
    @SerialName("indexer_title") val indexerTitle: String? = null,
    val match: Boolean = false,
    @SerialName("match_issue") val matchIssue: String? = null,
    override var mediaId: Long? = null,
) : ArrRelease {

    override val id: Int?
        get() = link?.hashCode() ?: displayTitle?.hashCode()

    override val guid: String
        get() = link ?: displayTitle ?: ""

    override val quality: QualityInfo?
        get() = null

    override val qualityWeight: Float
        get() = 0f

    override val age: Float
        get() = 0f

    override val ageHours: Float
        get() = 0f

    override val ageMinutes: Float
        get() = 0f

    override val indexerId: Int
        get() = indexerIdRaw ?: 0

    override val indexer: String?
        get() = indexerTitle

    override val releaseGroup: String?
        get() = null

    override val subGroup: String?
        get() = null

    override val releaseHash: String?
        get() = null

    override val title: String
        get() = displayTitle ?: series ?: "Unknown Release"

    override val sceneSource: Boolean
        get() = false

    override val languages: List<Language>
        get() = emptyList()

    override val approved: Boolean
        get() = match

    override val temporarilyRejected: Boolean
        get() = false

    override val rejected: Boolean
        get() = !match

    override val rejections: List<String>
        get() = listOfNotNull(matchIssue)

    override val publishDate: Instant?
        get() = null

    override val commentUrl: String?
        get() = link

    override val downloadUrl: String?
        get() = link

    override val infoUrl: String?
        get() = link

    override val downloadAllowed: Boolean
        get() = true

    override val releaseWeight: Float
        get() = if (match) 100f else 0f

    override val customFormats: List<CustomFormat>
        get() = emptyList()

    override val customFormatScore: Float
        get() = 0f

    override val magnetUrl: String?
        get() = null

    override val infoHash: String?
        get() = null

    override val seeders: Int
        get() = 0

    override val leechers: Int
        get() = 0

    override val protocol: ReleaseProtocol
        get() = ReleaseProtocol.Usenet

    override val downloadClientId: Int?
        get() = null

    override val downloadClient: String?
        get() = null

    override val shouldOverride: Boolean?
        get() = null
}
