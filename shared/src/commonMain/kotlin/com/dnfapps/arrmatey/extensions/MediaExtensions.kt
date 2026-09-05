package com.dnfapps.arrmatey.extensions

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.is24Hour
import kotlinx.datetime.LocalDate

fun ArrMedia.getUpcomingDateString(
    seerrNextAirDate: LocalDate? = null,
    mokoStrings: MokoStrings = MokoStrings(),
): String? =
    when (this) {
        is ArrSeries ->
            if (status == MediaStatus.Continuing) {
                val formattedNextAiring =
                    nextAiring?.format(
                        mokoStrings.getString(MR.strings.airing_next_format),
                    )
                if (formattedNextAiring != null) {
                    mokoStrings.getString(MR.strings.airing_next, listOf(formattedNextAiring))
                } else if (seerrNextAirDate != null) {
                    val formattedDate = seerrNextAirDate.format("MMMM d, yyyy")
                    val formattedTime = formatAirTime(airTime)
                    val combined = if (formattedTime != null) "$formattedDate at $formattedTime" else formattedDate
                    mokoStrings.getString(MR.strings.airing_next, listOf(combined))
                } else {
                    null
                }
            } else {
                null
            }
        is ArrMovie ->
            when {
                digitalRelease?.isTodayOrAfter() == true ->
                    mokoStrings.getString(MR.strings.digital_release_on, listOf(digitalRelease.format("MMMM d, yyyy")))
                physicalRelease?.isTodayOrAfter() == true ->
                    mokoStrings.getString(MR.strings.physical_release_on, listOf(physicalRelease.format("MMMM d, yyyy")))
                inCinemas?.isTodayOrAfter() == true ->
                    mokoStrings.getString(MR.strings.in_cinemas_on, listOf(inCinemas.format("MMMM d, yyyy")))
                else -> null
            }
        is Arrtist ->
            if (status == MediaStatus.Continuing) {
                nextAlbum?.releaseDate?.format("MMMM d, yyyy")?.let {
                    "${mokoStrings.getString(MR.strings.next_album)} $it"
                }
            } else {
                null
            }
        is Author ->
            if (status == MediaStatus.Continuing) {
                nextBook?.releaseDate?.format("MMMM d, yyyy")?.let {
                    "${mokoStrings.getString(MR.strings.next_book)} $it"
                }
            } else {
                null
            }
        is Audiobook ->
            publishedDate?.ifTodayOrAfter()?.format("MMMM d, yyyy")?.let {
                "${mokoStrings.getString(MR.strings.release_date)} $it"
            }
        is SearchAudiobook -> releaseDate?.ifTodayOrAfter()?.format("MMMM d, yyyy")
        is MockMedia -> "Next Airing: Monday"
    }

fun List<ArrMedia>.mergeWithLibrary(library: List<ArrMedia>): List<ArrMedia> =
    this.map { item ->
        val match =
            when (item) {
                is ArrSeries ->
                    library.filterIsInstance<ArrSeries>().firstOrNull {
                        (it.tvdbId != 0L && item.tvdbId != 0L && it.tvdbId == item.tvdbId) ||
                            (it.tmdbId != null && it.tmdbId != 0L && item.tmdbId != null && item.tmdbId != 0L && it.tmdbId == item.tmdbId)
                    }
                is ArrMovie ->
                    library.filterIsInstance<ArrMovie>().firstOrNull {
                        it.tmdbId != 0L && item.tmdbId != 0L && it.tmdbId == item.tmdbId
                    }
                is Arrtist ->
                    library.filterIsInstance<Arrtist>().firstOrNull {
                        (!it.mbId.isNullOrBlank() && !item.mbId.isNullOrBlank() && it.mbId == item.mbId) ||
                            (it.tadbId != 0L && item.tadbId != 0L && it.tadbId == item.tadbId)
                    }

                is Audiobook ->
                    library.filterIsInstance<Audiobook>().firstOrNull {
                        !it.asin.isNullOrBlank() && !item.asin.isNullOrBlank() && it.asin == item.asin
                    }
                is Author ->
                    library.filterIsInstance<Author>().firstOrNull {
                        !it.title.isNullOrBlank() && !item.title.isNullOrBlank() && it.title == item.title
                    }
                else -> null
            }
        match ?: item
    }

internal fun formatAirTime(airTime: String?): String? {
    if (airTime.isNullOrBlank()) return null
    val parts = airTime.split(":")
    if (parts.size < 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    return if (is24Hour()) {
        val padHour = hour.toString().padStart(2, '0')
        val padMinute = minute.toString().padStart(2, '0')
        "$padHour:$padMinute"
    } else {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour =
            when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
        val padMinute = minute.toString().padStart(2, '0')
        "$displayHour:$padMinute $amPm"
    }
}
