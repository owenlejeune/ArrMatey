package com.dnfapps.arrmatey.instances.model

import kotlinx.serialization.Serializable

@Serializable
enum class SeerrAuthType {
    ApiKey,
    Local,
    Plex,
    Jellyfin,
    JellyfinQuickConnect,
}
