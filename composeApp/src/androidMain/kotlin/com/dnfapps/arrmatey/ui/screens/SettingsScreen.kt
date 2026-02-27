package com.dnfapps.arrmatey.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.MoreScreenViewModel
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.isDebug
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SettingsNavigation
import com.dnfapps.arrmatey.navigation.SettingsScreen
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.LargeLabelledSwitch
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.components.settings.AboutCard
import com.dnfapps.arrmatey.utils.CrashManager
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.mokoString
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.mikepenz.aboutlibraries.util.withContext
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MoreScreenViewModel = koinInject(),
    navigationManager: NavigationManager = koinInject(),
    settingsNav: SettingsNavigation = navigationManager.settings(),
    moko: MokoStrings = koinInject(),
    crashManager: CrashManager = koinInject()
) {
    val context = LocalContext.current
    val allInstances by viewModel.instances.collectAsStateWithLifecycle()
    val instanceConnectionStatues by viewModel.testingStatus.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showLibrariesSheet by remember { mutableStateOf(false) }

    var confirmShareLastLog by remember { mutableStateOf<String?>(null) }

    val useServiceNavLogos by viewModel.useServiceNavLogos.collectAsStateWithLifecycle()

    BackHandler {
        navigationManager.openDrawer()
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = mokoString(MR.strings.instances),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        allInstances.forEach { instance ->
                            InstanceCard(
                                instance = instance,
                                connectionStatus = instanceConnectionStatues[instance.id],
                                onClick = {
                                    settingsNav.onInstanceTap(instance.id, instance.type)
                                }
                            )
                        }

                        Card(
                            shape = MaterialTheme.shapes.extraLarge,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                settingsNav.navigateTo(SettingsScreen.AddInstance())
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = mokoString(MR.strings.add_instance),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Text(
                    text = mokoString(MR.strings.user_interface),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        settingsNav.navigateTo(SettingsScreen.TabPreferences)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = mokoString(MR.strings.navigation_bar_configuration),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LargeLabelledSwitch(
                    label = mokoString(MR.strings.service_icons_title),
                    sublabel = mokoString(MR.strings.service_icons_description),
                    checked = useServiceNavLogos,
                    onCheckedChange = {
                        viewModel.toggleUseServiceNavLogos()
                    }
                )

                AboutCard(
                    onFeatureRequestClick = {
                        context.openLink(moko.getString(MR.strings.feature_request_link))
                    },
                    onBugReportClick = {
                        confirmShareLastLog = crashManager.getLastCrashLog()
                        if (confirmShareLastLog == null) {
                            context.openLink(moko.getString(MR.strings.bug_report_link))
                        }
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
                            settingsNav.navigateTo(SettingsScreen.Dev)
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

                Spacer(modifier = Modifier.height(16.dp))
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

        confirmShareLastLog?.let { log ->
            AlertDialog(
                onDismissRequest = {
                    confirmShareLastLog = null
                    crashManager.clearCrashLog()
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            crashManager.shareCrashLog(log)
                            crashManager.clearCrashLog()
                            confirmShareLastLog = null
                        }
                    ) { Text(mokoString(MR.strings.yes)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            confirmShareLastLog = null
                            crashManager.clearCrashLog()
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

@Composable
fun InstanceCard(
    instance: Instance,
    connectionStatus: OperationStatus?,
    onClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(instance.type.icon),
                contentDescription = instance.type.name,
                modifier = Modifier.size(40.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = instance.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier.size(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (connectionStatus) {
                            is OperationStatus.InProgress -> CircularProgressIndicator()
                            is OperationStatus.Success -> Icon(Icons.Default.Wifi, null)
                            is OperationStatus.Error -> Icon(Icons.Default.WifiOff,  null, tint = Color.Red)
                            else -> {}
                        }
                    }
                }
                Text(
                    text = instance.url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}