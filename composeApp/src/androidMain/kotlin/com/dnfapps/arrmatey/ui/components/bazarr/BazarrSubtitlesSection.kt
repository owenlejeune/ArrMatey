package com.dnfapps.arrmatey.ui.components.bazarr

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSubtitle
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.bazarr.state.BazarrSubtitlesUiState
import com.dnfapps.arrmatey.bazarr.viewmodel.BazarrMediaSubtitlesViewModel
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

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

    val context = LocalContext.current
    var pendingSubtitle by remember { mutableStateOf<BazarrSubtitle?>(null) }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let {
            pendingSubtitle?.let { subtitle ->
                viewModel.downloadToDevice(subtitle) { bytes ->
                    if (bytes != null) {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write(bytes)
                        }
                        Toast.makeText(context, "Subtitle downloaded", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to download subtitle", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = mokoString(MR.strings.bazarr_subtitles),
                style = MaterialTheme.typography.titleLarge
            )
            InstanceType.Bazarr.tabIcon?.let { icon ->
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { showSearch = true }) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(mokoString(MR.strings.bazarr_search_subtitles))
            }
        }

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
                if (s.embedded.isNotEmpty()) {
                    EmbeddedSubtitlesCard(s.embedded)
                }
                s.present.forEach { subtitle ->
                    PresentSubtitleRow(
                        subtitle = subtitle,
                        onDownload = {
                            pendingSubtitle = subtitle
                            val fileName = subtitle.path?.substringAfterLast('/') ?: subtitle.name
                            createDocumentLauncher.launch(fileName)
                        },
                        onDelete = { viewModel.delete(subtitle) }
                    )
                }

                s.missing.forEach { language ->
                    ContainerCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.autoSearch(language)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SubtitleLanguageChip(language)
                            Text(
                                text = mokoString(MR.strings.missing),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.Search, null)
                        }
                    }
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
private fun EmbeddedSubtitlesCard(
    embedded: List<BazarrSubtitle>
) {
    ContainerCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = mokoString(MR.strings.bazarr_embedded_count, embedded.count()),
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            embedded.forEach { subtitle ->
                SubtitleLanguageChip(
                    label = buildString {
                        append(subtitle.code2.orEmpty().uppercase().ifBlank { subtitle.name })
                        if (subtitle.forced) append(" · Forced")
                        if (subtitle.hi) append(" · HI")
                    }
                )
            }
        }
    }
}

@Composable
private fun PresentSubtitleRow(
    subtitle: BazarrSubtitle,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    ContainerCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = buildString {
                    append(subtitle.name)
                    subtitle.path?.let { path ->
                        append(" • ${path.breakable()}")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            SubtitleLanguageChip(
                label = buildString {
                    append(subtitle.code2.orEmpty().uppercase().ifBlank { subtitle.name })
                    if (subtitle.forced) append(" · Forced")
                    if (subtitle.hi) append(" · HI")
                }
            )
            Row {
                if (subtitle.isExternal) {
                    IconButton(onClick = onDownload) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = mokoString(MR.strings.bazarr_download_subtitle),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
    }
}
