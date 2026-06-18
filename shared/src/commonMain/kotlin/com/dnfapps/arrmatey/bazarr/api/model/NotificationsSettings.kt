package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationsSettings(
    val providers: List<NotificationProvider>
)
