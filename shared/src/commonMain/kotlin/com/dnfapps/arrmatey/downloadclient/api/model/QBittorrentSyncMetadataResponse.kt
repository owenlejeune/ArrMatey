package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QBittorrentSyncMetadataResponse(
    @SerialName("server_state") val serverState: QBittorrentServerState? = null,
)

@Serializable
data class QBittorrentServerState(
    @SerialName("alltime_dl") val alltimeDl: Long = 0,
    @SerialName("alltime_ul") val alltimeUl: Long = 0,
    @SerialName("average_time_queue") val averageTimeQueue: Long = 0,
    @SerialName("connection_status") val connectionStatus: String = "",
    @SerialName("dht_nodes") val dhtNodes: Long = 0,
    @SerialName("dl_info_data") val dlInfoData: Long = 0,
    @SerialName("dl_info_speed") val downloadSpeed: Long = 0,
    @SerialName("dl_rate_limit") val dlRateLimit: Long = 0,
    @SerialName("free_space_on_disk") val freeSpaceOnDisk: Long = 0,
    @SerialName("global_ratio") val globalRatio: String = "",
    @SerialName("last_external_address_v4") val lastExternalAddressV4: String = "",
    @SerialName("last_external_address_v6") val lastExternalAddressV6: String = "",
    @SerialName("queued_io_jobs") val queuedIoJobs: Long = 0,
    @SerialName("queueing") val queueing: Boolean = false,
    @SerialName("read_cache_hits") val readCacheHits: String = "",
    @SerialName("read_cache_overload") val readCacheOverload: String = "",
    @SerialName("refresh_interval") val refreshInterval: Long = 0,
    @SerialName("total_buffers_size") val totalBuffersSize: Long = 0,
    @SerialName("total_peer_connections") val totalPeerConnections: Long = 0,
    @SerialName("total_queued_size") val totalQueuedSize: Long = 0,
    @SerialName("total_wasted_session") val totalWastedSession: Long = 0,
    @SerialName("up_info_data") val upInfoData: Long = 0,
    @SerialName("up_info_speed") val uploadSpeed: Long = 0,
    @SerialName("up_rate_limit") val upRateLimit: Long = 0,
    @SerialName("use_alt_speed_limits") val useAltSpeedLimits: Boolean = false,
    @SerialName("write_cache_overload") val writeCacheOverload: String = "",
)
