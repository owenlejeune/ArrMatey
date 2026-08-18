package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.screens.AudiobookFileCard
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun AudiobookFileView(
    audiobook: Audiobook,
    searchIds: Set<Long>,
    onAutomaticSearch: () -> Unit,
    onNavigateToAudiobookFiles: (Audiobook) -> Unit,
    onNavigateToAudiobookRelease: (Long?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = mokoString(MR.strings.files),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = mokoString(MR.strings.history),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    onNavigateToAudiobookFiles(audiobook)
                }
            )
        }

        ReleaseDownloadButtons(
            onInteractiveClicked = {
                onNavigateToAudiobookRelease(audiobook.id, audiobook.releaseQuery)
            },
            onAutomaticClicked = onAutomaticSearch,
            automaticSearchEnabled = audiobook.monitored,
            automaticSearchInProgress = searchIds.contains(audiobook.id),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        audiobook.files.forEach { file ->
            AudiobookFileCard(file)
        }

        if (audiobook.files.isEmpty()) {
            Text(
                text = mokoString(MR.strings.no_files),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
