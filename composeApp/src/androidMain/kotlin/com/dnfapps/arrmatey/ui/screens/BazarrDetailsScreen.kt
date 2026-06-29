package com.dnfapps.arrmatey.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import com.dnfapps.arrmatey.bazarr.api.model.BazarrEpisode
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMediaType
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMovie
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSeries
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSubtitle
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSubtitleLanguage
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.bazarr.viewmodel.BazarrDetailsViewModel
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.model.toInfoList
import com.dnfapps.arrmatey.navigation.BazarrScreen
import com.dnfapps.arrmatey.navigation.LocalBazarrNavigator
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.BasePosterItem
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DetailHeaderBanner
import com.dnfapps.arrmatey.ui.components.InfoArea
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.bazarr.BazarrSubtitleSearchSheet
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun BazarrDetailsScreen(
    id: Long,
    type: BazarrMediaType,
    viewModel: BazarrDetailsViewModel = koinInjectParams(id, type),
    navigator: Navigator<BazarrScreen> = LocalBazarrNavigator.current
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var searchTarget by remember { mutableStateOf<BazarrMediaTarget?>(null) }

    val searchQueuedMessage = mokoString(MR.strings.search_queued)
    val searchErrorMessage = mokoString(MR.strings.search_error)
    LaunchedEffect(operationState) {
        when (operationState) {
            is OperationStatus.Success -> {
                Toast.makeText(context, searchQueuedMessage, Toast.LENGTH_SHORT).show()
                viewModel.clearOperation()
            }
            is OperationStatus.Error -> {
                Toast.makeText(context, searchErrorMessage, Toast.LENGTH_SHORT).show()
                viewModel.clearOperation()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            OverlayTopAppBar(
                scrollState = scrollState,
                navigationIcon = {
                    IconButton(
                        onClick = { navigator.popBackStack() },
                        colors = IconButtonDefaults.headerBarColors()
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (type == BazarrMediaType.Movie && uiState.details?.serviceId != null) {
                        IconButton(
                            onClick = {
                                searchTarget = BazarrMediaTarget.Movie(uiState.details!!.serviceId)
                            },
                            colors = IconButtonDefaults.headerBarColors()
                        ) {
                            Icon(Icons.Default.Person, null)
                        }
                    }
                    IconButton(
                        onClick = {
                            viewModel.performSearch()
                        },
                        colors = IconButtonDefaults.headerBarColors(),
                        enabled = operationState !is OperationStatus.InProgress
                    ) {
                        if (operationState is OperationStatus.InProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Search, null)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            BazarrDetailsHeader(
                poster = uiState.details?.poster,
                fanart = uiState.details?.fanart,
                topPadding = paddingValues.calculateTopPadding()
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = uiState.details?.title ?: "",
                    style = MaterialTheme.typography.headlineMedium
                )

                uiState.details?.overview?.let { overview ->
                    ItemDescriptionCard(overview)
                }

                when (val details = uiState.details) {
                    is BazarrMovie -> {
                        SubtitlesSection(details.subtitles, details.missingSubtitles, onSearch = {
                            searchTarget = BazarrMediaTarget.Movie(details.serviceId)
                        })
                    }
                    is BazarrSeries -> {
                        BazarrEpisodesSection(uiState.episodes, onSearch = { seriesId, epId ->
                            searchTarget = BazarrMediaTarget.Episode(seriesId, epId)
                        })
                    }
                    null -> {}
                }

                val infoItems = when (val details = uiState.details) {
                    is BazarrMovie -> buildMap {
                        put(mokoString(MR.strings.path), details.path)
                        put(mokoString(MR.strings.language), details.audioLanguage.joinToString { lang -> lang.name })
                    }

                    is BazarrSeries -> buildMap {
                        put(mokoString(MR.strings.path), details.path)
                        put(mokoString(MR.strings.files), mokoString(MR.strings.bazarr_files_count, details.episodeFileCount))
                        put(mokoString(MR.strings.missing), mokoString(MR.strings.bazarr_missing_count, details.episodeMissingCount))
                        put(mokoString(MR.strings.status), mokoString(if (details.ended) MR.strings.ended else MR.strings.continuing))
                        details.lastAired?.let { lastAired ->
                            put(mokoString(MR.strings.previous_airing), lastAired)
                        }
                        put(mokoString(MR.strings.series_type), details.seriesType)
                    }

                    else -> mapOf()
                }.toInfoList()
                InfoArea(infoItems)
            }
        }

        searchTarget?.let { target ->
            BazarrSubtitleSearchSheet(
                target = target,
                onDismiss = { searchTarget = null }
            )
        }
    }
}

@Composable
private fun BazarrDetailsHeader(
    poster: String?,
    fanart: String?,
    topPadding: Dp
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        DetailHeaderBanner(
            bannerUrl = fanart ?: poster,
            gradientHeight = 150.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topPadding)
                .padding(horizontal = 12.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            BasePosterItem(
                model = rememberRemoteImageData(poster),
                modifier = Modifier.width(150.dp),
                aspectRatio = AspectRatio.Poster
            )
        }
    }
}

@Composable
private fun SubtitlesSection(
    subtitles: List<BazarrSubtitle>,
    missingSubtitles: List<BazarrSubtitleLanguage>,
    onSearch: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = mokoString(MR.strings.subtitle),
            style = MaterialTheme.typography.titleLarge
        )
        subtitles.forEach { subtitle ->
            SubtitleItem(subtitle)
        }

        missingSubtitles.forEach { missing ->
            MissingSubtitleItem(missing, onSearch)
        }
    }
}

@Composable
private fun MissingSubtitleItem(
    subtitle: BazarrSubtitleLanguage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mokoString(MR.strings.missing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LanguageTag(text = subtitle.name.uppercase(), containerColor = Color(0xFF4A2C5E))

                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SubtitleItem(subtitle: BazarrSubtitle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subtitle.path ?: mokoString(MR.strings.unknown),
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = buildString {
                        append(subtitle.name.uppercase())
                        if (subtitle.hi) append(" HI")
                    },
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BazarrEpisodesSection(
    episodes: List<BazarrEpisode>,
    onSearch: (Long, Long) -> Unit
) {
    val seasons = episodes.groupBy { it.season }.toSortedMap(compareByDescending { it })

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = mokoString(MR.strings.seasons_header),
            style = MaterialTheme.typography.titleLarge
        )

        seasons.forEach { (seasonNumber, seasonEpisodes) ->
            var expanded by rememberSaveable { mutableStateOf(false) }
            val iconRotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "iconRotation"
            )

            Column {
                ContainerCard(
                    modifier = Modifier.clickable { expanded = !expanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (seasonNumber == 0) {
                                mokoString(MR.strings.specials)
                            } else {
                                mokoString(MR.strings.season_label, seasonNumber)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Icon(
                            imageVector = Icons.Default.ExpandCircleDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(iconRotation)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        seasonEpisodes.sortedBy { it.episode }.forEachIndexed { index, episode ->
                            BazarrEpisodeItem(episode, onClick = {
                                onSearch(episode.sonarrSeriesId, episode.sonarrEpisodeId)
                            })
                            if (index < seasonEpisodes.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BazarrEpisodeItem(
    episode: BazarrEpisode,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val titleString = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 16.sp)) {
                    withStyle(SpanStyle(color = primaryColor)) {
                        append("${episode.episode}. ")
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                        append(episode.title)
                    }
                }

                withStyle(SpanStyle(
                    color = MaterialTheme.colorScheme.error,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp
                )) {
                    append(" ")
                    append(mokoString(MR.strings.missing))
                }
            }
            Text(
                text = titleString,
                lineHeight = 1.5.em,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            FlowRow(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                episode.audioLanguages.forEach { lang ->
                    LanguageTag(text = lang.name.uppercase(), containerColor = Color(0xFF4A2C5E))
                }

                episode.subtitles
                    .distinctBy { it.code2 to it.hi }
                    .forEach { sub ->
                    LanguageTag(
                        text = buildString {
                            append(sub.code2.uppercase())
                            if (sub.hi) append(":HI")
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }


    }
}

@Composable
private fun LanguageTag(
    text: String,
    containerColor: Color,
    contentColor: Color = Color.White
) {
    Box(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}
