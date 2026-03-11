package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName

enum class IndexerMessageType {
    @SerialName("info")
    Info,

    @SerialName("warning")
    Warning,

    @SerialName("error")
    Error
}