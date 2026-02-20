package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ProwlarrSearchResult(
    val guid: String? = null,
    val title: String? = null,
    val indexerId: Long,
    val indexer: String? = null,
    val size: Long,
    val age: Int,
    val ageHours: Double,
    val ageMinutes: Double,
    @Contextual val publishDate: Instant? = null,
    val downloadUrl: String? = null,
    val infoUrl: String? = null,
    val magnetUrl: String? = null,
    val protocol: ReleaseProtocol? = null,
    val seeders: Int? = null,
    val leechers: Int? = null,
    val grabs: Int? = null,
    val description: String? = null,
    val categories: List<ProwlarrCategory> = emptyList(),
    val downloadAllowed: Boolean,
    val fullSeason: Boolean,
    val releaseGroup: String? = null,
    val providerMessage: ProviderMessage? = null
)
