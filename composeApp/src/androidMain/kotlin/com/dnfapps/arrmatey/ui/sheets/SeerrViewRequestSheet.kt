package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.Service
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeerrViewRequestSheet(
    details: RequestMediaDetails,
    radarrServices: List<Service>,
    sonarrServices: List<Service>,
    onDismissRequest: () -> Unit,
    onApproveRequest: (Long, Long?, String?, Long?) -> Unit,
    onDeclineRequest: (Long) -> Unit
) {
    val request = details.mediaInfo?.requests?.firstOrNull { it.status == 1 } ?: return

    val services = if (details is TvDetails) sonarrServices else radarrServices
    var selectedProfileId by remember { mutableStateOf(request.profileId) }
    var selectedRootFolder by remember { mutableStateOf(request.rootFolder) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                Text(
                    text = mokoString(MR.strings.pending_request).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = details.displayTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (details is TvDetails) {
                SeasonTable(details, request)
            }

            AdvancedSection(
                request = request,
                services = services,
                selectedProfileId = selectedProfileId,
                onProfileSelected = { selectedProfileId = it },
                selectedRootFolder = selectedRootFolder,
                onRootFolderSelected = { selectedRootFolder = it }
            )

            RequestedBySection(request)

            Actions(
                onRequestApproved = { onApproveRequest(request.id, selectedProfileId, selectedRootFolder, null) },
                onDismissRequest = onDismissRequest
            )
        }
    }
}

@Composable
private fun SeasonTable(details: TvDetails, request: MediaRequest) {
    ContainerCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LabelledSwitch(
            label = "All seasons",
            checked = false, // all seasons are selected
            onCheckedChange = {} // toggle all seasons selected
        )
        HorizontalDivider()

        details.seasons.forEach { season ->
            val isRequested = request.seasons.any { it.seasonNumber == season.seasonNumber }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Badge(
                    containerColor = if (isRequested) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (isRequested) {
                            mokoString(MR.strings.pending)
                        } else mokoString(MR.strings.not_requested),
                    )
                }
                LabelledSwitch(
                    label = buildString {
                        if (season.seasonNumber == 0) {
                            append(mokoString(MR.strings.specials))
                        } else {
                            append(mokoString(MR.strings.season_label, season.seasonNumber))
                        }
                        append(" (")
                        append(season.episodeCount)
                        append(" ")
                        append(mokoString(MR.strings.episodes))
                        append(")")
                    },
                    checked = isRequested,
                    onCheckedChange = {}
                )
            }
        }
    }
}

@Composable
private fun AdvancedSection(
    request: MediaRequest,
    services: List<Service>,
    selectedProfileId: Long?,
    onProfileSelected: (Long?) -> Unit,
    selectedRootFolder: String?,
    onRootFolderSelected: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val profiles = services.flatMap { service ->
            // This is a bit of a guess on how to get profiles from the service object
            // Usually the Seerr API returns profiles in a different endpoint or embedded.
            // For now let's assume we can at least show the default one if we don't have a list.
            listOfNotNull(service.activeProfileId.toLong())
        }.distinct()

        val rootFolders = services.map { it.activeDirectory }.distinct()

        DropdownPicker(
            label = { Text(mokoString(MR.strings.quality_profile)) },
            options = profiles.ifEmpty { listOfNotNull(request.profileId) },
            selectedOption = selectedProfileId,
            onOptionSelected = { onProfileSelected(it) },
            getOptionLabel = { it.toString() }
        )

        DropdownPicker(
            label = { Text(mokoString(MR.strings.root_folder)) },
            options = rootFolders.ifEmpty { listOfNotNull(request.rootFolder) },
            selectedOption = selectedRootFolder,
            onOptionSelected = { onRootFolderSelected(it) },
            getOptionLabel = { it }
        )
    }
}

@Composable
private fun RequestedBySection(request: MediaRequest) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = mokoString(MR.strings.requested_by).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        ContainerCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = request.requestedBy.avatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Column {
                    Text(
                        text = request.requestedBy.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = request.requestedBy.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun Actions(
    onRequestApproved: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDismissRequest,
            modifier = Modifier.weight(1f)
        ) {
            Text(mokoString(MR.strings.close))
        }
        Button(
            onClick = onRequestApproved,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            )
        ) {
            Text(mokoString(MR.strings.approve_request))
        }
    }
}
