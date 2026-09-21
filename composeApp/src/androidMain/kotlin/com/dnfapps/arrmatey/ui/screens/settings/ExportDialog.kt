package com.dnfapps.arrmatey.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.backup.state.ExportUiState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.LabelledCheckbox
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    exportState: ExportUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onToggleIncludeInstancePreferences: () -> Unit,
    onToggleIncludeTabPreferences: () -> Unit,
    onToggleIncludeUiPreferences: () -> Unit,
    onToggleInstanceSelection: (Long) -> Unit,
    onToggleDownloadClientSelection: (Long) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = mokoString(MR.strings.export_data),
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = mokoString(MR.strings.export_password_prompt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AMOutlinedTextField(
                value = exportState.password,
                onValueChange = onPasswordChanged,
                label = mokoString(MR.strings.password),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            ContainerCard(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LabelledCheckbox(
                    label = mokoString(MR.strings.include_preferences),
                    checked = exportState.includeInstancePreferences,
                    onCheckedChange = { onToggleIncludeInstancePreferences() },
                )
                LabelledCheckbox(
                    label = mokoString(MR.strings.navigation_bar_configuration),
                    checked = exportState.includeTabPreferences,
                    onCheckedChange = { onToggleIncludeTabPreferences() },
                )
                LabelledCheckbox(
                    label = mokoString(MR.strings.user_interface),
                    checked = exportState.includeUiPreferences,
                    onCheckedChange = { onToggleIncludeUiPreferences() },
                )
            }

            if (exportState.instances.isNotEmpty() || exportState.downloadClients.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = mokoString(MR.strings.select_items_to_export),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )

                if (exportState.instances.isNotEmpty()) {
                    Text(
                        text = mokoString(MR.strings.instances),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    ContainerCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        exportState.instances.forEach { instance ->
                            LabelledCheckbox(
                                label = instance.label,
                                checked = exportState.selectedInstanceIds.contains(instance.id),
                                onCheckedChange = { onToggleInstanceSelection(instance.id) },
                            )
                        }
                    }
                }

                if (exportState.downloadClients.isNotEmpty()) {
                    Text(
                        text = mokoString(MR.strings.download_clients),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    ContainerCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        exportState.downloadClients.forEach { client ->
                            LabelledCheckbox(
                                label = client.label,
                                checked = exportState.selectedDownloadClientIds.contains(client.id),
                                onCheckedChange = { onToggleDownloadClientSelection(client.id) },
                            )
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Button(
                    enabled =
                        exportState.password.isNotBlank() &&
                            (exportState.selectedInstanceIds.isNotEmpty() || exportState.selectedDownloadClientIds.isNotEmpty()),
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(mokoString(MR.strings.save))
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(mokoString(MR.strings.cancel))
                }
            }
        }
    }
}
