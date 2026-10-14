package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NzbgetGroup(
    @SerialName("NZBID") val nzbId: Int = 0,
    @SerialName("NZBName") val nzbName: String = "",
    @SerialName("Name") val name: String = "",
    @SerialName("Category") val category: String = "",
    @SerialName("FileSizeLo") val fileSizeLo: Long = 0,
    @SerialName("FileSizeHi") val fileSizeHi: Long = 0,
    @SerialName("FileSizeMB") val fileSizeMB: Long = 0,
    @SerialName("RemainingSizeLo") val remainingSizeLo: Long = 0,
    @SerialName("RemainingSizeHi") val remainingSizeHi: Long = 0,
    @SerialName("RemainingSizeMB") val remainingSizeMB: Long = 0,
    @SerialName("DownloadedSizeLo") val downloadedSizeLo: Long = 0,
    @SerialName("DownloadedSizeHi") val downloadedSizeHi: Long = 0,
    @SerialName("DownloadedSizeMB") val downloadedSizeMB: Long = 0,
    @SerialName("Paused") val paused: Boolean = false,
    @SerialName("Status") val status: NzbgetGroupStatus = NzbgetGroupStatus.Unknown,
    @SerialName("MinPostTime") val minPostTime: Long = 0,
    @SerialName("MaxPostTime") val maxPostTime: Long = 0,
) {
    val displayName: String
        get() = name.ifEmpty { nzbName }

    val totalSize: Long
        get() = if (fileSizeHi != 0L || fileSizeLo != 0L) {
            (fileSizeHi shl 32) or (fileSizeLo and 0xFFFFFFFFL)
        } else {
            fileSizeMB * 1024L * 1024L
        }

    val remainingSize: Long
        get() = if (remainingSizeHi != 0L || remainingSizeLo != 0L) {
            (remainingSizeHi shl 32) or (remainingSizeLo and 0xFFFFFFFFL)
        } else {
            remainingSizeMB * 1024L * 1024L
        }

    val downloadedSize: Long
        get() = when {
            downloadedSizeHi != 0L || downloadedSizeLo != 0L ->
                (downloadedSizeHi shl 32) or (downloadedSizeLo and 0xFFFFFFFFL)
            downloadedSizeMB != 0L ->
                downloadedSizeMB * 1024L * 1024L
            else ->
                (totalSize - remainingSize).coerceAtLeast(0L)
        }

    val progress: Double
        get() = if (totalSize > 0) {
            (downloadedSize.toDouble() / totalSize.toDouble()).coerceIn(0.0, 1.0)
        } else {
            0.0
        }
}
