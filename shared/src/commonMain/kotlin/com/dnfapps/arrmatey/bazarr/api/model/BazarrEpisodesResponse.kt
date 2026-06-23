package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BazarrEpisodesResponse(
    val data: List<BazarrEpisode> = emptyList(),
    val total: Int = 0
)
