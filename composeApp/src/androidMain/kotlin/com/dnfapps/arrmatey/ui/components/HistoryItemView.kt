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
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.compose.utils.singleLanguageLabel
import com.dnfapps.arrmatey.entensions.bullet
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun HistoryItemView(
    item: HistoryItem
) {
    ContainerCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = mokoString(item.eventType.resource),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
            Text(
                text = item.date.format("MMM d, yyyy"),
                fontSize = 12.sp
            )
        }
        Text(
            text = item.displayTitle?.breakable() ?: "---",
            fontWeight = FontWeight.SemiBold
        )

        val subLabel = buildString {
            append(item.quality.qualityLabel)
            bullet()
            append(item.languages.singleLanguageLabel())
            item.indexerLabel?.let { indexerLabel ->
                bullet()
                append(indexerLabel)
            }
        }
        Text(
            text = subLabel,
            fontSize = 12.sp
        )
    }
}