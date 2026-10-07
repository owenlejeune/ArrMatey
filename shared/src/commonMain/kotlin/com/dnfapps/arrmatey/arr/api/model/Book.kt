package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.arr.api.client.HasArrImages
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
enum class BookMediaType(
    val resource: StringResource,
) {
    @SerialName("ebook")
    EBook(MR.strings.ebook),

    @SerialName("audiobook")
    Audiobook(MR.strings.type_audiobook),

    @SerialName("both")
    Both(MR.strings.both),
    ;

    companion object {
        fun fromString(value: String?): BookMediaType? = entries.firstOrNull { it.name.equals(value, ignoreCase = true) || (it == EBook && value.equals("ebook", ignoreCase = true)) }
    }
}

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
    override val images: List<ArrImage> = emptyList(),
    val links: List<ArrLink> = emptyList(),
    val statistics: BookshelfStatistics? = null,
    @Contextual val added: Instant? = null,
    @Contextual val lastSearchTime: Instant? = null,
    val grabbed: Boolean = false,
    val author: Author? = null,
    override val instanceId: Long? = null,
    override val instanceIds: List<Long> = listOfNotNull(instanceId),
    val mediaType: BookMediaType? = null,
    val audiobookMonitored: Boolean? = null,
    val ebookMonitored: Boolean? = null,
    val narratorNames: List<String> = emptyList(),
    val availableNarrators: List<String> = emptyList(),
    val hasFiles: Boolean = false,
    val isOmnibus: Boolean = false,
) : CalendarItem,
    InstanceTypeIdentifiable,
    HasArrImages<Book> {
    override fun withLocalImages(instance: Instance): Book = copy(
        images = images.map { it.rebuildWithLocalUrls(instance) },
        author = author?.withLocalImages(instance),
    )

    override val instanceType: InstanceType
        get() = InstanceType.Bookshelf

    override val calendarId: Long
        get() = id

    override fun getCalendarDates(): List<Instant> = listOfNotNull(releaseDate)

    override val notificationScheduledTime: Instant?
        get() = releaseDate

    override val notificationMessage: String
        get() = "${authorTitle ?: "Unknown Author"} - $title"

    companion object {
        fun fromJson(value: String): Book = ArrMedia.json.decodeFromString(value)
    }

    fun toJson(): String = ArrMedia.json.encodeToString(this)

    fun getCover() = images.firstOrNull {
        it.coverType == CoverType.Cover
    }

    val isDownloaded: Boolean
        get() = statistics?.percentOfBooks?.equals(100f) ?: false

    val isPartiallyDownloaded: Boolean
        get() = (statistics?.percentOfBooks ?: 0f) > 0f
}
