package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ArrAlbum(
    val id: Long,
    val title: String? = null,
    val overview: String? = null,
    val monitored: Boolean = false,
    val albumType: String? = null,
    @Contextual val releaseDate: Instant? = null,
    val genres: List<String> = emptyList(),
    val statistics: AlbumStatistics? = null,
    val images: List<ArrImage> = emptyList(),
    val artist: Arrtist? = null,
    val artistId: Long,
    val foreignAlbumId: String,
    val anyReleaseOk: Boolean,
    val profileId: Int,
    val duration: Long,
    val ratings: LidarrRatings? = null,
    val releases: List<AlbumRelease> = emptyList(),

    override val instanceId: Long? = null
): CalendarItem, InstanceTypeIdentifiable {

    override val calendarId: Long
        get() = id

    override fun getCalendarDates(): List<Instant> =
        listOfNotNull(releaseDate)

    override val notificationScheduledTime: Instant?
        get() = releaseDate

    override val notificationMessage: String
        get() = "${artist?.title ?: "Unknown Artist"} - ${title ?: "Unknown Album"}"

    fun getCover() = images.firstOrNull {
        it.coverType == CoverType.Cover
    }

    val downloadedTrackCount: Int
        get() = statistics?.trackFileCount ?: 0

    val isDownloaded: Boolean
        get() = statistics?.percentOfTracks?.equals(100.toDouble()) ?: false

    val isPartiallyDownloaded: Boolean
        get() = (statistics?.percentOfTracks ?: 0.toDouble()) > 0.toDouble()
}