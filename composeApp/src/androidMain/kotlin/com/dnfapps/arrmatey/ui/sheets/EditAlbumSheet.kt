package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.AlbumRelease
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlbumSheet(
    album: ArrAlbum,
    editInProgress: Boolean,
    onEditAlbum: (ArrAlbum) -> Unit,
    onDismiss: () -> Unit
) {
    var monitored by remember { mutableStateOf(album.monitored) }
    var anyReleaseOk by remember { mutableStateOf(album.anyReleaseOk) }
    var selectedRelease by remember { 
        mutableStateOf(album.releases.firstOrNull { it.monitored } ?: album.releases.firstOrNull()) 
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.monitored),
                checked = monitored,
                onCheckedChange = { monitored = it }
            )

            LabelledSwitch(
                label = mokoString(MR.strings.automatically_switch_release),
                sublabel = mokoString(MR.strings.automatically_switch_releass_description),
                checked = anyReleaseOk,
                onCheckedChange = { anyReleaseOk = it }
            )

            if (album.releases.isNotEmpty()) {
                DropdownPicker(
                    options = album.releases,
                    modifier = Modifier.fillMaxWidth(),
                    selectedOption = selectedRelease!!,
                    onOptionSelected = { selectedRelease = it },
                    getOptionLabel = { release ->
                        buildString {
                            append(release.title ?: "")
                            append(", ${release.mediumCount} ")
                            append(mokoString(MR.strings.mediums_short))
                            append(", ${release.trackCount} ")
                            append(mokoString(MR.strings.tracks_lowercase))
                            if (release.country.isNotEmpty()) {
                                append(", ${release.country.joinToString(", ")}")
                            }
                            release.format?.let { append(", [$it]") }
                        }
                    },
                    label = { Text(mokoString(MR.strings.releases)) },
                    singleLine = false
                )
            }

            Button(
                onClick = {
                    val updatedReleases = album.releases.map { release ->
                        release.copy(monitored = release.id == selectedRelease?.id)
                    }
                    val updatedAlbum = album.copy(
                        monitored = monitored,
                        anyReleaseOk = anyReleaseOk,
                        releases = updatedReleases,
                        duration = selectedRelease?.duration ?: album.duration,
                        profileId = album.profileId,
                        statistics = album.statistics?.let { stats ->
                            stats.copy(
                                trackCount = selectedRelease?.trackCount ?: stats.trackCount,
                                totalTrackCount = selectedRelease?.trackCount ?: stats.totalTrackCount
                            )
                        }
                    )
                    onEditAlbum(updatedAlbum)
                },
                enabled = !editInProgress && (selectedRelease != null || album.releases.isEmpty())
            ) {
                if (editInProgress) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null
                    )
                    Text(
                        text = mokoString(MR.strings.save)
                    )
                }
            }
        }
    }
}
