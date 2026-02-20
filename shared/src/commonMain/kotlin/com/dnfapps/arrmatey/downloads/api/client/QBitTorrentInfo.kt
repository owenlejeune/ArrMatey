package com.dnfapps.arrmatey.downloads.api.client

import kotlinx.serialization.Serializable

@Serializable
internal data class QBitTorrentInfo(
    val hash: String,
    val name: String,
    val size: Long,
    val progress: Double,
    val state: String,
    val dlspeed: Long,
    val eta: Long,
    val num_seeds: Int,
    val num_leechs: Int,
    val category: String
)
