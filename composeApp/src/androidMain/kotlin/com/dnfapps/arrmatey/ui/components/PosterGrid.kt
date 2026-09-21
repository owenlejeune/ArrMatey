package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrPurple
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.GridDensity
import com.dnfapps.arrmatey.utils.GridSpacing
import com.dnfapps.arrmatey.utils.MultiSelectState
import com.dnfapps.arrmatey.utils.PosterElevation
import com.dnfapps.arrmatey.utils.PosterRadius
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun PosterGrid(
    aspectRatio: AspectRatio,
    items: List<ArrMedia>,
    onItemClick: (ArrMedia) -> Unit,
    itemIsActive: (ArrMedia) -> Boolean,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    showFullDetails: Boolean = false,
    showOverlay: Boolean = true,
    gridDensity: GridDensity = GridDensity.Normal,
    gridSpacing: GridSpacing = GridSpacing.Medium,
    posterElevation: PosterElevation = PosterElevation.Medium,
    posterRadius: PosterRadius = PosterRadius.Medium,
    multiSelectState: MultiSelectState<Long> = MultiSelectState(selectionModeAvailable = false),
) {
    val bottomPadding = LocalFloatingBarBottomPadding.current

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = gridDensity.minSize),
        contentPadding =
            PaddingValues(
                start = gridSpacing.spacing,
                top = gridSpacing.spacing,
                end = gridSpacing.spacing,
                bottom = gridSpacing.spacing + bottomPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(gridSpacing.spacing),
        verticalArrangement = Arrangement.spacedBy(gridSpacing.spacing),
        userScrollEnabled = userScrollEnabled,
    ) {
        items(items) { item ->
            val isActive = itemIsActive(item)
            PosterItem(
                aspectRatio = aspectRatio,
                radius = posterRadius,
                elevation = posterElevation,
                item = item,
                onItemClick = onItemClick,
                additionalContent = {
                    if (showOverlay && item.id != null) {
                        PosterGridItemOverlay(
                            monitored = item.monitored,
                            progress = { item.statusProgress },
                            statusColor = if (isActive) ArrPurple else item.statusColor,
                        )
                    }
                },
                showFooter = showFullDetails,
                multiSelectState = multiSelectState,
            )
        }
    }
}

@Composable
fun BoxScope.PosterGridItemOverlay(
    monitored: Boolean = true,
    progress: () -> Float = { 0.6f },
    statusColor: Color = ArrBlue,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp,
        modifier =
            Modifier
                .align(Alignment.TopStart)
                .padding(6.dp),
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = mokoString(if (monitored) MR.strings.monitored else MR.strings.unmonitored),
                tint = if (monitored) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
    LinearProgressIndicator(
        progress = progress,
        modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .height(4.dp)
                .clip(CircleShape),
        color = statusColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    )
}
