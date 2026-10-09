package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KapowarrSearchResponse(
    val error: String? = null,
    val result: List<KapowarrSearchResult>? = emptyList(),
)

@Serializable
data class KapowarrSearchResult(
    @SerialName("comicvine_id") val comicvineId: Long? = null,
    val title: String? = null,
    val year: Int? = null,
    @Serializable(with = NullableIntOrArraySerializer::class)
    @SerialName("volume_number") val volumeNumber: Int? = null,
    @SerialName("cover_link") val coverLink: String? = null,
    val description: String? = null,
    @SerialName("site_url") val siteUrl: String? = null,
    val aliases: List<String> = emptyList(),
    val publisher: String? = null,
    @SerialName("issue_count") val issueCount: Int = 0,
    val translated: Boolean = false,
    @SerialName("already_added") val alreadyAdded: Long? = null,
) {
    fun toComicVolume(): ComicVolume {
        val coverImages = if (!coverLink.isNullOrBlank()) {
            listOf(ArrImage(coverType = CoverType.Poster, url = coverLink, remoteUrl = coverLink))
        } else {
            emptyList()
        }

        return ComicVolume(
            id = alreadyAdded,
            comicvineId = comicvineId,
            title = title,
            year = year,
            publisher = publisher,
            volumeNumber = volumeNumber,
            overview = description,
            siteUrl = siteUrl,
            issueCount = issueCount,
            images = coverImages,
        )
    }
}
