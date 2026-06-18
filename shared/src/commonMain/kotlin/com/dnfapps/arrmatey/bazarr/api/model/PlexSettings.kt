package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class PlexSettings(
    val apikey: String,
    val auth_method: String,
    val client_identifier: String,
    val disable_auto_migration: Boolean,
    val email: String,
    val encryption_key: String,
    val ip: String,
    val migration_attempted: Boolean,
    val migration_successful: Boolean,
    val migration_timestamp: String,
    val movie_library: List<String>,
    val movie_library_ids: List<String>,
    val port: Int,
    val series_library: List<String>,
    val series_library_ids: List<String>,
    val server_local: Boolean,
    val server_machine_id: String,
    val server_name: String,
    val server_url: String,
    val set_episode_added: Boolean,
    val set_movie_added: Boolean,
    val ssl: Boolean,
    val token: String,
    val update_movie_library: Boolean,
    val update_series_library: Boolean,
    val user_id: String,
    val username: String
)
