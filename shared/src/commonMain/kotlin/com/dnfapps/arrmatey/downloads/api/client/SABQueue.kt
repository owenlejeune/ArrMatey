package com.dnfapps.arrmatey.downloads.api.client

import kotlinx.serialization.Serializable

@Serializable
internal data class SABQueue(
    val slots: List<SABSlot>,
    val kbpersec: Double,
    val paused: Boolean
)
