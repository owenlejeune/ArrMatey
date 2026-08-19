package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.model.InstanceMediaPresence
import com.dnfapps.arrmatey.ui.theme.ArrOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstanceChipsRow(
    presences: List<InstanceMediaPresence>,
    selectedInstanceId: Long?,
    onInstanceSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        presences.forEach { presence ->
            val instance = presence.instance
            val isSelected = instance.id == selectedInstanceId
            val isPresent = presence.isPresent
            val hasFile = when (val media = presence.arrMedia) {
                is ArrMovie -> media.hasFile
                is ArrSeries -> media.episodeFileCount > 0
                else -> true
            }

            FilterChip(
                selected = isSelected,
                onClick = { onInstanceSelected(instance.id) },
                label = { Text(instance.label) },
                leadingIcon = if (isPresent) {
                    {
                        Icon(
                            imageVector = if (hasFile) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (hasFile) MaterialTheme.colorScheme.primary else ArrOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
        }
    }
}
