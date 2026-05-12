package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ListenarrIndexer(
    val id: Long,
    val name: String,
    val type: String,
    val implementation: String,
    val url: String,
    val apiKey: String,
    val categories: String,
    val enableRss: Boolean,
    val enableAutomaticSearch: Boolean,
    val enabledInteractiveSearch: Boolean,
    val enabledAnimeStandardSearch: Boolean,
    val isEnabled: Boolean,
    val priority: Int,
    val minimumAge: Int,
    val retention: Int,
    val maximumSize: Int,
    @Serializable(with = ListenarrInstantSerializer::class)
    val createdAt: Instant? = null,
    @Serializable(with = ListenarrInstantSerializer::class)
    val updatedAt: Instant? = null
)