package com.dnfapps.arrmatey.arr.api.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.arr.api.client.HasArrImages
import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import com.dnfapps.arrmatey.extensions.formatMinutesAsRuntime
import com.dnfapps.arrmatey.extensions.formatSecondsAsRuntime
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrGrey
import com.dnfapps.arrmatey.ui.theme.ArrRed
import kotlin.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Audiobook(
    override val id: Long? = null,
    override val title: String? = null,
    override val genres: List<String> = emptyList(),
    override val monitored: Boolean = false,
    override val qualityProfileId: Int = 0,
    override val tags: List<Int> = emptyList(),
    override val runtime: Int? = null,
    override val images: List<ArrImage> = emptyList(),
    val authors: List<String> = emptyList(),
    val narrators: List<String> = emptyList(),
    val publishYear: String? = null,
    val series: String? = null,
    val seriesNumber: String? = null,
    val asin: String? = null,
    val publisher: String? = null,
    val language: String? = null,
    val imageUrl: String? = null,
    val basePath: String? = null,
    val filePath: String? = null,
    val fileCount: Int = 0,
    val authorAsins: List<String> = emptyList(),
    val wanted: Boolean = false,
    @SerialName("status") val statusStr: String? = null,
    val files: List<AudiobookFile> = emptyList(),
    @SerialName("description") override val overview: String? = null,

    @Serializable(with = ListenarrInstantSerializer::class)
    val publishedDate: Instant? = null,

    val instanceId: Long? = null
) : ArrMedia, HasArrImages<Audiobook>, CalendarItem {

    override fun withLocalImages(instanceUrl: String): Audiobook {
        val localImages = imageUrl?.let { path ->
            listOf(
                ArrImage(CoverType.Poster, path, path)
                    .rebuildWithLocalUrls(instanceUrl)
            )
        } ?: emptyList()

        return copy(images = localImages)
    }

    override val year: Int?
        get() = publishYear?.toIntOrNull()

    override val originalLanguage: Language?
        get() = language?.let { Language(0, it) }

    override val sortTitle: String?
        get() = title

    override val path: String?
        get() = basePath

    override val cleanTitle: String?
        get() = title

    override val titleSlug: String?
        get() = asin

    override val rootFolderPath: String?
        get() = basePath

    override val folder: String?
        get() = null

    override val certification: String?
        get() = null

    override val alternateTitles: List<AlternateTitle>
        get() = emptyList()

    override val ratings: ArrRatings?
        get() = null

    override val statistics: ArrStatistics
        get() = AudiobookStatistics(fileSize)

    override val added: Instant?
        get() = publishedDate

    override val status: MediaStatus get() = when(statusStr) {
        "quality-match" -> MediaStatus.Released
        "no-file" -> MediaStatus.Announced
        else -> MediaStatus.Deleted
    }

    override val guid: Long
        get() = id ?: (asin?.hashCode()?.toLong() ?: 0L)

    override fun ratingScore(): Double = 0.0

    override val statusProgress: Float
        get() = if (statusStr == "quality-match") 1.0f else 0.0f

    override val statusColor: Color
        get() = when {
            statusStr == "quality-match" -> ArrGreen
            monitored -> ArrRed
            else -> ArrGrey
        }

    override val releasedBy: String?
        get() = publisher

    override val statusString: String
        get() = statusStr ?: "Unknown"

    override fun setMonitored(monitored: Boolean): ArrMedia = copy(monitored = monitored)

    override val isMissing: Boolean
        get() = statusStr == "no-file"
    override val isWanted: Boolean
        get() = wanted

    override val runtimeString: String
        get() = when {
            runtime == null -> ""
            runtime > 20_000 -> runtime.formatSecondsAsRuntime()
            else -> runtime.formatMinutesAsRuntime()
        }

    override val fileSize: Long
        get() = files.sumOf { it.size ?: 0 }

    fun copyForCreation(
        monitored: Boolean,
        qualityProfileId: Int,
        rootFolderPath: String,
        tags: List<Int>
    ) = copy(
        id = 0,
        monitored = monitored,
        qualityProfileId = qualityProfileId,
        basePath = rootFolderPath,
        tags = tags
    )

    val releaseQuery: String
        get() = "$title $${authors.joinToString(" ")}"
}