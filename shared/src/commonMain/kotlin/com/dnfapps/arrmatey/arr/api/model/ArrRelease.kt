package com.dnfapps.arrmatey.arr.api.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrRed
import kotlinx.serialization.SerialName
import kotlin.time.Instant

sealed interface ArrRelease {
    val id: Int?
    val guid: String
    val quality: QualityInfo?
    val qualityWeight: Float
    val age: Float
    val ageHours: Float
    val ageMinutes: Float
    val size: Long
    val indexerId: Int
    val indexer: String?
    val releaseGroup: String?
    val subGroup: String?
    val releaseHash: String?
    val title: String
    val sceneSource: Boolean
    val languages: List<Language>
    val approved: Boolean
    val temporarilyRejected: Boolean
    val rejected: Boolean
    val rejections: List<String>
    val publishDate: Instant?
    val commentUrl: String?
    val downloadUrl: String?
    val infoUrl: String?
    val downloadAllowed: Boolean
    val releaseWeight: Float
    val customFormats: List<CustomFormat>
    val customFormatScore: Float
    val magnetUrl: String?
    val infoHash: String?
    val seeders: Int
    val leechers: Int
    val protocol: ReleaseProtocol
    val downloadClientId: Int?
    val downloadClient: String?
    val shouldOverride: Boolean?

    val typeLabel: String
        get() {
            if (protocol == ReleaseProtocol.Torrent) {
                return "${protocol.name} ($seeders/$leechers)"
            }
            return protocol.name
        }

    val indexerLabel: String
        get() = indexer ?: "Unknown"

    val peerColor: Color
        get() = when {
            protocol == ReleaseProtocol.Usenet -> ArrGreen
            rejections.any { it.contains("Not enough seeders") } -> ArrRed
            seeders >= 50 -> ArrGreen
            seeders >= 10 -> ArrBlue
            seeders >= 1 -> ArrOrange
            else -> ArrRed
        }

    val peerColorHex: String
        get() = when {
            protocol == ReleaseProtocol.Usenet -> "#01b801"
            rejections.any { it.contains("Not enough seeders") } -> "#ff3e3e"
            seeders >= 50 -> "#01b801"
            seeders >= 10 -> "#00b2ff"
            seeders >= 1 -> "#FFA505"
            else -> "#ff3e3e"
        }
}

enum class ReleaseProtocol {
    @SerialName("usenet")
    Usenet,

    @SerialName("torrent")
    Torrent,

    @SerialName("unknown")
    Unknown
}

sealed interface ReleaseParams {
    data class Movie(val movieId: Long): ReleaseParams
    data class Series(
        val seriesId: Long? = null,
        val seasonNumber: Int? = null,
        val episodeId: Long? = null
    ): ReleaseParams
    data class Album(
        val albumId: Long,
        val artistId: Long? = null,
    ): ReleaseParams
}