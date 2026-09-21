package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.MediaFile
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.BULLET
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
) {
    val state = rememberSwipeToDismissBoxState()
    val deleteSwipeBackground = MaterialTheme.colorScheme.error

    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete?.invoke()
        }
        state.snapTo(SwipeToDismissBoxValue.Settled)
    }

    SwipeToDismissBox(
        modifier = modifier,
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = onDelete != null,
        backgroundContent = {
            if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawRoundRect(
                                    color = deleteSwipeBackground,
                                    cornerRadius = CornerRadius(16.dp.toPx()),
                                )
                            }.wrapContentSize(Alignment.CenterEnd)
                            .padding(12.dp),
                )
            }
        },
        onDismiss = {},
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
