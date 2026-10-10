package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComicIssue(
    val id: Long? = null,
    @SerialName("volume_id") val volumeId: Long? = null,
    @SerialName("comicvine_id") val comicvineId: Long? = null,
    @SerialName("issue_number") val issueNumber: String? = null,
    @SerialName("calculated_issue_number") val calculatedIssueNumber: Double? = null,
    val title: String? = null,
    val date: String? = null,
    val description: String? = null,
    val monitored: Boolean = false,
    val files: List<ComicFile> = emptyList(),
)
