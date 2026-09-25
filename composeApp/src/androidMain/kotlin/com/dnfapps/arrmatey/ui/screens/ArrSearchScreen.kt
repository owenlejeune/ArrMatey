package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.arrmatey.arr.viewmodel.ArrSearchViewModel
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shortcuts.AppShortcutManager
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.MediaList
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.menu.SearchSortMenu
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds

@OptIn(
    ExperimentalMaterial3Api::class,
    FlowPreview::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun ArrSearchScreen(
    initialQuery: String,
    type: InstanceType,
    onBack: () -> Unit,
    onItemClick: (ArrMedia) -> Unit,
    instanceId: Long? = null,
    shortcutManager: AppShortcutManager = koinInject(),
    viewModel: ArrSearchViewModel = koinViewModel(key = "${type.name}_$instanceId", parameters = { parametersOf(type, instanceId) }),
) {
    val lookupState by viewModel.lookupUiState.collectAsStateWithLifecycle()
    val activeMediaIds by viewModel.activeMediaIds.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val showBanners by viewModel.searchShowBanners.collectAsStateWithLifecycle()

    val textFieldState = rememberTextFieldState(initialQuery)
    val searchBarState = rememberSearchBarState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(type) {
        shortcutManager.pushSearchShortcut(type)
    }

    LaunchedEffect(Unit) {
        if (initialQuery.isEmpty() && textFieldState.text.isEmpty()) {
            searchBarState.animateToExpanded()
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .debounce(250.milliseconds)
            .distinctUntilChanged()
            .collect { query ->
                viewModel.performLookup(query)
            }
    }

    Scaffold(
        topBar = {
            ArrAppBarWithSearch(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                inputFieldModifier = Modifier.focusRequester(focusRequester),
                navigationIcon = { BackButton(onBack) },
                actions = {
                    SearchSortMenu(
                        sortBy = sortBy,
                        onSortChanged = { viewModel.setSortBy(it) },
                        sortOrder = sortOrder,
                        onOrderChanged = { viewModel.setSortOrder(it) },
                    )
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            when (val state = lookupState) {
                is ArrLibrary.Initial -> {}
                is ArrLibrary.Loading -> {
                    LoadingIndicator(
                        modifier =
                            Modifier
                                .size(96.dp)
                                .align(Alignment.Center),
                    )
                }

                is ArrLibrary.Success -> {
                    if (state.items.isEmpty()) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(72.dp)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = mokoString(MR.strings.empty_library),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        MediaList(
                            aspectRatio = type.aspectRatio,
                            items = state.items,
                            onItemClick = onItemClick,
                            itemIsActive = { item -> item.id in activeMediaIds },
                            includeOverview = true,
                            showBannerBackground = showBanners,
                        )
                    }
                }

                is ArrLibrary.Error -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(72.dp)
                                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = mokoString(MR.strings.error_generic_title),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
