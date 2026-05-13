package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SearchNarrator(
    val name: String,
    @Serializable(with = ListenarrInstantSerializer::class)
    val updatedAt: Instant? = null
)
