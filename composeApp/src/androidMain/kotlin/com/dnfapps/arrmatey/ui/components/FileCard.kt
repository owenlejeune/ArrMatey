package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.MediaFile
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileCard(
    file: MediaFile,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
    swipeSensitivity: Float = 0.60f,
) {
    SwipeToDismissCard(
        modifier = modifier,
        swipeSensitivity = swipeSensitivity,
        enableStartToEnd = false,
        enableEndToStart = onDelete != null,
        onSwipeEndToStart = {
            onDelete?.invoke()
        },
        shape = MaterialTheme.shapes.large,
        backgroundContent = { direction, _ ->
            if (direction == SwipeDirection.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.error)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        },
    ) {
        ContainerCard(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = file.relativePath.breakable(),
                style = MaterialTheme.typography.titleSmallEmphasized,
            )
            Text(
                text =
                listOfNotNull(
                    file.quality?.qualityLabel,
                    file.languages.first().name,
                    file.size.bytesAsFileSizeString(),
                ).joinToString(BULLET),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            file.dateAdded?.format("MMM d, yyyy")?.let { formattedDate ->
                Text(
                    text = mokoString(MR.strings.added_on, formattedDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
