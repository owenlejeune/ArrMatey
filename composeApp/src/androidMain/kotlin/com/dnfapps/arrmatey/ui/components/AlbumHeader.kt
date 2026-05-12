package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.LidarrTrack
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.extensions.formatMinutesAsRuntime
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun AlbumHeader(
    artistId: Long?,
    album: ArrAlbum,
    tracks: List<LidarrTrack>,
    onPerformAutomaticSearch: (Long) -> Unit,
    searchInProgress: (Long) -> Boolean,
    onDeleteAlbum: () -> Unit,
    deleteInProgress: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<ArrScreen> = navigationManager.music()
) {
    val release = album.releaseDate?.format("MMM d, yyyy")
        ?: mokoString(MR.strings.tba)

    val runtime = remember(tracks) {
        tracks.sumOf { it.duration }.div(60_000).formatMinutesAsRuntime()
    }

    val albumInfo = listOfNotNull(
        release, runtime, album.statistics?.sizeOnDisk?.bytesAsFileSizeString()
    )
    val infoString = albumInfo.joinToString(Bullet)
    Text(
        text = infoString,
        fontSize = 16.sp
    )
    ReleaseDownloadButtons(
        onInteractiveClicked = {
            artistId?.let { artistId
                val destination = ArrScreen.AlbumRelease(
                    artistId = artistId,
                    albumId = album.id
                )
                navigation.navigateTo(destination)
            }
        },
        onAutomaticClicked = {
            onPerformAutomaticSearch(album.id)
        },
        automaticSearchInProgress = searchInProgress(album.id),
        modifier = Modifier.fillMaxWidth(),
        automaticSearchEnabled = album.monitored,
        deleteInProgress = deleteInProgress,
        onDelete = onDeleteAlbum,
    )
}