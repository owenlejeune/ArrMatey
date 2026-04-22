package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.model.IconSource
import com.dnfapps.arrmatey.model.SettingItem
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun SettingsGroup(
    title: String? = null,
    items: List<SettingItem>
) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items.forEachIndexed { index, item ->
            val total = items.size
            val baseShape = MaterialTheme.shapes.extraLarge
            val shape = when {
                total == 1 -> baseShape
                index == 0 -> baseShape.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp))
                index == total - 1 -> baseShape.copy(topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp))
                else -> RectangleShape
            }

            SettingsRow(
                item = item,
                shape = shape
            )
        }
    }
}

@Composable
fun SettingsRow(
    item: SettingItem,
    shape: Shape
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = item.backgroundColor ?: MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = item.onClick
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (val source = item.icon) {
                        is IconSource.Vector -> Icon(
                            imageVector = source.imageVector,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                        is IconSource.Resource -> Image(
                            painter = painterResource(source.resource),
                            contentDescription = null
                        )
                        is IconSource.Radio -> RadioButton(
                            selected = source.selected,
                            onClick = item.onClick
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        item.titleExtraContent?.let {
                            Spacer(modifier = Modifier.width(8.dp))
                            it()
                        }
                    }
                    item.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item.trailingContent?.let {
                    Spacer(modifier = Modifier.width(16.dp))
                    it()
                }
            }
        }
    }
}