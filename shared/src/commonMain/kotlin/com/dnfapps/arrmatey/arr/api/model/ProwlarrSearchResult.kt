package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val publishDate: String? = null,
    val downloadUrl: String? = null,
    val infoUrl: String? = null,
    val magnetUrl: String? = null,
    val protocol: ReleaseProtocol? = null,
    val seeders: Int? = null,
    val leechers: Int? = null,
    val grabs: Int? = null,
    val description: String? = null,
    val categories: List<ProwlarrCategory> = emptyList(),
    val downloadAllowed: Boolean = false,
    val fullSeason: Boolean = false,
    val releaseGroup: String? = null
)

@Serializable
data class ProwlarrCategory(
    val id: Int,
    val name: String? = null
)
