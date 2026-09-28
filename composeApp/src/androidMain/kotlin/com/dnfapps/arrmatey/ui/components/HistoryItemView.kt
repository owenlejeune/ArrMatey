package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.ListenarrHistoryItem
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.compose.utils.singleLanguageLabel
import com.dnfapps.arrmatey.entensions.bullet
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun HistoryItemView(
    item: HistoryItem,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            item.instanceType?.associatedColor?.let { color ->
                Box(
                    modifier = Modifier.matchParentSize(),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(6.dp)
                                .fillMaxHeight()
                                .background(color)
                                .align(Alignment.CenterStart),
                    )
                }
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = if (item.instanceType != null) 6.dp else 0.dp)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = mokoString(item.eventType.resource),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = item.date.format("MMM d, yyyy"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    item.instanceName?.takeIf { it.isNotBlank() }?.let { instanceName ->
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ) {
                            Text(
                                text = instanceName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Text(
                    text = item.displayTitle?.breakable() ?: "---",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                val subLabel =
                    buildString {
                        item.quality?.let { quality ->
                            append(quality.qualityLabel)
                            bullet()
                        }
                        if (item !is ListenarrHistoryItem) {
                            append(item.languages.singleLanguageLabel())
                        }
                        item.indexerLabel?.let { indexerLabel ->
                            bullet()
                            append(indexerLabel)
                        }
                        if (item is ListenarrHistoryItem) {
                            append(item.message)
                        }
                    }
                subLabel.takeUnless { it.isEmpty() }?.let { subLabel ->
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
