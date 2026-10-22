package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrVersion(
    val current: TracearrCurrentVersion? = null,
    val latest: TracearrLatestVersion? = null,
    val updateAvailable: Boolean = false,
    val lastChecked: String? = null,
)

@Serializable
data class TracearrCurrentVersion(
    val version: String? = null,
    val tag: String? = null,
    val commit: String? = null,
    val buildDate: String? = null,
    val isPrerelease: Boolean = false,
)

@Serializable
data class TracearrLatestVersion(
    val version: String? = null,
    val tag: String? = null,
    val releaseUrl: String? = null,
    val publishedAt: String? = null,
    val isPrerelease: Boolean = false,
    val releaseName: String? = null,
    val releaseNotes: String? = null,
)
