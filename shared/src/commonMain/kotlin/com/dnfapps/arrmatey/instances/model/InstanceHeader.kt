package com.dnfapps.arrmatey.instances.model

import kotlinx.serialization.Serializable

@Serializable
data class InstanceHeader(
    val key: String = "",
    val value: String = "",
    val restrictionType: HeaderRestrictionType = HeaderRestrictionType.Always,
    val restrictedSsids: List<String> = emptyList()
) {
    constructor(key: String, value: String): this(key, value, HeaderRestrictionType.Always, emptyList())
}

@Serializable
enum class HeaderRestrictionType {
    Always,
    RemoteOnly,
    SpecificSsids
}
