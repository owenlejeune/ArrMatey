package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName

enum class QBittorrentTorrentState {
    @SerialName("error")
    Error,

    @SerialName("missingFiles")
    MissingFiles,

    @SerialName("uploading")
    Uploading,

    @SerialName("pausedUP")
    UploadingPaused,

    @SerialName("queuedUP")
    UploadingQueued,

    @SerialName("stalledUP")
    UploadingStalled,

    @SerialName("checkingUP")
    UploadingChecking,

    @SerialName("forcedUP")
    UploadingForced,

    @SerialName("stoppedUP")
    UploadingStopped,

    @SerialName("allocating")
    Allocating,

    @SerialName("downloading")
    Downloading,

    @SerialName("metaDL")
    DownloadingMetadata,

    @SerialName("forcedMetaDL")
    DownloadingMetadataForced,

    @SerialName("pausedDL")
    DownloadingPaused,

    @SerialName("queuedDL")
    DownloadingQueued,

    @SerialName("stalledDL")
    DownloadingStalled,

    @SerialName("checkingDL")
    DownloadingChecking,

    @SerialName("forcedDL")
    DownloadingForced,

    @SerialName("stoppedDL")
    DownloadingStopped,

    @SerialName("checkingResumeData")
    CheckingResumeData,

    @SerialName("moving")
    Moving,

    @SerialName("unknown")
    Unknown
}