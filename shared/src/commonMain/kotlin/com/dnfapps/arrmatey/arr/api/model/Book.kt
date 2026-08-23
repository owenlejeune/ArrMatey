package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Book(
    val id: Long,
    val title: String,
    val authorTitle: String? = null,
    val seriesTitle: String? = null,
    val disambiguation: String? = null,
    val authorId: Long? = null,
    val foreignBookId: String? = null,
    val foreignEditionId: String? = null,
    val titleSlug: String? = null,
    val monitored: Boolean = false,
    val anyEditionOk: Boolean = true,
    val ratings: BookshelfRatings? = null,
    @Contextual val releaseDate: Instant? = null,
    val pageCount: Int? = null,
    val genres: List<String> = emptyList(),
    val images: List<ArrImage> = emptyList(),
    val links: List<ArrLink> = emptyList(),
    val statistics: BookshelfStatistics? = null,
    @Contextual val added: Instant? = null,
    @Contextual val lastSearchTime: Instant? = null,
    val grabbed: Boolean = false,

    override val instanceId: Long? = null
): CalendarItem {

    override val calendarId: Long
        get() = id

    override fun getCalendarDates(): List<Instant> =
        listOfNotNull(releaseDate)

    override val notificationScheduledTime: Instant?
        get() = releaseDate

    override val notificationMessage: String
        get() = "${authorTitle ?: "Unknown Author"} - $title"

    companion object {
        fun fromJson(value: String): Book {
            return ArrMedia.json.decodeFromString(value)
        }
    }

    fun toJson(): String {
        return ArrMedia.json.encodeToString(this)
    }

    fun getCover() = images.firstOrNull {
        it.coverType == CoverType.Cover
    }

    val isDownloaded: Boolean
        get() = statistics?.percentOfBooks?.equals(100f) ?: false

    val isPartiallyDownloaded: Boolean
        get() = (statistics?.percentOfBooks ?: 0f) > 0f
}