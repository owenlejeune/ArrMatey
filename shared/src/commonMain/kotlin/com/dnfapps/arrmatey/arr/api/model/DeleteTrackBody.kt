package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class DeleteTrackBody(
    val trackFileIds: List<Long>,
)
