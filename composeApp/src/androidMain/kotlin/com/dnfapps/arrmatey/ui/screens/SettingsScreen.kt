package com.dnfapps.arrmatey.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.ReleaseNotesSheet
import com.dnfapps.arrmatey.arr.viewmodel.MoreScreenViewModel
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.isDebug
import com.dnfapps.arrmatey.model.IconSource
import com.dnfapps.arrmatey.model.SettingItem
import com.dnfapps.arrmatey.navigation.navigationManager
import com.dnfapps.arrmatey.permissions.rememberLocalNetworkPermissionHandler
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
import org.koin.compose.viewmodel.koinViewModel
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MoreScreenViewModel = koinViewModel(),
    moko: MokoStrings = koinInject(),
    onNavigateToServices: () -> Unit = {},
    onNavigateToUserInterface: () -> Unit = {},
    onNavigateToIntegrations: () -> Unit = {},
    onNavigateToBackupRestore: () -> Unit = {},
    onNavigateToDev: () -> Unit = {},
) {
    val navManager = navigationManager
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showLibrariesSheet by remember { mutableStateOf(false) }
    var confirmShareLastLog by remember { mutableStateOf(false) }
    var showChangelogSheet by remember { mutableStateOf(false) }

    val localNetworkPermissionInfoDismissed by viewModel.localNetworkPermissionInfoDismissed.collectAsStateWithLifecycle()
    val localNetworkPermissionHandler = rememberLocalNetworkPermissionHandler()

    PredictiveBackHandler { progress ->
        try {
            progress.collect { }
            navManager.openDrawer()
        } catch (_: CancellationException) {
            // Gesture cancelled
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = mokoString(MR.strings.settings)) },
                navigationIcon = {
                    NavigationDrawerButton()
                },
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
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
                !localNetworkPermissionHandler.isGranted() &&
                !localNetworkPermissionInfoDismissed
            ) {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Default.WifiOff, null)
                            Text(
                                text = mokoString(MR.strings.local_network_denied_title),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = { viewModel.dismissLocalNetworkPermissionInfo() },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                        Text(
                            text = mokoString(MR.strings.local_network_denied_description),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Button(
                            onClick = {
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                ),
                        ) {
                            Text(mokoString(MR.strings.open_settings))
                        }
                    }
                }
            }

            SettingsGroup(
                items =
                    listOf(
                        SettingItem(
                            icon = IconSource.Vector(Hard_drive),
                            title = mokoString(MR.strings.services),
                            subtitle = mokoString(MR.strings.services_description),
                            onClick = onNavigateToServices,
                            trailingContent = {
                                Icon(Icons.Default.ChevronRight, null)
                            },
                        ),
                        SettingItem(
                            icon = IconSource.Vector(Icons.Default.Palette),
                            title = mokoString(MR.strings.user_interface),
                            subtitle = mokoString(MR.strings.user_interface_description),
                            onClick = onNavigateToUserInterface,
                            trailingContent = {
                                Icon(Icons.Default.ChevronRight, null)
                            },
                        ),
                        SettingItem(
                            icon = IconSource.Vector(Icons.Default.Share),
                            title = mokoString(MR.strings.integrations),
                            subtitle = mokoString(MR.strings.integrations_description),
                            onClick = onNavigateToIntegrations,
                            trailingContent = {
                                Icon(Icons.Default.ChevronRight, null)
                            },
                        ),
                        SettingItem(
                            icon = IconSource.Vector(Icons.Default.Restore),
                            title = mokoString(MR.strings.backup_restore),
                            subtitle = mokoString(MR.strings.backup_restore_description),
                            onClick = onNavigateToBackupRestore,
                            trailingContent = {
                                Icon(Icons.Default.ChevronRight, null)
                            },
                        ),
                    ),
            )

            AboutCard(
                onChangelogClick = {
                    showChangelogSheet = true
                },
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
                modifier = Modifier.padding(top = 12.dp),
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
                        onNavigateToDev()
                    },
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Text(
                        text = "Development Settings",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            }
        }

        if (showLibrariesSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLibrariesSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            ) {
                val libraries by produceState<Libs?>(null) {
                    value =
                        withContext(Dispatchers.IO) {
                            Libs.Builder().withContext(context).build()
                        }
                }
                LibrariesContainer(
                    libraries = libraries,
                    modifier = Modifier.fillMaxSize(),
                    colors =
                        LibraryDefaults.libraryColors(
                            libraryBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    padding =
                        LibraryDefaults.libraryPadding(
                            licenseDialogContentPadding = 16.dp,
                        ),
                    header = {
                        item {
                            Text(
                                text = mokoString(MR.strings.libraries),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                        }
                    },
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
                        },
                    ) { Text(mokoString(MR.strings.yes)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            confirmShareLastLog = false
                            context.openLink(moko.getString(MR.strings.bug_report_link))
                        },
                    ) { Text(mokoString(MR.strings.no)) }
                },
                title = { Text(mokoString(MR.strings.share_crash_log)) },
                text = {
                    Text(mokoString(MR.strings.share_crash_log_message))
                },
            )
        }

        if (showChangelogSheet) {
            ReleaseNotesSheet {
                showChangelogSheet = false
            }
        }
    }
}
