package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.seerr.api.model.IssueType
import com.dnfapps.arrmatey.seerr.state.ReportIssueUiState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.forms.RadioGroup
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeerrReportIssueSheet(
    state: ReportIssueUiState,
    updateIssueType: (IssueType) -> Unit,
    updateMessage: (String) -> Unit,
    updateProblemSeason: (Int?) -> Unit,
    updateProblemEpisode: (Int?) -> Unit,
    onSubmit: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        onReset()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                Text(
                    text = mokoString(MR.strings.report_an_issue),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = state.mediaTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = state.includeSeriesOptions,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (state.includeSeriesOptions) {
                        DropdownPicker(
                            options = state.availableSeasons.map { it.seasonNumber },
                            includeAllOption = true,
                            allLabel = mokoString(MR.strings.all_seasons),
                            selectedOption = state.problemSeason,
                            onOptionSelected = { updateProblemSeason(it) },
                            onAllSelected = { updateProblemSeason(null) },
                            allDivider = null,
                            getOptionLabel = { season ->
                                mokoString(MR.strings.season_label, season)
                            }
                        )

                        AnimatedVisibility(
                            visible = state.problemSeason != null,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            val episodes = remember(state.problemSeason) {
                                state.availableSeasons.firstOrNull { it.seasonNumber == state.problemSeason }?.episodes
                            }
                            episodes?.let { episodes ->
                                DropdownPicker(
                                    options = episodes.map { it.episodeNumber },
                                    includeAllOption = true,
                                    allLabel = mokoString(MR.strings.all_episodes),
                                    selectedOption = state.problemEpisode,
                                    onOptionSelected = { updateProblemEpisode(it) },
                                    onAllSelected = { updateProblemEpisode(null) },
                                    allDivider = null,
                                    getOptionLabel = { episode ->
                                        episodes.first { it.episodeNumber == episode }.name
                                    }
                                )
                            }
                        }
                    }
                }
            }

            RadioGroup(
                entries = IssueType.entries,
                onItemSelected = { updateIssueType(it) },
                isItemSelected = { it == state.issueType },
                itemLabel = { mokoString(it.label) }
            )

            AMOutlinedTextField(
                label = mokoString(MR.strings.message),
                modifier = Modifier.fillMaxWidth(),
                value = state.message,
                onValueChange = { updateMessage(it) },
                minLines = 5,
                maxLines = 5
            )

            Button(
                onClick = { onSubmit() },
                enabled = state.saveButtonEnabled
            ){
                Text(mokoString(MR.strings.save))
            }
        }
    }
}