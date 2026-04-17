package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandCircleDown
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.client.DEFAULT_SLOW_TIMEOUT
import com.dnfapps.arrmatey.database.dao.ConflictField
import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.entensions.openAppSettings
import com.dnfapps.arrmatey.instances.model.HeaderRestrictionType
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.state.AddInstanceUiState
import com.dnfapps.arrmatey.isDebug
import com.dnfapps.arrmatey.permissions.rememberLocationPermissionHandler
import com.dnfapps.arrmatey.permissions.rememberNotificationPermissionHandler
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.getNetworkUtils
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
    onInstanceLabelChanged: (String) -> Unit,
    onIsSlowInstanceChanged: (Boolean) -> Unit,
    onCustomTimeoutChanged: (Long?) -> Unit,
    onHeadersChanged: (List<InstanceHeader>) -> Unit,
    onTestConnection: () -> Unit,
    onLocalNetworkEnabledChanged: (Boolean) -> Unit,
    onLocalNetworkUrlChanged: (String) -> Unit,
    onLocalNetworkSsidChanged: (List<String>) -> Unit,
    onTestLocalConnection: () -> Unit,
    onToggleNotificationsEnabled: () -> Unit
) {
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

    val hasLabelConflict = remember(createResult) {
        (createResult as? InsertResult.Conflict)
            ?.fields
            ?.contains(ConflictField.InstanceLabel) == true
    }

    val hasUrlConflict = remember(createResult) {
        (createResult as? InsertResult.Conflict)
            ?.fields
            ?.contains(ConflictField.InstanceUrl) == true
    }

    val notificationPermissionHandler = rememberNotificationPermissionHandler(
        onGranted = { onToggleNotificationsEnabled() }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AMOutlinedTextField(
            label = mokoString(MR.strings.label),
            value = instanceLabel,
            onValueChange = onInstanceLabelChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = instanceType.toString(),
            singleLine = true,
            isError = hasLabelConflict,
            errorMessage = hasLabelConflict thenGet mokoString(MR.strings.instance_label_exists)
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
            errorMessage = when {
                endpointError -> mokoString(MR.strings.invalid_host)
                hasUrlConflict -> mokoString(MR.strings.instance_url_exists)
                else -> null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )

        AMOutlinedTextField(
            label = mokoString(MR.strings.api_key),
            required = true,
            value = apiKey,
            onValueChange = onApiKeyChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = mokoString(MR.strings.api_key_placeholder),
            singleLine = true
        )

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
                }
            )
        }

        LocalNetworkArea(
            instanceType,
            uiState,
            onLocalNetworkEnabledChanged,
            onLocalNetworkUrlChanged,
            onLocalNetworkSsidChanged = { ssids ->
                onLocalNetworkSsidChanged(ssids)
            },
            onTestLocalConnection
        )

        CustomTimeoutArea(isSlowInstance, customTimeout, onIsSlowInstanceChanged, onCustomTimeoutChanged)

        CustomHeaderSection(
            localNetworkSsids = uiState.localNetworkSsids,
            localNetworkConfigured = uiState.localNetworkConfigured,
            headers = headers,
            onHeadersChanged = onHeadersChanged
        )

        TestConnectionSection(
            isTesting = isTesting,
            testButtonEnabled = !isTesting && apiEndpoint.isNotBlank() && apiKey.isNotBlank(),
            testResult = testResult,
            onTestConnection = onTestConnection
        )
    }
}

@Composable
private fun HeadersEditor(
    localNetworkConfigured: Boolean,
    localNetworkSsids: List<String>,
    headers: List<InstanceHeader>,
    onHeadersChanged: (List<InstanceHeader>) -> Unit,
    modifier: Modifier = Modifier
) {
    var headersList by remember { mutableStateOf(headers) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        headersList.forEachIndexed { index, header ->
            HeaderItem(
                localNetworkSsids = localNetworkSsids,
                localNetworkConfigured = localNetworkConfigured,
                header = header,
                onHeaderChanged = { newHeader ->
                    val updated = headersList.toMutableList().apply {
                        set(index, newHeader)
                    }
                    headersList = updated
                    onHeadersChanged(updated)
                },
                onDelete = {
                    val updated = headersList.toMutableList().apply {
                        removeAt(index)
                    }
                    headersList = updated
                    onHeadersChanged(updated)
                }
            )
            HorizontalDivider()
        }

        OutlinedButton(
            onClick = {
                val updated = headersList + InstanceHeader("", "")
                headersList = updated
                onHeadersChanged(updated)
            },
            modifier = Modifier.fillMaxWidth()
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
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AMOutlinedTextField(
                value = header.key,
                onValueChange = { onHeaderChanged(header.copy(key = it)) },
                label = mokoString(MR.strings.header_name),
                modifier = Modifier.weight(1f),
                placeholder = "X-Custom-Header",
                singleLine = true
            )

            AMOutlinedTextField(
                value = header.value,
                onValueChange = { onHeaderChanged(header.copy(value = it)) },
                label = mokoString(MR.strings.header_value),
                modifier = Modifier.weight(1f),
                placeholder = "value",
                singleLine = true
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(top = 18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = mokoString(MR.strings.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        if (localNetworkConfigured) {
            var expanded by remember { mutableStateOf(false) }
            val iconRotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "iconRotation"
            )

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = when (header.restrictionType) {
                            HeaderRestrictionType.Always -> "Always"
                            HeaderRestrictionType.RemoteOnly -> "Remote Only"
                            HeaderRestrictionType.SpecificSsids -> {
                                if (header.restrictedSsids.isEmpty()) "Select SSIDs"
                                else header.restrictedSsids.joinToString(", ")
                            }
                        }
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.ExpandCircleDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(iconRotation)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    HeaderRestrictionType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = when (type) {
                                        HeaderRestrictionType.Always -> "Always"
                                        HeaderRestrictionType.RemoteOnly -> "Remote Only"
                                        HeaderRestrictionType.SpecificSsids -> "Specific SSIDs"
                                    }
                                )
                            },
                            onClick = {
                                onHeaderChanged(header.copy(restrictionType = type))
                                if (type != HeaderRestrictionType.SpecificSsids) {
                                    expanded = false
                                }
                            }
                        )
                    }
                }
            }

            if (header.restrictionType == HeaderRestrictionType.SpecificSsids) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    localNetworkSsids.forEach { ssid ->
                        val selected = header.restrictedSsids.contains(ssid) || localNetworkSsids.size == 1
                        ElevatedFilterChip(
                            selected = selected,
                            onClick = {
                                if (localNetworkSsids.size > 1) {
                                    val newList = if (selected) {
                                        header.restrictedSsids - ssid
                                    } else {
                                        header.restrictedSsids + ssid
                                    }
                                    onHeaderChanged(header.copy(restrictedSsids = newList))
                                }
                            },
                            label = { Text(ssid) },
                            leadingIcon = if (selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Done,
                                        contentDescription = "Done icon",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
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
fun TestConnectionSection(
    isTesting: Boolean,
    testButtonEnabled: Boolean,
    testResult: Boolean?,
    onTestConnection: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = onTestConnection,
                enabled = testButtonEnabled
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp).size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(text = mokoString(MR.strings.test))
            }

            testResult?.let { result ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (result) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = mokoString(MR.strings.success),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = mokoString(MR.strings.failure),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
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
    onCustomTimeoutChanged: (Long?) -> Unit
) {
    Section {
        LabelledSwitch(
            label = mokoString(MR.strings.slow_instance),
            checked = isSlowInstance,
            onCheckedChange = onIsSlowInstanceChanged
        )

        AMOutlinedTextField(
            value = customTimeout?.toString() ?: "",
            onValueChange = { onCustomTimeoutChanged(it.toLongOrNull()) },
            modifier = Modifier.fillMaxWidth(),
            label = mokoString(MR.strings.custom_timeout_seconds),
            enabled = isSlowInstance,
            placeholder = DEFAULT_SLOW_TIMEOUT.toString(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = mokoString(MR.strings.custom_headers),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = mokoString(MR.strings.custom_headers_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (headers.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            HeadersEditor(
                localNetworkSsids = localNetworkSsids,
                localNetworkConfigured = localNetworkConfigured,
                headers = headers,
                onHeadersChanged = onHeadersChanged
            )
        }
    }
}

@Composable
fun LocalNetworkArea(
    instanceType: InstanceType,
    uiState: AddInstanceUiState,
    onLocalNetworkEnabledChanged: (Boolean) -> Unit,
    onLocalNetworkUrlChanged: (String) -> Unit,
    onLocalNetworkSsidChanged: (List<String>) -> Unit,
    onTestLocalConnection: () -> Unit,
    moko: MokoStrings = koinInject()
) {
    val context = LocalContext.current
    val locationPermissionHandler = rememberLocationPermissionHandler(
        onDenied = {
            Toast.makeText(context, moko.getString(MR.strings.location_denied), Toast.LENGTH_SHORT).show()
        }
    )

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                }
            )

            AnimatedVisibility(visible = uiState.localNetworkEnabled && !locationPermissionHandler.isGranted()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(mokoString(MR.strings.location_denied_message))
                    FilledTonalButton(
                        onClick = {
                            context.openAppSettings()
                            onLocalNetworkEnabledChanged(false)
                        }
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
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    AMOutlinedTextField(
                        value = uiState.localNetworkUrl,
                        onValueChange = onLocalNetworkUrlChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = mokoString(MR.strings.local_network_url),
                        placeholder = "http://192.168.1.100:${instanceType.defaultPort}",
                        enabled = uiState.localNetworkEnabled,
                        singleLine = true,
                        isError = uiState.localNetworkUrlError,
                        errorMessage = if (uiState.localNetworkUrlError) {
                            mokoString(MR.strings.invalid_url)
                        } else null
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
                        singleLine = true
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
                                    Toast.makeText(context, moko.getString(MR.strings.ssid_get_error), Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.WifiFind, null)
                        Text(mokoString(MR.strings.use_current_network), modifier = Modifier.padding(start = 6.dp))
                    }

                    HorizontalDivider()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onTestLocalConnection,
                            enabled = !uiState.localTesting &&
                                    uiState.localNetworkUrl.isNotBlank() &&
                                    uiState.apiKey.isNotBlank(),
                        ) {
                            if (uiState.localTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            Text(text = mokoString(MR.strings.test_local_connection))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            when (uiState.localTestResult) {
                                true -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = mokoString(MR.strings.success),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                false -> {
                                    Icon(
                                        painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = mokoString(MR.strings.failure),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Section(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}