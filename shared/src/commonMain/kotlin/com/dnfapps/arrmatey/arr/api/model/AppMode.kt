package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AppMode {
    @SerialName("console")
    CONSOLE,

    @SerialName("service")
    SERVICE,

    @SerialName("tray")
    TRAY,
}
