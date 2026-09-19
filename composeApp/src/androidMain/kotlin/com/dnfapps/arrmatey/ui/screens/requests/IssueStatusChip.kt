package com.dnfapps.arrmatey.ui.screens.requests

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.seerr.api.model.Issue
import com.dnfapps.arrmatey.seerr.api.model.IssueStatus
import com.dnfapps.arrmatey.ui.components.AMStatusBadge
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun IssueStatusChip(
    issue: Issue,
    modifier: Modifier = Modifier,
) {
    val issueStatus = IssueStatus.fromValue(issue.status)
    val (label, container, content) =
        when (issueStatus) {
            IssueStatus.Open ->
                Triple(issueStatus.resource, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
            else ->
                Triple(issueStatus.resource, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        }

    AMStatusBadge(
        text = mokoString(label),
        containerColor = container,
        contentColor = content,
        modifier = modifier,
    )
}
