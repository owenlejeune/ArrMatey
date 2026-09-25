package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class IssueCount(
    val total: Int = 0,
    val video: Int = 0,
    val audio: Int = 0,
    val subtitles: Int = 0,
    val others: Int = 0,
    val open: Int = 0,
    val closed: Int = 0,
)
