package com.dnfapps.arrmatey.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.MoreScreenViewModel
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.isDebug
import com.dnfapps.arrmatey.model.IconSource
import com.dnfapps.arrmatey.model.SettingItem
import com.dnfapps.arrmatey.navigation.navigationManager
import com.dnfapps.arrmatey.navigation.onInstanceTap
import com.dnfapps.arrmatey.navigation.settingsNavigator
import com.dnfapps.arrmatey.navigation.toAddCustomWebpage
import com.dnfapps.arrmatey.navigation.toAddDownloadClient
import com.dnfapps.arrmatey.navigation.toAddInstance
import com.dnfapps.arrmatey.navigation.toDev
import com.dnfapps.arrmatey.navigation.toEditCustomWebpage
import com.dnfapps.arrmatey.navigation.toEditDownloadClient
import com.dnfapps.arrmatey.navigation.toShortcutsPreferences
import com.dnfapps.arrmatey.navigation.toTabPreferences
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.SettingsGroup
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.components.settings.AboutCard
import com.dnfapps.arrmatey.ui.icons.Hard_drive
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.mikepenz.aboutlibraries.util.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MoreScreenViewModel = koinInject(),
    moko: MokoStrings = koinInject()
) {
    val navManager = navigationManager
    val settingsNav = settingsNavigator
    val context = LocalContext.current
    val allInstances by viewModel.instances.collectAsStateWithLifecycle()
    val allDownloadClients by viewModel.downloadClients.collectAsStateWithLifecycle()
    val allCustomWebPages by viewModel.customWebpages.collectAsStateWithLifecycle()
    val instanceConnectionStatues by viewModel.testingStatus.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showLibrariesSheet by remember { mutableStateOf(false) }
    var confirmShareLastLog by remember { mutableStateOf(false) }

    val useServiceNavLogos by viewModel.useServiceNavLogos.collectAsStateWithLifecycle()
    val hideInstanceSwitcher by viewModel.hideInstanceSwitcher.collectAsStateWithLifecycle()

    BackHandler {
        navManager.openDrawer()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = mokoString(MR.strings.settings)) },
                navigationIcon = {
                    NavigationDrawerButton()
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = navigationBarBottomInset() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsGroup(
                title = mokoString(MR.strings.instances),
                items = allInstances.map { instance ->
                    SettingItem(
                        icon = IconSource.Resource(instance.type.icon),
                        title = instance.label,
                        subtitle = instance.url,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        onClick = {
                            settingsNav.onInstanceTap(instance.id, instance.type)
                        },
                        titleExtraContent = {
                            Box(
                                modifier = Modifier.size(18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when (instanceConnectionStatues[instance.id]) {
                                    is OperationStatus.InProgress -> CircularProgressIndicator()
                                    is OperationStatus.Success -> Icon(Icons.Default.Wifi, null)
                                    is OperationStatus.Error -> Icon(Icons.Default.WifiOff,  null, tint = Color.Red)
                                    else -> {}
                                }
                            }
                        }
                    )
                } + SettingItem(
                    title = mokoString(MR.strings.add_instance),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    icon = IconSource.Vector(Icons.Default.AddCircleOutline),
                    onClick = {
                        settingsNav.toAddInstance()
                    }
                )
            )

            SettingsGroup(
                title = mokoString(MR.strings.download_clients),
                items = allDownloadClients.map { downloadClient ->
                    SettingItem(
                        icon = IconSource.Resource(downloadClient.type.icon),
                        title = downloadClient.label,
                        subtitle = downloadClient.url,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        onClick = {
                            settingsNav.toEditDownloadClient(downloadClient.id)
                        },
                        titleExtraContent = {
                            Box(
                                modifier = Modifier.size(18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when (instanceConnectionStatues[downloadClient.id + 100_000]) {
                                    is OperationStatus.InProgress -> CircularProgressIndicator()
                                    is OperationStatus.Success -> Icon(Icons.Default.Wifi, null)
                                    is OperationStatus.Error -> Icon(Icons.Default.WifiOff,  null, tint = Color.Red)
                                    else -> {}
                                }
                            }
                        }
                    )
                } + SettingItem(
                    title = mokoString(MR.strings.add_download_client),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    icon = IconSource.Vector(Icons.Default.AddCircleOutline),
                    onClick = {
                        settingsNav.toAddDownloadClient()
                    }
                )
            )

            SettingsGroup(
                title = mokoString(MR.strings.custom_webpages),
                items = allCustomWebPages.map { webpage ->
                    SettingItem(
                        title = webpage.name,
                        subtitle = webpage.url,
                        icon = IconSource.Vector(Icons.Default.Language),
                        onClick = {
                            settingsNav.toEditCustomWebpage(webpage.id)
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                    )
                } + SettingItem(
                    title = mokoString(MR.strings.add_custom_webpage),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    icon = IconSource.Vector(Icons.Default.AddCircleOutline),
                    onClick = {
                        settingsNav.toAddCustomWebpage()
                    }
                )
            )

            SettingsGroup(
                title = mokoString(MR.strings.user_interface),
                items = listOf(
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.Navigation),
                        title = mokoString(MR.strings.navigation_bar_configuration),
                        onClick = {
                            settingsNav.toTabPreferences()
                        }
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Icons.AutoMirrored.Default.Shortcut),
                        title = mokoString(MR.strings.shortcuts_configuration),
                        onClick = {
                            settingsNav.toShortcutsPreferences()
                        }
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Icons.Default.MiscellaneousServices),
                        title = mokoString(MR.strings.service_icons_title),
                        subtitle = mokoString(MR.strings.service_icons_description),
                        trailingContent = {
                            Switch(
                                checked = useServiceNavLogos,
                                onCheckedChange = { viewModel.toggleUseServiceNavLogos() }
                            )
                        },
                        onClick = { viewModel.toggleUseServiceNavLogos() }
                    ),
                    SettingItem(
                        icon = IconSource.Vector(Hard_drive),
                        title = mokoString(MR.strings.instance_switcher_toggle_title),
                        subtitle = mokoString(MR.strings.instance_switcher_toggle_description),
                        trailingContent = {
                            Switch(
                                checked = hideInstanceSwitcher,
                                onCheckedChange = { viewModel.toggleInstanceSwitcher() }
                            )
                        },
                        onClick = { viewModel.toggleInstanceSwitcher() }
                    )
                )
            )

            AboutCard(
                onFeatureRequestClick = {
                    context.openLink(moko.getString(MR.strings.feature_request_link))
                },
                onBugReportClick = {
                    confirmShareLastLog = true
                },
                onGitHubClick = {
                    context.openLink(moko.getString(MR.strings.app_link))
                },
                onDonateClick = {
                    context.openLink(moko.getString(MR.strings.bmac_link))
                },
                onLibrariesClick = { showLibrariesSheet = true },
                modifier = Modifier.padding(top = 12.dp)
            )

            if (isDebug()) {
                Button(onClick = {
                    throw IllegalStateException("THIS IS A SIMULATED CRASH")
                }) {
                    Text("Simulate crash")
                }

                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        settingsNav.toDev()
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Development Settings",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }

        if (showLibrariesSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLibrariesSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ) {
                val libraries by produceState<Libs?>(null) {
                    value = withContext(Dispatchers.IO) {
                        Libs.Builder().withContext(context).build()
                    }
                }
                LibrariesContainer(
                    libraries = libraries,
                    modifier = Modifier.fillMaxSize(),
                    colors = LibraryDefaults.libraryColors(
                        libraryBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    padding = LibraryDefaults.libraryPadding(
                        licenseDialogContentPadding = 16.dp
                    ),
                    header = { item {
                        Text(
                            text = mokoString(MR.strings.libraries),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    } }
                )
            }
        }

        if (confirmShareLastLog) {
            AlertDialog(
                onDismissRequest = {
                    confirmShareLastLog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            shareLogs(context)
                            confirmShareLastLog = false
                        }
                    ) { Text(mokoString(MR.strings.yes)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            confirmShareLastLog = false
                            context.openLink(moko.getString(MR.strings.bug_report_link))
                        }
                    ) { Text(mokoString(MR.strings.no)) }
                },
                title = { Text(mokoString(MR.strings.share_crash_log)) },
                text = {
                    Text(mokoString(MR.strings.share_crash_log_message))
                }
            )
        }
    }
}
