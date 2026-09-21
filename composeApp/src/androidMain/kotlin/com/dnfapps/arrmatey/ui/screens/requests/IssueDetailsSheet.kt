package com.dnfapps.arrmatey.ui.screens.requests

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.seerr.viewmodel.IssueDetailsViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.sheets.ArrDestructiveConfirmationSheet
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IssueDetailsSheet(
    ip: MediaIssuePackage,
    onDismiss: () -> Unit,
    onIssueClosed: () -> Unit = onDismiss,
    viewModel: IssueDetailsViewModel = koinViewModel(key = ip.toString(), parameters = { parametersOf(ip) }),
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var confirmCloseIssue by remember { mutableStateOf(false) }
    var newComment by remember { mutableStateOf("") }
    val issueClosedMsg = mokoString(MR.strings.issue_closed)

    LaunchedEffect(uiState.commentSubmissionStatus) {
        when (val commentState = uiState.commentSubmissionStatus) {
            is OperationStatus.Success -> {
                newComment = ""
            }
            is OperationStatus.Error -> {
                Toast.makeText(context, commentState.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    LaunchedEffect(uiState.issueCloseStatus) {
        when (val closeState = uiState.issueCloseStatus) {
            is OperationStatus.Success -> {
                confirmCloseIssue = false
                val msg = closeState.message ?: issueClosedMsg
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                onIssueClosed()
            }
            is OperationStatus.Error -> {
                confirmCloseIssue = false
                Toast.makeText(context, closeState.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    val commentsList = uiState.issuePackage.issue.comments
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier =
                Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                commentsList.minByOrNull { it.id }?.let { description ->
                    item {
                        Text(
                            text = mokoString(MR.strings.description),
                            style = MaterialTheme.typography.titleMediumEmphasized,
                        )
                    }
                    item {
                        ContainerCard(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            UserInfoRow(
                                label = mokoString(MR.strings.opened_by),
                                avatar = description.user?.avatar,
                                displayName = description.user?.displayName ?: mokoString(MR.strings.unknown),
                            )
                            description.createdAt?.format()?.let { createdAt ->
                                Text(
                                    text = createdAt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = description.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                if (commentsList.size > 1) {
                    item {
                        Text(
                            text = mokoString(MR.strings.comments),
                            style = MaterialTheme.typography.titleMediumEmphasized,
                        )
                    }
                    val subList = commentsList.subList(fromIndex = 1, toIndex = commentsList.size)
                    items(items = subList, key = { it.id }) { comment ->
                        ContainerCard(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                AsyncImage(
                                    model = rememberRemoteImageData(comment.user?.avatar),
                                    modifier = Modifier.size(36.dp).clip(CircleShape),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = comment.user?.displayName ?: mokoString(MR.strings.unknown),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    comment.createdAt?.format()?.let { createdAt ->
                                        Text(
                                            text = createdAt,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = comment.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { confirmCloseIssue = true },
                    modifier = Modifier.size(48.dp),
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                ) {
                    Icon(Icons.Default.CheckCircleOutline, null)
                }
                OutlinedTextField(
                    value = newComment,
                    onValueChange = { newComment = it },
                    modifier = Modifier.weight(1f),
                    shape = CircleShape,
                    placeholder = { Text(mokoString(MR.strings.comment)) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.submitIssueComment(newComment)
                            },
                            enabled = uiState.commentSubmissionStatus != OperationStatus.InProgress && newComment.isNotEmpty(),
                        ) {
                            if (uiState.commentSubmissionStatus is OperationStatus.InProgress) {
                                CircularProgressIndicator(Modifier.size(24.dp))
                            } else {
                                Icon(Icons.AutoMirrored.Default.Send, null)
                            }
                        }
                    },
                    enabled = uiState.commentSubmissionStatus != OperationStatus.InProgress,
                )
            }
        }
    }

    if (confirmCloseIssue) {
        ArrDestructiveConfirmationSheet(
            title = mokoString(MR.strings.confirm_close_issue),
            confirmText = mokoString(MR.strings.yes),
            dismissText = mokoString(MR.strings.no),
            onDismissRequest = { confirmCloseIssue = false },
            onConfirm = {
                viewModel.closeIssue(ip.issue.id)
            },
        )
    }
}
