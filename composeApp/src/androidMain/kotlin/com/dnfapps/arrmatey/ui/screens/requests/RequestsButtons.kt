package com.dnfapps.arrmatey.ui.screens.requests

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.isDebug
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.state.RequestOperationsState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ConfirmableButton
import com.dnfapps.arrmatey.ui.theme.onPrimaryDark
import com.dnfapps.arrmatey.ui.theme.primaryDark
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.delay

@Composable
fun RequestButtons(
    isAdmin: Boolean,
    request: MediaRequest,
    operationsState: RequestOperationsState,
    onApproveClicked: () -> Unit,
    onDeclineClicked: () -> Unit,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onRemoveFromServiceClicked: () -> Unit
) {
    var showDeclineConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(showDeclineConfirm) {
        if (showDeclineConfirm) {
            delay(3000)
            showDeclineConfirm = false
        }
    }

    LaunchedEffect(showDeleteConfirm) {
        if (showDeleteConfirm) {
            delay(3000)
            showDeleteConfirm = false
        }
    }

    LaunchedEffect(showRemoveConfirm) {
        if (showRemoveConfirm) {
            delay(3000)
            showRemoveConfirm = false
        }
    }

    val requestStatusValue = request.status
    val mediaStatusValue = request.media.status

    val isPendingApproval = requestStatusValue == 1
    val isApproved = requestStatusValue == 2 ||
            requestStatusValue == 5 ||
            mediaStatusValue >= 4
    val isDeclined = requestStatusValue == 3

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (isPendingApproval && !isApproved) {
            PendingApprovalButtons(
                isAdmin = isAdmin,
                request = request,
                operationsState = operationsState,
                showDeclineConfirm = showDeclineConfirm,
                onApprove = onApproveClicked,
                onDecline = {
                    if (showDeclineConfirm) {
                        onDeclineClicked()
                        showDeclineConfirm = false
                    } else {
                        showDeclineConfirm = true
                    }
                },
                onEdit = onEditClicked
            )
        }

        if (isAdmin && (isApproved || isDeclined) && !isPendingApproval) {
            AdminActionButtons(
                request = request,
                isApproved = isApproved,
                mediaStatusValue = mediaStatusValue,
                showDeleteConfirm = showDeleteConfirm,
                showRemoveConfirm = showRemoveConfirm,
                onDelete = {
                    if (showDeleteConfirm) {
                        onDeleteClicked()
                        showDeleteConfirm = false
                    } else {
                        showDeleteConfirm = true
                    }
                },
                onRemoveFromService = {
                    if (showRemoveConfirm) {
                        onRemoveFromServiceClicked()
                        showRemoveConfirm = false
                    } else {
                        showRemoveConfirm = true
                    }
                }
            )
        }
    }
}

@Composable
private fun PendingApprovalButtons(
    isAdmin: Boolean,
    request: MediaRequest,
    operationsState: RequestOperationsState,
    showDeclineConfirm: Boolean,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
    onEdit: () -> Unit
) {
    val approveColors = ButtonDefaults.buttonColors(
        containerColor = primaryDark,
        contentColor = onPrimaryDark
    )
    val declineColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    )
    val editColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary
    )

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (isAdmin) {
            Button(
                onClick = onApprove,
                modifier = Modifier.weight(1f),
                colors = approveColors,
                enabled = operationsState.approvalStates.none { it.key == request.id }
            ) {
                if (operationsState.approvalStates.any { it.key == request.id }) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(4.dp))
                    Text(mokoString(MR.strings.approve))
                }
            }
        }

        ConfirmableButton(
            isConfirming = showDeclineConfirm,
            onClick = onDecline,
            modifier = Modifier.weight(1f),
            colors = declineColors,
            enabled = operationsState.cancelStates.none { it.key == request.id },
            content = {
                if (operationsState.cancelStates.any { it.key == request.id }) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Icon(Icons.Default.Close, null)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (isAdmin) mokoString(MR.strings.decline)
                        else mokoString(MR.strings.cancel_request)
                    )
                }
            }
        )
    }

    if (isDebug()) {
        if (isAdmin || request.type == RequestType.Tv) {
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                colors = editColors
            ) {
                Icon(Icons.Default.Edit, null)
                Spacer(Modifier.width(4.dp))
                Text("Edit")
            }
        }
    }
}

@Composable
private fun AdminActionButtons(
    request: MediaRequest,
    isApproved: Boolean,
    mediaStatusValue: Int,
    showDeleteConfirm: Boolean,
    showRemoveConfirm: Boolean,
    onDelete: () -> Unit,
    onRemoveFromService: () -> Unit
) {
    val declineColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    )

    ConfirmableButton(
        isConfirming = showDeleteConfirm,
        onClick = onDelete,
        modifier = Modifier.fillMaxWidth(),
        colors = declineColors,
        content = {
            Icon(Icons.Default.Delete, null)
            Spacer(Modifier.width(4.dp))
            Text(mokoString(MR.strings.delete_request))
        }
    )

    if (isApproved && mediaStatusValue != 6) {
        val serviceName = when (request.type) {
            RequestType.Movie -> InstanceType.Radarr.name
            RequestType.Tv -> InstanceType.Sonarr.name
        }

        ConfirmableButton(
            isConfirming = showRemoveConfirm,
            onClick = onRemoveFromService,
            modifier = Modifier.fillMaxWidth(),
            colors = declineColors,
            content = {
                Icon(Icons.Default.Delete, null)
                Spacer(Modifier.width(4.dp))
                Text(mokoString(MR.strings.remove_from_service, serviceName))
            }
        )
    }
}