package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class DeleteEpisodeBody(
    val episodeFileIds: List<Long>,
)
