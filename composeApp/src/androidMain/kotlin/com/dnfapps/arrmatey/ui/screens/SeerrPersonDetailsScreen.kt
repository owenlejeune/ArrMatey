package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.entensions.breakPadding
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.entensions.takeUnlessEmpty
import com.dnfapps.arrmatey.entensions.unlessEmpty
import com.dnfapps.arrmatey.navigation.navigationManager
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.PersonDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.state.SeerrDetailsState
import com.dnfapps.arrmatey.seerr.viewmodel.SeerrMediaDetailsViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.PersonDetailsHeader
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.utils.GridDensity
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.StringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeerrPersonDetailsScreen(
    personId: Long,
    onBack: () -> Unit,
    onMediaClick: (Long, RequestType) -> Unit = { _, _ -> },
    viewModel: SeerrMediaDetailsViewModel = koinInjectParams(personId, RequestType.Person),
) {
    val navManager = navigationManager
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedInstance by viewModel.selectedInstance.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .padding(paddingValues.copy(bottom = 0.dp, top = 0.dp))
                    .fillMaxSize(),
        ) {
            when (val state = uiState) {
                is SeerrDetailsState.Initial,
                is SeerrDetailsState.Loading,
                -> {
                    LoadingIndicator(
                        modifier =
                            Modifier
                                .size(96.dp)
                                .align(Alignment.Center),
                    )
                }
                is SeerrDetailsState.Error -> {
                    ErrorView(
                        errorType = state.errorType,
                        message = state.message ?: mokoString(MR.strings.unknown),
                        onOpenSettings = {
                            selectedInstance?.id?.let { id ->
                                navManager.openEditInstanceScreen(id)
                            }
                        },
                        onRetry = {
                            viewModel.refreshDetails()
                        },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                is SeerrDetailsState.Success -> {
                    val item = state.item as PersonDetails
                    val credits by viewModel.personCredits.collectAsStateWithLifecycle()
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = { viewModel.refreshDetails() },
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = GridDensity.Normal.minSize),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp, start = 24.dp, end = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                PersonDetailsHeader(
                                    item = item,
                                    credits = credits,
                                    modifier = Modifier.breakPadding(24.dp),
                                )
                            }

                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column(
                                    modifier = Modifier.padding(top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(24.dp),
                                ) {
                                    Column {
                                        Text(
                                            text = item.displayTitle,
                                            style = MaterialTheme.typography.headlineMedium,
                                        )
                                        val birthday = item.birthday?.format("MMMM d, yyyy") ?: mokoString(MR.strings.unknown)
                                        val birthplace = item.placeOfBirth?.takeUnlessEmpty() ?: mokoString(MR.strings.unknown)
                                        Text(
                                            text = mokoString(MR.strings.born_on, birthday, birthplace),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )

                                        if (item.alsoKnownAs.isNotEmpty()) {
                                            Text(
                                                modifier = Modifier.padding(top = 8.dp),
                                                text =
                                                    buildAnnotatedString {
                                                        withStyle(
                                                            style = MaterialTheme.typography.titleMediumEmphasized.toSpanStyle(),
                                                        ) {
                                                            append(mokoString(MR.strings.also_known_as))
                                                        }
                                                        withStyle(
                                                            style = MaterialTheme.typography.bodyMedium.toSpanStyle(),
                                                        ) {
                                                            append(" ")
                                                            append(item.alsoKnownAs.joinToString(", "))
                                                        }
                                                    },
                                            )
                                        }
                                    }

                                    item.biography?.unlessEmpty { biography ->
                                        ItemDescriptionCard(biography)
                                    }
                                }
                            }

                            credits?.let { personCredits ->
                                if (personCredits.cast.isNotEmpty()) {
                                    seerrCreditsGrid(
                                        title = MR.strings.appearances,
                                        icon = Icons.Default.Movie,
                                        items = personCredits.cast,
                                        onItemClick = { result ->
                                            onMediaClick(result.id, result.mediaType)
                                        },
                                    )
                                }
                                if (personCredits.crew.isNotEmpty()) {
                                    seerrCreditsGrid(
                                        title = MR.strings.crew,
                                        icon = Icons.Default.Settings,
                                        items = personCredits.crew,
                                        onItemClick = { result ->
                                            onMediaClick(result.id, result.mediaType)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            OverlayTopAppBar(
                gridState = gridState,
                modifier = Modifier.align(Alignment.TopCenter),
                navigationIcon = {
                    IconButton(
                        onClick = { onBack() },
                        colors = IconButtonDefaults.headerBarColors(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(MR.strings.back),
                        )
                    }
                },
            )
        }
    }
}

private fun LazyGridScope.seerrCreditsGrid(
    title: StringResource,
    icon: ImageVector,
    items: List<DiscoverResult>,
    onItemClick: (DiscoverResult) -> Unit,
) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = mokoString(title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }

    items(items) { item ->
        PosterItem(
            item = item,
            onItemClick = { onItemClick(item) },
            includeCredits = true,
        )
    }
}
