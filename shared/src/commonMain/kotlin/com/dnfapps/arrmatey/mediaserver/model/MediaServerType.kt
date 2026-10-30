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
    val defaultPort: Int,
    val brandColor: Color,
    val scheme: String,
) {
    Plex(
        resource = MR.strings.plex,
        icon = MR.images.plex,
        defaultPort = 32400,
        brandColor = Color(0xFFE5A00D),
        scheme = "plex",
    ),
    Jellyfin(
        resource = MR.strings.jellyfin,
        icon = MR.images.jellyfin,
        defaultPort = 8096,
        brandColor = Color(0xFF4747ED),
        scheme = "jellyfin",
    ),
    Emby(
        resource = MR.strings.emby,
        icon = MR.images.emby,
        defaultPort = 8096,
        brandColor = Color(0xFF52B54B),
        scheme = "emby",
    ),
}
