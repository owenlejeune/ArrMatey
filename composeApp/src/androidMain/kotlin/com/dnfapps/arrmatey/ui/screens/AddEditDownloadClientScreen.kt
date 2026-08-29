package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientConflictField
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientConfigurationUiState
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientMutationState
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientSettingsViewModel
import com.dnfapps.arrmatey.entensions.openAppSettings
import com.dnfapps.arrmatey.isDebug
import com.dnfapps.arrmatey.permissions.rememberLocationPermissionHandler
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledCheckbox
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.getNetworkUtils
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import com.dnfapps.arrmatey.utils.thenGet
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDownloadClientScreen(
    clientId: Long? = null,
    viewModel: DownloadClientSettingsViewModel = koinInjectParams(clientId),
    onBack: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var confirmDelete by remember { mutableStateOf(false) }
    var conflictFields by remember { mutableStateOf<List<DownloadClientConflictField>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.resetMutationState()
    }

    LaunchedEffect(uiState.mutationState) {
        when (val state = uiState.mutationState) {
            is DownloadClientMutationState.Success -> {
                viewModel.resetMutationState()
                onBack()
            }
            is DownloadClientMutationState.Conflict -> {
                conflictFields = state.fields
            }
            is DownloadClientMutationState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            is DownloadClientMutationState.ConnectionFailed -> {
                snackbarHostState.showSnackbar(
                    message = "Connection test failed: ${state.message}",
                    duration = SnackbarDuration.Long,
                )
            }
            else -> {
                conflictFields = emptyList()
            }
        }
    }

    val titleText =
        if (uiState.isEditing) {
            mokoString(MR.strings.edit_download_client)
        } else {
            mokoString(MR.strings.add_download_client)
        }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = titleText) },
                navigationIcon = {
                    BackButton(onClick = onBack)
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(
                            onClick = {
                                confirmDelete = true
                            },
                            colors =
                                IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                ),
                            modifier = Modifier.padding(end = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                            )
                        }
                    }
                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                viewModel.submit()
                            }
                        },
                        enabled = uiState.saveButtonEnabled && !uiState.isTesting,
                        modifier = Modifier.padding(end = 16.dp),
                    ) {
                        AnimatedContent(
                            targetState = uiState.isTesting,
                            label = "save_button",
                        ) { isTesting ->
                            if (isTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(text = mokoString(MR.strings.save))
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = navigationBarBottomInset() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AnimatedVisibility(
                visible = uiState.mutationState is DownloadClientMutationState.ConnectionFailed,
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
                                text = "Connection Failed",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            (uiState.mutationState as? DownloadClientMutationState.ConnectionFailed)?.let { state ->
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                    }
                }
            }

            DropdownPicker(
                options = DownloadClientType.entries,
                selectedOption = uiState.selectedType,
                onOptionSelected = { viewModel.updateSelectedType(it) },
                getOptionLabel = { it.displayName },
                label = { Text(mokoString(MR.strings.client_type)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val hasLabelConflict = conflictFields.contains(DownloadClientConflictField.DownloadClientLabel)
                AMOutlinedTextField(
                    value = uiState.label,
                    placeholder = uiState.selectedType.displayName,
                    onValueChange = { viewModel.updateLabel(it) },
                    label = mokoString(MR.strings.client_label),
                    isError = hasLabelConflict,
                    errorMessage = hasLabelConflict thenGet mokoString(MR.strings.field_conflict, mokoString(MR.strings.client_label)),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                val hasUrlConflict = conflictFields.contains(DownloadClientConflictField.DownloadClientUrl)
                AMOutlinedTextField(
                    label = mokoString(MR.strings.host),
                    required = true,
                    value = uiState.url,
                    onValueChange = { viewModel.updateUrl(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = mokoString(MR.strings.host_placeholder) + "${uiState.selectedType.defaultPort}",
                    description = mokoString(MR.strings.host_description, uiState.selectedType.displayName),
                    singleLine = true,
                    isError = uiState.endpointError || hasUrlConflict,
                    errorMessage =
                        when {
                            uiState.endpointError -> mokoString(MR.strings.invalid_host)
                            hasUrlConflict -> mokoString(MR.strings.field_conflict, mokoString(MR.strings.client_url))
                            else -> null
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )

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
                            text = mokoString(MR.strings.authentication),
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Text(
                            text = mokoString(MR.strings.download_client_authentication),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        LabelledCheckbox(
                            label = mokoString(MR.strings.no_auth_required),
                            checked = uiState.noApiKeyRequired,
                            onCheckedChange = { viewModel.updateNoApiKeyRequired(it) },
                        )

                        AMOutlinedTextField(
                            value = uiState.username,
                            onValueChange = { viewModel.updateUsername(it) },
                            label = mokoString(MR.strings.client_username),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.noApiKeyRequired,
                        )

                        var showPassword by remember { mutableStateOf(false) }
                        AMOutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.updatePassword(it) },
                            label = mokoString(MR.strings.client_password),
                            visualTransformation =
                                if (showPassword) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                AnimatedContent(
                                    targetState = showPassword,
                                    modifier = Modifier.clickable { showPassword = !showPassword },
                                ) { visible ->
                                    if (visible) {
                                        Icon(Icons.Default.Visibility, null)
                                    } else {
                                        Icon(Icons.Default.VisibilityOff, null)
                                    }
                                }
                            },
                            enabled = !uiState.noApiKeyRequired,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        ) {
                            HorizontalDivider(Modifier.weight(1f))
                            Text(
                                mokoString(MR.strings.or),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            HorizontalDivider(Modifier.weight(1f))
                        }

                        AMOutlinedTextField(
                            value = uiState.apiKey,
                            onValueChange = { viewModel.updateApiKey(it) },
                            label = mokoString(MR.strings.client_api_key),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = mokoString(MR.strings.api_key_placeholder),
                            enabled = !uiState.noApiKeyRequired,
                        )
                    }
                }

                CustomHeaderSection(
                    localNetworkSsids = uiState.localNetworkSsids,
                    localNetworkConfigured = uiState.localNetworkConfigured,
                    headers = uiState.headers,
                    onHeadersChanged = { viewModel.updateHeaders(it) },
                )

                LocalNetworkArea(
                    defaultPort = uiState.selectedType.defaultPort,
                    uiState = uiState,
                    onLocalNetworkEnabledChanged = { viewModel.updateLocalNetworkEnabled(it) },
                    onLocalNetworkUrlChanged = { viewModel.updateLocalNetworkUrl(it) },
                    onLocalNetworkSsidChanged = { viewModel.updateLocalNetworkSsid(it) },
                    onTestLocalConnection = { viewModel.testLocalConnection() },
                )

                TestConnectionSection(
                    isTesting = uiState.isTesting,
                    testButtonEnabled = !uiState.isTesting && uiState.url.isNotBlank(),
                    testResult = uiState.testResult,
                    onTestConnection = { viewModel.testConnection() },
                )
            }
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                viewModel.deleteClient()
                            }
                        },
                    ) { Text(mokoString(MR.strings.yes)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            confirmDelete = false
                        },
                    ) { Text(mokoString(MR.strings.no)) }
                },
                title = { Text(mokoString(MR.strings.confirm)) },
                text = {
                    Text(mokoString(MR.strings.confirm_delete_download_client))
                },
            )
        }
    }
}

@Composable
fun LocalNetworkArea(
    defaultPort: Int,
    uiState: DownloadClientConfigurationUiState,
    onLocalNetworkEnabledChanged: (Boolean) -> Unit,
    onLocalNetworkUrlChanged: (String) -> Unit,
    onLocalNetworkSsidChanged: (List<String>) -> Unit,
    onTestLocalConnection: () -> Unit,
    moko: MokoStrings = koinInject(),
) {
    val context = LocalContext.current
    val locationPermissionHandler =
        rememberLocationPermissionHandler(
            onDenied = {
                Toast.makeText(context, moko.getString(MR.strings.location_denied), Toast.LENGTH_SHORT).show()
            },
        )

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
                        value = uiState.localNetworkEndpoint,
                        onValueChange = onLocalNetworkUrlChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = mokoString(MR.strings.local_network_url),
                        placeholder = "http://192.168.1.100:$defaultPort",
                        enabled = uiState.localNetworkEnabled,
                        singleLine = true,
                        isError = uiState.localNetworkEndpointError,
                        errorMessage =
                            if (uiState.localNetworkEndpointError) {
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
                                    Toast.makeText(context, moko.getString(MR.strings.ssid_get_error), Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.WifiFind, null)
                        Text(mokoString(MR.strings.use_current_network), modifier = Modifier.padding(start = 6.dp))
                    }

                    HorizontalDivider()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Button(
                            onClick = onTestLocalConnection,
                            enabled =
                                !uiState.localTesting &&
                                    uiState.localNetworkEndpoint.isNotBlank(),
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            when (uiState.localTestResult) {
                                true -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = mokoString(MR.strings.success),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                false -> {
                                    Icon(
                                        painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                    Text(
                                        text = mokoString(MR.strings.failure),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
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
