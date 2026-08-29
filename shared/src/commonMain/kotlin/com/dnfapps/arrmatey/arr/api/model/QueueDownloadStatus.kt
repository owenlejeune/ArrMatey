package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName

enum class QueueDownloadStatus {
    @SerialName("ok")
    Ok,

    @SerialName("warning")
    Warning,

    @SerialName("error")
    Error,
}
