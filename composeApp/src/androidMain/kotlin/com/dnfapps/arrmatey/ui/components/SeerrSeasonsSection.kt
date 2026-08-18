package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.seerr.api.model.Season
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.screens.EpisodeCard
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun SeerrSeasonsSection(
    seasons: List<Season>,
    modifier: Modifier = Modifier
) {
    if (seasons.isEmpty()) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = mokoString(MR.strings.seasons_header),
            style = MaterialTheme.typography.titleLarge
        )
        seasons.forEach { season ->
            var expanded by rememberSaveable { mutableStateOf(false) }
            val iconRotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "iconRotation"
            )
            ContainerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (season.seasonNumber == 0) {
                            mokoString(MR.strings.specials)
                        } else {
                            mokoString(MR.strings.season_label, season.seasonNumber)
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = mokoPlural(MR.plurals.episodes, season.episodeCount),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.ExpandCircleDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(iconRotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    season.episodes.forEachIndexed { index, episode ->
                        EpisodeCard(
                            episode,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        if (index < season.episodeCount - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    horizontal = 24.dp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
