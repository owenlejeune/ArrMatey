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
) {
    val fullProfilePath: String?
        get() = profilePath?.let {
            "https://image.tmdb.org/t/p/w600_and_h900_bestv2/$it"
        }
}