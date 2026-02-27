package com.dnfapps.arrmatey.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.BuildConfig
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AppIcon
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutCard(
    onFeatureRequestClick: () -> Unit,
    onBugReportClick: () -> Unit,
    onGitHubClick: () -> Unit,
    onDonateClick: () -> Unit,
    onLibrariesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppIcon(
                modifier = Modifier
                    .size(48.dp)
                    .padding(top = 8.dp)
            )

            Text(
                text = mokoString(MR.strings.app_name),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = mokoString(MR.strings.version_label, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically)
            ) {
                AssistChip(
                    onClick = onBugReportClick,
                    label = { Text(mokoString(MR.strings.bug_report)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                AssistChip(
                    onClick = onFeatureRequestClick,
                    label = { Text(mokoString(MR.strings.feature_request)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.NewReleases,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                AssistChip(
                    onClick = onDonateClick,
                    label = { Text(mokoString(MR.strings.donate)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                AssistChip(
                    onClick = onGitHubClick,
                    label = { Text(mokoString(MR.strings.github)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(MR.images.github),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                AssistChip(
                    onClick = onLibrariesClick,
                    label = { Text(mokoString(MR.strings.libraries)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}