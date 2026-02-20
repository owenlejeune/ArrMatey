package com.dnfapps.arrmatey.downloads.api.client

import kotlinx.serialization.Serializable

@Serializable
internal data class SABSlot(
    val nzo_id: String,
    val filename: String,
    val mb: String,
    val mbleft: String,
    val percentage: String,
    val status: String,
    val timeleft: String
)
