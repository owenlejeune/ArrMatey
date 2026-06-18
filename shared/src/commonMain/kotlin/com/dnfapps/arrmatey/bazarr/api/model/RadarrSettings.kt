package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RadarrSettings(
    val apikey: String,
    val base_url: String,
    val defer_search_signalr: Boolean,
    val excluded_tags: List<String>,
    val full_update: String,
    val full_update_day: Int,
    val full_update_hour: Int,
    val http_timeout: Int,
    val ip: String,
    val movies_sync: Int,
    val movies_sync_on_live: Boolean,
    val only_monitored: Boolean,
    val port: Int,
    val ssl: Boolean,
    val sync_only_monitored_movies: Boolean,
    val use_ffprobe_cache: Boolean
)
