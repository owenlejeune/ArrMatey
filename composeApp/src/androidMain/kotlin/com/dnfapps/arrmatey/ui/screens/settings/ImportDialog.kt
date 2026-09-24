package com.dnfapps.arrmatey.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.backup.state.ImportUiState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.LabelledCheckbox
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDialog(
    importState: ImportUiState,
    onDismiss: () -> Unit,
    onConfirmDecrypt: () -> Unit,
    onConfirmImport: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onToggleInstanceSelection: (Int) -> Unit,
    onToggleDownloadClientSelection: (Int) -> Unit,
    onToggleCustomWebpageSelection: (Int) -> Unit,
    onToggleImportTabPreferences: () -> Unit,
    onToggleImportUiPreferences: () -> Unit,
    onToggleImportIntegrationsPreferences: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }

                Column {
                    Text(
                        text = mokoString(MR.strings.import_data),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text =
                            if (importState.decryptedBackup == null) {
                                mokoString(MR.strings.import_password_prompt)
                            } else {
                                mokoString(MR.strings.select_items_to_import)
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val backup = importState.decryptedBackup

            if (backup == null) {
                AMOutlinedTextField(
                    value = importState.password,
                    onValueChange = onPasswordChanged,
                    label = mokoString(MR.strings.password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = importState.error != null,
                    errorMessage = importState.error,
                )
            } else {
                val hasAnyPreferences =
                    backup.globalPreferences?.let {
                        it.tabPreferences != null || it.hasUiPreferences || it.hasIntegrationsPreferences
                    } == true

                if (hasAnyPreferences) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = mokoString(MR.strings.onboarding_preferences_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    ContainerCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (backup.globalPreferences?.tabPreferences != null) {
                            LabelledCheckbox(
                                label = mokoString(MR.strings.navigation_bar_configuration),
                                checked = importState.importTabPreferences,
                                onCheckedChange = { onToggleImportTabPreferences() },
                            )
                        }

                        if (backup.globalPreferences?.hasUiPreferences == true) {
                            LabelledCheckbox(
                                label = mokoString(MR.strings.user_interface),
                                checked = importState.importUiPreferences,
                                onCheckedChange = { onToggleImportUiPreferences() },
                            )
                        }

                        if (backup.globalPreferences?.hasIntegrationsPreferences == true) {
                            LabelledCheckbox(
                                label = mokoString(MR.strings.integrations),
                                checked = importState.importIntegrationsPreferences,
                                onCheckedChange = { onToggleImportIntegrationsPreferences() },
                            )
                        }
                    }
                }

                if (backup.instances.isNotEmpty() ||
                    backup.downloadClients.isNotEmpty() ||
                    backup.customWebpages.isNotEmpty()
                ) {
                    if (hasAnyPreferences) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }

                    Text(
                        text = mokoString(MR.strings.select_items_to_import),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    if (backup.instances.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = mokoString(MR.strings.instances),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            Text(
                                text = "${importState.selectedInstanceIndices.size}/${backup.instances.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        ContainerCard(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            backup.instances.forEachIndexed { index, instance ->
                                LabelledCheckbox(
                                    label = instance.label,
                                    checked = importState.selectedInstanceIndices.contains(index),
                                    onCheckedChange = { onToggleInstanceSelection(index) },
                                )
                            }
                        }
                    }

                    if (backup.downloadClients.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = mokoString(MR.strings.download_clients),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            Text(
                                text = "${importState.selectedDownloadClientIndices.size}/${backup.downloadClients.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        ContainerCard(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            backup.downloadClients.forEachIndexed { index, client ->
                                LabelledCheckbox(
                                    label = client.label,
                                    checked = importState.selectedDownloadClientIndices.contains(index),
                                    onCheckedChange = { onToggleDownloadClientSelection(index) },
                                )
                            }
                        }
                    }

                    if (backup.customWebpages.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = mokoString(MR.strings.custom_webpages),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            Text(
                                text = "${importState.selectedCustomWebpageIndices.size}/${backup.customWebpages.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        ContainerCard(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            backup.customWebpages.forEachIndexed { index, webpage ->
                                LabelledCheckbox(
                                    label = webpage.name,
                                    checked = importState.selectedCustomWebpageIndices.contains(index),
                                    onCheckedChange = { onToggleCustomWebpageSelection(index) },
                                )
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(mokoString(MR.strings.cancel))
                }

                if (importState.decryptedBackup == null) {
                    Button(
                        enabled = importState.password.isNotBlank(),
                        onClick = onConfirmDecrypt,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(mokoString(MR.strings.ok))
                    }
                } else {
                    val hasGlobalPrefToImport =
                        (importState.decryptedBackup?.globalPreferences?.tabPreferences != null && importState.importTabPreferences) ||
                            (importState.decryptedBackup?.globalPreferences?.hasUiPreferences == true && importState.importUiPreferences) ||
                            (
                                importState.decryptedBackup?.globalPreferences?.hasIntegrationsPreferences == true &&
                                    importState.importIntegrationsPreferences
                            )

                    Button(
                        enabled =
                            importState.selectedInstanceIndices.isNotEmpty() ||
                                importState.selectedDownloadClientIndices.isNotEmpty() ||
                                importState.selectedCustomWebpageIndices.isNotEmpty() ||
                                hasGlobalPrefToImport,
                        onClick = onConfirmImport,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(mokoString(MR.strings.import_data))
                    }
                }
            }
        }
    }
}
