package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun DashboardProwlarrSection(
    state: CombinedDashboardState.Success,
    isEditing: Boolean,
) {
    val prowlarrStats = state.prowlarrStats

    val totalHealthyIndexers = prowlarrStats.sumOf { it.healthyIndexers }
    val totalFailingIndexers = prowlarrStats.sumOf { it.failingIndexers }

    val containerColor by animateColorAsState(
        targetValue =
            if (isEditing || prowlarrStats.isEmpty()) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                Color.Transparent
            },
        label = "ProwlarrCardBackgroundAnimation",
    )

    val internalPadding by animateDpAsState(
        targetValue = if (isEditing || prowlarrStats.isEmpty()) 16.dp else 0.dp,
        label = "ProwlarrCardPaddingAnimation",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor,
            ),
        border =
            if (isEditing ||
                prowlarrStats.isEmpty()
            ) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            } else {
                null
            },
    ) {
        Column(
            modifier = Modifier.padding(internalPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = isEditing || prowlarrStats.isEmpty(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(
                        painter = painterResource(InstanceType.Prowlarr.icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = mokoString(MR.strings.dashboard_prowlarr_overview),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (prowlarrStats.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.no_type_instances_message, InstanceType.Prowlarr.name),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CountStatItem(
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f),
                        label = mokoString(MR.strings.healthy_indexers),
                        count = totalHealthyIndexers,
                        iconColor = ArrGreen,
                    )
                    CountStatItem(
                        icon = Icons.Default.Error,
                        modifier = Modifier.weight(1f),
                        label = mokoString(MR.strings.failing_indexers),
                        count = totalFailingIndexers,
                        iconColor =
                            if (totalFailingIndexers > 0) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                        containerColor =
                            if (totalFailingIndexers > 0) {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                    )
                }
            }
        }
    }
}
