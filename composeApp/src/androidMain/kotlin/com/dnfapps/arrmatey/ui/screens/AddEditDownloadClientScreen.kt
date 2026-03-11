package com.dnfapps.arrmatey.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientConflictField
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientMutationState
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientSettingsViewModel
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SettingsScreen
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.thenGet
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDownloadClientScreen(
    clientId: Long? = null,
    viewModel: DownloadClientSettingsViewModel = koinInjectParams(clientId),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<SettingsScreen> = navigationManager.settings()
) {
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
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
                navigation.popBackStack()
            }
            is DownloadClientMutationState.Conflict -> {
                conflictFields = state.fields
            }
            is DownloadClientMutationState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            is DownloadClientMutationState.ConnectionFailed -> {
                // Show connection failed error but stay on page
                snackbarHostState.showSnackbar(
                    message = "Connection test failed: ${state.message}",
                    duration = SnackbarDuration.Long
                )
            }
            else -> {
                conflictFields = emptyList()
            }
        }
    }

    val titleText = if (uiState.isEditing) {
        mokoString(MR.strings.edit_download_client)
    } else {
        mokoString(MR.strings.add_download_client)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = titleText) },
                navigationIcon = {
                    BackButton(navigation)
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(
                            onClick = {
                                confirmDelete = true
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null
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
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        AnimatedContent(
                            targetState = uiState.isTesting,
                            label = "save_button"
                        ) { isTesting ->
                            if (isTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(text = mokoString(MR.strings.save))
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(
                visible = uiState.mutationState is DownloadClientMutationState.ConnectionFailed
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Column {
                            Text(
                                text = "Connection Failed",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            (uiState.mutationState as? DownloadClientMutationState.ConnectionFailed)?.let { state ->
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
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
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    singleLine = true
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
                    errorMessage = when {
                        uiState.endpointError -> mokoString(MR.strings.invalid_host)
                        hasUrlConflict -> mokoString(MR.strings.field_conflict, mokoString(MR.strings.client_url))
                        else -> null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                AMOutlinedTextField(
                    value = uiState.username,
                    onValueChange = { viewModel.updateUsername(it) },
                    label = mokoString(MR.strings.client_username),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                var showPassword by remember { mutableStateOf(false) }
                AMOutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = mokoString(MR.strings.client_password),
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        AnimatedContent (
                            targetState = showPassword,
                            modifier = Modifier.clickable { showPassword = !showPassword }
                        ) { visible ->
                            if (visible) {
                                Icon(Icons.Default.Visibility, null)
                            } else {
                                Icon(Icons.Default.VisibilityOff, null)
                            }
                        }
                    }
                )

                AMOutlinedTextField(
                    value = uiState.apiKey,
                    onValueChange = { viewModel.updateApiKey(it) },
                    label = mokoString(MR.strings.client_api_key),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = mokoString(MR.strings.api_key_placeholder)
                )

                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LabelledSwitch(
                        label = mokoString(MR.strings.client_enabled),
                        checked = uiState.enabled,
                        onCheckedChange = { viewModel.updateEnabled(it) },
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false},
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                viewModel.deleteClient()
                            }
                        }
                    ) { Text(mokoString(MR.strings.yes)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            confirmDelete = false

                        }
                    ) { Text(mokoString(MR.strings.no)) }
                },
                title = { Text(mokoString(MR.strings.confirm)) },
                text = {
                    Text(mokoString(MR.strings.confirm_delete_download_client))
                }
            )
        }
    }
}
