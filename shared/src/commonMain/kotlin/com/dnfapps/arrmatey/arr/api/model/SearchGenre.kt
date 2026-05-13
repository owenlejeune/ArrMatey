package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SearchGenre(
    val asin: String? = null,
    val name: String,
    val type: String? = null,
    @Serializable(with = ListenarrInstantSerializer::class)
    val updatedAt: Instant? = null
)
