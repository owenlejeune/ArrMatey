package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class QueueStatusMessage(
    val title: String? = null,
    val messages: List<String> = emptyList(),
)
