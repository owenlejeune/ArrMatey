package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun ReleaseDownloadButtons(
    onInteractiveClicked: () -> Unit,
    automaticSearchEnabled: Boolean,
    onAutomaticClicked: () -> Unit,
    deleteInProgress: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    automaticSearchInProgress: Boolean = false,
) {
    ReleaseDownloadButtons(
        onInteractiveClicked = onInteractiveClicked,
        automaticSearchEnabled = automaticSearchEnabled,
        onAutomaticClicked = onAutomaticClicked,
        modifier = modifier,
        deleteInProgress = deleteInProgress,
        onDelete = onDelete,
        automaticSearchInProgress = automaticSearchInProgress,
        includeDeleteButton = true,
    )
}

@Composable
fun ReleaseDownloadButtons(
    onInteractiveClicked: () -> Unit,
    automaticSearchEnabled: Boolean,
    onAutomaticClicked: () -> Unit,
    modifier: Modifier = Modifier,
    automaticSearchInProgress: Boolean = false,
) {
    ReleaseDownloadButtons(
        onInteractiveClicked = onInteractiveClicked,
        automaticSearchEnabled = automaticSearchEnabled,
        onAutomaticClicked = onAutomaticClicked,
        modifier = modifier,
        automaticSearchInProgress = automaticSearchInProgress,
        includeDeleteButton = false,
    )
}

@Composable
private fun ReleaseDownloadButtons(
    onInteractiveClicked: () -> Unit,
    automaticSearchEnabled: Boolean,
    onAutomaticClicked: () -> Unit,
    modifier: Modifier = Modifier,
    deleteInProgress: Boolean = false,
    onDelete: () -> Unit = {},
    automaticSearchInProgress: Boolean = false,
    includeDeleteButton: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        if (includeDeleteButton) {
            IconButton(
                onClick = { onDelete() },
                shape = RoundedCornerShape(10.dp),
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                enabled = !deleteInProgress,
            ) {
                if (deleteInProgress) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = mokoString(MR.strings.delete),
                    )
                }
            }
        }
        Button(
            modifier = Modifier.weight(1f),
            onClick = onInteractiveClicked,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = mokoString(MR.strings.interactive),
            )
            Text(text = mokoString(MR.strings.interactive))
        }

        Button(
            modifier = Modifier.weight(1f),
            onClick = onAutomaticClicked,
            enabled = automaticSearchEnabled && !automaticSearchInProgress,
        ) {
            if (automaticSearchInProgress) {
                CircularProgressIndicator(modifier = Modifier.size(25.dp))
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = mokoString(MR.strings.automatic),
                )
                Text(text = mokoString(MR.strings.automatic))
            }
        }
    }
}
