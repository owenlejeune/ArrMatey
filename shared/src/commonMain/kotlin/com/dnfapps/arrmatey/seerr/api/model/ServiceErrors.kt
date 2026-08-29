package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ServiceErrors(
    val radarr: List<String> = emptyList(),
    val sonarr: List<String> = emptyList(),
)
