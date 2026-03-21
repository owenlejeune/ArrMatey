package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SettingsScreen
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.webpage.viewmodel.CustomWebpageViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomWebpageScreen(
    webpageId: Long? = null,
    viewModel: CustomWebpageViewModel = koinInjectParams(webpageId),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<SettingsScreen> = navigationManager.settings()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

//    LaunchedEffect(uiState.error) {
//        if (uiState.error == null && uiState.name.isEmpty() && uiState.url.isEmpty() && !uiState.isEditing) {
//            navigation.popBackStack()
//        }
//    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditing) "Edit Webpage" else "Add Webpage")
                },
                navigationIcon = { BackButton(navigation) },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveWebpage()
                        },
                        enabled = uiState.name.isNotBlank() && uiState.url.isNotBlank()
                    ) {
                        Text(mokoString(MR.strings.save))
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
                label = "Name",
                required = true,
                value = uiState.name,
                onValueChange = { viewModel.setName(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "My Custom Page",
                singleLine = true
            )

            AMOutlinedTextField(
                label = "URL",
                required = true,
                value = uiState.url,
                onValueChange = { viewModel.setUrl(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "https://example.com",
                description = "Full URL including https://",
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            CustomHeaderSection(
                headers = uiState.headers,
                onHeadersChanged = { viewModel.setHeaders(it) }
            )

            Spacer(Modifier.height(16.dp))
        }
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