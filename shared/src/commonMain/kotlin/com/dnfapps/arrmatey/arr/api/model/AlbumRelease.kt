package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AlbumRelease(
    val id: Long,
    val albumId: Long,
    val foreignReleaseId: String? = null,
    val title: String? = null,
    val status: String? = null,
    val duration: Long,
    val trackCount: Int,
    val media: List<AlbumMedium> = emptyList(),
    val mediumCount: Int,
    val disambiguation: String? = null,
    val country: List<String> = emptyList(),
    val label: List<String> = emptyList(),
    val format: String? = null,
    val monitored: Boolean = false
)
