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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeerrRequestSheet(
    details: RequestMediaDetails,
    serviceDetails: ServiceDetails?,
    currentUser: SeerrUser?,
    users: List<SeerrUser>,
    requestInProgress: Boolean,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (Long?, String?, Long?, List<Int>?, Long?) -> Unit
) {
    var selectedProfileId by remember { mutableStateOf<Long?>(null) }
    var selectedRootFolder by remember { mutableStateOf<String?>(null) }
    var selectedUserId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(serviceDetails, currentUser) {
        if (selectedProfileId == null) {
            selectedProfileId = serviceDetails?.server?.activeProfileId?.toLong()
        }
        if (selectedRootFolder == null) {
            selectedRootFolder = serviceDetails?.server?.activeDirectory
        }
        if (selectedUserId == null) {
            selectedUserId = currentUser?.id
        }
    }

    var selectedSeasons by remember {
        mutableStateOf(
            if (details is TvDetails) details.seasons.map { it.seasonNumber }.toSet()
            else emptySet()
        )
    }

    val isAdmin = currentUser?.hasPermission(UserPermission.ADMIN) == true

    ModalBottomSheet(
        onDismissRequest = {
            if (!requestInProgress) {
                onDismissRequest()
            }
        },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { !requestInProgress }
        )
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
                    text = mokoString(
                        if (details is TvDetails) MR.strings.type_series else MR.strings.type_movie
                    ).uppercase(),
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
                SeasonSelector(
                    details = details,
                    selectedSeasons = selectedSeasons,
                    onSeasonsChanged = { selectedSeasons = it },
                    enabled = !requestInProgress
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = mokoString(MR.strings.advanced).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                val profiles = serviceDetails?.profiles ?: emptyList()
                val rootFolders = serviceDetails?.rootFolders ?: emptyList()

                DropdownPicker(
                    label = { Text(mokoString(MR.strings.quality_profile)) },
                    options = profiles.map { it.id }.ifEmpty { listOfNotNull(selectedProfileId) },
                    selectedOption = selectedProfileId,
                    onOptionSelected = { selectedProfileId = it },
                    getOptionLabel = { profileId ->
                        profiles.find { it.id == profileId }?.name ?: profileId.toString()
                    },
                    enabled = !requestInProgress
                )

                DropdownPicker(
                    label = { Text(mokoString(MR.strings.root_folder)) },
                    options = rootFolders.map { it.path }.ifEmpty { listOfNotNull(selectedRootFolder) },
                    selectedOption = selectedRootFolder,
                    onOptionSelected = { selectedRootFolder = it },
                    getOptionLabel = { it },
                    enabled = !requestInProgress
                )

                if (isAdmin && users.isNotEmpty()) {
                    DropdownPicker(
                        label = { Text(mokoString(MR.strings.request_as)) },
                        options = users.map { it.id },
                        selectedOption = selectedUserId,
                        onOptionSelected = { selectedUserId = it },
                        getOptionLabel = { userId ->
                            users.find { it.id == userId }?.displayName ?: userId.toString()
                        },
                        enabled = !requestInProgress
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        enabled = !requestInProgress
                    ) {
                        Text(mokoString(MR.strings.cancel))
                    }
                    Button(
                        onClick = {
                            val seasons = if (details is TvDetails) selectedSeasons.toList() else null
                            onSubmitRequest(
                                selectedProfileId,
                                selectedRootFolder,
                                null, // languageProfileId
                                seasons,
                                selectedUserId
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !requestInProgress && if (details is TvDetails) selectedSeasons.isNotEmpty() else true
                    ) {
                        if (requestInProgress) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                        } else {
                            Text(mokoString(MR.strings.request))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeasonSelector(
    details: TvDetails,
    selectedSeasons: Set<Int>,
    onSeasonsChanged: (Set<Int>) -> Unit,
    enabled: Boolean = true
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
            },
            enabled = enabled
        )
        HorizontalDivider()

        details.seasons.forEach { season ->
            val isSelected = season.seasonNumber in selectedSeasons

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
                },
                enabled = enabled
            )
        }
    }
}
