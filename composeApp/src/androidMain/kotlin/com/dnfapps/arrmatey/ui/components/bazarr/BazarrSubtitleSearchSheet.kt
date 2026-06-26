package com.dnfapps.arrmatey.ui.components.bazarr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.bazarr.api.model.ProviderSubtitle
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.bazarr.state.SubtitleSearchState
import com.dnfapps.arrmatey.bazarr.viewmodel.BazarrSubtitleSearchViewModel
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString

/**
 * Modal sheet listing manual provider subtitle results for a single episode or movie,
 * with a per-row download action. Reused from both the Wanted lists and the detail screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazarrSubtitleSearchSheet(
    target: BazarrMediaTarget,
    onDismiss: () -> Unit,
    viewModel: BazarrSubtitleSearchViewModel = koinInjectParams(target)
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val downloadStates by viewModel.downloadStates.collectAsStateWithLifecycle()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = mokoString(MR.strings.bazarr_search_subtitles),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.size(12.dp))

            when (val state = searchState) {
                SubtitleSearchState.Idle,
                SubtitleSearchState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is SubtitleSearchState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }

                is SubtitleSearchState.Success -> {
                    if (state.results.isEmpty()) {
                        Text(mokoString(MR.strings.bazarr_no_results))
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 480.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.results, key = { "${it.provider}:${it.subtitle}" }) { result ->
                                SearchResultRow(
                                    result = result,
                                    status = downloadStates["${result.provider}:${result.subtitle}"],
                                    onDownload = { viewModel.download(result) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    result: ProviderSubtitle,
    status: OperationStatus?,
    onDownload: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SubtitleLanguageChip(label = result.languageLabel())
                Text(result.provider, fontWeight = FontWeight.SemiBold)
                Text("· ${result.score}", style = MaterialTheme.typography.labelMedium)
            }
            result.releaseInfo.firstOrNull()?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
        }
        Spacer(Modifier.width(8.dp))
        when (status) {
            OperationStatus.InProgress -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
            is OperationStatus.Success -> Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            else -> Button(onClick = onDownload) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(mokoString(MR.strings.bazarr_download_subtitle))
            }
        }
    }
}

private fun ProviderSubtitle.languageLabel(): String = buildString {
    append(language.uppercase())
    if (isForced) append(" · Forced")
    if (isHearingImpaired) append(" · HI")
}
