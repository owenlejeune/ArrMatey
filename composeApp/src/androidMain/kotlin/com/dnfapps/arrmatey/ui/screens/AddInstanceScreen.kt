package com.dnfapps.arrmatey.ui.screens

import com.dnfapps.arrmatey.shared.MR
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.AddInstanceViewModel
import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.SettingsNavigation
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.InstanceInfoCard
import com.dnfapps.arrmatey.utils.NetworkUtils
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInstanceScreen(
    initialType: InstanceType = InstanceType.Sonarr,
    viewModel: AddInstanceViewModel = koinInject(),
    navigation: SettingsNavigation = koinInject<SettingsNavigation>()
) {
    val scope = rememberCoroutineScope()
    var selectedInstanceType by remember { mutableStateOf(initialType) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
            navigation.popBackStack()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = mokoString(MR.strings.add_instance)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navigation.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(MR.strings.back)
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                viewModel.createInstance(selectedInstanceType)
                            }
                        },
                        enabled = uiState.saveButtonEnabled,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(text = mokoString(MR.strings.save))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { contentPadding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(
                visible = uiState.infoCardMaps[selectedInstanceType] ?: false
            ) {
                InstanceInfoCard(selectedInstanceType) {
                    viewModel.dismissInfoCard(selectedInstanceType)
                }
            }

            DropdownPicker(
                modifier = Modifier.fillMaxWidth(),
                options = InstanceType.entries,
                selectedOption = selectedInstanceType,
                onOptionSelected = { selectedInstanceType = it },
                label = {
                    Text(
                        text = mokoString(MR.strings.instance_type),
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                    )
                }
            )

            ArrConfigurationScreen(
                instanceType = selectedInstanceType,
                uiState = uiState,
                onApiEndpointChanged = { viewModel.setApiEndpoint(it) },
                onApiKeyChanged = { viewModel.setApiKey(it) },
                onInstanceLabelChanged = { viewModel.setInstanceLabel(it) },
                onIsSlowInstanceChanged = { viewModel.setIsSlowInstance(it) },
                onCustomTimeoutChanged = { viewModel.setCustomTimeout(it) },
                onTestConnection = { viewModel.testConnection(selectedInstanceType) },
                onHeadersChanged = { viewModel.updateHeaders(it) },
                onLocalNetworkEnabledChanged = { viewModel.setLocalNetworkEnabled(it) },
                onLocalNetworkUrlChanged = { viewModel.setLocalNetworkUrl(it) },
                onLocalNetworkSsidChanged = { viewModel.setLocalNetworkSsid(it) },
                onTestLocalConnection = { viewModel.testLocalConnection(selectedInstanceType) },
                onToggleNotificationsEnabled = { viewModel.toggleNotificationsEnabled() }
            )
        }
    }
}