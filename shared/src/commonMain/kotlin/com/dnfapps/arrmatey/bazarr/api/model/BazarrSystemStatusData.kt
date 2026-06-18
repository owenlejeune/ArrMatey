package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BazarrSystemStatusData(
    val bazarr_version: String,
    val package_version: String,
    val sonarr_version: String,
    val radarr_version: String,
    val operating_system: String,
    val python_version: String,
    val database_engine: String,
    val database_migration: String,
    val bazarr_directory: String,
    val bazarr_config_directory: String,
    val start_time: Double,
    val timezone: String,
    val cpu_cores: Int
)
