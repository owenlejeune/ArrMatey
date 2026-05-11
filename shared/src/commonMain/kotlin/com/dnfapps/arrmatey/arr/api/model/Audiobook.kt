package com.dnfapps.arrmatey.arr.api.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import kotlin.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Audiobook(
    override val id: Long? = null,
    override val title: String? = null,
    val authors: List<String> = emptyList(),
    val narrators: List<String> = emptyList(),
    val publishYear: String? = null,
    @Contextual val publishedDate: Instant? = null,
    val series: String? = null,
    val seriesNumber: String? = null,
    override val genres: List<String> = emptyList(),
    val asin: String? = null,
    val publisher: String? = null,
    val language: String? = null,
    override val runtime: Int? = null,
    val imageUrl: String? = null,
    override val monitored: Boolean = false,
    val basePath: String? = null,
    val filePath: String? = null,
    override val fileSize: Long = 0,
    val fileCount: Int = 0,
    val authorAsins: List<String> = emptyList(),
    val wanted: Boolean = false,
    @SerialName("status") val statusStr: String? = null,

    val instanceId: Long? = null
) : ArrMedia {
    override val year: Int? get() = publishYear?.toIntOrNull()
    override val originalLanguage: Language? get() = language?.let { Language(0, it) }
    override val images: List<ArrImage> get() = imageUrl?.let { listOf(ArrImage(CoverType.Cover, url = it, remoteUrl = it)) } ?: emptyList()

    override val qualityProfileId: Int get() = 0
    override val sortTitle: String? get() = title
    override val overview: String? get() = null
    override val path: String? get() = basePath
    override val cleanTitle: String? get() = title
    override val titleSlug: String? get() = asin
    override val rootFolderPath: String? get() = null
    override val folder: String? get() = null
    override val certification: String? get() = null
    override val tags: List<Int> get() = emptyList()
    override val alternateTitles: List<AlternateTitle> get() = emptyList()
    override val ratings: ArrRatings? get() = null
    override val statistics: ArrStatistics get() = AudiobookStatistics(fileSize)
    override val added: Instant? get() = publishedDate
    override val status: MediaStatus get() = when(statusStr) {
        "quality-match" -> MediaStatus.Released
        "no-file" -> MediaStatus.Announced
        else -> MediaStatus.Deleted
    }

    override val guid: Long get() = id ?: (asin?.hashCode()?.toLong() ?: 0L)
    override fun ratingScore(): Double = 0.0
    override val statusProgress: Float get() = if (statusStr == "quality-match") 1.0f else 0.0f
    override val statusColor: Color get() = if (statusStr == "quality-match") ArrGreen else Color.Unspecified
    override val releasedBy: String? get() = publisher
    override val statusString: String get() = statusStr ?: "Unknown"

    override fun setMonitored(monitored: Boolean): ArrMedia = copy(monitored = monitored)

    override val isMissing: Boolean get() = statusStr == "no-file"
    override val isWanted: Boolean get() = wanted
}
