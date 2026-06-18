package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupSettings(
    val day: Int,
    val folder: String,
    val frequency: String,
    val hour: Int,
    val retention: Int
)
