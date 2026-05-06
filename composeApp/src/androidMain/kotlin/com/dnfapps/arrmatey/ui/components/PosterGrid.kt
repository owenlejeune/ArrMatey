package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.ui.theme.ArrPurple
import com.dnfapps.arrmatey.ui.theme.TranslucentBlackDarker
import com.dnfapps.arrmatey.utils.AspectRatio

@Composable
fun PosterGrid(
    aspectRatio: AspectRatio,
    items: List<ArrMedia>,
    onItemClick: (ArrMedia) -> Unit,
    itemIsActive: (ArrMedia) -> Boolean,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    showFullDetails: Boolean = false,
    showOverlay: Boolean = true
) {
    val windowInfo = LocalWindowInfo.current
    val screenWidth = windowInfo.containerDpSize.width

    // divide by 4 for min 3 columns + spacing
    val minPosterSize = minOf(120.dp, screenWidth/4)
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = minPosterSize),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        userScrollEnabled = userScrollEnabled
    ) {
        items(items) { item ->
            val isActive = itemIsActive(item)
            PosterItem(
                aspectRatio = aspectRatio,
                item = item,
                onItemClick = onItemClick,
                modifier = Modifier.padding(8.dp),
                additionalContent = {
                    if (showOverlay && item.id != null) {
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
                            imageVector = if (item.monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                            tint = Color.White
                        )
                        LinearProgressIndicator(
                            progress = { item.statusProgress },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .height(6.dp),
                            color = if (isActive) ArrPurple else item.statusColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                },
                showFooter = showFullDetails
            )
        }
    }
}