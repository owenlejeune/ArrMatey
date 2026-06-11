package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.arr.state.ArrDashboardState
import com.dnfapps.arrmatey.arr.viewmodel.ArrInstanceDashboardViewModel
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.model.InfoItem
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.navigationManager
import com.dnfapps.arrmatey.navigation.settingsNavigator
import com.dnfapps.arrmatey.navigation.toEditInstance
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrHealthCard
import com.dnfapps.arrmatey.ui.components.DiskSpaceSection
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.InfoArea
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArrInstanceDashboard(
    id: Long,
    navigation: Navigator<*>,
    windowSizeClass: WindowSizeClass,
    viewModel: ArrInstanceDashboardViewModel = koinInjectParams(id),
    moko: MokoStrings = koinInject()
) {
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val navManager = navigationManager
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val instance by viewModel.instance.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        modifier = if (isCompact) {
            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        } else Modifier,
        topBar = {
            TopAppBar(
                title = { instance?.let { instance ->
                    Text(instance.label)
                }},
                navigationIcon = {
                    BackButton(navigation)
                },
                actions = {
                    IconButton(
                        onClick = {
                            navManager.openEditInstanceScreen(id)
                        }
                    ) {
                        Icon(Icons.Default.Edit, null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { contentPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = state) {
                is ArrDashboardState.Initial -> {}
                is ArrDashboardState.Loading -> {
                    LoadingIndicator()
                }
                is ArrDashboardState.Error -> {
                    ErrorView(
                        errorType = state.type,
                        message = state.message ?: "",
                        onRetry = { viewModel.refresh() },
                        onOpenSettings = {
                            instance?.let {
                                navManager.openEditInstanceScreen(it.id)
                            }
                        }
                    )
                }
                is ArrDashboardState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = navigationBarBottomInset() + 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = mokoString(MR.strings.health),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        state.healthItems.forEach {
                            ArrHealthCard(it)
                        }
                        if (state.healthItems.isEmpty()) {
                            Text(
                                text = mokoString(MR.strings.no_issues),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            text = mokoString(MR.strings.disk_space),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        DiskSpaceSection(state.disks)

                        state.softwareStatus?.let { ss ->
                            val infoItems = buildList {
                                add(InfoItem(moko.getString(MR.strings.host_endpoint), instance?.url ?: ""))
                                add(
                                    InfoItem(
                                        moko.getString(MR.strings.version),
                                        ss.version ?: moko.getString(MR.strings.unknown)
                                    )
                                )
                                add(
                                    InfoItem(
                                        moko.getString(MR.strings.startup_path),
                                        ss.startupPath ?: moko.getString(MR.strings.unknown)
                                    )
                                )
                                add(
                                    InfoItem(
                                        moko.getString(MR.strings.app_data_path),
                                        ss.appData ?: moko.getString(MR.strings.unknown)
                                    )
                                )
                                add(
                                    InfoItem(
                                        moko.getString(MR.strings.host_platform),
                                        moko.getString(ss.hostPlatform)
                                    )
                                )
                                add(
                                    InfoItem(
                                        moko.getString(MR.strings.host_os),
                                        ss.hostOs ?: moko.getString(MR.strings.unknown)
                                    )
                                )
                            }
                            InfoArea(infoItems, title = MR.strings.system_info)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}