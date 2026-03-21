package com.dnfapps.arrmatey.ui.screens

import android.content.Context
import android.content.Intent
import com.dnfapps.arrmatey.shared.MR
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.client.LoggerLevel
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.logging.LogReader
import com.dnfapps.arrmatey.navigation.SettingsNavigation
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.isActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevSettingsScreen(
    preferenceStore: PreferencesStore = koinInject<PreferencesStore>(),
    settingsNav: SettingsNavigation = koinInject<SettingsNavigation>()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val showInfoCardMap by preferenceStore.showInfoCards.collectAsState(emptyMap())
    val activityPollingOn by preferenceStore.enableActivityPolling.collectAsState(true)
    val logLevel by preferenceStore.httpLogLevel.collectAsState(LoggerLevel.Headers)
    val useDynamicTheme by preferenceStore.useDynamicTheme.collectAsStateWithLifecycle(true)
    val useClearLogo by preferenceStore.useClearLogo.collectAsStateWithLifecycle(true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = { settingsNav.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(MR.strings.back)
                        )
                    }
                }
            )
        }
    ) { pv ->
        Box(modifier = Modifier.padding(pv)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(12.dp)
            ) {
                InstanceType.entries.forEach { instanceType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = showInfoCardMap[instanceType] ?: true,
                                onValueChange = { preferenceStore.setInfoCardVisibility(instanceType, it) }
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Show ${instanceType.name} info card"
                        )
                        Switch(
                            checked = showInfoCardMap[instanceType] ?: true,
                            onCheckedChange = null
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = activityPollingOn,
                            onValueChange = { preferenceStore.toggleActivityPolling() }
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable activity polling"
                    )
                    Switch(
                        checked = activityPollingOn,
                        onCheckedChange = null
                    )
                }

                DropdownPicker(
                    options = LoggerLevel.entries,
                    selectedOption = logLevel,
                    onOptionSelected = { preferenceStore.setLogLevel(it) },
                    label = { Text("HTTP Logging Level") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = useDynamicTheme,
                                onValueChange = { preferenceStore.toggleUseDynamicTheme() }
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Use dynamic themeing"
                        )
                        Switch(
                            checked = useDynamicTheme,
                            onCheckedChange = null
                        )
                    }
                }

                val scrollState = rememberScrollState()
                val logsFlow = flow {
                    while(currentCoroutineContext().isActive) {
                        val logs = LogReader.readLogs()
                        emit(logs)
                        delay(10_000L)
                    }
                }
                val logContent by logsFlow.collectAsStateWithLifecycle("Loading...")

                LaunchedEffect(logContent) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }

                Box(
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    SelectionContainer {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .horizontalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(
                                text = logContent.takeUnless { it.isEmpty() } ?: "NO LOGS",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                Button(onClick = { shareLogs(context) }) {
                    Text("Share logs")
                }
            }
        }
    }
}

fun shareLogs(context: Context) {
    try {
        val logFile = File(LogReader.getLogFilePath())

        if (!logFile.exists()) {
            return
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            logFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "ArrMatey Application Logs")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Logs"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}