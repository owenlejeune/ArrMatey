package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.policy.SeerrPermissionPolicy
import com.dnfapps.arrmatey.shared.*
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
    onSubmitRequest: (Long?, String?, Long?, List<Int>?, Long?) -> Unit,
    canSwitchToAddDirectly: Boolean = false,
    instanceTypeName: String? = null,
    onSwitchToAddDirectly: (() -> Unit)? = null,
    canRequest4k: Boolean = false,
    is4k: Boolean = false,
    onIs4kChange: (Boolean) -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = {
            if (!requestInProgress) {
                onDismissRequest()
            }
        },
        sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { !requestInProgress },
        ),
    ) {
        SeerrRequestSheetContent(
            details = details,
            serviceDetails = serviceDetails,
            currentUser = currentUser,
            users = users,
            requestInProgress = requestInProgress,
            onDismissRequest = onDismissRequest,
            onSubmitRequest = onSubmitRequest,
            canSwitchToAddDirectly = canSwitchToAddDirectly,
            instanceTypeName = instanceTypeName,
            onSwitchToAddDirectly = onSwitchToAddDirectly,
            canRequest4k = canRequest4k,
            is4k = is4k,
            onIs4kChange = onIs4kChange,
        )
    }
}

@Composable
fun SeerrRequestSheetContent(
    details: RequestMediaDetails,
    serviceDetails: ServiceDetails?,
    currentUser: SeerrUser?,
    users: List<SeerrUser>,
    requestInProgress: Boolean,
    onDismissRequest: () -> Unit,
    onSubmitRequest: (Long?, String?, Long?, List<Int>?, Long?) -> Unit,
    canSwitchToAddDirectly: Boolean = false,
    instanceTypeName: String? = null,
    onSwitchToAddDirectly: (() -> Unit)? = null,
    canRequest4k: Boolean = false,
    is4k: Boolean = false,
    onIs4kChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
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
            if (details is TvDetails) {
                details.seasons.map { it.seasonNumber }.toSet()
            } else {
                emptySet()
            },
        )
    }

    val requestType = if (details is TvDetails) RequestType.Tv else RequestType.Movie
    val permissions = SeerrPermissionPolicy.getRequestPermissions(currentUser, requestType, is4k)
    val canConfigureAdvanced = permissions.canConfigureAdvanced
    val canRequestForOtherUsers = permissions.canRequestForOtherUsers
    val canRequestThis = permissions.canRequestThis
    val willAutoApprove = permissions.willAutoApprove

    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .animateContentSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column {
            Text(
                text =
                mokoString(
                    if (details is TvDetails) MR.strings.type_series else MR.strings.type_movie,
                ).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = details.displayTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (canSwitchToAddDirectly && onSwitchToAddDirectly != null) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                SegmentedButton(
                    selected = true,
                    onClick = {},
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    label = { Text(mokoString(MR.strings.request)) },
                )
                SegmentedButton(
                    selected = false,
                    onClick = onSwitchToAddDirectly,
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    label = { Text(mokoString(MR.strings.add_to_arr, instanceTypeName ?: "Arr")) },
                )
            }
        }

        SeerrRequestConfigurationContent(
            details = details,
            serviceDetails = serviceDetails,
            canConfigureAdvanced = canConfigureAdvanced,
            canRequestForOtherUsers = canRequestForOtherUsers,
            users = users,
            selectedProfileId = selectedProfileId,
            onSelectedProfileIdChange = { selectedProfileId = it },
            selectedRootFolder = selectedRootFolder,
            onSelectedRootFolderChange = { selectedRootFolder = it },
            selectedUserId = selectedUserId,
            onSelectedUserIdChange = { selectedUserId = it },
            selectedSeasons = selectedSeasons,
            onSelectedSeasonsChange = { selectedSeasons = it },
            canRequest4k = canRequest4k,
            is4k = is4k,
            onIs4kChange = onIs4kChange,
            enabled = !requestInProgress,
        )

        if (!canRequestThis) {
            Text(
                text = mokoString(MR.strings.request_no_permission),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = if (willAutoApprove) Icons.Default.Bolt else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (willAutoApprove) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = if (willAutoApprove) {
                        mokoString(MR.strings.request_will_auto_approve)
                    } else {
                        mokoString(MR.strings.request_will_require_approval)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f),
                enabled = !requestInProgress,
            ) {
                Text(mokoString(MR.strings.cancel))
            }
            Button(
                onClick = {
                    val seasons = if (details is TvDetails) selectedSeasons.toList() else null
                    val effectiveUserId = if (canRequestForOtherUsers && selectedUserId != currentUser?.id) selectedUserId else null
                    onSubmitRequest(
                        selectedProfileId,
                        selectedRootFolder,
                        null, // languageProfileId
                        seasons,
                        effectiveUserId,
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = !requestInProgress && canRequestThis && if (details is TvDetails) selectedSeasons.isNotEmpty() else true,
            ) {
                if (requestInProgress) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Text(
                        text = when {
                            !canRequestThis -> mokoString(MR.strings.request)
                            is4k -> mokoString(MR.strings.request_in_4k)
                            else -> mokoString(MR.strings.submit_request)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun SeerrRequestConfigurationContent(
    details: RequestMediaDetails,
    serviceDetails: ServiceDetails?,
    isAdmin: Boolean = false,
    canConfigureAdvanced: Boolean = isAdmin,
    canRequestForOtherUsers: Boolean = isAdmin,
    users: List<SeerrUser>,
    selectedProfileId: Long?,
    onSelectedProfileIdChange: (Long?) -> Unit,
    selectedRootFolder: String?,
    onSelectedRootFolderChange: (String?) -> Unit,
    selectedUserId: Long?,
    onSelectedUserIdChange: (Long?) -> Unit,
    selectedSeasons: Set<Int>,
    onSelectedSeasonsChange: (Set<Int>) -> Unit,
    canRequest4k: Boolean = false,
    is4k: Boolean = false,
    onIs4kChange: (Boolean) -> Unit = {},
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (canRequest4k) {
            LabelledSwitch(
                label = mokoString(MR.strings.request_in_4k),
                checked = is4k,
                onCheckedChange = onIs4kChange,
                enabled = enabled,
            )
        }

        if (details is TvDetails) {
            SeasonSelector(
                details = details,
                selectedSeasons = selectedSeasons,
                onSeasonsChanged = onSelectedSeasonsChange,
                enabled = enabled,
            )
        }

        val showAdvanced = canConfigureAdvanced || (canRequestForOtherUsers && users.isNotEmpty())
        if (showAdvanced) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = mokoString(MR.strings.advanced).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )

                if (canConfigureAdvanced) {
                    val profiles = serviceDetails?.profiles ?: emptyList()
                    val rootFolders = serviceDetails?.rootFolders ?: emptyList()

                    DropdownPicker(
                        label = { Text(mokoString(MR.strings.quality_profile)) },
                        options = profiles.map { it.id }.ifEmpty { listOfNotNull(selectedProfileId) },
                        selectedOption = selectedProfileId,
                        onOptionSelected = onSelectedProfileIdChange,
                        getOptionLabel = { profileId ->
                            profiles.find { it.id == profileId }?.name ?: profileId.toString()
                        },
                        enabled = enabled,
                    )

                    DropdownPicker(
                        label = { Text(mokoString(MR.strings.root_folder)) },
                        options = rootFolders.map { it.path }.ifEmpty { listOfNotNull(selectedRootFolder) },
                        selectedOption = selectedRootFolder,
                        onOptionSelected = onSelectedRootFolderChange,
                        getOptionLabel = { it },
                        enabled = enabled,
                    )
                }

                if (canRequestForOtherUsers && users.isNotEmpty()) {
                    DropdownPicker(
                        label = { Text(mokoString(MR.strings.request_as)) },
                        options = users.map { it.id },
                        selectedOption = selectedUserId,
                        onOptionSelected = onSelectedUserIdChange,
                        getOptionLabel = { userId ->
                            users.find { it.id == userId }?.displayName ?: userId.toString()
                        },
                        enabled = enabled,
                    )
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
    enabled: Boolean = true,
) {
    ContainerCard(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
            enabled = enabled,
        )
        HorizontalDivider()

        details.seasons.forEach { season ->
            val isSelected = season.seasonNumber in selectedSeasons

            LabelledSwitch(
                label =
                if (season.seasonNumber == 0) {
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
                enabled = enabled,
            )
        }
    }
}
