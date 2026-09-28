package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.bullet
import com.dnfapps.arrmatey.isDebug
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun QueueItemInfoSheet(
    onDismiss: () -> Unit,
    onRemove: () -> Unit,
    item: QueueItem,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
        ) {
            Text(
                text = item.titleLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = item.title ?: mokoString(MR.strings.unknown),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            val statusRow =
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium,
                        ),
                    ) {
                        append(item.statusLabel)
                    }
                    bullet()
                    append(item.quality.qualityLabel)
                    bullet()
                    append(item.size.toLong().bytesAsFileSizeString())
                }

            Text(text = statusRow, modifier = Modifier.padding(vertical = 4.dp))

            item.remainingTimeLabel?.let { remainingTime ->
                Column(
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "$remainingTime left",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = item.progressLabel,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { item.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                    )
                }
            }

            val chipItems =
                listOfNotNull(
                    item.scoreLabel,
                ) + item.customFormats.map { it.name }
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                chipItems.forEach { chipItem ->
                    Box(
                        modifier =
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = MaterialTheme.shapes.small,
                            ),
                    ) {
                        Text(
                            chipItem,
                            modifier = Modifier.padding(vertical = 2.dp, horizontal = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            item.errorMessage?.let { errorMessage ->
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Text(
                        errorMessage,
                        modifier =
                            Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp,
                            ),
                    )
                }
            } ?: item.statusMessages.forEach { status ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    ) {
                        Text(
                            text = status.title ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        status.messages.forEach { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            )
                        }
                    }
                }
            }

            val infoItems =
                mapOf(
                    MR.strings.protocol to item.protocol.name,
                    MR.strings.download_client to item.downloadClient,
                    MR.strings.indexer to item.indexer,
                    MR.strings.languages to item.languageLabels.takeUnless { it.isEmpty() }?.joinToString(", "),
                    MR.strings.added to item.added?.format(),
                    MR.strings.destination to item.outputPath,
                )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                modifier = Modifier.heightIn(max = 1000.dp),
                userScrollEnabled = false,
            ) {
                infoItems.forEach { (key, value) ->
                    value?.let {
                        item {
                            Text(
                                text = mokoString(key),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        item {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onRemove,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                    Text(
                        text = mokoString(MR.strings.remove),
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                ) {
                    if (isDebug() && item.needsManualImport) {
                        Button(
                            onClick = {
                                // todo
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                            )
                            Text(
                                text = mokoString(MR.strings.manual_import),
                            )
                        }
                    }
                }
            }
        }
    }
}
