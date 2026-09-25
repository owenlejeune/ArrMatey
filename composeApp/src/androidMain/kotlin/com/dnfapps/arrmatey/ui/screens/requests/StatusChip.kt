package com.dnfapps.arrmatey.ui.screens.requests

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestStatus
import com.dnfapps.arrmatey.ui.components.AMStatusBadge
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun StatusChip(
    request: MediaRequest,
    modifier: Modifier = Modifier,
) {
    val mediaStatus = MediaStatus.fromValue(request.media.status)
    val requestStatus = RequestStatus.fromValue(request.status)

    val (label, container, content) =
        when {
            mediaStatus == MediaStatus.Deleted ->
                Triple(mediaStatus.resource, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)

            mediaStatus == MediaStatus.Available ->
                Triple(mediaStatus.resource, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)

            mediaStatus == MediaStatus.PartiallyAvailable ->
                Triple(mediaStatus.resource, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)

            mediaStatus == MediaStatus.Processing ->
                Triple(mediaStatus.resource, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)

            requestStatus == RequestStatus.Failed || requestStatus == RequestStatus.Declined ->
                Triple(requestStatus.resource, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)

            requestStatus == RequestStatus.Approved ->
                Triple(requestStatus.resource, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)

            else -> // Default to Pending
                Triple(requestStatus.resource, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        }

    AMStatusBadge(
        text = mokoString(label),
        containerColor = container,
        contentColor = content,
        modifier = modifier,
    )
}
