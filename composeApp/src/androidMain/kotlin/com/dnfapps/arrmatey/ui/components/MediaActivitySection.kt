package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.shared.*

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
        queueItems.forEach { item ->
            ActivityItem(
                item = item,
                onClick = { onQueueItemClicked(item) },
            )
        }
    }
}
