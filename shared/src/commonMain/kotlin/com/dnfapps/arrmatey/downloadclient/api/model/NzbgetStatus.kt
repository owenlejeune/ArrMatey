package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NzbgetStatus(
    @SerialName("RemainingSizeLo") val remainingSizeLo: Long = 0,
    @SerialName("RemainingSizeHi") val remainingSizeHi: Long = 0,
    @SerialName("RemainingSizeMB") val remainingSizeMB: Long = 0,
    @SerialName("DownloadRate") val downloadRate: Long = 0,
    @SerialName("DownloadRateLo") val downloadRateLo: Long = 0,
    @SerialName("DownloadRateHi") val downloadRateHi: Long = 0,
    @SerialName("AverageDownloadRate") val averageDownloadRate: Long = 0,
    @SerialName("DownloadPaused") val downloadPaused: Boolean = false,
    @SerialName("ServerPaused") val serverPaused: Boolean = false,
    @SerialName("FreeDiskSpaceMB") val freeDiskSpaceMB: Long = 0,
) {
    val totalRemainingBytes: Long
        get() = if (remainingSizeHi != 0L || remainingSizeLo != 0L) {
            (remainingSizeHi shl 32) or (remainingSizeLo and 0xFFFFFFFFL)
        } else {
            remainingSizeMB * 1024L * 1024L
        }

    val currentDownloadSpeed: Long
        get() = when {
            downloadRateHi != 0L || downloadRateLo != 0L ->
                (downloadRateHi shl 32) or (downloadRateLo and 0xFFFFFFFFL)
            downloadRate > 0 -> downloadRate
            else -> 0L
        }
}
