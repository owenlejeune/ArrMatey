package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SonarrSettings(
    val apikey: String,
    val base_url: String,
    val defer_search_signalr: Boolean,
    val episodes_sync: Int,
    val exclude_season_zero: Boolean,
    val excluded_series_types: List<String>,
    val excluded_tags: List<String>,
    val full_update: String,
    val full_update_day: Int,
    val full_update_hour: Int,
    val http_timeout: Int,
    val ip: String,
    val only_monitored: Boolean,
    val port: Int,
    val series_sync: Int,
    val series_sync_on_live: Boolean,
    val ssl: Boolean,
    val sync_only_monitored_episodes: Boolean,
    val sync_only_monitored_series: Boolean,
    val use_ffprobe_cache: Boolean
)
