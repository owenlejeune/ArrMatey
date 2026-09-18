package com.dnfapps.arrmatey.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientConflictField
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientMutationState
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientSettingsViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledCheckbox
import com.dnfapps.arrmatey.ui.screens.CustomHeaderSection
import com.dnfapps.arrmatey.ui.screens.LocalNetworkArea
import com.dnfapps.arrmatey.ui.screens.TestConnectionSection
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.thenGet
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDownloadClientSheet(
    onDismiss: () -> Unit,
    viewModel: DownloadClientSettingsViewModel = koinViewModel(
        key = "onboarding_download_client",
        parameters = { parametersOf(null) }),
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var conflictFields by remember { mutableStateOf<List<DownloadClientConflictField>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.resetMutationState()
    }

    LaunchedEffect(uiState.mutationState) {
        when (val state = uiState.mutationState) {
            is DownloadClientMutationState.Success -> {
                viewModel.resetMutationState()
                onDismiss()
            }

            is DownloadClientMutationState.Conflict -> {
                conflictFields = state.fields
            }

            else -> {
                conflictFields = emptyList()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = mokoString(MR.strings.cancel))
                    }
                    Text(
                        text = mokoString(MR.strings.add_download_client),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                FilledTonalButton(
                    onClick = {
                        scope.launch {
                            viewModel.submit()
                        }
                    },
                    enabled = uiState.saveButtonEnabled && !uiState.isTesting,
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
            }

            HorizontalDivider()

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
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
                                    text = mokoString(MR.strings.connection_failed),
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

                val hasLabelConflict = conflictFields.contains(DownloadClientConflictField.DownloadClientLabel)
                AMOutlinedTextField(
                    value = uiState.label,
                    placeholder = uiState.selectedType.displayName,
                    onValueChange = { viewModel.updateLabel(it) },
                    label = mokoString(MR.strings.client_label),
                    isError = hasLabelConflict,
                    errorMessage = hasLabelConflict thenGet mokoString(
                        MR.strings.field_conflict,
                        mokoString(MR.strings.client_label)
                    ),
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
                                    label = "password_visibility",
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
    }
}
