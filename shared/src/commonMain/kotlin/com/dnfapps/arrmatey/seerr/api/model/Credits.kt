package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Credits(
    val cast: List<CastMember> = emptyList(),
    val crew: List<CrewMember> = emptyList()
)