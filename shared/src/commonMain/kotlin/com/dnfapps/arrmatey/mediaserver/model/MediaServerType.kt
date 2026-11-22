package com.dnfapps.arrmatey.mediaserver.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.ui.theme.EmbyGreen
import com.dnfapps.arrmatey.ui.theme.JellyfinPurple
import com.dnfapps.arrmatey.ui.theme.PlexOrange
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
        brandColor = PlexOrange, // Plex yellow
        scheme = "plex",
    ),
    Jellyfin(
        resource = MR.strings.jellyfin,
        icon = MR.images.jellyfin,
        tabIcon = MR.images.jellyfin_tab,
        defaultPort = 8096,
        brandColor = JellyfinPurple, // Jellyfin purple
        scheme = "jellyfin",
    ),
    Emby(
        resource = MR.strings.emby,
        icon = MR.images.emby,
        tabIcon = MR.images.emby_tab,
        defaultPort = 8096,
        brandColor = EmbyGreen, // Emby green
        scheme = "emby",
    ),
}
