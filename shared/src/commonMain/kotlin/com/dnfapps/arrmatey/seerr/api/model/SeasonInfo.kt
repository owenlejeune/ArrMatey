package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SeasonInfo(
    val id: Long,
    val seasonNumber: Int,
    val status: Int,
    val status4k: Int? = null
)