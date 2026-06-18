package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationProvider(
    val enabled: Boolean,
    val name: String,
    val url: String?
)
