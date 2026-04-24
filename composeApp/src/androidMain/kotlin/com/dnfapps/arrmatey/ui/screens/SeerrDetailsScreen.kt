package com.dnfapps.arrmatey.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SeerrScreen
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.state.MediaProvider
import com.dnfapps.arrmatey.seerr.state.SeerrDetailsState
import com.dnfapps.arrmatey.seerr.viewmodel.SeerrMediaDetailsViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DetailsHeader
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.buttons.MediaDetailsActions
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.koinInject
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.compose.utils.formatWithCommas
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.isDebug
import com.dnfapps.arrmatey.model.InfoItem
import com.dnfapps.arrmatey.seerr.api.model.Episode
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.InfoArea
import com.dnfapps.arrmatey.ui.components.SeerrCreditsSection
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.sheets.SeerrReportIssueSheet
import com.dnfapps.arrmatey.ui.sheets.SeerrViewRequestSheet
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoPlural
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeerrDetailsScreen(
    tmdbId: Long,
    requestType: RequestType,
    viewModel: SeerrMediaDetailsViewModel = koinInjectParams(tmdbId, requestType),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<SeerrScreen> = navigationManager.requests(),
    moko: MokoStrings = koinInject()
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedInstance by viewModel.selectedInstance.collectAsStateWithLifecycle()
    val buttonState by viewModel.buttonState.collectAsStateWithLifecycle()

    val isViewRequestSheetVisible by viewModel.isViewRequestSheetVisible.collectAsStateWithLifecycle()
    val isReportIssueSheetVisible by viewModel.isReportIssueSheetVisible.collectAsStateWithLifecycle()
    val reportIssueState by viewModel.reportIssueState.collectAsStateWithLifecycle()

    val serviceDetails by viewModel.serviceDetails.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues.copy(bottom = 0.dp, top = 0.dp))
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is SeerrDetailsState.Initial,
                is SeerrDetailsState.Loading -> {
                    LoadingIndicator(
                        modifier = Modifier
                            .size(96.dp)
                            .align(Alignment.Center)
                    )
                }
                is SeerrDetailsState.Error -> {
                    ErrorView(
                        errorType = state.errorType,
                        message = state.message ?: mokoString(MR.strings.unknown),
                        onOpenSettings = {
                            selectedInstance?.id?.let { id ->
                                navigationManager.openEditInstanceScreen(id)
                            }
                        },
                        onRetry = {
                            viewModel.refreshDetails()
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SeerrDetailsState.Success -> {
                    val item = state.item
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = { viewModel.refreshDetails() }
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailsHeader(item)

                            Column(
                                modifier = Modifier.padding(bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MediaDetailsActions(
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    buttonState = buttonState,
                                    onWatchClicked = { url, provider ->
                                        handleWatchClick(
                                            url,
                                            provider,
                                            context,
                                            moko
                                        )
                                    },
                                    onRequestClicked = { },
                                    onRequest4kClicked = { },
                                    onWatchTrailerClicked = { context.openLink(it) },
                                    onViewRequestClicked = { viewModel.showViewRequestSheet() },
                                    onApproveRequestClicked = { viewModel.showViewRequestSheet() },
                                    onDeclineRequestClicked = { viewModel.declineRequest(it) },
                                )

                                item.tagline?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                    )
                                }

                                item.overview?.let { overview ->
                                    ItemDescriptionCard(overview, modifier = Modifier.padding(horizontal = 24.dp))
                                }

                                (item as? TvDetails)?.let { series ->
                                    Text(
                                        text = mokoString(MR.strings.seasons_header),
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                    series.seasons.forEach { season ->
                                        var expanded by rememberSaveable { mutableStateOf(false) }
                                        val iconRotation by animateFloatAsState(
                                            targetValue = if (expanded) 180f else 0f,
                                            animationSpec = tween(durationMillis = 200),
                                            label = "iconRotation"
                                        )
                                        ContainerCard(modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp)
                                            .clickable { expanded = !expanded }
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (season.seasonNumber == 0) {
                                                        mokoString(MR.strings.specials)
                                                    } else {
                                                        mokoString(MR.strings.season_label, season.seasonNumber)
                                                    },
                                                    style = MaterialTheme.typography.titleLarge
                                                )
                                                Text(
                                                    text = mokoPlural(MR.plurals.episodes, season.episodeCount),//"${season.episodeCount} episodes",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
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
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                season.episodes.forEachIndexed { index, episode ->
                                                    EpisodeCard(
                                                        episode,
                                                        modifier = Modifier.padding(horizontal = 32.dp)
                                                    )
                                                    if (index < season.episodeCount - 1) {
                                                        HorizontalDivider(
                                                            modifier = Modifier.padding(
                                                                horizontal = 24.dp
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item.credits?.let { credits ->
                                    SeerrCreditsSection(credits)
                                }

                                val infoItems = buildList {
                                    add(InfoItem(mokoString(MR.strings.status), item.status))
                                    (item as? MovieDetails)?.let { movie ->
                                        movie.releaseDate?.format("MMM dd, yyyy")?.let { releaseDate ->
                                            add(InfoItem(mokoString(MR.strings.release_date), releaseDate))
                                        }
                                        add(InfoItem(mokoString(MR.strings.revenue), movie.revenue.formatWithCommas()))
                                        add(InfoItem(mokoString(MR.strings.budget), movie.budget.formatWithCommas()))
                                    }
                                    val countriesText = item.productionCountries.joinToString("\n") { it.name }
                                    add(InfoItem(mokoString(MR.strings.production_countries), countriesText))
                                    val studiosText = item.productionCompanies.joinToString("\n") { it.name }
                                    add(InfoItem(mokoString(MR.strings.studios), studiosText))
                                }
                                InfoArea(
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    infoItems = infoItems,
                                    header = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            state.rtRatings?.let { rt ->
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    RatingView(
                                                        rt.criticsRating.icon,
                                                        "${rt.criticsScore}%"
                                                    )
                                                    RatingView(
                                                        rt.audienceRating.icon,
                                                        "${rt.audienceScore}%"
                                                    )
                                                }
                                            }
                                            state.imdbRatings?.let { imdb ->
                                                RatingView(MR.images.imdb, "${(imdb.criticsScore*10).roundToInt()}%")
                                            }
                                            RatingView(MR.images.tmdb, "${(item.voteAverage*10).roundToInt()}%")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            OverlayTopAppBar(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.TopCenter),
                navigationIcon = {
                    IconButton(
                        onClick = { navigation.popBackStack() },
                        colors = IconButtonDefaults.headerBarColors()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(MR.strings.back)
                        )
                    }
                },
                actions = {
                    if (buttonState.showReportIssueButton) {
                        IconButton(
                            onClick = { viewModel.showReportIssueSheet() },
                            colors = IconButtonDefaults.headerBarColors()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = mokoString(MR.strings.report_issue),
                                tint = ArrOrange
                            )
                        }
                    }
                    if (isDebug()) {
                        if (buttonState.showManageMenu) {
                            IconButton(
                                onClick = { },
                                colors = IconButtonDefaults.headerBarColors()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = mokoString(MR.strings.manage)
                                )
                            }
                        }
                    }
                }
            )

            if (isReportIssueSheetVisible) {
                SeerrReportIssueSheet(
                    state = reportIssueState,
                    updateIssueType = { viewModel.setIssueType(it) },
                    updateMessage = { viewModel.setIssueMessage(it) },
                    updateProblemSeason = { viewModel.setProblemSeason(it) },
                    updateProblemEpisode = { viewModel.setProblemEpisode(it) },
                    onReset = { viewModel.resetIssueState() },
                    onSubmit = { viewModel.submitIssue() },
                    onDismiss = { viewModel.hideReportIssueSheet() }
                )
            }

            if (isViewRequestSheetVisible) {
                (uiState as? SeerrDetailsState.Success)?.item?.let { details ->
                    SeerrViewRequestSheet(
                        details = details,
                        serviceDetails = serviceDetails,
                        onDismissRequest = { viewModel.hideViewRequestSheet() },
                        onApproveRequest = { id, profileId, rootFolder, langId, seasons ->
                            viewModel.approveRequest(id, profileId, rootFolder, langId, seasons)
                            viewModel.hideViewRequestSheet()
                        },
                        onDeclineRequest = { id: Long ->
                            viewModel.declineRequest(id)
                            viewModel.hideViewRequestSheet()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingView(
    logo: ImageResource,
    rating: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = painterResource(logo),
            contentDescription = null,
            modifier = Modifier.height(18.dp)
        )
        Text(text = rating)
    }
}

@Composable
fun EpisodeCard(
    episode: Episode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "${episode.episodeNumber} - ${episode.name}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            episode.airDate?.let { airDate ->
                Text(
                    airDate.format(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        episode.overview?.let { overview ->
            Text(overview, style = MaterialTheme.typography.bodyMedium)
        }
        episode.stillPath?.let { stillPath ->
            AsyncImage(
                model = rememberRemoteImageData(stillPath),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                contentDescription = null
            )
        }
    }
}

fun handleWatchClick(
    url: String,
    provider: MediaProvider,
    context: Context,
    moko: MokoStrings
) {
    when (provider) {
        MediaProvider.Plex -> {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, moko.getString(MR.strings.no_app_found), Toast.LENGTH_SHORT).show()
            }
        }
        MediaProvider.Jellyfin -> {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, moko.getString(MR.strings.no_app_found), Toast.LENGTH_SHORT).show()
            }
        }
        MediaProvider.None -> {
            Toast.makeText(context, moko.getString(MR.strings.no_app_found), Toast.LENGTH_SHORT).show()
        }
    }
}