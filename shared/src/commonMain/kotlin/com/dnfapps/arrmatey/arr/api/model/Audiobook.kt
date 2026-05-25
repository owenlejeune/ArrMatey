package com.dnfapps.arrmatey.arr.api.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.arr.api.client.HasArrImages
import com.dnfapps.arrmatey.arr.api.client.ListenarrNullableInstantSerializer
import com.dnfapps.arrmatey.extensions.formatMinutesAsRuntime
import com.dnfapps.arrmatey.extensions.formatSecondsAsRuntime
import com.dnfapps.arrmatey.extensions.toJsonArray
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrGrey
import com.dnfapps.arrmatey.ui.theme.ArrRed
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Instant

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
    val seriesMemberships: List<SeriesMembership> = emptyList(),
    val asin: String? = null,
    val publisher: String? = null,
    val language: String? = null,
    val imageUrl: String? = null,
    val basePath: String? = null,
    val filePath: String? = null,
    val fileCount: Int = 0,
    val wanted: Boolean = false,
    val subtitle: String? = null,
    val edition: String? = null,
    val abridged: Boolean = false,
    val explicit: Boolean = false,
    @SerialName("fileSize") val remoteFileSize: Long? = null,
    @SerialName("status") val statusStr: String? = null,
    val files: List<AudiobookFile> = emptyList(),
    @SerialName("description") override val overview: String? = null,

    @Serializable(with =  ListenarrNullableInstantSerializer::class)
    val publishedDate: Instant? = null,

    override val instanceId: Long? = null
) : ArrMedia, HasArrImages<Audiobook>, CalendarItem {

    companion object {
        fun fromJson(value: String): Audiobook {
            return ArrMedia.json.decodeFromString(value)
        }
    }

    override val calendarId: Long
        get() = id ?: (asin?.hashCode()?.toLong() ?: 0L)

    override fun getCalendarDates(): List<Instant> =
        listOfNotNull(publishedDate)

    override val notificationScheduledTime: Instant?
        get() = publishedDate

    override val notificationMessage: String
        get() = "$title - ${authors.joinToString(", ")}"

    override fun withLocalImages(instanceUrl: String): Audiobook {
        val localImages = imageUrl?.let { path ->
            listOf(
                ArrImage(CoverType.Cover, path, path)
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
        get() = if (fileSize > 0 || fileCount > 0) 1.0f else 0.0f

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
        get() = remoteFileSize ?: files.sumOf { it.size ?: 0 }

    override fun getPoster(): ArrImage? =
        images.firstOrNull()

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

    fun copyForEdit(
        monitored: Boolean,
        qualityProfileId: Int,
        rootFolderPath: String,
        relativePath: String
    ) = copy(
        monitored = monitored,
        qualityProfileId = qualityProfileId,
        basePath = "${rootFolderPath}/${relativePath.trimStart('/')}"
    )

    val releaseQuery: String
        get() = "$title $${authors.joinToString(" ")}"
}

fun Audiobook.toEditBody(): JsonElement = buildJsonObject {
    put("monitored", monitored)
    put("title", title)
    put("subtitle", subtitle)
    put("authors", authors.toJsonArray())
    put("narrators", narrators.toJsonArray())
    put("description", overview)
    put("publisher", publisher)
    put("language", language)
    put("publishedDate", publishedDate?.toString() ?: "")
    put("publishYear", publishYear)
    put("edition", edition ?: "")
    put("series", series)
    put("seriesNumber", seriesNumber)
    put("seriesMemberships", JsonArray(seriesMemberships.map { Json.encodeToJsonElement(SeriesMembership.serializer(), it) }))
    put("genres", genres.toJsonArray())
    put("imageUrl", imageUrl)
    put("tags", JsonArray(tags.map { JsonPrimitive(it) }))
    put("abridged", abridged)
    put("explicit", explicit)
    put("runtime", runtime)
    put("basePath", basePath)
    put("qualityProfileId", if (qualityProfileId > 0) qualityProfileId else -1)
}
