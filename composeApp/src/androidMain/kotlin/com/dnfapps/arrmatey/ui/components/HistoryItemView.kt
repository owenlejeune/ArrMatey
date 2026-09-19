package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    ContainerCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = mokoString(item.eventType.resource),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = item.date.format("MMM d, yyyy"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
