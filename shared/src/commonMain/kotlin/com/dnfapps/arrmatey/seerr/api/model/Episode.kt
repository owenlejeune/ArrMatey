package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Episode(
    val id: Long,
    val name: String,
    @Contextual val airDate: LocalDate? = null,
    val episodeNumber: Int,
    val overview: String? = null,
    val productionCode: String? = null,
    val seasonNumber: Int,
    val showId: Long,
    val stillPath: String? = null,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0
)