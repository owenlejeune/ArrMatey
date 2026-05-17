package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import kotlinx.datetime.TimeZone
import kotlinx.datetime.ZoneOffset
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class AudiobookMetadata(
    val asin: String,
    val title: String,
    val subtitle: String? = null,
    val authors: List<SearchAuthor> = emptyList(),
    val narrators: List<SearchNarrator> = emptyList(),
    val publisher: String? = null,
    @Serializable(with = ListenarrInstantSerializer::class)
    val publishDate: Instant? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val lengthMinutes: Int? = null,
    val language: String? = null,
    val genres: List<SearchGenre> = emptyList(),
    val series: List<SearchSeries> = emptyList(),
    val explicit: Boolean = false,
    val releaseDate: String? = null,
    val isbn: String? = null,
    val region: String? = null,
    val bookFormat: String? = null,
    val contentType: String? = null,
    val contentDeliveryType: String? = null,
    val sku: String? = null
) {
    fun toBody(source: String): AudiobookMetadataBody {
        val primarySeries = series.firstOrNull()

        return AudiobookMetadataBody(
            abridged = contentDeliveryType?.contains("abridged", ignoreCase = true) == true,
            asin = asin,
            authors = authors.map { it.name },
            description = description.orEmpty(),
            explicit = explicit,
            genres = genres.map { it.name },
            imageUrl = imageUrl.orEmpty(),
            isbn = listOfNotNull(isbn),
            language = language.orEmpty(),
            narrators = narrators.map { it.name },
            publishYear = publishDate?.toLocalDateTime(TimeZone.UTC)?.year?.toString()
                ?: releaseDate?.take(4).orEmpty(),
            publishDate = releaseDate.orEmpty(),
            publisher = publisher.orEmpty(),
            runtime = lengthMinutes ?: 0,
            series = primarySeries?.name.orEmpty(),
            seriesMemberships = series.mapIndexed { index, s ->
                SeriesMembership(
                    seriesName = s.name,
                    seriesNumber = s.position.orEmpty(),
                    isPrimary = index == 0,
                    sortOrder = index
                )
            },
            seriesNumber = primarySeries?.position.orEmpty(),
            source = source,
            tags = emptyList(),
            title = title
        )
    }
}
