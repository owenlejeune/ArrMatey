package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.LidarrTrack
import com.dnfapps.arrmatey.arr.api.model.LidarrTrackFile
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun AlbumsArea(
    artist: Arrtist,
    albums: List<ArrAlbum>,
    tracks: Map<Long, List<LidarrTrack>>,
    trackFiles: Map<Long, List<LidarrTrackFile>>,
    searchIds: Set<Long>,
    onToggleAlbumMonitor: (ArrAlbum) -> Unit,
    onEditAlbum: (ArrAlbum) -> Unit,
    onAlbumAutomaticSearch: (Long) -> Unit,
    deleteAlbumFiles: (Long) -> Unit,
    albumDeleteInProgress: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = mokoString(MR.strings.albums_header),
            style = MaterialTheme.typography.titleLarge
        )
        albums.forEach { album ->
            var expanded by rememberSaveable { mutableStateOf(false) }
            val iconRotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "iconRotation"
            )
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                ContainerCard(
                    modifier = Modifier.clickable { expanded = !expanded }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AlbumCover(item = album, modifier = Modifier.size(60.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = buildString {
                                    append(album.title)
                                    album.releaseDate?.format("YYYY")?.let { year ->
                                        append(" ($year)")
                                    }
                                },
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.MiddleEllipsis
                            )
                            album.statistics?.let { statistics ->
                                Text(
                                    text = "${statistics.trackFileCount}/${statistics.totalTrackCount}",
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.ExpandCircleDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(iconRotation)
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = mokoString(MR.strings.edit),
                            modifier = Modifier.clickable {
                                onEditAlbum(album)
                            }
                        )
                        Icon(
                            imageVector = if (album.monitored) Icons.Default.Bookmark
                                          else Icons.Default.BookmarkBorder,
                            contentDescription = if (album.monitored) {
                                mokoString(MR.strings.monitored)
                            } else {
                                mokoString(MR.strings.unmonitored)
                            },
                            modifier = Modifier.clickable {
                                onToggleAlbumMonitor(album)
                            }
                        )
                    }
                }
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        val albumTracks = (tracks[album.id] ?: emptyList())
                            .sortedBy { it.absoluteTrackNumber }
                        val albumTrackFiles = (trackFiles[album.id] ?: emptyList())

                        Spacer(modifier = Modifier.height(6.dp))
                        AlbumHeader(
                            artistId = artist.id,
                            album = album,
                            tracks = albumTracks,
                            onPerformAutomaticSearch = onAlbumAutomaticSearch,
                            searchInProgress = { searchIds.contains(it) },
                            onDeleteAlbum = {
                                deleteAlbumFiles(album.id)
                            },
                            deleteInProgress = albumDeleteInProgress
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        albumTracks.forEachIndexed { index, track ->
                            TrackRow(
                                track = track,
                                trackFile = if (track.hasFile) {
                                    albumTrackFiles.firstOrNull { file ->
                                        track.trackFileId?.let { it == file.id } ?: false
                                    }
                                } else {
                                    null
                                }
                            )
                            if (index < albumTracks.size-1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}