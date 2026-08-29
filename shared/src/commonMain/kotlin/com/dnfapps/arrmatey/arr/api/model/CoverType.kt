package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName

enum class CoverType(
    val defaultFileName: String,
) {
    @SerialName("clearlogo")
    ClearLogo("clearlogo.jpg"),

    @SerialName("banner")
    Banner("banner.jpg"),

    @SerialName("poster")
    Poster("poster.jpg"),

    @SerialName("fanart")
    FanArt("fanart.jpg"),

    @SerialName("screenshot")
    Screenshot("screenshot.jpg"),

    @SerialName("cover")
    Cover("cover.jpg"),

    @SerialName("disc")
    Disc("disc.jpg"),

    @SerialName("logo")
    Logo("logo.jpg"),

    Undefined(""),
}
