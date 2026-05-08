package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName

enum class DelugeTorrentState {
    @SerialName("Downloading")
    Downloading,

    @SerialName("Seeding")
    Seeding,

    @SerialName("Paused")
    Paused,

    @SerialName("Queued")
    Queued,

    @SerialName("Checking")
    Checking,

    @SerialName("Error")
    Error,

    @SerialName("Allocating")
    Allocating,

    @SerialName("Moving")
    Moving,

    Unknown
}