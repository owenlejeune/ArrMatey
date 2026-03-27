package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class IssueBody(
    val issueType: Int,
    val message: String,
    val mediaId: Long,
    val problemSeason: Int? = null,
    val problemEpisode: Int? = null
)