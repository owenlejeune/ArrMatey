package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ProwlarrIndexer(
    val id: Long,
    val name: String? = null,
    val implementationName: String? = null,
    val implementation: String? = null,
    val configContract: String? = null,
    val infoLink: String? = null,
    val message: ProviderMessage? = null,
    val tags: List<Int> = emptyList(),
    val presets: List<String> = emptyList(),
    val enable: Boolean,
    val redirect: Boolean = false,
    val supportsRss: Boolean,
    val supportsSearch: Boolean,
    val supportsRedirect: Boolean,
    val appFriendlyName: String? = null,
    val protocol: ReleaseProtocol? = null, // torrent, usenet
    val priority: Int,
    val downloadClientId: Int? = null
)
