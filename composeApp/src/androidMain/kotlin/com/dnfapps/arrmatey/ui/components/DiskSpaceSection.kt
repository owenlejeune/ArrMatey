package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun DiskSpaceSection(diskSpaces: List<ArrDiskSpace>) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            diskSpaces.forEachIndexed { index, disk ->
                DiskSpaceItem(disk = disk)
                if (index < diskSpaces.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiskSpaceItem(disk: ArrDiskSpace) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = disk.path ?: mokoString(MR.strings.unknown),
                style = MaterialTheme.typography.titleMediumEmphasized
            )
            Text(
                text = "${disk.freeSpace.bytesAsFileSizeString()} ${mokoString(MR.strings.free_space_lowercase)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val progressColor = if (disk.usedPercentage > 0.9f) Color.Red else MaterialTheme.colorScheme.primary

        LinearProgressIndicator(
            progress = { disk.usedPercentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val usedSpace = disk.totalSpace - disk.freeSpace
            Text(
                text = "${usedSpace.bytesAsFileSizeString()} / ${disk.totalSpace.bytesAsFileSizeString()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(disk.usedPercentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
