package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.EditInstanceViewModel
import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.navigation.SettingsNavigation
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInstanceScreen(
    id: Long,
    viewModel: EditInstanceViewModel = koinInjectParams(id),
    settingsNav: SettingsNavigation = koinInject<SettingsNavigation>()
) {
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val instance by viewModel.instance.collectAsStateWithLifecycle()

    var confirmDelete by remember { mutableStateOf(false) }
    var saveClicked by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.testResult) {
        if (uiState.testResult == true && saveClicked) {
            viewModel.updateInstance()
        }
    }

    LaunchedEffect(uiState.editResult) {
        if (uiState.editResult is InsertResult.Success) {
            settingsNav.popBackStack()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(text = mokoString(MR.strings.edit_instance)) },
                navigationIcon = {
                    IconButton(
                        onClick = { settingsNav.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(MR.strings.back)
                        )
                    }
                },
                actions = {
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

                    Button(
                        onClick = {
                            scope.launch {
                                saveClicked = true
                                viewModel.testConnection()
                            }
                        },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(text = mokoString(MR.strings.save))
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = navigationBarBottomInset() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            instance?.let { instance ->
                ArrConfigurationScreen(
                    instanceType = instance.type,
                    uiState = uiState,
                    onApiEndpointChanged = { viewModel.setApiEndpoint(it) },
                    onApiKeyChanged = { viewModel.setApiKey(it) },
                    onInstanceLabelChanged = { viewModel.setInstanceLabel(it) },
                    onIsSlowInstanceChanged = { viewModel.setIsSlowInstance(it) },
                    onCustomTimeoutChanged = { viewModel.setCustomTimeout(it) },
                    onHeadersChanged = { viewModel.updateHeaders(it) },
                    onTestConnection = { viewModel.testConnection() },
                    onLocalNetworkEnabledChanged = { viewModel.setLocalNetworkEnabled(it) },
                    onLocalNetworkUrlChanged = { viewModel.setLocalNetworkUrl(it) },
                    onLocalNetworkSsidChanged = { viewModel.setLocalNetworkSsids(it) },
                    onTestLocalConnection = { viewModel.testLocalConnection() },
                    onToggleNotificationsEnabled = { viewModel.toggleNotificationsEnabled() }
                )
            }

            if (confirmDelete) {
                AlertDialog(
                    onDismissRequest = { confirmDelete = false},
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    instance?.let { instance ->
                                        viewModel.deleteInstance(instance)
                                        settingsNav.popBackStack()
                                    }
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
                        Text(mokoString(MR.strings.confirm_delete_instance, instance?.label ?: ""))
                    }
                )
            }
        }
    }
}