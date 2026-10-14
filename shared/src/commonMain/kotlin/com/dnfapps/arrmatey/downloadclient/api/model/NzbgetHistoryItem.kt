package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NzbgetHistoryItem(
    @SerialName("NZBID") val nzbId: Int = 0,
    @SerialName("Name") val name: String = "",
    @SerialName("NZBName") val nzbName: String = "",
    @SerialName("Category") val category: String = "",
    @SerialName("FileSizeLo") val fileSizeLo: Long = 0,
    @SerialName("FileSizeHi") val fileSizeHi: Long = 0,
    @SerialName("FileSizeMB") val fileSizeMB: Long = 0,
    @SerialName("Status") val status: String = "",
    @SerialName("DestDir") val destDir: String = "",
    @SerialName("Time") val time: Long = 0,
)
