package com.dnfapps.arrmatey.seerr.state

import com.dnfapps.arrmatey.seerr.api.model.IssueType
import com.dnfapps.arrmatey.seerr.api.model.Season

data class ReportIssueUiState(
    val issueType: IssueType = IssueType.Video,
    val message: String = "",
    val problemSeason: Int? = null,
    val problemEpisode: Int? = null,
    val includeSeriesOptions: Boolean = false,
    val availableSeasons: List<Season> = emptyList(),
    val mediaTitle: String = "",
    val saveButtonEnabled: Boolean = false,
    val saveInProgress: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
) {
    constructor(): this(IssueType.Video) // empty constructor for ios
}