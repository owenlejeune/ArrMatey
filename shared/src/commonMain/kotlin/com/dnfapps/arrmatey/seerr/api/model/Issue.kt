package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Issue(
    val id: Long,
    val issueType: Int,
    val status: Int,
    val createdAt: String
)