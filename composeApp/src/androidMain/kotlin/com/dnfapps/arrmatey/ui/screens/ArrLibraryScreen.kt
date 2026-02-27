package com.dnfapps.arrmatey.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrMediaViewModel
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.InstancePicker
import com.dnfapps.arrmatey.ui.components.MediaView
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.menu.LibraryFilterMenu
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.koinInject

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArrLibraryScreen(
    type: InstanceType,
    arrMediaViewModel: ArrMediaViewModel = koinInjectParams(type),
    instancesViewModel: InstancesViewModel = koinInjectParams(type),
    activityQueueViewModel: ActivityQueueViewModel = koinInject(),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<ArrScreen> = navigationManager.arr(type)
) {
    val context = LocalContext.current

    val queueItems by activityQueueViewModel.queueItems.collectAsStateWithLifecycle()
    val uiState by arrMediaViewModel.uiState.collectAsStateWithLifecycle()
    val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()
    val preferences by arrMediaViewModel.preferences.collectAsStateWithLifecycle()

    val errorMessage by arrMediaViewModel.errorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(errorMessage) {
        errorMessage?.takeUnless { it.isEmpty() }?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        arrMediaViewModel.resetErrorMessage()
    }

    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState.text) {
        arrMediaViewModel.updateSearchQuery(textFieldState.text.toString())
    }

    Scaffold(
        floatingActionButton = {
            instancesState.selectedInstance?.let {
                FloatingActionButton(
                    onClick = { navigation.navigateTo(ArrScreen.Search()) }
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        },
        topBar = {
            ArrAppBarWithSearch(
                textFieldState = textFieldState,
                textFieldEnabled = instancesState.selectedInstance != null,
                searchPlaceholder = mokoString(MR.strings.search_placeholder, instancesState.selectedInstance?.label ?: ""),
                trailingIcon = {
                    Image(
                        painter = painterResource(type.icon),
                        contentDescription = mokoString(type.resource),
                        modifier = Modifier.size(24.dp)
                    )
                },
                navigationIcon = { NavigationDrawerButton() },
                actions = {
                    InstancePicker(
                        type = type,
                        currentInstance = instancesState.selectedInstance,
                        typeInstances = instancesState.instances,
                        onInstanceSelected = { instancesViewModel.setInstanceActive(it) }
                    )
                    LibraryFilterMenu(
                        type = type,
                        filterBy = preferences.filterBy,
                        onFilterByChanged = { arrMediaViewModel.updateFilterBy(it) },
                        sortBy = preferences.sortBy,
                        onSortByChanged = { arrMediaViewModel.updateSortBy(it) },
                        sortOrder = preferences.sortOrder,
                        onSortOrderChanged = { arrMediaViewModel.updateSortOrder(it) },
                        viewType = preferences.viewType,
                        onViewTypeChanged = { arrMediaViewModel.updateViewType(it) }
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (instancesState.selectedInstance == null) {
                NoInstanceView(type)
            } else {
                when (val state = uiState) {
                    is ArrLibrary.Initial -> {
                        NoInstanceView(type)
                    }

                    is ArrLibrary.Loading -> {
                        LoadingIndicator(
                            modifier = Modifier.size(96.dp)
                        )
                    }

                    is ArrLibrary.Error -> {
                        ErrorView(
                            errorType = state.type,
                            message = state.message,
                            onOpenSettings = {
                                instancesState.selectedInstance?.let {
                                    navigationManager.openEditInstanceScreen(it.id)
                                }
                            },
                            onRetry = {
                                arrMediaViewModel.refresh()
                            }
                        )
                    }

                    is ArrLibrary.Success -> {
                        PullToRefreshBox(
                            isRefreshing = false,
                            onRefresh = {
                                arrMediaViewModel.refresh()
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val items = state.items
                            if (items.isEmpty() && textFieldState.text.isEmpty()) {
                                EmptyLibraryView(modifier = Modifier.align(Alignment.Center))
                            } else if (items.isNotEmpty()) {
                                MediaView(
                                    type = type,
                                    items = items,
                                    onItemClick = {
                                        it.id?.let { id ->
                                            navigation.navigateTo(
                                                ArrScreen.Details(id = id)
                                            )
                                        }
                                    },
                                    viewType = preferences.viewType,
                                    itemIsActive = { item ->
                                        queueItems.any { it.mediaId == item.id }
                                    }
                                )
                            } else {
                                EmptySearchResultsView(type, textFieldState.text.toString()) {
                                    val destination = ArrScreen.Search(textFieldState.text.toString())
                                    navigation.navigateTo(destination)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchResultsView(
    type: InstanceType,
    query: String,
    onShouldSearch: () -> Unit
) {
    val mediaType = when (type) {
        InstanceType.Sonarr -> mokoString(MR.strings.type_series)
        InstanceType.Radarr -> mokoString(MR.strings.type_movie)
        InstanceType.Lidarr -> mokoString(MR.strings.type_artist)
        else -> mokoString(MR.strings.unknown)
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = mokoString(MR.strings.no_query_results, query),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = buildAnnotatedString {
                append(mokoString(MR.strings.no_query_results_label))
                append(" ")
                withLink(
                    link = LinkAnnotation.Clickable(tag = "new_entry") {
                        onShouldSearch()
                    }
                ) {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )) {
                        append(mokoString(MR.strings.no_query_results_link, mediaType))
                    }
                }
            }
        )
    }
}

@Composable
private fun EmptyLibraryView(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.VideoLibrary,
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )
        Text(
            text = mokoString(MR.strings.empty_library),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = mokoString(MR.strings.empty_library_message)
        )
    }
}