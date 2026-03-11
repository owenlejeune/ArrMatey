package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ProwlarrIndexer(
    val id: Long,
    val name: String? = null,
    val implementationName: String? = null,
    val implementation: String? = null,
    val configContract: String? = null,
    val infoLink: String? = null,
    val message: ProwlarrIndexerMessage? = null,
    val tags: List<Int> = emptyList(),
    val presets: List<String> = emptyList(),
    val enable: Boolean,
    val redirect: String? = null,
    val supportsRss: Boolean,
    val supportsSearch: Boolean,
    val supportsRedirect: Boolean,
    val appFriendlyName: String? = null,
    val protocol: ReleaseProtocol? = null,
    val priority: Int,
    val downloadClientId: Int? = null,
    val privacy: IndexerPrivacy? = null,
    @Contextual val added: Instant? = null
)
