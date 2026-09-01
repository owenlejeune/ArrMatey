package com.dnfapps.arrmatey.ui.screens.settings

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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.MoreScreenViewModel
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.IconSource
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.model.SettingItem
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.SettingsGroup
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesSettingsScreen(
    onNavigateToInstance: (Long, InstanceType) -> Unit,
    onNavigateToAddInstance: () -> Unit,
    onNavigateToEditDownloadClient: (Long) -> Unit,
    onNavigateToAddDownloadClient: () -> Unit,
    onNavigateToEditCustomWebpage: (Long) -> Unit,
    onNavigateToAddCustomWebpage: () -> Unit,
    onBack: () -> Unit,
    viewModel: MoreScreenViewModel = koinInject(),
) {
    val allInstances by viewModel.instances.collectAsStateWithLifecycle()
    val allDownloadClients by viewModel.downloadClients.collectAsStateWithLifecycle()
    val allCustomWebPages by viewModel.customWebpages.collectAsStateWithLifecycle()
    val instanceConnectionStatues by viewModel.testingStatus.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = mokoString(MR.strings.services)) },
                navigationIcon = { BackButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = navigationBarBottomInset() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsGroup(
                title = mokoString(MR.strings.instances),
                items =
                    allInstances.map { instance ->
                        SettingItem(
                            icon = IconSource.Resource(instance.type.icon),
                            title = instance.label,
                            subtitle = instance.url,
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            onClick = {
                                onNavigateToInstance(instance.id, instance.type)
                            },
                            titleExtraContent = {
                                Box(
                                    modifier = Modifier.size(18.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    when (instanceConnectionStatues[instance.id]) {
                                        is OperationStatus.InProgress -> CircularProgressIndicator()
                                        is OperationStatus.Success -> Icon(Icons.Default.Wifi, null)
                                        is OperationStatus.Error -> Icon(Icons.Default.WifiOff, null, tint = Color.Red)
                                        else -> {}
                                    }
                                }
                            },
                        )
                    } +
                        SettingItem(
                            title = mokoString(MR.strings.add_instance),
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            icon = IconSource.Vector(Icons.Default.AddCircleOutline),
                            onClick = {
                                onNavigateToAddInstance()
                            },
                        ),
            )

            SettingsGroup(
                title = mokoString(MR.strings.download_clients),
                items =
                    allDownloadClients.map { downloadClient ->
                        SettingItem(
                            icon = IconSource.Resource(downloadClient.type.icon),
                            title = downloadClient.label,
                            subtitle = downloadClient.url,
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            onClick = {
                                onNavigateToEditDownloadClient(downloadClient.id)
                            },
                            titleExtraContent = {
                                Box(
                                    modifier = Modifier.size(18.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    when (instanceConnectionStatues[downloadClient.id + 100_000]) {
                                        is OperationStatus.InProgress -> CircularProgressIndicator()
                                        is OperationStatus.Success -> Icon(Icons.Default.Wifi, null)
                                        is OperationStatus.Error -> Icon(Icons.Default.WifiOff, null, tint = Color.Red)
                                        else -> {}
                                    }
                                }
                            },
                        )
                    } +
                        SettingItem(
                            title = mokoString(MR.strings.add_download_client),
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            icon = IconSource.Vector(Icons.Default.AddCircleOutline),
                            onClick = {
                                onNavigateToAddDownloadClient()
                            },
                        ),
            )

            SettingsGroup(
                title = mokoString(MR.strings.custom_webpages),
                items =
                    allCustomWebPages.map { webpage ->
                        SettingItem(
                            title = webpage.name,
                            subtitle = webpage.url,
                            icon = IconSource.Vector(Icons.Default.Language),
                            onClick = {
                                onNavigateToEditCustomWebpage(webpage.id)
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                        )
                    } +
                        SettingItem(
                            title = mokoString(MR.strings.add_custom_webpage),
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            icon = IconSource.Vector(Icons.Default.AddCircleOutline),
                            onClick = {
                                onNavigateToAddCustomWebpage()
                            },
                        ),
            )
        }
    }
}
