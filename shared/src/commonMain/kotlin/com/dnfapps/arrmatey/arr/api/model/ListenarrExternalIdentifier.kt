package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ListenarrExternalIdentifier(
    val id: Long? = null,
    val audiobookId: Long? = null,
    val type: String,
    val valueRaw: String,
    val valueNormalized: String,
    val region: String? = null,
    val isPrimary: Boolean,
    val source: String,
    @Serializable(with = ListenarrInstantSerializer::class)
    val createdAt: Instant? = null,
    @Serializable(with = ListenarrInstantSerializer::class)
    val updatedAt: Instant? = null
)
