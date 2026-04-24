package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeerrViewRequestSheet(
    details: RequestMediaDetails,
    serviceDetails: ServiceDetails?,
    onDismissRequest: () -> Unit,
    onApproveRequest: (Long, Long?, String?, Long?, List<Int>?) -> Unit,
    onDeclineRequest: (Long) -> Unit
) {
    val request = details.mediaInfo?.requests?.firstOrNull { it.status == 1 } ?: return

    var selectedProfileId by remember { mutableStateOf<Long?>(null) }
    var selectedRootFolder by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(request, serviceDetails) {
        if (selectedProfileId == null) {
            selectedProfileId = request.profileId
                ?: serviceDetails?.server?.activeProfileId?.toLong()
        }
        if (selectedRootFolder == null) {
            selectedRootFolder = request.rootFolder
                ?: serviceDetails?.server?.activeDirectory
        }
    }

    val initialSeasons = remember(request.seasons) {
        request.seasons.map { it.seasonNumber }.toSet()
    }
    var selectedSeasons by remember { mutableStateOf(initialSeasons) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp)
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
                SeasonTable(
                    details = details,
                    selectedSeasons = selectedSeasons,
                    onSeasonsChanged = { selectedSeasons = it }
                )
            }

            RequestedBySection(request)

            AdvancedSection(
                serviceDetails = serviceDetails,
                selectedProfileId = selectedProfileId,
                onProfileSelected = { selectedProfileId = it },
                selectedRootFolder = selectedRootFolder,
                onRootFolderSelected = { selectedRootFolder = it },
                onApproveRequest = {
                    val seasons = if (details is TvDetails) selectedSeasons.toList() else null
                    onApproveRequest(
                        request.id,
                        selectedProfileId,
                        selectedRootFolder,
                        request.languageProfileId,
                        seasons
                    )
                },
                onDeclineRequest = { onDeclineRequest(request.id) }
            )
        }
    }
}

@Composable
private fun SeasonTable(
    details: TvDetails,
    selectedSeasons: Set<Int>,
    onSeasonsChanged: (Set<Int>) -> Unit
) {
    ContainerCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val allSeasonsSelected = details.seasons.all { it.seasonNumber in selectedSeasons }

        LabelledSwitch(
            label = mokoString(MR.strings.all_seasons),
            checked = allSeasonsSelected,
            onCheckedChange = { checked ->
                if (checked) {
                    onSeasonsChanged(details.seasons.map { it.seasonNumber }.toSet())
                } else {
                    onSeasonsChanged(emptySet())
                }
            }
        )
        HorizontalDivider()

        details.seasons.forEach { season ->
            val isSelected = season.seasonNumber in selectedSeasons

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Badge(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Text(
                        text = if (isSelected) {
                            mokoString(MR.strings.pending)
                        } else mokoString(MR.strings.not_requested),
                        modifier = Modifier.padding(2.dp)
                    )
                }
                LabelledSwitch(
                    label = if (season.seasonNumber == 0) {
                        mokoString(MR.strings.specials)
                    } else {
                        mokoString(MR.strings.season_label, season.seasonNumber)
                    },
                    sublabel = mokoPlural(MR.plurals.episodes, season.episodeCount),
                    checked = isSelected,
                    onCheckedChange = { checked ->
                        if (checked) {
                            onSeasonsChanged(selectedSeasons + season.seasonNumber)
                        } else {
                            onSeasonsChanged(selectedSeasons - season.seasonNumber)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AdvancedSection(
    serviceDetails: ServiceDetails?,
    selectedProfileId: Long?,
    onProfileSelected: (Long?) -> Unit,
    selectedRootFolder: String?,
    onRootFolderSelected: (String?) -> Unit,
    onApproveRequest: () -> Unit,
    onDeclineRequest: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val profiles = serviceDetails?.profiles ?: emptyList()
        val rootFolders = serviceDetails?.rootFolders ?: emptyList()

        DropdownPicker(
            label = { Text(mokoString(MR.strings.quality_profile)) },
            options = profiles.map { it.id }.ifEmpty { listOfNotNull(selectedProfileId) },
            selectedOption = selectedProfileId,
            onOptionSelected = { onProfileSelected(it) },
            getOptionLabel = { profileId ->
                profiles.find { it.id == profileId }?.name ?: profileId.toString()
            }
        )

        DropdownPicker(
            label = { Text(mokoString(MR.strings.root_folder)) },
            options = rootFolders.map { it.path }.ifEmpty { listOfNotNull(selectedRootFolder) },
            selectedOption = selectedRootFolder,
            onOptionSelected = { onRootFolderSelected(it) },
            getOptionLabel = { it }
        )

        Button(
            onClick = onApproveRequest,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            )
        ) {
            Text(mokoString(MR.strings.approve_request))
        }

        OutlinedButton(
            onClick = onDeclineRequest,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(mokoString(MR.strings.decline_request))
        }
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


