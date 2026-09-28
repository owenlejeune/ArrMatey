package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.datastore.QueueRemovalPreferences
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteItemSheet(
    onDismiss: () -> Unit,
    deleteInProgress: Boolean,
    onDelete: (Boolean, Boolean, Boolean) -> Unit,
    preferencesStore: PreferencesStore = koinInject(),
) {
    val savedChoices by preferencesStore.queueRemovalPreferences.collectAsStateWithLifecycle(QueueRemovalPreferences())
    var removeFromClient by remember(savedChoices) { mutableStateOf(savedChoices.removeFromClient) }
    var blocklistRelease by remember(savedChoices) { mutableStateOf(savedChoices.addToBlocklist) }
    var skipRedownload by remember(savedChoices) { mutableStateOf(savedChoices.skipRedownload) }

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
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.client_remove_title),
                sublabel = mokoString(MR.strings.client_remove_message),
                checked = removeFromClient,
                onCheckedChange = { removeFromClient = it },
            )
            LabelledSwitch(
                label = mokoString(MR.strings.blocklist_title),
                sublabel = mokoString(MR.strings.blocklist_message),
                checked = blocklistRelease,
                onCheckedChange = { blocklistRelease = it },
            )
            if (blocklistRelease) {
                LabelledSwitch(
                    label = mokoString(MR.strings.skip_redownload_title),
                    sublabel = mokoString(MR.strings.skip_redownload_message),
                    checked = skipRedownload,
                    onCheckedChange = { skipRedownload = it },
                )
            }
            Button(
                onClick = {
                    preferencesStore.saveQueueRemovalPreferences(
                        QueueRemovalPreferences(
                            removeFromClient = removeFromClient,
                            addToBlocklist = blocklistRelease,
                            skipRedownload = skipRedownload,
                        ),
                    )
                    onDelete(removeFromClient, blocklistRelease, blocklistRelease && skipRedownload)
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                enabled = !deleteInProgress,
            ) {
                if (deleteInProgress) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                    Text(
                        text = mokoString(MR.strings.remove),
                    )
                }
            }
        }
    }
}
