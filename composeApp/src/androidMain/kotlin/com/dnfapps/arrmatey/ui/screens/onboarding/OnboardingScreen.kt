package com.dnfapps.arrmatey.ui.screens.onboarding

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.MoreScreenViewModel
import com.dnfapps.arrmatey.backup.viewmodel.BackupViewModel
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.datastore.TabPreferences
import com.dnfapps.arrmatey.mediaserver.viewmodel.MediaServerSettingsViewModel
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.screens.settings.ImportDialog
import com.dnfapps.arrmatey.ui.screens.settings.ScanQrCodeSheet
import com.dnfapps.arrmatey.ui.sheets.MediaServerConfigurationSheet
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val PAGE_COUNT = 8

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    preferences: PreferencesStore = koinInject(),
    moreViewModel: MoreScreenViewModel = koinViewModel(),
    backupViewModel: BackupViewModel = koinViewModel(),
    mediaServerViewModel: MediaServerSettingsViewModel = koinViewModel(),
    moko: MokoStrings = koinInject(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })

    val instances by moreViewModel.instances.collectAsStateWithLifecycle()
    val downloadClients by moreViewModel.downloadClients.collectAsStateWithLifecycle()
    val configuredServersMap by mediaServerViewModel.configuredServers.collectAsStateWithLifecycle()
    val mediaServers = remember(configuredServersMap) { configuredServersMap.values.toList() }
    val mediaServerConfigState by mediaServerViewModel.configState.collectAsStateWithLifecycle()
    val appTheme by moreViewModel.appTheme.collectAsStateWithLifecycle()
    val appColor by moreViewModel.appColor.collectAsStateWithLifecycle()
    val useServiceNavLogos by moreViewModel.useServiceNavLogos.collectAsStateWithLifecycle()
    val useFloatingNavigationBar by moreViewModel.useFloatingNavigationBar.collectAsStateWithLifecycle()
    val tabPreferences by preferences.tabPreferences.collectAsStateWithLifecycle(TabPreferences())

    var showAddInstanceSheet by remember { mutableStateOf(false) }
    var showAddDownloadClientSheet by remember { mutableStateOf(false) }
    var showSelectMediaServerSheet by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showScanQrSheet by remember { mutableStateOf(false) }
    var pendingImportData by remember { mutableStateOf<String?>(null) }
    var isQrImport by remember { mutableStateOf(false) }

    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                uri?.let {
                    val encryptedData =
                        context.contentResolver.openInputStream(it)?.use { inputStream ->
                            inputStream.readBytes().decodeToString()
                        }
                    if (encryptedData != null) {
                        backupViewModel.resetImportState()
                        pendingImportData = encryptedData
                        showImportDialog = true
                    }
                }
            },
        )

    Scaffold(
        bottomBar = {
            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }) {
                            Text(mokoString(MR.strings.onboarding_back))
                        }
                    } else {
                        TextButton(onClick = onComplete) {
                            Text(mokoString(MR.strings.onboarding_skip))
                        }
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(PAGE_COUNT) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier =
                            Modifier
                                .height(6.dp)
                                .width(if (isSelected) 24.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                ),
                        )
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (pagerState.currentPage < PAGE_COUNT - 1) {
                        Button(onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }) {
                            Text(mokoString(MR.strings.onboarding_next))
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    } else {
                        Button(
                            onClick = onComplete,
                            colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text(mokoString(MR.strings.onboarding_get_started))
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        HorizontalPager(
            userScrollEnabled = false,
            state = pagerState,
            modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> MediaFeaturesPage()
                2 -> PowerFeaturesPage()
                3 ->
                    SetupChoicePage(
                        onManualSetup = {
                            scope.launch {
                                pagerState.animateScrollToPage(4)
                            }
                        },
                        onRestoreBackup = {
                            isQrImport = false
                            importLauncher.launch(arrayOf("application/json"))
                        },
                        onScanQrCode = {
                            showScanQrSheet = true
                        },
                        onSkip = {
                            scope.launch {
                                pagerState.animateScrollToPage(5)
                            }
                        },
                    )

                4 ->
                    InstancesSetupPage(
                        instances = instances,
                        downloadClients = downloadClients,
                        mediaServers = mediaServers,
                        onAddInstance = { showAddInstanceSheet = true },
                        onAddDownloadClient = { showAddDownloadClientSheet = true },
                        onAddMediaServer = { showSelectMediaServerSheet = true },
                    )

                5 ->
                    PreferencesSetupPage(
                        appTheme = appTheme,
                        onThemeChange = { moreViewModel.setAppTheme(it) },
                        appColor = appColor,
                        onColorChange = { moreViewModel.setAppColor(it) },
                    )

                6 ->
                    NavigationSetupPage(
                        useFloatingNavigationBar = useFloatingNavigationBar,
                        onToggleFloatingNavigationBar = { moreViewModel.toggleUseFloatingNavigationBar() },
                        tabPreferences = tabPreferences,
                        onUpdateTabPreferences = { preferences.updateTabPreferences(it) },
                        useServiceNavLogos = useServiceNavLogos,
                        onToggleServiceNavLogos = { moreViewModel.toggleUseServiceNavLogos() },
                    )

                7 ->
                    ReadyPage(
                        instancesCount = instances.size,
                        downloadClientsCount = downloadClients.size,
                        mediaServersCount = mediaServers.size,
                        onFinish = onComplete,
                    )
            }
        }
    }

    if (showAddInstanceSheet) {
        AddInstanceSheet(
            onDismiss = { showAddInstanceSheet = false },
        )
    }

    if (showAddDownloadClientSheet) {
        AddDownloadClientSheet(
            onDismiss = { showAddDownloadClientSheet = false },
        )
    }

    if (showSelectMediaServerSheet) {
        SelectMediaServerTypeSheet(
            onDismiss = { showSelectMediaServerSheet = false },
            onSelectType = { serverType ->
                mediaServerViewModel.openConfiguration(serverType)
            },
        )
    }

    if (mediaServerConfigState?.serverType != null) {
        MediaServerConfigurationSheet(
            onDismiss = { mediaServerViewModel.closeConfiguration() },
            viewModel = mediaServerViewModel,
        )
    }

    if (showScanQrSheet) {
        ScanQrCodeSheet(
            onDismiss = { showScanQrSheet = false },
            onQrScanned = { scannedString ->
                showScanQrSheet = false
                backupViewModel.resetImportState()
                pendingImportData = scannedString
                isQrImport = true
                showImportDialog = true
            },
        )
    }

    if (showImportDialog) {
        val importState by backupViewModel.importUiState.collectAsStateWithLifecycle()

        ImportDialog(
            importState = importState,
            isQrImport = isQrImport,
            onDismiss = {
                showImportDialog = false
                backupViewModel.resetImportState()
                pendingImportData = null
                isQrImport = false
            },
            onPasswordChanged = { backupViewModel.setImportPassword(it) },
            onToggleInstanceSelection = { backupViewModel.toggleImportInstanceSelection(it) },
            onToggleDownloadClientSelection = { backupViewModel.toggleImportDownloadClientSelection(it) },
            onToggleCustomWebpageSelection = { backupViewModel.toggleImportCustomWebpageSelection(it) },
            onToggleImportMediaServerSelection = { backupViewModel.toggleImportMediaServerSelection(it) },
            onToggleImportInstancePreferences = { backupViewModel.toggleImportInstancePreferences() },
            onToggleImportTabPreferences = { backupViewModel.toggleImportTabPreferences() },
            onToggleImportUiPreferences = { backupViewModel.toggleImportUiPreferences() },
            onToggleImportIntegrationsPreferences = { backupViewModel.toggleImportIntegrationsPreferences() },
            onConfirmDecrypt = {
                pendingImportData?.let { data ->
                    if (isQrImport) {
                        backupViewModel.prepareImportFromQr(data)
                    } else {
                        backupViewModel.prepareImport(data)
                    }
                }
            },
            onConfirmImport = {
                backupViewModel.executeImport {
                    showImportDialog = false
                    pendingImportData = null
                    isQrImport = false
                    Toast.makeText(context, moko.getString(MR.strings.import_complete), Toast.LENGTH_SHORT).show()
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
        )
    }
}
