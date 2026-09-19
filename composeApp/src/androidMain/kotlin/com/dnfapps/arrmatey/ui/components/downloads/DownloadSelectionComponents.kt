package com.dnfapps.arrmatey.ui.components.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadSelectionTopBar(
    count: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    isAllSelected: Boolean,
) {
    TopAppBar(
        title = {
            Text(
                text = mokoPlural(MR.plurals.selected_count, count),
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, null)
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(
                    imageVector = if (isAllSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
fun DownloadSelectionBottomBar(
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
) {
    FlowRow(
        modifier =
            Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        DownloadSelectionActionItem(
            icon = Icons.Default.Pause,
            label = mokoString(MR.strings.pause),
            onClick = onPause,
        )
        DownloadSelectionActionItem(
            icon = Icons.Default.PlayArrow,
            label = mokoString(MR.strings.resume),
            onClick = onResume,
        )
        DownloadSelectionActionItem(
            icon = Icons.Default.Delete,
            label = mokoString(MR.strings.delete),
            onClick = onDelete,
            isError = true,
        )
    }
}

@Composable
fun DownloadSelectionActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isError: Boolean = false,
) {
    Button(
        onClick = onClick,
        colors =
            if (isError) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                )
            } else {
                ButtonDefaults.buttonColors()
            },
        enabled = enabled,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.padding(end = 4.dp),
        )
        Text(
            text = label.breakable(),
        )
    }
}
