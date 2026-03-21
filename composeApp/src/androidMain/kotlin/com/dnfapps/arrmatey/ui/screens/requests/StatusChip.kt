package com.dnfapps.arrmatey.ui.screens.requests

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestStatus
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun StatusChip(request: MediaRequest) {
    val mediaStatus = MediaStatus.fromValue(request.media.status)
    val requestStatus = RequestStatus.fromValue(request.status)

    val (label, container, content) = when {
        mediaStatus == MediaStatus.Deleted ->
            Triple(mediaStatus.resource, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)

        mediaStatus == MediaStatus.Available ->
            Triple(mediaStatus.resource, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)

        mediaStatus == MediaStatus.PartiallyAvailable ->
            Triple(mediaStatus.resource, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)

        mediaStatus == MediaStatus.Processing ->
            Triple(mediaStatus.resource, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)

        requestStatus == RequestStatus.Declined ->
            Triple(requestStatus.resource, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)

        requestStatus == RequestStatus.Approved ->
            Triple(requestStatus.resource, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)

        else -> // Default to Pending
            Triple(requestStatus.resource, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
    }

    AssistChip(
        onClick = { },
        label = { Text(mokoString(label)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = container,
            labelColor = content
        ),
        border = null
    )
}