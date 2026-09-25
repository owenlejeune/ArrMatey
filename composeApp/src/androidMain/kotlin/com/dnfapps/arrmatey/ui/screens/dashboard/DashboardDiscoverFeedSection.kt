package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.datastore.DiscoverSectionPreferences
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.discover.model.DiscoverCategory
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.koinInject

@Composable
fun DashboardDiscoverFeedSection(
    state: CombinedDashboardState.Success,
    onMediaClick: (tmdbId: Long, requestType: RequestType) -> Unit,
    isEditing: Boolean = false,
    enabled: Boolean = true,
    preferencesStore: PreferencesStore = koinInject(),
) {
    val sectionPreferences by preferencesStore.discoverSectionPreferences.collectAsStateWithLifecycle(
        initialValue = DiscoverSectionPreferences(),
    )

    val categories =
        remember(sectionPreferences.visibleCategories) {
            val visible = sectionPreferences.visibleCategories
            if (visible.isNotEmpty()) visible else DiscoverCategory.entries
        }

    var selectedCategory by rememberSaveable { mutableStateOf<DiscoverCategory?>(null) }

    val activeCategory = (selectedCategory?.takeIf { it in categories }) ?: categories.firstOrNull() ?: DiscoverCategory.TRENDING

    val currentItems: List<DiscoverResult> = state.getDiscoverFeedItems(activeCategory)

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
            AnimatedVisibility(
                visible = isEditing || state.seerrInstances.isEmpty(),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(InstanceType.Seerr.icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = mokoString(MR.strings.dashboard_discover_feed),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (state.seerrInstances.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.no_type_instances_message, InstanceType.Seerr.name),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = enabled,
                ) {
                    items(categories) { category ->
                        val isSelected = activeCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (enabled) {
                                    selectedCategory = category
                                }
                            },
                            label = {
                                Text(
                                    text = mokoString(category.title),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            leadingIcon = {
                                val icon =
                                    when (category) {
                                        DiscoverCategory.TRENDING -> Icons.AutoMirrored.Filled.TrendingUp
                                        DiscoverCategory.POPULAR_MOVIES -> Icons.Default.Movie
                                        DiscoverCategory.POPULAR_SERIES -> Icons.Default.Tv
                                        DiscoverCategory.UPCOMING_MOVIES,
                                        DiscoverCategory.UPCOMING_SERIES,
                                        -> Icons.Default.CalendarToday
                                    }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                        )
                    }
                }

                AnimatedContent(
                    targetState = currentItems,
                    transitionSpec = {
                        (fadeIn() + slideInHorizontally { it / 4 }) togetherWith (fadeOut() + slideOutHorizontally { -it / 4 })
                    },
                    label = "DiscoverFeedCarouselTransition",
                ) { itemsList ->
                    if (itemsList.isEmpty()) {
                        Text(
                            text = mokoString(MR.strings.no_media_found),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            userScrollEnabled = enabled,
                        ) {
                            items(itemsList) { item ->
                                PosterItem(
                                    item = item,
                                    modifier = Modifier.width(120.dp),
                                    onItemClick = {
                                        if (enabled) {
                                            onMediaClick(item.id, item.mediaType)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
