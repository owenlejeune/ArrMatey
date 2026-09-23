package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.InstanceTypeIdentifiable
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun RecentlyAddedSection(
    state: CombinedDashboardState.Success,
    onOpenItem: (Long, InstanceType) -> Unit,
    enabled: Boolean,
) {
    val items = state.recentlyAdded

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Icon(
                    Icons.Default.History,
                    null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = mokoString(MR.strings.recently_added),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (state.instances.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.no_type_instances_message, "Arr"),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else if (items.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.nothing_recently_added),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = enabled,
                ) {
                    items(items) { item ->
                        val type = (item as InstanceTypeIdentifiable).instanceType
                        val posterModel =
                            if (item is MockMedia) {
                                type.mockCover?.let { painterResource(it) }
                            } else {
                                null
                            }
                        PosterItem(
                            item = item,
                            modifier = Modifier.width(120.dp),
                            posterModel = posterModel,
                            onItemClick = {
                                onOpenItem(item.id ?: 0, type)
                            },
                            showFooter = true,
                            additionalContent = {
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
                                            imageVector =
                                                if (item.monitored) {
                                                    Icons.Default.Bookmark
                                                } else {
                                                    Icons.Default.BookmarkBorder
                                                },
                                            contentDescription =
                                                mokoString(
                                                    if (item.monitored) MR.strings.monitored else MR.strings.unmonitored,
                                                ),
                                            tint =
                                                if (item.monitored) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    shadowElevation = 2.dp,
                                    modifier =
                                        Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp),
                                ) {
                                    Box(
                                        modifier = Modifier.padding(4.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Image(
                                            painter = painterResource(type.icon),
                                            contentDescription = type.name,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
