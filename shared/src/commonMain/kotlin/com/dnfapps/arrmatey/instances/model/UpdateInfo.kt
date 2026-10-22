package com.dnfapps.arrmatey.instances.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfo(
    val isAvailable: Boolean = false,
    val currentVersion: String? = null,
    val latestVersion: String? = null,
    val releaseUrl: String? = null,
)
