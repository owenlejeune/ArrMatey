package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaIssuePackage(
    val issue: Issue,
    val details: RequestMediaDetails?,
)
