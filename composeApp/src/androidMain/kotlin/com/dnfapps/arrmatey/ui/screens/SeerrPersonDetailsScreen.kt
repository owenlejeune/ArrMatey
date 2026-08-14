package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.navigation.navigationManager
import com.dnfapps.arrmatey.seerr.api.model.PersonDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.state.SeerrDetailsState
import com.dnfapps.arrmatey.seerr.viewmodel.SeerrMediaDetailsViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DetailsHeader
import com.dnfapps.arrmatey.ui.components.DiscoverSection
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeerrPersonDetailsScreen(
    personId: Long,
    onBack: () -> Unit,
    viewModel: SeerrMediaDetailsViewModel = koinInjectParams(personId, RequestType.Person)
) {
    val navManager = navigationManager
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedInstance by viewModel.selectedInstance.collectAsStateWithLifecycle()
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
                                navManager.openEditInstanceScreen(id)
                            }
                        },
                        onRetry = {
                            viewModel.refreshDetails()
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SeerrDetailsState.Success -> {
                    val item = state.item as PersonDetails
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
                                modifier = Modifier
                                    .padding(bottom = 24.dp)
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Text(
                                    text = item.displayTitle,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )

                                if (item.alsoKnownAs.isNotEmpty()) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    ) {
                                        Text(
                                            text = mokoString(MR.strings.also_known_as),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.alsoKnownAs.joinToString(", "),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                val credits by viewModel.personCredits.collectAsStateWithLifecycle()
                                credits?.let { personCredits ->
                                    if (personCredits.cast.isNotEmpty()) {
                                        DiscoverSection(
                                            title = MR.strings.appearances,
                                            icon = Icons.Default.Movie,
                                            data = PagedData(items = personCredits.cast),
                                            onItemClick = { result ->
                                                navManager.openSeerrDetails(result.id, result.mediaType)
                                            },
                                            onLoadMore = { }
                                        )
                                    }
                                    if (personCredits.crew.isNotEmpty()) {
                                        DiscoverSection(
                                            title = MR.strings.crew,
                                            icon = Icons.Default.Settings,
                                            data = PagedData(items = personCredits.crew),
                                            onItemClick = { result ->
                                                navManager.openSeerrDetails(result.id, result.mediaType)
                                            },
                                            onLoadMore = { }
                                        )
                                    }
                                }
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
                        onClick = { onBack() },
                        colors = IconButtonDefaults.headerBarColors()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(MR.strings.back)
                        )
                    }
                },
                actions = { }
            )
        }
    }
}
