package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class QualityProfile(
    val id: Long,
    val name: String
)