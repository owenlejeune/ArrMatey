package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.extensions.formatMinutesAsRuntime
import com.dnfapps.arrmatey.extensions.isBeforeToday
import com.dnfapps.arrmatey.extensions.padStart
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.formatLocalDateTime
import dev.icerock.moko.resources.StringResource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Episode(
    val id: Long,
    val seriesId: Long,
    val tvdbId: Long?,
    val episodeFileId: Long?,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String? = null,
    val airDate: LocalDate? = null,
    @Contextual val airDateUtc: Instant? = null,
    @Contextual val lastSearchTime: Instant? = null,
    val runtime: Int?,
    val finaleType: FinaleType? = null,
    val overview: String? = null,
    val episodeFile: EpisodeFile? = null,
    val hasFile: Boolean,
    val monitored: Boolean,
    val absoluteEpisodeNumber: Int? = null,
    val sceneAbsoluteEpisodeNumber: Int? = null,
    val sceneEpisodeNumber: Int? = null,
    val sceneSeasonNumber: Int? = null,
    val unverifiedSceneNumbering: Boolean,
    val endTime: String? = null,
    val grabDate: String? = null,
    val images: List<ArrImage> = emptyList(),

    val series: ArrSeries? = null,
    override var instanceId: Long? = null
): CalendarItem, InstanceTypeIdentifiable {
    override val calendarId: Long 
        get() = tvdbId ?: id
    override fun getCalendarDates(): List<Instant> = 
        listOfNotNull(airDateUtc)
    override val notificationScheduledTime: Instant? 
        get() = airDateUtc
    override val notificationMessage: String 
        get() = "${series?.title ?: "Unknown Series"} - S${seasonNumber}E${episodeNumber}${airDateUtc?.let { " - ${it.format("HH:mm")}" } ?: ""}"

    val displayTitle: String
        get() = title ?: "Unknown"

    val runtimeString: String?
        get() = runtime?.formatMinutesAsRuntime()

    val seasonEpLabel: String
        get() = "${seasonNumber}x${episodeNumber.padStart(2, '0')}"

    val fileQualityName: String?
        get() = episodeFile?.qualityName

    val hasAired: Boolean
        get() = airDate?.isBeforeToday() ?: false

    fun formatAirDateUtc(
        friendlyTodayFormat: Boolean = true,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String? {
        if (airDateUtc == null) return null
        val localDateTime = airDateUtc.toLocalDateTime(timeZone)
        val localDate = localDateTime.date
        val today = Clock.System.todayIn(timeZone)

        return if (localDate == today && friendlyTodayFormat) {
            val timeString = formatLocalDateTime(localDateTime, "HH:mm", timeZone)
            "Today at $timeString"
        } else {
            formatLocalDateTime(localDateTime, "MMM d, yyyy", timeZone)
        }
    }

    fun formatAirDateUtc() = formatAirDateUtc(true, TimeZone.currentSystemDefault())

    val episodeLabel: String
        get() = "s${seasonNumber.padStart(2, '0')}e${episodeNumber.padStart(2, '0')}"

    fun getPoster(): ArrImage?  {
        return images.firstOrNull { it.coverType == CoverType.Poster }
            ?: images.firstOrNull { it.coverType == CoverType.Screenshot }
    }
    fun getBanner(): ArrImage? {
        return images.firstOrNull { it.coverType == CoverType.FanArt }
            ?: images.firstOrNull { it.coverType == CoverType.Banner }
            ?: images.firstOrNull { it.coverType == CoverType.Poster }
            ?: images.firstOrNull { it.coverType == CoverType.Screenshot }
    }

    val statusLabel: String
        get() = when {
            hasFile -> "Downloaded"
            airDate?.isBeforeToday() == true -> "Unaired"
            else -> "Missing"
        }

    fun toJson(): String = ArrMedia.json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = ArrMedia.json.decodeFromString<Episode>(json)
    }
}

enum class FinaleType(val resource: StringResource) {
    @SerialName("series")
    Series(MR.strings.series_finale),

    @SerialName("season")
    Season(MR.strings.season_finale),

    @SerialName("midseason")
    Midseason(MR.strings.midseason_finale)
}
