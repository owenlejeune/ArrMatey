package com.dnfapps.arrmatey.ui.components

import com.dnfapps.arrmatey.shared.MR
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.extensions.ifTodayOrAfter
import com.dnfapps.arrmatey.extensions.isTodayOrAfter
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun UpcomingDateView(item: ArrMedia) {
    when (item) {
        is ArrSeries -> if (item.status == MediaStatus.Continuing) item.nextAiring?.format(
            mokoString(MR.strings.airing_next_format)
        )?.let { mokoString(MR.strings.airing_next, it) } else null
        is ArrMovie -> when {
            item.digitalRelease?.isTodayOrAfter() == true ->
                mokoString(MR.strings.digital_release_on, item.digitalRelease?.format("MMMM d, yyyy") ?: "")
            item.physicalRelease?.isTodayOrAfter() == true ->
                mokoString(MR.strings.physical_release_on, item.physicalRelease?.format("MMMM d, yyyy") ?: "")
            item.inCinemas?.isTodayOrAfter()  == true ->
                mokoString(MR.strings.in_cinemas_on, item.inCinemas?.format("MMMM d, yyyy") ?: "")
            else -> null
        }
        is Arrtist -> if (item.status == MediaStatus.Continuing) item.nextAlbum?.releaseDate?.format("MMMM d, yyyy")?.let {
            "${mokoString(MR.strings.next_album)} $it"
        } else null
        is Author -> if (item.status == MediaStatus.Continuing) item.nextBook?.releaseDate?.format("MMMM d, yyyy")?.let {
            "${mokoString(MR.strings.next_book)} $it"
        } else null
        is Audiobook -> item.publishedDate?.ifTodayOrAfter()?.format("MMMM d, yyyy")?.let {
            "${mokoString(MR.strings.release_date)} $it"
        }
        is SearchAudiobook -> item.releaseDate?.ifTodayOrAfter()?.format("MMMM d, yyyy")
        is MockMedia -> "Next Airing: Monday"
    }?.let { airingString ->
        Text(
            text = airingString,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}