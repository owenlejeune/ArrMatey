package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class CommandResponse(
    val name: String,
    val commandName: String,
    val priority: String,
    val status: String,
    val result: String,
    val queued: String,
    val started: String? = null,
    val trigger: String,
    val stateChangeTime: String? = null,
    val sendUpdatesToClient: Boolean,
    val updateScheduledTask: Boolean,
    val id: Int,
    val body: CommandResponseBody,
)

@Serializable
data class CommandResponseBody(
    val sendUpdatesToClient: Boolean,
    val updateScheduledTask: Boolean,
    val requiresDiskAccess: Boolean,
    val isExclusive: Boolean = false,
    val isTypeExclusive: Boolean = false,
    val isLongRunning: Boolean = false,
    val name: String,
    val trigger: String,
    val suppressMessages: String,
)
