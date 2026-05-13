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
        is ArrSeries -> if (item.status == MediaStatus.Continuing) item.nextAiring?.format()?.let {
            "${mokoString(MR.strings.airing_next)} $it"
        } ?: mokoString(MR.strings.continuing_unknown) else null
        is ArrMovie -> item.inCinemas?.format()?.takeUnless {
            item.digitalRelease != null || item.physicalRelease != null
        }?.let { "${mokoString(MR.strings.in_cinemas)} $it" }
        is Arrtist -> if (item.status == MediaStatus.Continuing) item.nextAlbum?.releaseDate?.format()?.let {
            "${mokoString(MR.strings.next_album)} $it"
        } ?: mokoString(MR.strings.continuing_unknown) else null
        is Author -> if (item.status == MediaStatus.Continuing) item.nextBook?.releaseDate?.format()?.let {
            "${mokoString(MR.strings.next_book)} $it"
        } ?: mokoString(MR.strings.continuing_unknown) else null
        is Audiobook -> item.publishedDate?.ifTodayOrAfter()?.format()?.let {
            "${mokoString(MR.strings.release_date)} $it"
        }
        is SearchAudiobook -> item.releaseDate?.ifTodayOrAfter()?.format("MMM d, yyyy")
        is MockMedia -> "Next Airing: Monday"
    }?.let { airingString ->
        Text(
            text = airingString,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}