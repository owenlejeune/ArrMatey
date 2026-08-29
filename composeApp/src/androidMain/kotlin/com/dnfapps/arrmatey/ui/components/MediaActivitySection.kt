package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.tabs.ActivityItem
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun MediaActivitySection(
    queueItems: List<QueueItem>,
    onQueueItemClicked: (QueueItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Text(
            text = mokoString(MR.strings.activity),
            style = MaterialTheme.typography.titleLarge,
        )

        queueItems.forEach { item ->
            ActivityItem(
                item = item,
                onClick = { onQueueItemClicked(item) },
            )
        }
    }
}
