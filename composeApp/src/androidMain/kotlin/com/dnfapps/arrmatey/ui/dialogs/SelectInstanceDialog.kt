package com.dnfapps.arrmatey.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.usecase.ResolvedMediaDestination
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun SelectInstanceDialog(
    destinations: List<ResolvedMediaDestination>,
    onSelect: (ResolvedMediaDestination) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = mokoString(MR.strings.select_instance),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn {
                items(destinations) { dest ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = dest.instance.label,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        supportingContent = {
                            Text(
                                text = dest.instance.url,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingContent = {
                            dest.instance.type.tabIcon?.let { icon ->
                                Icon(
                                    painter = painterResource(icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(dest) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(mokoString(MR.strings.cancel))
            }
        }
    )
}
