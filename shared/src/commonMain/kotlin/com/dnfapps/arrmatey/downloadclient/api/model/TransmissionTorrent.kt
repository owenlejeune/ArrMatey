package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransmissionTorrent(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("totalSize") val totalSize: Long = 0,
    @SerialName("percentDone") val percentDone: Double = 0.0,
    @SerialName("rateDownload") val rateDownload: Long = 0,
    @SerialName("rateUpload") val rateUpload: Long = 0,
    @SerialName("eta") val eta: Long = 0,
    @SerialName("status") val status: Int = 0,
    @SerialName("downloadDir") val downloadDir: String = "",
    @SerialName("addedDate") val addedDate: Long = 0,
    @SerialName("hashString") val hashString: String = ""
)
