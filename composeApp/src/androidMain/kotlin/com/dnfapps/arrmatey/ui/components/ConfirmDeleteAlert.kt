package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteAlert(
    deleteInProgress: Boolean,
    initialAddExclusion: Boolean = false,
    initialDeleteFiles: Boolean = false,
    onDismiss: () -> Unit,
    onDelete: (Boolean, Boolean) -> Unit,
) {
    var addExclusion by remember { mutableStateOf(initialAddExclusion) }
    var deleteFiles by remember { mutableStateOf(initialDeleteFiles) }
    ModalBottomSheet(
        onDismissRequest = {
            if (!deleteInProgress) {
                onDismiss()
            }
        },
        sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { !deleteInProgress },
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier =
                Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.add_exclusion),
                sublabel = mokoString(MR.strings.add_exclusion_description),
                checked = addExclusion,
                onCheckedChange = { addExclusion = !addExclusion },
            )
            LabelledSwitch(
                label = mokoString(MR.strings.delete_files),
                sublabel = mokoString(MR.strings.delete_files_description),
                checked = deleteFiles,
                onCheckedChange = { deleteFiles = !deleteFiles },
            )
            Button(
                onClick = { onDelete(deleteFiles, addExclusion) },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                enabled = !deleteInProgress,
            ) {
                if (deleteInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                    Text(text = mokoString(MR.strings.delete))
                }
            }
        }
    }
}
