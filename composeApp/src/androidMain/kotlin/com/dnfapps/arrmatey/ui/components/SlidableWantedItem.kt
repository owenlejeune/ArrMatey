package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.interactive
import com.dnfapps.arrmatey.shared.search
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun SlidableWantedItem(
    onAutomaticSearch: () -> Unit,
    onInteractiveSearch: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val offsetSize = 180f

    val offset by animateFloatAsState(if (isExpanded) -offsetSize else 0f)

    Box(
        modifier =
        modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable {
                isExpanded = !isExpanded
            },
    ) {
        Row(
            modifier =
            Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        Icons.Default.Search to mokoString(MR.strings.search),
                        Icons.Default.Person to mokoString(MR.strings.interactive),
                    ).forEachIndexed { index, (icon, text) ->
                        Box(
                            modifier =
                            Modifier
                                .height(80.dp)
                                .widthIn(min = 80.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .clickable {
                                    isExpanded = false
                                    when (index) {
                                        0 -> onAutomaticSearch()
                                        1 -> onInteractiveSearch()
                                    }
                                },
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.Center).padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = offset.dp.toPx()
                },
        ) {
            content()
        }
    }
}
