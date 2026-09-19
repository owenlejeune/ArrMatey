package com.dnfapps.arrmatey.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun MediaFeaturesPage(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = mokoString(MR.strings.onboarding_features_media_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = mokoString(MR.strings.onboarding_features_media_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        FeatureCard(
            icon = Icons.Default.VideoLibrary,
            title = mokoString(MR.strings.onboarding_feature_unified_library_title),
            description = mokoString(MR.strings.onboarding_feature_unified_library_desc),
            tint = MaterialTheme.colorScheme.primary,
        )

        FeatureCard(
            icon = Icons.Default.AutoAwesome,
            title = mokoString(MR.strings.onboarding_feature_cross_service_title),
            description = mokoString(MR.strings.onboarding_feature_cross_service_desc),
            tint = MaterialTheme.colorScheme.secondary,
        )

        FeatureCard(
            icon = Icons.Default.Downloading,
            title = mokoString(MR.strings.onboarding_feature_activity_title),
            description = mokoString(MR.strings.onboarding_feature_activity_desc),
            tint = MaterialTheme.colorScheme.tertiary,
        )

        FeatureCard(
            icon = Icons.Default.CalendarMonth,
            title = mokoString(MR.strings.onboarding_feature_calendar_title),
            description = mokoString(MR.strings.onboarding_feature_calendar_desc),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun PowerFeaturesPage(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = mokoString(MR.strings.onboarding_features_power_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = mokoString(MR.strings.onboarding_features_power_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        FeatureCard(
            icon = Icons.Default.Search,
            title = mokoString(MR.strings.onboarding_feature_search_indexers_title),
            description = mokoString(MR.strings.onboarding_feature_search_indexers_desc),
            tint = MaterialTheme.colorScheme.secondary,
        )

        FeatureCard(
            icon = Icons.Default.Wifi,
            title = mokoString(MR.strings.onboarding_feature_local_network_title),
            description = mokoString(MR.strings.onboarding_feature_local_network_desc),
            tint = MaterialTheme.colorScheme.tertiary,
        )

        FeatureCard(
            icon = Icons.Default.Dns,
            title = mokoString(MR.strings.onboarding_feature_multi_instance_title),
            description = mokoString(MR.strings.onboarding_feature_multi_instance_desc),
            tint = MaterialTheme.colorScheme.primary,
        )

        FeatureCard(
            icon = Icons.Default.Lock,
            title = mokoString(MR.strings.onboarding_feature_backup_sync_title),
            description = mokoString(MR.strings.onboarding_feature_backup_sync_desc),
            tint = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    tint: Color,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
