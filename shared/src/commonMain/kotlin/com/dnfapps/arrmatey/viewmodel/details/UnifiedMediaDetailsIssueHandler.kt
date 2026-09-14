package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.IssueBody
import com.dnfapps.arrmatey.seerr.api.model.IssueType
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.state.ReportIssueUiState
import com.dnfapps.arrmatey.seerr.usecase.SubmitIssueUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UnifiedMediaDetailsIssueHandler(
    private val submitIssueUseCase: SubmitIssueUseCase,
) {
    private val _isReportIssueSheetVisible = MutableStateFlow(false)
    val isReportIssueSheetVisible: StateFlow<Boolean> = _isReportIssueSheetVisible.asStateFlow()

    private val _rawReportIssueState = MutableStateFlow(ReportIssueUiState())
    val rawReportIssueState: StateFlow<ReportIssueUiState> = _rawReportIssueState.asStateFlow()

    fun createCombinedReportIssueState(
        scope: CoroutineScope,
        uiStateFlow: Flow<UnifiedMediaDetailsUiState>,
        onSeerrMediaIdExtracted: (Long?) -> Unit,
    ): StateFlow<ReportIssueUiState> =
        _rawReportIssueState
            .combine(uiStateFlow) { issueState, uiState ->
                if (uiState is UnifiedMediaDetailsUiState.Success && uiState.seerrMedia != null) {
                    val mediaId = uiState.seerrMedia.mediaInfo?.id
                    onSeerrMediaIdExtracted(mediaId)
                    if (issueState.saveSuccess) {
                        _isReportIssueSheetVisible.value = false
                    }
                    issueState.copy(
                        includeSeriesOptions = uiState.seerrMedia.requestType == RequestType.Tv,
                        mediaTitle = uiState.seerrMedia.displayTitle,
                        availableSeasons = (uiState.seerrMedia as? TvDetails)?.seasons ?: emptyList(),
                        saveButtonEnabled = issueState.message.isNotEmpty() && !issueState.saveInProgress,
                    )
                } else {
                    issueState
                }
            }.stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ReportIssueUiState(),
            )

    fun showReportIssueSheet() {
        _isReportIssueSheetVisible.value = true
    }

    fun hideReportIssueSheet() {
        _isReportIssueSheetVisible.value = false
    }

    fun setIssueType(issueType: IssueType) {
        _rawReportIssueState.update {
            it.copy(issueType = issueType)
        }
    }

    fun setIssueMessage(message: String) {
        _rawReportIssueState.update {
            it.copy(message = message)
        }
    }

    fun setProblemSeason(season: Int?) {
        _rawReportIssueState.update {
            it.copy(problemSeason = season)
        }
    }

    fun setProblemEpisode(episode: Int?) {
        _rawReportIssueState.update {
            it.copy(problemEpisode = episode)
        }
    }

    fun resetIssueState() {
        _rawReportIssueState.value = ReportIssueUiState()
    }

    fun submitIssue(
        scope: CoroutineScope,
        seerrMediaIdProvider: () -> Long?,
    ) {
        val seerrId = seerrMediaIdProvider() ?: return
        val state = _rawReportIssueState.value
        val issue =
            IssueBody(
                issueType = state.issueType.value,
                message = state.message,
                mediaId = seerrId,
                problemSeason = state.problemSeason ?: 0,
                problemEpisode = state.problemSeason?.let { state.problemEpisode } ?: 0,
            )
        scope.launch {
            submitIssueUseCase(issue)
                .collect { issueStatus ->
                    _rawReportIssueState.update {
                        it.copy(
                            saveInProgress = issueStatus == OperationStatus.InProgress,
                            saveError = (issueStatus as? OperationStatus.Error)?.message,
                            saveSuccess = issueStatus is OperationStatus.Success,
                        )
                    }
                }
        }
    }
}
