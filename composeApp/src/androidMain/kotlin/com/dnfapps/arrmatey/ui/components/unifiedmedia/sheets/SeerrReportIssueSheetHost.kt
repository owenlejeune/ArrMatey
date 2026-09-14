package com.dnfapps.arrmatey.ui.components.unifiedmedia.sheets

import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.seerr.api.model.IssueType
import com.dnfapps.arrmatey.seerr.state.ReportIssueUiState
import com.dnfapps.arrmatey.ui.sheets.SeerrReportIssueSheet

@Composable
fun SeerrReportIssueSheetHost(
    visible: Boolean,
    state: ReportIssueUiState,
    onUpdateIssueType: (IssueType) -> Unit,
    onUpdateMessage: (String) -> Unit,
    onUpdateProblemSeason: (Int?) -> Unit,
    onUpdateProblemEpisode: (Int?) -> Unit,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (visible) {
        SeerrReportIssueSheet(
            state = state,
            updateIssueType = onUpdateIssueType,
            updateMessage = onUpdateMessage,
            updateProblemSeason = onUpdateProblemSeason,
            updateProblemEpisode = onUpdateProblemEpisode,
            onReset = onReset,
            onSubmit = onSubmit,
            onDismiss = onDismiss,
        )
    }
}
