package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.model.InstanceMediaPresence
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun InstancePresenceChips(
    presences: List<InstanceMediaPresence>,
    selectedInstanceId: Long?,
    onSelectInstance: (Long) -> Unit,
    onAddInstance: (Instance) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (presences.size <= 1) return

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        presences.forEach { presence ->
            val isSelected = presence.instance.id == selectedInstanceId
            val isPresent = presence.isPresent
            val arrMedia = presence.arrMedia

            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }

            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }

            val (statusText, statusColor) = when {
                !isPresent -> Pair(
                    mokoString(MR.strings.not_added),
                    if (isSelected) contentColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline,
                )
                arrMedia != null && (arrMedia.isDownloaded || (arrMedia as? ArrMovie)?.hasFile == true || ((arrMedia as? ArrSeries)?.statistics?.episodeFileCount ?: 0) > 0) -> Pair(
                    mokoString(MR.strings.downloaded),
                    if (isSelected) Color(0xFF4ADE80) else Color(0xFF16A34A),
                )
                arrMedia?.monitored == true -> Pair(
                    mokoString(MR.strings.monitored),
                    if (isSelected) Color(0xFFFBBF24) else Color(0xFFD97706),
                )
                else -> Pair(
                    mokoString(MR.strings.unmonitored),
                    if (isSelected) contentColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline,
                )
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (isPresent) {
                            onSelectInstance(presence.instance.id)
                        } else {
                            onAddInstance(presence.instance)
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                color = containerColor,
                tonalElevation = if (isSelected) 4.dp else 0.dp,
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                } else if (!isPresent) {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                } else null,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (!isPresent) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isSelected) contentColor else MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor),
                        )
                    }

                    Text(
                        text = presence.instance.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                    )

                    Text(
                        text = "• $statusText",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}
