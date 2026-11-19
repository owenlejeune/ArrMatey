package com.dnfapps.arrmatey.ui.screens.settings

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.dnfapps.arrmatey.backup.state.ExportUiState
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.LabelledCheckbox
import com.dnfapps.arrmatey.ui.components.ServiceSelectionCard
import com.dnfapps.arrmatey.ui.theme.associatedColor
import com.dnfapps.arrmatey.utils.mokoString
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateQrCodeSheet(
    exportState: ExportUiState,
    onDismiss: () -> Unit,
    onGenerateExportData: ((String) -> Unit) -> Unit,
    onToggleIncludeInstancePreferences: () -> Unit,
    onToggleIncludeTabPreferences: () -> Unit,
    onToggleIncludeUiPreferences: () -> Unit,
    onToggleIncludeIntegrationsPreferences: () -> Unit,
    onToggleInstanceSelection: (Long) -> Unit,
    onToggleDownloadClientSelection: (Long) -> Unit,
    onToggleCustomWebpageSelection: (Long) -> Unit,
    onToggleMediaServerSelection: (Long) -> Unit = {},
) {
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }

                Column {
                    Text(
                        text = mokoString(MR.strings.generate_qr_code),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = mokoString(MR.strings.generate_qr_code_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (generatedBitmap != null) {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    generatedBitmap?.let { bitmap ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .padding(8.dp),
                            )
                        }
                    }

                    Text(
                        text = mokoString(MR.strings.qr_code_expiration_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                ) {
                    OutlinedButton(
                        onClick = { generatedBitmap = null },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(mokoString(MR.strings.back))
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(mokoString(MR.strings.done))
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                ) {
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

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
                        LabelledCheckbox(
                            label = mokoString(MR.strings.integrations),
                            checked = exportState.includeIntegrationsPreferences,
                            onCheckedChange = { onToggleIncludeIntegrationsPreferences() },
                        )
                    }

                    if (exportState.instances.isNotEmpty() || exportState.downloadClients.isNotEmpty() || exportState.customWebpages.isNotEmpty() || exportState.mediaServers.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = mokoString(MR.strings.select_items_to_export),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )

                        if (exportState.instances.isNotEmpty()) {
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
                                    text = "${exportState.selectedInstanceIds.size}/${exportState.instances.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                exportState.instances.forEach { instance ->
                                    ServiceSelectionCard(
                                        label = instance.label,
                                        url = instance.url,
                                        checked = exportState.selectedInstanceIds.contains(instance.id),
                                        onCheckedChange = { onToggleInstanceSelection(instance.id) },
                                        icon = instance.type.icon,
                                        color = instance.type.associatedColor,
                                    )
                                }
                            }
                        }

                        if (exportState.downloadClients.isNotEmpty()) {
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
                                    text = "${exportState.selectedDownloadClientIds.size}/${exportState.downloadClients.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                exportState.downloadClients.forEach { client ->
                                    ServiceSelectionCard(
                                        label = client.label,
                                        url = client.url,
                                        checked = exportState.selectedDownloadClientIds.contains(client.id),
                                        onCheckedChange = { onToggleDownloadClientSelection(client.id) },
                                        icon = client.type.icon,
                                        color = client.type.associatedColor,
                                    )
                                }
                            }
                        }

                        if (exportState.customWebpages.isNotEmpty()) {
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
                                    text = "${exportState.selectedCustomWebpageIds.size}/${exportState.customWebpages.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                exportState.customWebpages.forEach { webpage ->
                                    ServiceSelectionCard(
                                        label = webpage.name,
                                        url = webpage.url,
                                        checked = exportState.selectedCustomWebpageIds.contains(webpage.id),
                                        onCheckedChange = { onToggleCustomWebpageSelection(webpage.id) },
                                        vectorIcon = Icons.Default.Language,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }

                        if (exportState.mediaServers.isNotEmpty()) {
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
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = mokoString(MR.strings.media_servers),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }

                                Text(
                                    text = "${exportState.selectedMediaServerIds.size}/${exportState.mediaServers.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                exportState.mediaServers.forEach { server ->
                                    ServiceSelectionCard(
                                        label = server.label.ifBlank { server.serverType.name },
                                        url = server.url,
                                        checked = exportState.selectedMediaServerIds.contains(server.id),
                                        onCheckedChange = { onToggleMediaServerSelection(server.id) },
                                        icon = server.serverType.icon,
                                        color = server.serverType.brandColor,
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(mokoString(MR.strings.cancel))
                    }

                    val tooLargeErrorText = mokoString(MR.strings.qr_code_too_large)

                    Button(
                        enabled =
                        exportState.selectedInstanceIds.isNotEmpty() ||
                            exportState.selectedDownloadClientIds.isNotEmpty() ||
                            exportState.selectedCustomWebpageIds.isNotEmpty() ||
                            exportState.selectedMediaServerIds.isNotEmpty() ||
                            exportState.includeTabPreferences ||
                            exportState.includeUiPreferences ||
                            exportState.includeIntegrationsPreferences,
                        onClick = {
                            errorMessage = null
                            onGenerateExportData { encryptedData ->
                                val bitmap = generateQrCodeBitmap(encryptedData)
                                if (bitmap != null) {
                                    generatedBitmap = bitmap
                                } else {
                                    errorMessage = tooLargeErrorText
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(mokoString(MR.strings.generate))
                    }
                }
            }
        }
    }
}

private fun generateQrCodeBitmap(content: String, size: Int = 800): Bitmap? = try {
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to 2,
        EncodeHintType.CHARACTER_SET to "UTF-8",
    )
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap[x, y] = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }
    bitmap
} catch (_: Exception) {
    null
}
