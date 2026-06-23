package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMediaType
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMovie
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSeries
import com.dnfapps.arrmatey.bazarr.state.BazarrLibrary
import com.dnfapps.arrmatey.bazarr.viewmodel.BazarrLibraryViewModel
import com.dnfapps.arrmatey.navigation.LocalBazarrNavigator
import com.dnfapps.arrmatey.navigation.openDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.BannerView
import com.dnfapps.arrmatey.ui.components.BasePosterItem
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.TranslucentBlack
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BazarrLibraryScreen(
    wideRailIsVisible: Boolean,
    viewModel: BazarrLibraryViewModel = koinInject(),
) {
    val navigator = LocalBazarrNavigator.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(mokoString(MR.strings.series), mokoString(MR.strings.movies))

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(mokoString(MR.strings.bazarr)) },
                    navigationIcon = {
                        if (!wideRailIsVisible) {
                            NavigationDrawerButton()
                        }
                    }
                )
                (uiState as? BazarrLibrary.Success)?.let { state ->
                    if (state.series.isNotEmpty() && state.movies.isNotEmpty()) {
                        SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { Text(title) }
                                )
                            }
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is BazarrLibrary.Initial, is BazarrLibrary.Loading -> {
                    LoadingIndicator(
                        modifier = Modifier
                            .size(94.dp)
                            .align(Alignment.Center)
                    )
                }
                is BazarrLibrary.Success -> {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = {
                            viewModel.refresh()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val hasSeries = state.series.isNotEmpty()
                        val hasMovies = state.movies.isNotEmpty()

                        when {
                            hasSeries && hasMovies -> {
                                if (selectedTabIndex == 0) {
                                    BazarrSeriesList(
                                        series = state.series,
                                        onClick = { series ->
                                            navigator.openDetails(series.serviceId, BazarrMediaType.Series)
                                        }
                                    )
                                } else {
                                    BazarrMoviesList(
                                        movies = state.movies,
                                        onClick = { movie ->
                                            navigator.openDetails(movie.serviceId, BazarrMediaType.Movie)
                                        }
                                    )
                                }
                            }

                            hasSeries -> BazarrSeriesList(
                                series = state.series,
                                onClick = { series ->
                                    navigator.openDetails(series.serviceId, BazarrMediaType.Series)
                                }
                            )
                            hasMovies -> BazarrMoviesList(
                                movies = state.movies,
                                onClick = { movie ->
                                    navigator.openDetails(movie.serviceId, BazarrMediaType.Movie)
                                }
                            )
                            else -> {
                                // Both empty
                            }
                        }
                    }
                }
                is BazarrLibrary.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp).align(Alignment.Center)
                    )
                }
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
                    text = "${item.episodeFileCount} / ${item.episodeFileCount + item.episodeMissingCount} Episodes",
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
                    text = "$subtitleCount Subtitles, $missingCount Missing",
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
