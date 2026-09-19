package com.dnfapps.arrmatey.ui.components.downloads

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun DownloadTransferSpeedChips(
    downloadClients: List<DownloadClient>,
    transferInfo: List<DownloadTransferInfo>,
    selectedClientIds: List<Long>,
    onToggleClientIdFilter: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Spacer(Modifier.width(18.dp))
        downloadClients.forEach { client ->
            val info = transferInfo.firstOrNull { it.client.id == client.id }
            FilterChip(
                selected = downloadClients.size > 1 && selectedClientIds.contains(client.id),
                onClick = { onToggleClientIdFilter(client.id) },
                leadingIcon = {
                    Image(
                        painter = painterResource(client.type.icon),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                label = {
                    Text(
                        text =
                            "↓ ${(info?.downloadSpeed ?: 0).bytesAsFileSizeString()}/s  " +
                                "↑ ${(info?.uploadSpeed ?: 0).bytesAsFileSizeString()}/s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                },
            )
        }
        Spacer(Modifier.width(18.dp))
    }
}
