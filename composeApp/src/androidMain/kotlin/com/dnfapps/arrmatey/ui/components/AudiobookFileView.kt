package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.AudiobookFile
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AudiobookFileView(
    audiobook: Audiobook,
    searchIds: Set<Long>,
    onAutomaticSearch: () -> Unit,
    onNavigateToAudiobookRelease: (Long?, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        ReleaseDownloadButtons(
            onInteractiveClicked = {
                onNavigateToAudiobookRelease(audiobook.id, audiobook.releaseQuery)
            },
            onAutomaticClicked = onAutomaticSearch,
            automaticSearchEnabled = audiobook.monitored,
            automaticSearchInProgress = searchIds.contains(audiobook.id),
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )

        audiobook.files.forEach { file ->
            AudiobookFileCard(file)
        }

        if (audiobook.files.isEmpty()) {
            Text(
                text = mokoString(MR.strings.no_files),
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AudiobookFileCard(
    file: AudiobookFile,
    modifier: Modifier = Modifier,
) {
    ContainerCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = file.path?.substringAfterLast('/') ?: mokoString(MR.strings.unknown),
            style = MaterialTheme.typography.titleSmallEmphasized,
        )
        Text(
            text =
            listOfNotNull(
                file.format,
                file.size?.bytesAsFileSizeString(),
            ).joinToString(BULLET),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        file.createdAt?.format("MMM d, yyyy")?.let { formattedDate ->
            Text(
                text = mokoString(MR.strings.added_on, formattedDate),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
