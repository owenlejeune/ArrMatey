package com.dnfapps.arrmatey.mediaserver.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.shared.*
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

@Serializable
enum class MediaServerType(
    val resource: StringResource,
    val icon: ImageResource,
    val tabIcon: ImageResource,
    val defaultPort: Int,
    val brandColor: Color,
    val scheme: String,
) {
    Plex(
        resource = MR.strings.plex,
        icon = MR.images.plex,
        tabIcon = MR.images.plex_tab,
        defaultPort = 32400,
        brandColor = Color(0xFFE5A00D), // Plex yellow
        scheme = "plex",
    ),
    Jellyfin(
        resource = MR.strings.jellyfin,
        icon = MR.images.jellyfin,
        tabIcon = MR.images.jellyfin_tab,
        defaultPort = 8096,
        brandColor = Color(0xFFAA5CC3), // Jellyfin purple
        scheme = "jellyfin",
    ),
    Emby(
        resource = MR.strings.emby,
        icon = MR.images.emby,
        tabIcon = MR.images.emby_tab,
        defaultPort = 8096,
        brandColor = Color(0xFF52B54B), // Emby green
        scheme = "emby",
    ),
}
