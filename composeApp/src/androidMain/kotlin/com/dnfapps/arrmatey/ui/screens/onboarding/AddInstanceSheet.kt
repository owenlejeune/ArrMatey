package com.dnfapps.arrmatey.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.AddInstanceViewModel
import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.screens.ArrConfigurationScreen
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInstanceSheet(
    onDismiss: () -> Unit,
    initialType: InstanceType = InstanceType.Sonarr,
    viewModel: AddInstanceViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    var selectedInstanceType by remember { mutableStateOf(initialType) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.reset()
    }

    LaunchedEffect(selectedInstanceType) {
        viewModel.reset()
        viewModel.setInstanceLabel(selectedInstanceType.name)
    }

    LaunchedEffect(uiState.createResult) {
        if (uiState.createResult is InsertResult.Success) {
            onDismiss()
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
                        text = mokoString(MR.strings.add_instance),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                FilledTonalButton(
                    onClick = {
                        scope.launch {
                            viewModel.createInstance(selectedInstanceType)
                        }
                    },
                    enabled = uiState.saveButtonEnabled,
                ) {
                    Text(text = mokoString(MR.strings.save))
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
                DropdownPicker(
                    modifier = Modifier.fillMaxWidth(),
                    options = InstanceType.entries,
                    selectedOption = selectedInstanceType,
                    onOptionSelected = { selectedInstanceType = it },
                    label = {
                        Text(
                            text = mokoString(MR.strings.instance_type),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )

                ArrConfigurationScreen(
                    instanceType = selectedInstanceType,
                    uiState = uiState,
                    onApiEndpointChanged = { viewModel.setApiEndpoint(it) },
                    onApiKeyChanged = { viewModel.setApiKey(it) },
                    onNoApiKeyRequiredChanged = { viewModel.setNoApiKeyRequired(it) },
                    onInstanceLabelChanged = { viewModel.setInstanceLabel(it) },
                    onIsSlowInstanceChanged = { viewModel.setIsSlowInstance(it) },
                    onCustomTimeoutChanged = { viewModel.setCustomTimeout(it) },
                    onTestConnection = { viewModel.testConnection(selectedInstanceType) },
                    onHeadersChanged = { viewModel.updateHeaders(it) },
                    onLocalNetworkEnabledChanged = { viewModel.setLocalNetworkEnabled(it) },
                    onLocalNetworkUrlChanged = { viewModel.setLocalNetworkUrl(it) },
                    onLocalNetworkSsidChanged = { viewModel.setLocalNetworkSsid(it) },
                    onTestLocalConnection = { viewModel.testLocalConnection(selectedInstanceType) },
                    onToggleNotificationsEnabled = { viewModel.toggleNotificationsEnabled() },
                )
            }
        }
    }
}
