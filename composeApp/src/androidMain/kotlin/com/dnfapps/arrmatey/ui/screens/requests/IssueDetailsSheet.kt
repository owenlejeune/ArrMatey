package com.dnfapps.arrmatey.ui.screens.requests

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.seerr.viewmodel.IssueDetailsViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailsSheet(
    ip: MediaIssuePackage,
    onDismiss: () -> Unit,
    viewModel: IssueDetailsViewModel = koinInjectParams(ip)
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var confirmCloseIssue by remember { mutableStateOf(false) }
    var newComment by remember { mutableStateOf("") }

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

    val commentsList = uiState.issuePackage.issue.comments
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                commentsList.minByOrNull { it.id }?.let { description ->
                    item {
                        Text(
                            text = mokoString(MR.strings.description),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    item {
                        Text(
                            text = description.message,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    item {
                        UserInfoRow(
                            label = mokoString(MR.strings.opened_by),
                            avatar = description.user?.avatar,
                            displayName = description.user?.displayName ?: mokoString(MR.strings.unknown)
                        )
                        description.createdAt?.format()?.let { createdAt ->
                            Text(
                                text = createdAt,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                if (commentsList.size > 1) {
                    item {
                        Text(
                            text = mokoString(MR.strings.comments),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    val subList = commentsList.subList(fromIndex = 1, toIndex = commentsList.size)
                    items(items = subList, key = { it.id }) { comment ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            AsyncImage(
                                model = rememberRemoteImageData(comment.user?.avatar),
                                modifier = Modifier.size(36.dp).clip(CircleShape),
                                contentDescription = null,
                                contentScale = ContentScale.Fit
                            )
                            Column {
                                Text(
                                    text = comment.message,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                comment.createdAt?.format()?.let { createdAt ->
                                    Text(
                                        text = createdAt,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton (
                    onClick = { confirmCloseIssue = true },
                    modifier = Modifier.size(50.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
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
                            enabled = uiState.commentSubmissionStatus != OperationStatus.InProgress && newComment.isNotEmpty()
                        ) {
                            if (uiState.commentSubmissionStatus is OperationStatus.InProgress) {
                                CircularProgressIndicator(Modifier.size(24.dp))
                            } else {
                                Icon(Icons.AutoMirrored.Default.Send, null)
                            }
                        }
                    },
                    enabled = uiState.commentSubmissionStatus != OperationStatus.InProgress
                )
            }
        }
    }

    if (confirmCloseIssue) {
        AlertDialog(
            onDismissRequest = { confirmCloseIssue = false },
            title = { Text(mokoString(MR.strings.confirm_close_issue)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.closeIssue(ip.issue.id)
                }) {
                    Text(mokoString(MR.strings.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCloseIssue = false }) {
                    Text(mokoString(MR.strings.no))
                }
            }
        )
    }
}