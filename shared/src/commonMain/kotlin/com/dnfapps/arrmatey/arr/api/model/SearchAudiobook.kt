package com.dnfapps.arrmatey.arr.api.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.arr.api.client.HasArrImages
import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import com.dnfapps.arrmatey.ui.theme.ArrGrey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SearchAudiobook(
    val asin: String,
    override val title: String,
    val subtitle: String? = null,
    val region: String? = null,
    val regions: List<String> = emptyList(),
    val description: String? = null,
    val summary: String? = null,
    val bookFormat: String? = null,
    val imageUrl: String? = null,
    val lengthMinutes: Int? = null,
    val whisperSync: Boolean = false,
    val publisher: String? = null,
    val isbn: String? = null,
    val language: String? = null,
    val releaseDate: LocalDate? = null,
    val explicit: Boolean = false,
    val hasPdf: Boolean = false,
    val link: String? = null,
    val sku: String? = null,
    val isListenable: Boolean = false,
    val isAvailable: Boolean = false,
    val isBuyable: Boolean = false,
    val contentType: String? = null,
    val contentDeliveryType: String? = null,
    val authors: List<SearchAuthor> = emptyList(),
    val narrators: List<SearchNarrator> = emptyList(),
    @SerialName("genres") val searchGenres: List<SearchGenre> = emptyList(),
    val series: List<SearchSeries> = emptyList(),
    val seriesList: List<String> = emptyList(),
    @Serializable(with = ListenarrInstantSerializer::class)
    val updatedAt: Instant? = null,
    override val images: List<ArrImage> = emptyList(),

    override val monitored: Boolean = false,
    override val qualityProfileId: Int = 0,
    override val rootFolderPath: String? = null,
    val relativePath: String? = null
) : ArrMedia, HasArrImages<SearchAudiobook> {

    override fun withLocalImages(instanceUrl: String): SearchAudiobook {
        val localImages = imageUrl?.let { path ->
            listOf(
                ArrImage(CoverType.Poster, path, path)
                    .rebuildWithLocalUrls(instanceUrl)
            )
        } ?: emptyList()
        return copy(images = localImages)
    }

    override val id: Long?
        get() = null
    override val originalLanguage: Language?
        get() = language?.let { Language(0, it) }
    override val year: Int?
        get() = releaseDate?.year
    override val runtime: Int?
        get() = lengthMinutes
    override val sortTitle: String
        get() = title
    override val overview: String?
        get() = summary ?: description
    override val path: String?
        get() = null
    override val cleanTitle: String
        get() = title
    override val titleSlug: String
        get() = asin
    override val folder: String?
        get() = null
    override val certification: String?
        get() = null
    override val genres: List<String>
        get() = searchGenres.map { it.name }
    override val tags: List<Int>
        get() = emptyList()
    override val alternateTitles: List<AlternateTitle>
        get() = emptyList()
    override val ratings: ArrRatings?
        get() = null
    override val statistics: ArrStatistics?
        get() = null
    override val added: Instant?
        get() = updatedAt
    override val status: MediaStatus
        get() = MediaStatus.Released

    override val guid: Long
        get() = asin.hashCode().toLong()
    override fun ratingScore(): Double = 0.0
    override val statusProgress: Float
        get() = 0f
    override val statusColor: Color
        get() = ArrGrey
    override val releasedBy: String?
        get() = publisher
    override val statusString: String
        get() = "Search Result"

    override fun setMonitored(monitored: Boolean): ArrMedia = this
    override val isMissing: Boolean
        get() = true

    fun copyForCreation(
        monitored: Boolean,
        qualityProfileId: Int,
        rootFolderPath: String,
        relativePath: String
    ) = copy(
        monitored = monitored,
        qualityProfileId = qualityProfileId,
        rootFolderPath = rootFolderPath,
        relativePath = relativePath
    )
}
