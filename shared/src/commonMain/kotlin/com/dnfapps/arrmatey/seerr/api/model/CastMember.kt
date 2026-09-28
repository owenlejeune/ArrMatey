package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class CastMember(
    val id: Long = 0L,
    val castId: Long? = null,
    val character: String = "",
    val creditId: String = "",
    val gender: Int = 0,
    val name: String = "",
    val order: Int = 0,
    val profilePath: String? = null,
) {
    val fullProfilePath: String?
        get() =
            profilePath?.let {
                "https://image.tmdb.org/t/p/w600_and_h900_bestv2/$it"
            }
}
