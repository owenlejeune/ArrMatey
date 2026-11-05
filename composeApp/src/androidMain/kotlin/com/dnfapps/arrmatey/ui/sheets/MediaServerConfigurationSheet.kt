package com.dnfapps.arrmatey.ui.sheets

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.mediaserver.client.PlexConnectionUtils
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.PlexServerConnection
import com.dnfapps.arrmatey.mediaserver.model.PlexServerResource
import com.dnfapps.arrmatey.mediaserver.viewmodel.MediaServerAuthMode
import com.dnfapps.arrmatey.mediaserver.viewmodel.MediaServerConfigState
import com.dnfapps.arrmatey.mediaserver.viewmodel.MediaServerSettingsViewModel
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.ConfirmableButton
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.screens.CustomHeaderSection
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaServerConfigurationSheet(
    onDismiss: () -> Unit,
    viewModel: MediaServerSettingsViewModel = koinViewModel(),
) {
    val configState by viewModel.configState.collectAsStateWithLifecycle()

    LaunchedEffect(configState?.saveCompleted) {
        if (configState?.saveCompleted == true) {
            viewModel.closeConfiguration()
            onDismiss()
        }
    }

    configState?.let { state ->
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.closeConfiguration()
                onDismiss()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            MediaServerConfigurationContent(
                state = state,
                viewModel = viewModel,
                onDismiss = {
                    viewModel.closeConfiguration()
                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun MediaServerConfigurationContent(
    state: MediaServerConfigState,
    viewModel: MediaServerSettingsViewModel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val copiedMessage = mokoString(MR.strings.copied_to_clipboard)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(state.serverType.icon),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = mokoString(MR.strings.configure_server, mokoString(state.serverType.resource)),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }

        AMOutlinedTextField(
            value = state.label,
            onValueChange = viewModel::updateLabel,
            label = mokoString(MR.strings.server_name),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        AMOutlinedTextField(
            value = state.url,
            onValueChange = viewModel::updateUrl,
            label = mokoString(MR.strings.server_url),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = when (state.serverType) {
                MediaServerType.Plex -> mokoString(MR.strings.plex_url_placeholder)
                MediaServerType.Jellyfin, MediaServerType.Emby -> mokoString(MR.strings.jellyfin_emby_url_placeholder)
            },
        )

        AuthModeSelector(
            serverType = state.serverType,
            currentMode = state.authMode,
            onModeSelected = { viewModel.updateAuthMode(it) },
        )

        when (state.authMode) {
            MediaServerAuthMode.OAuthOrQuickConnect -> {
                when (state.serverType) {
                    MediaServerType.Plex -> {
                        PlexOAuthSection(
                            state = state,
                            onStartPlexSignIn = {
                                viewModel.startPlexSignIn { url -> context.openLink(url) }
                            },
                            onCancelPlexSignIn = { viewModel.cancelPlexSignIn() },
                            onSelectServer = { viewModel.selectDiscoveredPlexServer(it) },
                            onSelectConnection = { conn, useDirectIp ->
                                viewModel.selectPlexConnection(conn, useDirectIp)
                            },
                            copiedMessage = copiedMessage,
                        )
                    }
                    MediaServerType.Jellyfin -> {
                        JellyfinQuickConnectSection(
                            state = state,
                            onStartQuickConnect = { viewModel.startJellyfinQuickConnect() },
                            onCancelQuickConnect = { viewModel.cancelQuickConnect() },
                        )
                    }
                    MediaServerType.Emby -> {
                        AMOutlinedTextField(
                            value = state.token,
                            onValueChange = { viewModel.updateToken(it) },
                            label = mokoString(MR.strings.manual_token),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                }
            }
            MediaServerAuthMode.UsernamePassword -> {
                UsernamePasswordSection(
                    state = state,
                    onUsernameChanged = { viewModel.updateUsername(it) },
                    onPasswordChanged = { viewModel.updatePassword(it) },
                    onAuthenticate = { viewModel.authenticateCredentials() },
                )
            }
            MediaServerAuthMode.ManualToken -> {
                AMOutlinedTextField(
                    value = state.token,
                    onValueChange = { viewModel.updateToken(it) },
                    label = when (state.serverType) {
                        MediaServerType.Plex -> mokoString(MR.strings.plex_auth_token)
                        else -> mokoString(MR.strings.manual_token)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }

        ConnectionStatusArea(
            state = state,
            onTestConnection = { viewModel.testConnection() },
        )

        HorizontalDivider()

        LocalNetworkSection(
            state = state,
            onLocalNetworkEnabledChanged = { viewModel.updateLocalNetworkEnabled(it) },
            onLocalNetworkEndpointChanged = { viewModel.updateLocalNetworkEndpoint(it) },
            onLocalNetworkSsidsChanged = { viewModel.updateLocalNetworkSsids(it) },
        )

        CustomHeaderSection(
            headers = state.headers,
            onHeadersChanged = { viewModel.updateHeaders(it) },
            localNetworkSsids = state.localNetworkSsids,
            localNetworkConfigured = state.localNetworkEnabled,
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { viewModel.saveConfiguration() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving && state.url.isNotBlank() && state.token.isNotBlank(),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(mokoString(MR.strings.save))
        }

        if (state.existingId > 0) {
            val confirmPrompt = mokoString(MR.strings.disconnect_media_server, mokoString(state.serverType.resource))
            var isConfirming by remember { mutableStateOf(false) }

            ConfirmableButton(
                isConfirming = isConfirming,
                onClick = {
                    if (isConfirming) {
                        viewModel.disconnectServer(state.serverType)
                        onDismiss()
                    } else {
                        isConfirming = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(confirmPrompt)
            }
        }
    }
}

@Composable
private fun AuthModeSelector(
    serverType: MediaServerType,
    currentMode: MediaServerAuthMode,
    onModeSelected: (MediaServerAuthMode) -> Unit,
) {
    val modes = when (serverType) {
        MediaServerType.Plex -> listOf(
            MediaServerAuthMode.OAuthOrQuickConnect to mokoString(MR.strings.auth_type_plex),
            MediaServerAuthMode.ManualToken to mokoString(MR.strings.manual_token),
        )
        MediaServerType.Jellyfin -> listOf(
            MediaServerAuthMode.OAuthOrQuickConnect to mokoString(MR.strings.auth_type_quick_connect),
            MediaServerAuthMode.UsernamePassword to mokoString(MR.strings.auth_type_local),
            MediaServerAuthMode.ManualToken to mokoString(MR.strings.manual_token),
        )
        MediaServerType.Emby -> listOf(
            MediaServerAuthMode.UsernamePassword to mokoString(MR.strings.auth_type_local),
            MediaServerAuthMode.ManualToken to mokoString(MR.strings.manual_token),
        )
    }

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                selected = currentMode == mode,
                onClick = { onModeSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
            ) {
                Text(label, maxLines = 1)
            }
        }
    }
}

@Composable
private fun PlexOAuthSection(
    state: MediaServerConfigState,
    onStartPlexSignIn: () -> Unit,
    onCancelPlexSignIn: () -> Unit,
    onSelectServer: (PlexServerResource) -> Unit,
    onSelectConnection: (PlexServerConnection, Boolean) -> Unit,
    copiedMessage: String,
) {
    val context = LocalContext.current

    if (state.isPlexWaiting) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text(
                        text = mokoString(MR.strings.waiting_for_plex),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Text(
                    text = mokoString(MR.strings.plex_sign_in_instructions),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                state.plexAuthUrl?.let { url ->
                    FilledTonalButton(
                        onClick = { context.openLink(url) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(mokoString(MR.strings.open_page_again))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val linkLabel = mokoString(MR.strings.plex_sign_in_link)
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText(linkLabel, url))
                                Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(mokoString(MR.strings.copy_sign_in_link), maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = onCancelPlexSignIn,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(mokoString(MR.strings.cancel), maxLines = 1)
                        }
                    }
                }
            }
        }
    } else {
        Button(
            onClick = onStartPlexSignIn,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = state.serverType.brandColor,
                contentColor = Color.White,
            ),
        ) {
            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(mokoString(MR.strings.sign_in_with_plex))
        }
    }

    if (state.discoveredPlexServers.isNotEmpty()) {
        val selectedServer = state.discoveredPlexServers.firstOrNull { it.clientIdentifier == state.serverId }

        DropdownPicker(
            options = state.discoveredPlexServers,
            selectedOption = selectedServer,
            onOptionSelected = onSelectServer,
            getOptionLabel = { server ->
                val platform = listOfNotNull(server.platform, server.productVersion).filter { it.isNotBlank() }.joinToString(" ")
                if (platform.isNotBlank()) "${server.name} ($platform)" else server.name
            },
            label = {
                Text(
                    text = mokoString(MR.strings.discovered_plex_servers),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        selectedServer?.let { server ->
            if (server.connections.isNotEmpty()) {
                val endpointOptions = remember(server) {
                    PlexConnectionUtils.sortConnections(server.connections).flatMap { conn ->
                        listOf(
                            PlexEndpointOption(connection = conn, isDirect = true),
                            PlexEndpointOption(connection = conn, isDirect = false),
                        ).filter { it.url.isNotBlank() }
                    }
                }
                val selectedEndpointOption = endpointOptions.firstOrNull { it.url == state.url }

                DropdownPicker(
                    options = endpointOptions,
                    selectedOption = selectedEndpointOption,
                    onOptionSelected = { option ->
                        onSelectConnection(option.connection, option.isDirect)
                    },
                    getOptionLabel = { option -> option.getLabel() },
                    label = {
                        Text(
                            text = mokoString(MR.strings.available_endpoints),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    unknownValueLabel = state.url.ifBlank { mokoString(MR.strings.unknown) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun JellyfinQuickConnectSection(
    state: MediaServerConfigState,
    onStartQuickConnect: () -> Unit,
    onCancelQuickConnect: () -> Unit,
) {
    val qcCode = state.quickConnectCode
    if (state.isQuickConnectWaiting && qcCode != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = mokoString(MR.strings.quick_connect_instructions),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = qcCode,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = state.serverType.brandColor,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text(
                        text = mokoString(MR.strings.quick_connect_waiting),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                OutlinedButton(onClick = onCancelQuickConnect) {
                    Text(mokoString(MR.strings.cancel))
                }
            }
        }
    } else {
        Button(
            onClick = onStartQuickConnect,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.url.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = state.serverType.brandColor,
                contentColor = Color.White,
            ),
        ) {
            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(mokoString(MR.strings.quick_connect_start))
        }
    }
}

@Composable
private fun UsernamePasswordSection(
    state: MediaServerConfigState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onAuthenticate: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AMOutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChanged,
            label = mokoString(MR.strings.username),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        AMOutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            label = mokoString(MR.strings.password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )

        FilledTonalButton(
            onClick = onAuthenticate,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isTesting && state.username.isNotBlank() && state.password.isNotBlank() && state.url.isNotBlank(),
        ) {
            if (state.isTesting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(mokoString(MR.strings.sign_in))
        }
    }
}

@Composable
private fun ConnectionStatusArea(
    state: MediaServerConfigState,
    onTestConnection: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = onTestConnection,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isTesting && state.url.isNotBlank() && state.token.isNotBlank(),
        ) {
            if (state.isTesting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            } else {
                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(mokoString(MR.strings.test_connection))
        }

        AnimatedVisibility(visible = state.testSuccess == true) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(
                        text = mokoString(MR.strings.connection_success),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        AnimatedVisibility(visible = state.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(
                        text = state.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalNetworkSection(
    state: MediaServerConfigState,
    onLocalNetworkEnabledChanged: (Boolean) -> Unit,
    onLocalNetworkEndpointChanged: (String) -> Unit,
    onLocalNetworkSsidsChanged: (List<String>) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.local_network_switching),
                sublabel = mokoString(MR.strings.local_network_description),
                checked = state.localNetworkEnabled,
                onCheckedChange = onLocalNetworkEnabledChanged,
            )

            AnimatedVisibility(visible = state.localNetworkEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AMOutlinedTextField(
                        value = state.localNetworkEndpoint,
                        onValueChange = onLocalNetworkEndpointChanged,
                        label = mokoString(MR.strings.local_network_endpoint),
                        placeholder = when (state.serverType) {
                            MediaServerType.Plex -> mokoString(MR.strings.plex_url_placeholder)
                            MediaServerType.Jellyfin, MediaServerType.Emby -> mokoString(MR.strings.jellyfin_emby_url_placeholder)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    AMOutlinedTextField(
                        value = state.localNetworkSsids.joinToString(", "),
                        onValueChange = { input ->
                            val ssids = input.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            onLocalNetworkSsidsChanged(ssids)
                        },
                        label = mokoString(MR.strings.wifi_network_name),
                        placeholder = mokoString(MR.strings.wifi_network_placeholder),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
        }
    }
}

private data class PlexEndpointOption(
    val connection: PlexServerConnection,
    val isDirect: Boolean,
) {
    val url: String = if (isDirect) "http://${connection.address}:${connection.port}" else connection.uri

    @Composable
    fun getLabel(): String {
        val isDocker = PlexConnectionUtils.isDockerBridgeIp(connection.address)
        val location = when {
            isDocker -> mokoString(MR.strings.docker_bridge_unroutable)
            connection.local -> mokoString(MR.strings.lan)
            else -> mokoString(MR.strings.remote)
        }
        val protocol = if (isDirect) mokoString(MR.strings.direct_http) else mokoString(MR.strings.secure_https)
        return "${connection.address}:${connection.port} ($protocol • $location)"
    }
}
