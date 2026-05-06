package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrPurple
import com.dnfapps.arrmatey.ui.theme.TranslucentBlackDarker
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.GridDensity
import com.dnfapps.arrmatey.utils.GridSpacing
import com.dnfapps.arrmatey.utils.PosterElevation
import com.dnfapps.arrmatey.utils.PosterRadius

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
    posterRadius: PosterRadius = PosterRadius.Medium
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = gridDensity.minSize),
        contentPadding = PaddingValues(gridSpacing.spacing),
        horizontalArrangement = Arrangement.SpaceBetween,
        userScrollEnabled = userScrollEnabled
    ) {
        items(items) { item ->
            val isActive = itemIsActive(item)
            PosterItem(
                aspectRatio = aspectRatio,
                radius = posterRadius,
                elevation = posterElevation,
                item = item,
                onItemClick = onItemClick,
                modifier = Modifier.padding(gridSpacing.spacing),
                additionalContent = {
                    if (showOverlay && item.id != null) {
                        PosterGridItemOverlay(
                            monitored = item.monitored,
                            progress = { item.statusProgress },
                            statusColor = if (isActive) ArrPurple else item.statusColor
                        )
                    }
                },
                showFooter = showFullDetails
            )
        }
    }
}

@Composable
fun BoxScope.PosterGridItemOverlay(
    monitored: Boolean = true,
    progress: () -> Float = { 0.6f },
    statusColor: Color = ArrBlue
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(.5f)
            .background(
                brush = Brush.verticalGradient(
                    listOf(TranslucentBlackDarker, Color.Transparent)
                )
            )
    )
    Icon(
        imageVector = if (monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
        contentDescription = null,
        modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
        tint = Color.White
    )
    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .height(6.dp),
        color = statusColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}