package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Issue(
    val id: Long,
    val issueType: Int,
    val status: Int,
    val problemSeason: Int = 0,
    val problemEpisode: Int = 0,
    @Contextual val createdAt: Instant? = null,
    @Contextual val updatedAt: Instant? = null,
    val media: RequestMedia? = null,
    val createdBy: RequestUser? = null,
    val comments: List<Comment> = emptyList(),
) {
    fun matchesFilter(state: IssueState): Boolean =
        when (state) {
            IssueState.All -> true
            IssueState.Open -> IssueStatus.fromValue(status) == IssueStatus.Open
            IssueState.Closed -> IssueStatus.fromValue(status) == IssueStatus.Closed
        }
}
