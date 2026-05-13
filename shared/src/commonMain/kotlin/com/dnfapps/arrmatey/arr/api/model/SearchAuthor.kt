package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SearchAuthor(
    val asin: String? = null,
    val name: String,
    val region: String? = null,
    val regions: List<String> = emptyList(),
    @Serializable(with = ListenarrInstantSerializer::class)
    val updatedAt: Instant? = null
)
