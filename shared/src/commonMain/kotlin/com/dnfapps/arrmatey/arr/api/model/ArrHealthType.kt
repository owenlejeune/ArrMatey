package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName

enum class ArrHealthType {
    @SerialName("ok")
    Ok,

    @SerialName("notice")
    Notice,

    @SerialName("warning")
    Warning,

    @SerialName("error")
    Error,
}
