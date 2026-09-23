package com.dnfapps.arrmatey.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.client.LoggerLevel
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.logging.LogReader
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.io.File

private const val MAX_LOG_PREVIEW_CHARS = 30_000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevSettingsScreen(
    preferenceStore: PreferencesStore = koinInject<PreferencesStore>(),
    onNavigateToOnboarding: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current

    val showInfoCardMap by preferenceStore.showInfoCards.collectAsStateWithLifecycle(emptyMap())
    val logLevel by preferenceStore.httpLogLevel.collectAsStateWithLifecycle(LoggerLevel.Headers)

    val logsScrollState = rememberScrollState()
    val logsFlow =
        remember {
            flow {
                while (currentCoroutineContext().isActive) {
                    val logs =
                        withContext(Dispatchers.IO) {
                            try {
                                val logFilePath = LogReader.getLogFilePath()
                                val logFile = File(logFilePath)
                                if (logFile.exists()) {
                                    val text = logFile.readText()
                                    if (text.length > MAX_LOG_PREVIEW_CHARS) {
                                        "...[truncated]\n" + text.takeLast(MAX_LOG_PREVIEW_CHARS)
                                    } else {
                                        text.ifEmpty { "NO LOGS" }
                                    }
                                } else {
                                    "No logs found"
                                }
                            } catch (e: Exception) {
                                "Error reading logs: ${e.message}"
                            }
                        }
                    emit(logs)
                    delay(10_000L)
                }
            }.flowOn(Dispatchers.IO)
        }
    val logContent by logsFlow.collectAsStateWithLifecycle("Loading logs...")

    LaunchedEffect(logContent) {
        if (logContent != "Loading logs...") {
            logsScrollState.scrollTo(logsScrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.developer_settings)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(MR.strings.back),
                        )
                    }
                },
            )
        },
    ) { pv ->
        Box(modifier = Modifier.padding(pv)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
            ) {
                ContainerCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InstanceType.entries.forEach { instanceType ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .toggleable(
                                            value = showInfoCardMap[instanceType] ?: true,
                                            onValueChange = { preferenceStore.setInfoCardVisibility(instanceType, it) },
                                        ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = mokoString(MR.strings.show_instance_info_card, instanceType.name),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Switch(
                                    checked = showInfoCardMap[instanceType] ?: true,
                                    onCheckedChange = null,
                                )
                            }
                        }
                    }
                }

                DropdownPicker(
                    options = LoggerLevel.entries,
                    selectedOption = logLevel,
                    onOptionSelected = { preferenceStore.setLogLevel(it) },
                    label = { Text(mokoString(MR.strings.http_logging_level)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedButton(
                    onClick = onNavigateToOnboarding,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(mokoString(MR.strings.dev_settings_launch_onboarding))
                }

                Box(
                    modifier =
                        Modifier
                            .height(250.dp)
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                ) {
                    SelectionContainer {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(logsScrollState)
                                    .horizontalScroll(rememberScrollState())
                                    .padding(16.dp),
                        ) {
                            Text(
                                text = logContent,
                                style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                    ),
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Button(
                    onClick = { shareLogs(context) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(mokoString(MR.strings.share_logs))
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

        val uri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                logFile,
            )

        val shareIntent =
            Intent(Intent.ACTION_SEND).apply {
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
