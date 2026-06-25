package com.dnfapps.arrmatey.ui.components.bazarr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSubtitle
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.bazarr.state.BazarrSubtitlesUiState
import com.dnfapps.arrmatey.bazarr.viewmodel.BazarrMediaSubtitlesViewModel
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString

/**
 * "Subtitles" section embedded in a Sonarr episode or Radarr movie detail screen, backed by
 * the selected Bazarr instance. Renders nothing when no Bazarr instance is configured or the
 * item isn't tracked by Bazarr, so it's safe to drop into any detail screen unconditionally.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BazarrSubtitlesSection(
    target: BazarrMediaTarget,
    modifier: Modifier = Modifier,
    viewModel: BazarrMediaSubtitlesViewModel = koinInjectParams(target)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()

    // Hide entirely when Bazarr isn't configured or isn't tracking this item.
    if (state is BazarrSubtitlesUiState.NoInstance || state is BazarrSubtitlesUiState.NotTracked) {
        return
    }

    var showSearch by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = mokoString(MR.strings.bazarr_subtitles),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        when (val s = state) {
            BazarrSubtitlesUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }

            is BazarrSubtitlesUiState.Error -> {
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }

            is BazarrSubtitlesUiState.Success -> {
                if (s.present.isEmpty()) {
                    Text(mokoString(MR.strings.bazarr_no_subtitles), style = MaterialTheme.typography.bodyMedium)
                } else {
                    s.present.forEach { subtitle ->
                        PresentSubtitleRow(
                            subtitle = subtitle,
                            onDelete = { viewModel.delete(subtitle) }
                        )
                    }
                }

                if (s.missing.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(mokoString(MR.strings.bazarr_missing_subtitles), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        s.missing.forEach { language ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SubtitleLanguageChip(language)
                                TextButton(onClick = { viewModel.autoSearch(language) }) {
                                    Text(mokoString(MR.strings.bazarr_auto_search))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showSearch = true }) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(mokoString(MR.strings.bazarr_search_subtitles))
                }
            }

            else -> Unit
        }

        if (operationState is OperationStatus.InProgress) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(mokoString(MR.strings.bazarr_auto_search), style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (showSearch) {
        BazarrSubtitleSearchSheet(
            target = target,
            onDismiss = {
                showSearch = false
                viewModel.load()
            }
        )
    }
}

@Composable
private fun PresentSubtitleRow(
    subtitle: BazarrSubtitle,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubtitleLanguageChip(
            label = buildString {
                append(subtitle.code2.uppercase().ifBlank { subtitle.name })
                if (subtitle.forced) append(" · Forced")
                if (subtitle.hi) append(" · HI")
            }
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = subtitle.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (subtitle.isExternal) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = mokoString(MR.strings.bazarr_delete_subtitle),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
