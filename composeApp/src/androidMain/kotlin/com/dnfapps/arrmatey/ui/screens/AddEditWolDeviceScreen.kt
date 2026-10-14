package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.wol.viewmodel.WolDeviceConfigurationViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWolDeviceScreen(
    deviceId: Long? = null,
    viewModel: WolDeviceConfigurationViewModel = koinViewModel(key = deviceId?.toString(), parameters = { parametersOf(deviceId) }),
    moko: MokoStrings = koinInject(),
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveResult) {
        when (val state = uiState.saveResult) {
            is InsertResult.Success -> {
                viewModel.reset()
                onBack()
            }
            is InsertResult.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    val titleText =
        remember(uiState.isEditing) {
            if (uiState.isEditing) {
                moko.getString(MR.strings.edit_wol_device)
            } else {
                moko.getString(MR.strings.add_wol_device)
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleText) },
                navigationIcon = { BackButton(onClick = onBack) },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(
                            onClick = { confirmDelete = true },
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
                        onClick = { viewModel.saveDevice() },
                        enabled = uiState.saveButtonEnabled,
                        modifier = Modifier.padding(end = 16.dp),
                    ) {
                        Text(text = mokoString(MR.strings.save))
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AMOutlinedTextField(
                label = mokoString(MR.strings.name),
                required = true,
                value = uiState.name,
                onValueChange = { viewModel.setName(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "My Media Server",
                singleLine = true,
            )

            AMOutlinedTextField(
                label = mokoString(MR.strings.mac_address),
                required = true,
                value = uiState.macAddress,
                onValueChange = { viewModel.setMacAddress(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "00:11:22:33:44:55",
                singleLine = true,
            )

            AMOutlinedTextField(
                label = mokoString(MR.strings.ip_address),
                required = false,
                value = uiState.ipAddress,
                onValueChange = { viewModel.setIpAddress(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "255.255.255.255",
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )

            AMOutlinedTextField(
                label = mokoString(MR.strings.port),
                required = true,
                value = uiState.port.toString(),
                onValueChange = {
                    val p = it.toIntOrNull() ?: 9
                    viewModel.setPort(p)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "9",
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            if (uiState.isEditing) {
                HorizontalDivider()

                Button(
                    onClick = {
                        viewModel.sendWakeCommand()
                        Toast.makeText(
                            context,
                            moko.getString(MR.strings.wake_packet_sent, listOf<Any>(uiState.name)),
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = mokoString(MR.strings.wake))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteDevice() },
                ) { Text(mokoString(MR.strings.yes)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDelete = false },
                ) { Text(mokoString(MR.strings.no)) }
            },
            title = { Text(mokoString(MR.strings.confirm)) },
            text = { Text(mokoString(MR.strings.confirm_delete_wol_device)) },
        )
    }
}
