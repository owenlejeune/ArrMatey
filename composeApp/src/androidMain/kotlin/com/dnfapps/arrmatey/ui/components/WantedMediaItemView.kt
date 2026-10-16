package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.WantedMediaItem
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.PosterRadius
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun WantedMediaItemView(
    item: WantedMediaItem,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier.matchParentSize(),
            ) {
                Box(
                    modifier =
                    Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(item.instanceType?.associatedColor ?: MaterialTheme.colorScheme.primary)
                        .align(Alignment.CenterStart),
                )
            }

            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                val media = item.media
                val album = item.album
                val book = item.book

                if (media != null) {
                    PosterItem(
                        item = media,
                        aspectRatio = AspectRatio.Poster,
                        radius = PosterRadius.Small,
                        modifier = Modifier.width(65.dp),
                    )
                } else if (album != null) {
                    AlbumCover(
                        item = album,
                        modifier = Modifier.width(65.dp).height(95.dp),
                    )
                } else if (book != null) {
                    BasePosterItem(
                        model = rememberRemoteImageData(book.getCover()?.remoteUrl),
                        modifier = Modifier.width(65.dp),
                    )
                } else {
                    Surface(
                        modifier = Modifier.width(65.dp).height(95.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ) {}
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = (item.title ?: "").breakable(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (!item.instanceName.isNullOrBlank()) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.padding(start = 6.dp),
                            ) {
                                Text(
                                    text = item.instanceName ?: mokoString(MR.strings.unknown),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }

                    item.year?.let { year ->
                        Text(
                            text = year.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    val overview = item.media?.overview ?: item.album?.overview ?: item.book?.authorTitle
                    overview?.takeIf { it.isNotBlank() }?.let { text ->
                        Text(
                            text = text.breakable(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
