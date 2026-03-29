package com.dnfapps.arrmatey.ui.screens.requests

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.seerr.api.model.Issue
import com.dnfapps.arrmatey.seerr.api.model.IssueStatus
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun IssueStatusChip(issue: Issue) {
    val issueStatus = IssueStatus.fromValue(issue.status)
    val (label, container, content) = when(issueStatus) {
        IssueStatus.Open ->
            Triple(issueStatus.resource, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        else ->
            Triple(issueStatus.resource, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
    }

    AssistChip(
        onClick = {},
        label = { Text(mokoString(label)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = container,
            labelColor = content
        ),
        border = null
    )
}