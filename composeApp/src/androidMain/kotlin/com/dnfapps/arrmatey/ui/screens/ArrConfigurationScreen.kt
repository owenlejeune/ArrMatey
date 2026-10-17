package com.dnfapps.arrmatey.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.client.DEFAULT_SLOW_TIMEOUT
import com.dnfapps.arrmatey.entensions.openAppSettings
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.instances.model.HeaderRestrictionType
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.model.SeerrAuthType
import com.dnfapps.arrmatey.instances.state.AddInstanceUiState
import com.dnfapps.arrmatey.isDebug
import com.dnfapps.arrmatey.permissions.rememberLocationPermissionHandler
import com.dnfapps.arrmatey.permissions.rememberNotificationPermissionHandler
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledCheckbox
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.getNetworkUtils
import com.dnfapps.arrmatey.utils.isValidUrl
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.thenGet
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrConfigurationScreen(
    instanceType: InstanceType,
    uiState: AddInstanceUiState,
    onApiEndpointChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onNoApiKeyRequiredChanged: (Boolean) -> Unit = {},
    onInstanceLabelChanged: (String) -> Unit,
    onIsSlowInstanceChanged: (Boolean) -> Unit,
    onCustomTimeoutChanged: (Long?) -> Unit,
    onHeadersChanged: (List<InstanceHeader>) -> Unit,
    onLocalNetworkEnabledChanged: (Boolean) -> Unit,
    onLocalNetworkUrlChanged: (String) -> Unit,
    onLocalNetworkSsidChanged: (List<String>) -> Unit,
    onTestLocalConnection: () -> Unit,
    onToggleNotificationsEnabled: () -> Unit,
    onSeerrAuthTypeChanged: (SeerrAuthType) -> Unit = {},
    onLoginLocal: ((String, String) -> Unit)? = null,
    onLoginPlex: ((String) -> Unit)? = null,
    onLoginJellyfin: ((String, String) -> Unit)? = null,
    onStartQuickConnect: (() -> Unit)? = null,
    onCancelQuickConnect: (() -> Unit)? = null,
    onPlexTokenChange: ((String) -> Unit)? = null,
    onStartPlexSignIn: ((onOpenUrl: (String) -> Unit) -> Unit)? = null,
    onCancelPlexSignIn: (() -> Unit)? = null,
    onClearAuthSession: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    var localEmail by remember { mutableStateOf("") }
    var localPassword by remember { mutableStateOf("") }
    var jellyfinUsername by remember { mutableStateOf("") }
    var jellyfinPassword by remember { mutableStateOf("") }

    val apiEndpoint = uiState.apiEndpoint
    val apiKey = uiState.apiKey
    val instanceLabel = uiState.instanceLabel

    val endpointError = uiState.endpointError
    val isTesting = uiState.testing
    val testResult = uiState.testResult

    val isSlowInstance = uiState.isSlowInstance
    val customTimeout = uiState.customTimeout
    val headers = uiState.headers

    val createResult = uiState.createResult

    val hasLabelConflict = uiState.hasLabelConflict
    val hasUrlConflict = uiState.hasUrlConflict

    val notificationPermissionHandler =
        rememberNotificationPermissionHandler(
            onGranted = { onToggleNotificationsEnabled() },
        )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AnimatedVisibility(
            visible = testResult == false,
        ) {
            Card(
                colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Column {
                        Text(
                            text = mokoString(MR.strings.connection_failed),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
        }
        AMOutlinedTextField(
            label = mokoString(MR.strings.label),
            value = instanceLabel,
            onValueChange = onInstanceLabelChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = instanceType.toString(),
            singleLine = true,
            isError = hasLabelConflict,
            errorMessage = hasLabelConflict thenGet mokoString(MR.strings.instance_label_exists),
        )

        AMOutlinedTextField(
            label = mokoString(MR.strings.host),
            required = true,
            value = apiEndpoint,
            onValueChange = onApiEndpointChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = mokoString(MR.strings.host_placeholder) + "${instanceType.defaultPort}",
            description = mokoString(MR.strings.host_description, instanceType.name),
            singleLine = true,
            isError = endpointError || hasUrlConflict,
            errorMessage =
            when {
                endpointError -> mokoString(MR.strings.invalid_host)
                hasUrlConflict -> mokoString(MR.strings.instance_url_exists)
                else -> null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        )

        if (instanceType == InstanceType.Seerr) {
            DropdownPicker(
                modifier = Modifier.fillMaxWidth(),
                options = SeerrAuthType.entries,
                selectedOption = uiState.seerrAuthType,
                onOptionSelected = onSeerrAuthTypeChanged,
                label = { Text(mokoString(MR.strings.auth_type)) },
                getOptionLabel = { type ->
                    when (type) {
                        SeerrAuthType.ApiKey -> mokoString(MR.strings.auth_type_api_key)
                        SeerrAuthType.Local -> mokoString(MR.strings.auth_type_local)
                        SeerrAuthType.Plex -> mokoString(MR.strings.auth_type_plex)
                        SeerrAuthType.Jellyfin -> mokoString(MR.strings.auth_type_jellyfin)
                        SeerrAuthType.JellyfinQuickConnect -> mokoString(MR.strings.auth_type_quick_connect)
                    }
                },
            )
        }

        if (instanceType != InstanceType.Seerr || uiState.seerrAuthType == SeerrAuthType.ApiKey) {
            LabelledCheckbox(
                label = mokoString(MR.strings.no_api_key),
                checked = uiState.noApiKeyRequired,
                onCheckedChange = onNoApiKeyRequiredChanged,
            )

            AMOutlinedTextField(
                label = mokoString(MR.strings.api_key),
                required = !uiState.noApiKeyRequired,
                value = apiKey,
                onValueChange = onApiKeyChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = mokoString(MR.strings.api_key_placeholder),
                singleLine = true,
                enabled = !uiState.noApiKeyRequired,
            )

            instanceType.getApiKeyEndpoint?.let { getApiKey ->
                val enabled = uiState.apiEndpoint.isValidUrl()
                val color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier =
                    Modifier.clickable(
                        enabled = enabled,
                    ) {
                        val fullUrl = "${uiState.apiEndpoint}/$getApiKey"
                        context.openLink(fullUrl)
                    },
                ) {
                    Text(
                        text = mokoString(MR.strings.api_key_in_browser),
                        textDecoration = TextDecoration.Underline,
                        style = MaterialTheme.typography.bodySmall,
                        color = color,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = color,
                    )
                }
            }
        } else {
            if (uiState.sessionToken.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = uiState.authenticatedUserDisplayName?.let {
                                        mokoString(MR.strings.signed_in_as, it)
                                    } ?: mokoString(MR.strings.authenticated_session),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { onClearAuthSession?.invoke() },
                        ) {
                            Text(mokoString(MR.strings.sign_out))
                        }
                    }
                }
            } else {
                uiState.authError?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                when (uiState.seerrAuthType) {
                    SeerrAuthType.Local -> {
                        AMOutlinedTextField(
                            label = mokoString(MR.strings.email),
                            value = localEmail,
                            onValueChange = { localEmail = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        )
                        AMOutlinedTextField(
                            label = mokoString(MR.strings.password),
                            value = localPassword,
                            onValueChange = { localPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        )
                        Button(
                            onClick = { onLoginLocal?.invoke(localEmail, localPassword) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isAuthenticating && localEmail.isNotBlank() && localPassword.isNotBlank() && uiState.apiEndpoint.isValidUrl(),
                        ) {
                            if (uiState.isAuthenticating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(mokoString(MR.strings.sign_in))
                            }
                        }
                    }
                    SeerrAuthType.Plex -> {
                        val copiedMessage = mokoString(MR.strings.copied_to_clipboard)

                        if (uiState.isPlexWaiting) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
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

                                    uiState.plexAuthUrl?.let { url ->
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
                                            OutlinedButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("Plex Sign-in Link", url))
                                                    Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Link,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = mokoString(MR.strings.copy_link),
                                                    maxLines = 1,
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = { onCancelPlexSignIn?.invoke() },
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Text(
                                                    text = mokoString(MR.strings.cancel),
                                                    maxLines = 1,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    onStartPlexSignIn?.invoke { url ->
                                        context.openLink(url)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isAuthenticating,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Login,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(mokoString(MR.strings.sign_in_with_plex))
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        ) {
                            HorizontalDivider(Modifier.weight(1f))
                            Text(
                                text = mokoString(MR.strings.or),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            HorizontalDivider(Modifier.weight(1f))
                        }

                        AMOutlinedTextField(
                            label = mokoString(MR.strings.plex_auth_token),
                            value = uiState.plexToken,
                            onValueChange = { onPlexTokenChange?.invoke(it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Button(
                            onClick = { onLoginPlex?.invoke(uiState.plexToken) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isAuthenticating && uiState.plexToken.isNotBlank() && uiState.apiEndpoint.isValidUrl(),
                        ) {
                            if (uiState.isAuthenticating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(mokoString(MR.strings.sign_in))
                            }
                        }
                    }
                    SeerrAuthType.Jellyfin -> {
                        AMOutlinedTextField(
                            label = mokoString(MR.strings.username),
                            value = jellyfinUsername,
                            onValueChange = { jellyfinUsername = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        AMOutlinedTextField(
                            label = mokoString(MR.strings.password),
                            value = jellyfinPassword,
                            onValueChange = { jellyfinPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        )
                        Button(
                            onClick = { onLoginJellyfin?.invoke(jellyfinUsername, jellyfinPassword) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isAuthenticating && jellyfinUsername.isNotBlank() && jellyfinPassword.isNotBlank() && uiState.apiEndpoint.isValidUrl(),
                        ) {
                            if (uiState.isAuthenticating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(mokoString(MR.strings.sign_in))
                            }
                        }
                    }
                    SeerrAuthType.JellyfinQuickConnect -> {
                        val qcCode = uiState.quickConnectCode
                        if (qcCode != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        text = mokoString(MR.strings.quick_connect_instructions),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = qcCode,
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
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
                                    OutlinedButton(
                                        onClick = { onCancelQuickConnect?.invoke() },
                                    ) {
                                        Text(mokoString(MR.strings.cancel))
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { onStartQuickConnect?.invoke() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isAuthenticating && uiState.apiEndpoint.isValidUrl(),
                            ) {
                                if (uiState.isAuthenticating) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(mokoString(MR.strings.quick_connect_start))
                                }
                            }
                        }
                    }
                    SeerrAuthType.ApiKey -> Unit
                }
            }
        }

        if (instanceType.supportsNotifications) {
            Section {
                LabelledSwitch(
                    label = mokoString(MR.strings.enable_notifications),
                    sublabel = mokoString(MR.strings.enable_notifications_description),
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = {
                        if (!uiState.notificationsEnabled && it) {
                            notificationPermissionHandler.requestPermission()
                        } else {
                            onToggleNotificationsEnabled()
                        }
                    },
                )
            }
        }

        LocalNetworkArea(
            defaultPort = instanceType.defaultPort,
            uiState,
            onLocalNetworkEnabledChanged,
            onLocalNetworkUrlChanged,
            onLocalNetworkSsidChanged = { ssids ->
                onLocalNetworkSsidChanged(ssids)
            },
            onTestLocalConnection,
        )

        CustomTimeoutArea(isSlowInstance, customTimeout, onIsSlowInstanceChanged, onCustomTimeoutChanged)

        CustomHeaderSection(
            localNetworkSsids = uiState.localNetworkSsids,
            localNetworkConfigured = uiState.localNetworkConfigured,
            headers = headers,
            onHeadersChanged = onHeadersChanged,
        )
    }
}

@Composable
private fun HeadersEditor(
    localNetworkConfigured: Boolean,
    localNetworkSsids: List<String>,
    headers: List<InstanceHeader>,
    onHeadersChanged: (List<InstanceHeader>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var headersList by remember { mutableStateOf(headers) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        headersList.forEachIndexed { index, header ->
            HeaderItem(
                localNetworkSsids = localNetworkSsids,
                localNetworkConfigured = localNetworkConfigured,
                header = header,
                onHeaderChanged = { newHeader ->
                    val updated =
                        headersList.toMutableList().apply {
                            set(index, newHeader)
                        }
                    headersList = updated
                    onHeadersChanged(updated)
                },
                onDelete = {
                    val updated =
                        headersList.toMutableList().apply {
                            removeAt(index)
                        }
                    headersList = updated
                    onHeadersChanged(updated)
                },
            )
            HorizontalDivider()
        }

        OutlinedButton(
            onClick = {
                val updated = headersList + InstanceHeader("", "")
                headersList = updated
                onHeadersChanged(updated)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(mokoString(MR.strings.add_header))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderItem(
    localNetworkConfigured: Boolean,
    localNetworkSsids: List<String>,
    header: InstanceHeader,
    onHeaderChanged: (InstanceHeader) -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AMOutlinedTextField(
                value = header.key,
                onValueChange = { onHeaderChanged(header.copy(key = it)) },
                label = mokoString(MR.strings.header_name),
                modifier = Modifier.weight(1f),
                placeholder = "X-Custom-Header",
                singleLine = true,
            )

            AMOutlinedTextField(
                value = header.value,
                onValueChange = { onHeaderChanged(header.copy(value = it)) },
                label = mokoString(MR.strings.header_value),
                modifier = Modifier.weight(1f),
                placeholder = "value",
                singleLine = true,
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(top = 18.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = mokoString(MR.strings.delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }

        if (localNetworkConfigured) {
            var expanded by remember { mutableStateOf(false) }
            val iconRotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "iconRotation",
            )

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        text =
                        when (header.restrictionType) {
                            HeaderRestrictionType.Always -> "Always"
                            HeaderRestrictionType.RemoteOnly -> "Remote Only"
                            HeaderRestrictionType.SpecificSsids -> {
                                if (header.restrictedSsids.isEmpty()) {
                                    "Select SSIDs"
                                } else {
                                    header.restrictedSsids.joinToString(", ")
                                }
                            }
                        },
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.ExpandCircleDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(iconRotation),
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    HeaderRestrictionType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text =
                                    when (type) {
                                        HeaderRestrictionType.Always -> "Always"
                                        HeaderRestrictionType.RemoteOnly -> "Remote Only"
                                        HeaderRestrictionType.SpecificSsids -> "Specific SSIDs"
                                    },
                                )
                            },
                            onClick = {
                                onHeaderChanged(header.copy(restrictionType = type))
                                if (type != HeaderRestrictionType.SpecificSsids) {
                                    expanded = false
                                }
                            },
                        )
                    }
                }
            }

            if (header.restrictionType == HeaderRestrictionType.SpecificSsids) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    localNetworkSsids.forEach { ssid ->
                        val selected = header.restrictedSsids.contains(ssid) || localNetworkSsids.size == 1
                        ElevatedFilterChip(
                            selected = selected,
                            onClick = {
                                if (localNetworkSsids.size > 1) {
                                    val newList =
                                        if (selected) {
                                            header.restrictedSsids - ssid
                                        } else {
                                            header.restrictedSsids + ssid
                                        }
                                    onHeaderChanged(header.copy(restrictedSsids = newList))
                                }
                            },
                            label = { Text(ssid) },
                            leadingIcon =
                            if (selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Done,
                                        contentDescription = "Done icon",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomTimeoutArea(
    isSlowInstance: Boolean,
    customTimeout: Long?,
    onIsSlowInstanceChanged: (Boolean) -> Unit,
    onCustomTimeoutChanged: (Long?) -> Unit,
) {
    Section {
        LabelledSwitch(
            label = mokoString(MR.strings.slow_instance),
            checked = isSlowInstance,
            onCheckedChange = onIsSlowInstanceChanged,
        )

        AMOutlinedTextField(
            value = customTimeout?.toString() ?: "",
            onValueChange = { onCustomTimeoutChanged(it.toLongOrNull()) },
            modifier = Modifier.fillMaxWidth(),
            label = mokoString(MR.strings.custom_timeout_seconds),
            enabled = isSlowInstance,
            placeholder = DEFAULT_SLOW_TIMEOUT.toString(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@Composable
fun CustomHeaderSection(
    headers: List<InstanceHeader>,
    onHeadersChanged: (List<InstanceHeader>) -> Unit,
    localNetworkSsids: List<String> = emptyList(),
    localNetworkConfigured: Boolean = false,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = mokoString(MR.strings.custom_headers),
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = mokoString(MR.strings.custom_headers_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (headers.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            HeadersEditor(
                localNetworkSsids = localNetworkSsids,
                localNetworkConfigured = localNetworkConfigured,
                headers = headers,
                onHeadersChanged = onHeadersChanged,
            )
        }
    }
}

@Composable
fun LocalNetworkArea(
    defaultPort: Int,
    uiState: AddInstanceUiState,
    onLocalNetworkEnabledChanged: (Boolean) -> Unit,
    onLocalNetworkUrlChanged: (String) -> Unit,
    onLocalNetworkSsidChanged: (List<String>) -> Unit,
    onTestLocalConnection: () -> Unit,
    moko: MokoStrings = koinInject(),
) {
    val context = LocalContext.current
    val locationDeniedString = mokoString(MR.strings.location_denied)
    val ssidGetErrorString = mokoString(MR.strings.ssid_get_error)

    val locationPermissionHandler =
        rememberLocationPermissionHandler(
            onDenied = {
                Toast.makeText(context, locationDeniedString, Toast.LENGTH_SHORT).show()
            },
        )

    Card(
        shape = MaterialTheme.shapes.large,
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.local_network_switching),
                sublabel = mokoString(MR.strings.local_network_description),
                checked = uiState.localNetworkEnabled,
                onCheckedChange = {
                    onLocalNetworkEnabledChanged(it)
                    if (it) {
                        locationPermissionHandler.checkAndPerformAction()
                    }
                },
            )

            AnimatedVisibility(visible = uiState.localNetworkEnabled && !locationPermissionHandler.isGranted()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(mokoString(MR.strings.location_denied_message))
                    FilledTonalButton(
                        onClick = {
                            context.openAppSettings()
                            onLocalNetworkEnabledChanged(false)
                        },
                    ) {
                        Text(mokoString(MR.strings.open_location_permissions))
                    }
                }
            }

            AnimatedVisibility(visible = uiState.localNetworkEnabled && locationPermissionHandler.isGranted()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isDebug()) {
                        getNetworkUtils().getCurrentWifiSsid()?.let { ssid ->
                            Text(
                                text = mokoString(MR.strings.current_network, ssid),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    AMOutlinedTextField(
                        value = uiState.localNetworkUrl,
                        onValueChange = onLocalNetworkUrlChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = mokoString(MR.strings.local_network_url),
                        placeholder = "http://192.168.1.100:$defaultPort",
                        enabled = uiState.localNetworkEnabled,
                        singleLine = true,
                        isError = uiState.localNetworkUrlError,
                        errorMessage =
                        if (uiState.localNetworkUrlError) {
                            mokoString(MR.strings.invalid_url)
                        } else {
                            null
                        },
                    )

                    AMOutlinedTextField(
                        value = uiState.localNetworkSsids.joinToString(", "),
                        onValueChange = { value ->
                            onLocalNetworkSsidChanged(value.split(",").map { it.trim() }.filter { it.isNotBlank() })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = mokoString(MR.strings.wifi_network_name),
                        placeholder = "MyHomeWiFi, MyHomeWiFi_5G",
                        description = mokoString(MR.strings.wifi_ssid_description),
                        enabled = uiState.localNetworkEnabled,
                        singleLine = true,
                    )

                    OutlinedButton(
                        onClick = {
                            if (locationPermissionHandler.isGranted()) {
                                getNetworkUtils().getCurrentWifiSsid()?.let { ssid ->
                                    val currentSsids = uiState.localNetworkSsids.toMutableList()
                                    if (!currentSsids.contains(ssid)) {
                                        currentSsids.add(ssid)
                                        onLocalNetworkSsidChanged(currentSsids)
                                    }
                                } ?: run {
                                    Toast.makeText(context, ssidGetErrorString, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                locationPermissionHandler.checkAndPerformAction()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.WifiFind, null)
                        Text(mokoString(MR.strings.use_current_network), modifier = Modifier.padding(start = 6.dp))
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = onTestLocalConnection,
                            enabled =
                            !uiState.localTesting &&
                                uiState.localNetworkUrl.isNotBlank() &&
                                (uiState.noApiKeyRequired || uiState.apiKey.isNotBlank()),
                        ) {
                            if (uiState.localTesting) {
                                CircularProgressIndicator(
                                    modifier =
                                    Modifier
                                        .padding(end = 8.dp)
                                        .size(16.dp),
                                    strokeWidth = 2.dp,
                                )
                            }
                            Text(text = mokoString(MR.strings.test_local_connection))
                        }

                        uiState.localTestResult?.let { result ->
                            Surface(
                                shape = CircleShape,
                                color =
                                if (result) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.errorContainer
                                },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    if (result) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = mokoString(MR.strings.success),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = mokoString(MR.strings.failure),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Section(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}
