package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName

enum class SABnzbdQueueSlotStatus {
    @SerialName("Downloading")
    Downloading,

    @SerialName("Queued")
    Queued,

    @SerialName("Paused")
    Paused,

    @SerialName("Propagating")
    Propagating,

    @SerialName("Fetching")
    Fetching,

    Unknown
}