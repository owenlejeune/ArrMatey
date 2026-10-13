package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.instances.repository.buildPmsImageUrl
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tautulli.api.model.TautulliHomeStatId
import com.dnfapps.arrmatey.tautulli.api.model.TautulliHomeStatSection
import com.dnfapps.arrmatey.tautulli.state.TautulliStatsState
import com.dnfapps.arrmatey.tautulli.viewmodel.TautulliStatsViewModel
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TautulliStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TautulliStatsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = mokoString(MR.strings.statistics),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    BackButton(onClick = onNavigateBack)
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier =
            Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            when (val currentState = state) {
                is TautulliStatsState.Initial,
                is TautulliStatsState.Loading,
                -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(96.dp))
                    }
                }
                is TautulliStatsState.Error -> {
                    ErrorView(
                        errorType = currentState.type,
                        message = currentState.message,
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize().wrapContentSize(),
                    )
                }
                is TautulliStatsState.Success -> {
                    val activeSections = currentState.sections.filter { it.rows.isNotEmpty() }
                    if (activeSections.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = mokoString(MR.strings.no_stats_available),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding =
                            PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom =
                                16.dp +
                                    if (LocalFloatingBarBottomPadding.current > 0.dp) {
                                        LocalFloatingBarBottomPadding.current
                                    } else {
                                        navigationBarBottomInset()
                                    },
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(
                                items = activeSections,
                                key = { it.statId?.value ?: it.statTitle ?: "" },
                            ) { section ->
                                HomeStatSectionCard(
                                    section = section,
                                    instance = currentState.instance,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeStatSectionCard(
    section: TautulliHomeStatSection,
    instance: com.dnfapps.arrmatey.instances.model.Instance,
) {
    val (icon, title) =
        when (section.statId) {
            TautulliHomeStatId.TopMovies -> Icons.Default.Movie to mokoString(MR.strings.top_movies)
            TautulliHomeStatId.PopularMovies -> Icons.Default.Movie to mokoString(MR.strings.popular_movies)
            TautulliHomeStatId.TopTv -> Icons.Default.Tv to mokoString(MR.strings.top_tv)
            TautulliHomeStatId.PopularTv -> Icons.Default.Tv to mokoString(MR.strings.popular_tv)
            TautulliHomeStatId.TopMusic -> Icons.Default.MusicNote to mokoString(MR.strings.top_music)
            TautulliHomeStatId.PopularMusic -> Icons.Default.MusicNote to mokoString(MR.strings.popular_music)
            TautulliHomeStatId.TopPlatforms -> Icons.Default.Leaderboard to mokoString(MR.strings.top_platforms)
            else -> Icons.Default.Leaderboard to (section.statTitle ?: section.statId?.value ?: "")
        }

    val maxPlays = section.rows.maxOfOrNull { it.totalPlays ?: it.totalDuration ?: 1L } ?: 1L
    val greenAccent = Color(0xFF2E7D32)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = greenAccent,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = greenAccent,
                )
            }

            section.rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFC8E6C9),
                        modifier = Modifier.size(26.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                            )
                        }
                    }

                    val imageUrl = buildPmsImageUrl(instance, row.thumb) ?: row.userThumb
                    Box(
                        modifier =
                        Modifier
                            .width(42.dp)
                            .aspectRatio(AspectRatio.Poster.ratio)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = rememberRemoteImageData(imageUrl, trim = false),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = row.title ?: row.friendlyName ?: row.user ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )

                            val countText =
                                row.totalPlays?.let { "$it plays" }
                                    ?: row.totalDuration?.let { formatStatDuration(it) }
                                    ?: ""
                            if (countText.isNotBlank()) {
                                Text(
                                    text = countText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                        }

                        val value = row.totalPlays ?: row.totalDuration ?: 0L
                        val progressFraction =
                            if (maxPlays > 0) (value.toFloat() / maxPlays.toFloat()).coerceIn(0f, 1f) else 0f

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = greenAccent,
                            trackColor = MaterialTheme.colorScheme.surface,
                        )
                    }
                }
            }
        }
    }
}

private fun formatStatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
