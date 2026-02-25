package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class CrewMember(
    val id: Long,
    val creditId: String,
    val gender: Int,
    val name: String,
    val job: String,
    val department: String,
    val profilePath: String? = null
)