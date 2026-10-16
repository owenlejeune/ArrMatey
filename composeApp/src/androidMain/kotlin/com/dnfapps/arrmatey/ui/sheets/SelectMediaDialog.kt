package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.cancel
import com.dnfapps.arrmatey.shared.no_results_found
import com.dnfapps.arrmatey.shared.search_artists
import com.dnfapps.arrmatey.shared.search_books
import com.dnfapps.arrmatey.shared.search_media
import com.dnfapps.arrmatey.shared.search_movies
import com.dnfapps.arrmatey.shared.search_series
import com.dnfapps.arrmatey.shared.select_artist
import com.dnfapps.arrmatey.shared.select_book
import com.dnfapps.arrmatey.shared.select_media
import com.dnfapps.arrmatey.shared.select_movie
import com.dnfapps.arrmatey.shared.select_series
import com.dnfapps.arrmatey.shared.unknown
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun SelectMediaDialog(
    instance: Instance?,
    currentSelectionId: Long?,
    onSelect: (ArrMedia) -> Unit,
    onDismiss: () -> Unit,
    viewModel: ActivityQueueViewModel,
) {
    var searchQuery by remember { mutableStateOf("") }
    var mediaList by remember { mutableStateOf<List<ArrMedia>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val titleText = when (instance?.type) {
        InstanceType.Sonarr -> mokoString(MR.strings.select_series)
        InstanceType.Radarr -> mokoString(MR.strings.select_movie)
        InstanceType.Lidarr -> mokoString(MR.strings.select_artist)
        InstanceType.Bookshelf, InstanceType.Chaptarr -> mokoString(MR.strings.select_book)
        else -> mokoString(MR.strings.select_media)
    }

    val searchPlaceholder = when (instance?.type) {
        InstanceType.Sonarr -> mokoString(MR.strings.search_series)
        InstanceType.Radarr -> mokoString(MR.strings.search_movies)
        InstanceType.Lidarr -> mokoString(MR.strings.search_artists)
        InstanceType.Bookshelf, InstanceType.Chaptarr -> mokoString(MR.strings.search_books)
        else -> mokoString(MR.strings.search_media)
    }

    LaunchedEffect(instance?.id) {
        val instanceId = instance?.id
        if (instanceId == null) {
            isLoading = false
            return@LaunchedEffect
        }
        viewModel.getMediaForInstance(
            instanceId = instanceId,
            onSuccess = { list ->
                mediaList = list.sortedBy { it.title?.lowercase() ?: "" }
                isLoading = false
            },
            onError = { err ->
                errorMessage = err
                isLoading = false
            },
        )
    }

    val filteredList = remember(mediaList, searchQuery) {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            mediaList
        } else {
            mediaList.filter {
                (it.title?.lowercase()?.contains(query) == true) ||
                    (it.cleanTitle?.lowercase()?.contains(query) == true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(min = 280.dp, max = 560.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            ) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(searchPlaceholder) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = mokoString(MR.strings.no_results_found),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(filteredList, key = { it.guid }) { media ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(media) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                            ) {
                                Text(
                                    text = media.title ?: mokoString(MR.strings.unknown),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                val subtitle = listOfNotNull(
                                    media.releasedBy?.takeIf { it.isNotBlank() },
                                    media.year?.takeIf { it > 0 }?.toString(),
                                ).joinToString(" • ")
                                if (subtitle.isNotEmpty()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(mokoString(MR.strings.cancel))
                    }
                }
            }
        }
    }
}
