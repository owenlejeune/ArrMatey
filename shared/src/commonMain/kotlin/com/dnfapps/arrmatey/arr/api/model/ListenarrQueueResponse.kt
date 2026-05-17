package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ListenarrQueueResponse(
    val items: List<ListenarrQueueItem>
)