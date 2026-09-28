package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Season(
    val id: Long = 0L,
    @Contextual val airDate: LocalDate? = null,
    val name: String = "",
    val overview: String? = null,
    val posterPath: String? = null,
    val seasonNumber: Int = 0,
    val episodes: List<Episode> = emptyList(),
    val episodeCount: Int = episodes.size,
)
