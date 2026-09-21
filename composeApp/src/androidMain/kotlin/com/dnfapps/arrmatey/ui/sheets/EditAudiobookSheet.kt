package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AMOutlinedTextField
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.DropdownPicker
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditAudiobookSheet(
    item: Audiobook,
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    editInProgress: Boolean,
    onEditItem: (Audiobook) -> Unit,
    onDismiss: () -> Unit,
) {
    var monitored by remember { mutableStateOf(item.monitored) }
    var selectedQualityProfileId by remember { mutableIntStateOf(item.qualityProfileId) }
    var selectedRootFolder by remember {
        mutableStateOf(
            rootFolders.firstOrNull { item.path?.startsWith(it.path) == true } ?: rootFolders.firstOrNull(),
        )
    }
    var relativePath by remember { mutableStateOf(item.path?.removePrefix(selectedRootFolder?.path ?: "") ?: "") }

    ModalBottomSheet(
        onDismissRequest = {
            if (!editInProgress) {
                onDismiss()
            }
        },
        sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { !editInProgress },
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = item.title ?: "",
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                ContainerCard(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LabelledSwitch(
                        label = mokoString(MR.strings.monitored),
                        checked = monitored,
                        onCheckedChange = { monitored = it },
                        enabled = !editInProgress,
                    )

                    DropdownPicker(
                        options = qualityProfiles,
                        modifier = Modifier.fillMaxWidth(),
                        selectedOption = qualityProfiles.firstOrNull { it.id == selectedQualityProfileId } ?: qualityProfiles.firstOrNull(),
                        onOptionSelected = { selectedQualityProfileId = it.id },
                        getOptionLabel = { it.name ?: "" },
                        label = { Text(mokoString(MR.strings.quality_profile)) },
                        enabled = !editInProgress,
                    )
                }

                if (rootFolders.isNotEmpty()) {
                    ContainerCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        DropdownPicker(
                            options = rootFolders,
                            modifier = Modifier.fillMaxWidth(),
                            selectedOption = selectedRootFolder ?: rootFolders.first(),
                            onOptionSelected = { selectedRootFolder = it },
                            getOptionLabel = { it.path },
                            label = { Text(mokoString(MR.strings.root_folder)) },
                            enabled = !editInProgress,
                        )

                        AMOutlinedTextField(
                            value = relativePath,
                            onValueChange = { relativePath = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = mokoString(MR.strings.relative_path),
                            enabled = !editInProgress,
                        )
                    }
                }
            }

            Button(
                onClick = {
                    onEditItem(
                        item.copyForEdit(
                            monitored = monitored,
                            qualityProfileId = selectedQualityProfileId,
                            rootFolderPath = selectedRootFolder?.path ?: "",
                            relativePath = relativePath,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !editInProgress,
            ) {
                if (editInProgress) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(mokoString(MR.strings.save))
                }
            }
        }
    }
}
