package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMediaType
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMovie
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSeries
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSubtitleLanguage
import com.dnfapps.arrmatey.bazarr.api.model.ProviderStatus
import com.dnfapps.arrmatey.bazarr.api.model.WantedEpisode
import com.dnfapps.arrmatey.bazarr.api.model.WantedMovie
import com.dnfapps.arrmatey.bazarr.state.BazarrLibrary
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.bazarr.state.BazarrSection
import com.dnfapps.arrmatey.bazarr.viewmodel.BazarrViewModel
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.LocalBazarrNavigator
import com.dnfapps.arrmatey.navigation.openDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.BannerView
import com.dnfapps.arrmatey.ui.components.BasePosterItem
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.components.bazarr.BazarrSubtitleSearchSheet
import com.dnfapps.arrmatey.ui.components.bazarr.SubtitleLanguageChip
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.TranslucentBlack
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BazarrScreen(
    wideRailIsVisible: Boolean,
    viewModel: BazarrViewModel = koinInject(),
    instancesViewModel: InstancesViewModel = koinInjectParams(InstanceType.Bazarr)
) {
    val navigator = LocalBazarrNavigator.current

    val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()
    val section by viewModel.selectedSection.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var searchTarget by remember { mutableStateOf<BazarrMediaTarget?>(null) }

    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState.text) {
        viewModel.updateSearchQuery(textFieldState.text.toString())
    }

    LaunchedEffect(section) {
        textFieldState.clearText()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ArrAppBarWithSearch(
                textFieldState = textFieldState,
                textFieldEnabled = instancesState.selectedInstance != null && section != BazarrSection.Providers,
                searchPlaceholder = mokoString(MR.strings.search_placeholder, instancesState.selectedInstance?.label ?: ""),
                trailingIcon = {
                    Image(
                        painter = painterResource(InstanceType.Bazarr.icon),
                        contentDescription = mokoString(InstanceType.Bazarr.resource),
                        modifier = Modifier.size(24.dp)
                    )
                },
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                },
                actions = {}
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        if (instancesState.selectedInstance == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                NoInstanceView(InstanceType.Bazarr)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SecondaryScrollableTabRow(
                    selectedTabIndex = section.ordinal,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    divider = {}
                ) {
                    BazarrSection.entries.forEach { entry ->
                        Tab(
                            selected = section == entry,
                            onClick = { viewModel.selectSection(entry) },
                            text = {
                                val label = when (entry) {
                                    BazarrSection.Series -> mokoString(MR.strings.series)
                                    BazarrSection.Movies -> mokoString(MR.strings.movies)
                                    BazarrSection.WantedEpisodes -> mokoString(MR.strings.bazarr_wanted_episodes)
                                    BazarrSection.WantedMovies -> mokoString(MR.strings.bazarr_wanted_movies)
                                    BazarrSection.Providers -> mokoString(MR.strings.bazarr_providers)
                                }
                                val count = (uiState as? BazarrLibrary.Success)?.let { state ->
                                    when (entry) {
                                        BazarrSection.Series -> state.series.size
                                        BazarrSection.Movies -> state.movies.size
                                        BazarrSection.WantedEpisodes -> state.wantedEpisodes.size
                                        BazarrSection.WantedMovies -> state.wantedMovies.size
                                        else -> 0
                                    }
                                } ?: 0
                                Text(sectionLabel(label, count))
                            }
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = uiState) {
                        is BazarrLibrary.Initial, is BazarrLibrary.Loading -> {
                            LoadingIndicator(
                                modifier = Modifier.size(96.dp)
                            )
                        }

                        is BazarrLibrary.Error -> {
                            Text(
                                text = state.message,
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        is BazarrLibrary.Success -> {
                            PullToRefreshBox(
                                isRefreshing = false,
                                onRefresh = viewModel::refresh,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                when (section) {
                                    BazarrSection.Series -> BazarrSeriesList(
                                        series = state.series,
                                        onClick = { series ->
                                            navigator.openDetails(series.serviceId, BazarrMediaType.Series)
                                        }
                                    )

                                    BazarrSection.Movies -> BazarrMoviesList(
                                        movies = state.movies,
                                        onClick = { movie ->
                                            navigator.openDetails(movie.serviceId, BazarrMediaType.Movie)
                                        }
                                    )

                                    BazarrSection.WantedEpisodes -> WantedEpisodesContent(
                                        items = state.wantedEpisodes,
                                        onSearch = {
                                            searchTarget = BazarrMediaTarget.Episode(it.sonarrSeriesId, it.sonarrEpisodeId)
                                        }
                                    )

                                    BazarrSection.WantedMovies -> WantedMoviesContent(
                                        items = state.wantedMovies,
                                        onSearch = { searchTarget = BazarrMediaTarget.Movie(it.radarrId) }
                                    )

                                    BazarrSection.Providers -> ProvidersContent(
                                        providers = state.providers,
                                        onReset = viewModel::resetProviders
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    searchTarget?.let { target ->
        BazarrSubtitleSearchSheet(
            target = target,
            onDismiss = { searchTarget = null }
        )
    }
}

private fun sectionLabel(base: String, count: Int): String =
    if (count > 0) "$base ($count)" else base

@Composable
private fun WantedEpisodesContent(
    items: List<WantedEpisode>,
    onSearch: (WantedEpisode) -> Unit
) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(mokoString(MR.strings.bazarr_no_wanted_episodes))
        }
    } else {
        WantedList(
            items = items,
            key = { it.sonarrEpisodeId },
            title = { it.seriesTitle },
            subtitle = { "${it.episodeNumber} · ${it.episodeTitle}" },
            missing = { it.missingSubtitles },
            onSearch = onSearch
        )
    }
}

@Composable
private fun WantedMoviesContent(
    items: List<WantedMovie>,
    onSearch: (WantedMovie) -> Unit
) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(mokoString(MR.strings.bazarr_no_wanted_movies))
        }
    } else {
        WantedList(
            items = items,
            key = { it.radarrId },
            title = { it.title },
            subtitle = { null },
            missing = { it.missingSubtitles },
            onSearch = onSearch
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> WantedList(
    items: List<T>,
    key: (T) -> Any,
    title: (T) -> String,
    subtitle: (T) -> String?,
    missing: (T) -> List<BazarrSubtitleLanguage>,
    onSearch: (T) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { key(it) }) { item ->
            ContainerCard(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSearch(item) }
            ) {
                Column(modifier = Modifier.fillMaxWidth(1f)) {
                    Text(title(item), fontWeight = FontWeight.SemiBold)
                    subtitle(item)?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        missing(item).forEach { lang ->
                            SubtitleLanguageChip(lang)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProvidersContent(
    providers: List<ProviderStatus>,
    onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onReset) {
                Text(mokoString(MR.strings.bazarr_reset_providers))
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(providers, key = { it.name }) { provider ->
                ProviderRow(provider)
            }
        }
    }
}

@Composable
private fun ProviderRow(provider: ProviderStatus) {
    val status = provider.status
    val retry = provider.retry
    val healthy = status.isNullOrBlank() || status.equals("good", ignoreCase = true)
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (healthy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                shape = CircleShape,
                modifier = Modifier.size(10.dp)
            ) {}
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(provider.name, fontWeight = FontWeight.SemiBold)
                if (!status.isNullOrBlank()) {
                    Text(status, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (!retry.isNullOrBlank() && !retry.equals("now", ignoreCase = true)) {
                Text(retry, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun BazarrSeriesList(
    series: List<BazarrSeries>,
    onClick: (BazarrSeries) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(series) { item ->
            BazarrItem(
                title = item.title,
                year = item.year,
                overview = item.overview,
                poster = item.poster,
                fanart = item.fanart,
                monitored = item.monitored,
                onClick = { onClick(item) }
            ) {
                Text(
                    text = mokoString(MR.strings.bazarr_series_subtitle_count, item.episodeFileCount, item.episodeFileCount + item.episodeMissingCount),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun BazarrMoviesList(
    movies: List<BazarrMovie>,
    onClick: (BazarrMovie) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(movies) { item ->
            BazarrItem(
                title = item.title,
                year = item.year,
                overview = item.overview,
                poster = item.poster,
                fanart = item.fanart,
                monitored = item.monitored,
                onClick = { onClick(item) }
            ) {
                val subtitleCount = item.subtitles.size
                val missingCount = item.missingSubtitles.size
                Text(
                    text = mokoString(MR.strings.bazarr_movie_subtitle_count, subtitleCount, missingCount),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun BazarrItem(
    title: String,
    year: String,
    overview: String,
    poster: String?,
    fanart: String?,
    monitored: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    details: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            BannerView(
                bannerModel = fanart?.let { rememberRemoteImageData(it) },
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(TranslucentBlack)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    BasePosterItem(
                        model = rememberRemoteImageData(poster),
                        modifier = Modifier.height(100.dp),
                        aspectRatio = AspectRatio.Poster
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = buildString {
                                    append(title)
                                    append(" ($year)")
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        details()

                        Text(
                            text = overview,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
