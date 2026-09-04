package com.dnfapps.arrmatey.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.backup.viewmodel.BackupViewModel
import com.dnfapps.arrmatey.extensions.nowTimestamp
import com.dnfapps.arrmatey.model.IconSource
import com.dnfapps.arrmatey.model.SettingItem
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.SettingsGroup
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreSettingsScreen(
    onBack: () -> Unit,
    backupViewModel: BackupViewModel = koinViewModel(),
    moko: MokoStrings = koinInject(),
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingImportData by remember { mutableStateOf<String?>(null) }

    val exportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json"),
            onResult = { uri ->
                uri?.let {
                    backupViewModel.exportData { encryptedData ->
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write(encryptedData.toByteArray())
                        }
                        Toast.makeText(context, moko.getString(MR.strings.export_ready), Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )

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
                        pendingImportData = encryptedData
                        showImportDialog = true
                    }
                }
            },
        )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = mokoString(MR.strings.backup_restore)) },
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
                title = mokoString(MR.strings.backup_restore),
                items =
                    listOf(
                        SettingItem(
                            icon = IconSource.Vector(Icons.Default.Upload),
                            title = mokoString(MR.strings.backup),
                            subtitle = mokoString(MR.strings.backup_description),
                            onClick = { showExportDialog = true },
                        ),
                        SettingItem(
                            icon = IconSource.Vector(Icons.Default.Download),
                            title = mokoString(MR.strings.restore),
                            subtitle = mokoString(MR.strings.restore_description),
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                        ),
                    ),
            )
        }

        if (showExportDialog) {
            val exportState by backupViewModel.exportUiState.collectAsStateWithLifecycle()

            ExportDialog(
                exportState = exportState,
                onDismiss = { showExportDialog = false },
                onConfirm = {
                    showExportDialog = false
                    exportLauncher.launch("${nowTimestamp()}_ArrMatey_Backup.json")
                },
                onPasswordChanged = { backupViewModel.setExportPassword(it) },
                onToggleIncludeInstancePreferences = { backupViewModel.toggleIncludePreferences() },
                onToggleIncludeTabPreferences = { backupViewModel.toggleIncludeTabPreferences() },
                onToggleIncludeUiPreferences = { backupViewModel.toggleIncludeUiPreferences() },
                onToggleInstanceSelection = { backupViewModel.toggleInstanceSelection(it) },
                onToggleDownloadClientSelection = { backupViewModel.toggleDownloadClientSelection(it) },
            )
        }

        if (showImportDialog) {
            val importState by backupViewModel.importUiState.collectAsStateWithLifecycle()

            ImportDialog(
                importState = importState,
                onDismiss = {
                    showImportDialog = false
                    pendingImportData = null
                },
                onPasswordChanged = { backupViewModel.setImportPassword(it) },
                onToggleInstanceSelection = { backupViewModel.toggleImportInstanceSelection(it) },
                onToggleDownloadClientSelection = { backupViewModel.toggleImportDownloadClientSelection(it) },
                onToggleImportTabPreferences = { backupViewModel.toggleImportTabPreferences() },
                onToggleImportUiPreferences = { backupViewModel.toggleImportUiPreferences() },
                onConfirmDecrypt = {
                    pendingImportData?.let { data ->
                        backupViewModel.prepareImport(data)
                    }
                },
                onConfirmImport = {
                    backupViewModel.executeImport {
                        showImportDialog = false
                        pendingImportData = null
                        Toast.makeText(context, moko.getString(MR.strings.import_complete), Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
    }
}
