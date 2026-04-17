package com.dnfapps.arrmatey.instances.model

import kotlinx.serialization.Serializable

@Serializable
data class InstanceHeader(
    val key: String = "",
    val value: String = ""
)