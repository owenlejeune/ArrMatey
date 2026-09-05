package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrYellow
import com.dnfapps.arrmatey.ui.theme.surfaceLight
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun BazarrSection(
    state: CombinedDashboardState.Success,
    isEditing: Boolean,
) {
    val bazarrStats = state.bazarrStats

    val totalEpisodes = bazarrStats.sumOf { it.wantedEpisodesCount }
    val totalMovies = bazarrStats.sumOf { it.wantedMoviesCount }

    val containerColor by animateColorAsState(
        targetValue =
            if (isEditing) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                Color.Transparent
            },
        label = "BazarrCardBackgroundAnimation",
    )

    val internalPadding by animateDpAsState(
        targetValue = if (isEditing) 16.dp else 0.dp,
        label = "BazarrCardPaddingAnimation",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor,
            ),
    ) {
        Column(
            modifier = Modifier.padding(internalPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = isEditing,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(
                        painter = painterResource(InstanceType.Bazarr.icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = mokoString(MR.strings.dashboard_bazarr_overview),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CountStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Tv,
                    label = mokoString(MR.strings.bazarr_wanted_episodes),
                    count = totalEpisodes,
                    containerColor = ArrBlue,
                    contentColor = surfaceLight,
                )
                CountStatItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Movie,
                    label = mokoString(MR.strings.bazarr_wanted_movies),
                    count = totalMovies,
                    containerColor = ArrYellow,
                    contentColor = surfaceLight,
                )
            }
        }
    }
}
