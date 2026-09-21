package com.dnfapps.arrmatey.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrConfirmationSheet(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: String? = null,
    text: String? = null,
    dismissButton: (@Composable RowScope.() -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
        ) {
            if (icon != null || title != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    icon?.invoke()
                    title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
            }

            text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            content?.invoke()

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                confirmButton()
                dismissButton?.invoke(this@Row)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrDestructiveConfirmationSheet(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = mokoString(MR.strings.confirm),
    text: String? = null,
    confirmText: String = mokoString(MR.strings.delete),
    dismissText: String? = mokoString(MR.strings.cancel),
    icon: ImageVector? = Icons.Default.Delete,
    confirmEnabled: Boolean = true,
    inProgress: Boolean = false,
    content: (@Composable () -> Unit)? = null,
) {
    ArrConfirmationSheet(
        onDismissRequest = {
            if (!inProgress) {
                onDismissRequest()
            }
        },
        modifier = modifier,
        sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { !inProgress },
            ),
        title = title,
        text = text,
        content = content,
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled && !inProgress,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                modifier = Modifier.weight(1f),
            ) {
                if (inProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError,
                    )
                } else {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = confirmText)
                }
            }
        },
        dismissButton =
            dismissText?.let {
                {
                    Button(
                        onClick = onDismissRequest,
                        enabled = !inProgress,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = it)
                    }
                }
            },
    )
}
