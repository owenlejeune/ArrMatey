package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName

enum class QueueItemStatus {
    @SerialName("queued")
    Queued,

    @SerialName("paused")
    Paused,

    @SerialName("downloading")
    Downloading,

    @SerialName("completed")
    Completed,

    @SerialName("failed")
    Failed,

    @SerialName("warning")
    Warning,

    @SerialName("delay")
    Delay,

    @SerialName("downloadClientUnavailable")
    DownlaodClientUnavailable,

    @SerialName("fallback")
    Fallback,

    @SerialName("unknown")
    Unknown,
}
