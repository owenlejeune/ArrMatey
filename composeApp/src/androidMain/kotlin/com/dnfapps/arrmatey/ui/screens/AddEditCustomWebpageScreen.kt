package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SettingsScreen
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.webpage.viewmodel.CustomWebpageConfigurationViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomWebpageScreen(
    webpageId: Long? = null,
    viewModel: CustomWebpageConfigurationViewModel = koinInjectParams(webpageId),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<SettingsScreen> = navigationManager.settings(),
    moko: MokoStrings = koinInject()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveResult) {
        when (val state = uiState.saveResult) {
            is InsertResult.Success -> {
                viewModel.reset()
                navigation.popBackStack()
            }
            is InsertResult.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    val titleText = remember(uiState.isEditing) {
        if (uiState.isEditing) {
            moko.getString(MR.strings.edit_webpage)
        } else {
            moko.getString(MR.strings.add_webpage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(titleText)
                },
                navigationIcon = { BackButton(navigation) },
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
                            viewModel.saveWebpage()
                        },
                        enabled = uiState.saveButtonEnabled,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(text = mokoString(MR.strings.save))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            AMOutlinedTextField(
                label = mokoString(MR.strings.name),
                required = true,
                value = uiState.name,
                onValueChange = { viewModel.setName(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = mokoString(MR.strings.custom_page_placeholder),
                singleLine = true
            )

            AMOutlinedTextField(
                label = mokoString(MR.strings.url),
                required = true,
                value = uiState.url,
                onValueChange = { viewModel.setUrl(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "https://example.com",
                description = mokoString(MR.strings.custom_page_description),
                singleLine = true,
                isError = uiState.endpointError,
                errorMessage = when {
                    uiState.endpointError -> mokoString(MR.strings.invalid_host)
                    else -> null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            CustomHeaderSection(
                headers = uiState.headers,
                onHeadersChanged = { viewModel.setHeaders(it) }
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false},
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWebpage()
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
                Text(mokoString(MR.strings.confirm_delete_custom_webpage))
            }
        )
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(mokoString(MR.strings.ok))
                }
            }
        )
    }
}