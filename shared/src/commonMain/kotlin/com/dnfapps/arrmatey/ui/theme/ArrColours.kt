package com.dnfapps.arrmatey.ui.theme

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.tracearr.api.model.TracearrServerType

val ArrGreen = Color(0xFF00853d)
val ArrGrey = Color(0xFF888888)
val ArrRed = Color(0xFFF05050)
val ArrOrange = Color(0xFFFFA505)
val ArrYellow = Color(0xFFFFC653)
val ArrPurple = Color(0xFF7A43b6)
val ArrLightPurple = Color(0xffae71ff)
val ArrBlue = Color(0xFF5D9CEC)
val ArrBazarr = Color(0xFF0FA3B1)
val TracearrBlue = Color(0xFF19D2E7)

val PlexColor = Color(0xFFE5A00D)
val JellyfinColor = Color(0xFFAA5CC3)
val EmbyColor = Color(0xFF52B54B)

val TracearrServerType?.brandColor: Color
    get() = when (this) {
        TracearrServerType.Plex -> PlexColor
        TracearrServerType.Jellyfin -> JellyfinColor
        TracearrServerType.Emby -> EmbyColor
        null -> TracearrBlue
    }

fun getTracearrServerColor(type: TracearrServerType?, name: String? = null): Color {
    if (type != null) return type.brandColor
    val lower = name?.lowercase() ?: ""
    return when {
        lower.contains("plex") -> PlexColor
        lower.contains("jellyfin") -> JellyfinColor
        lower.contains("emby") -> EmbyColor
        else -> TracearrBlue
    }
}
