package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AuthType {
    @SerialName("none")
    NONE,

    @SerialName("basic")
    BASIC,

    @SerialName("forms")
    FORMS,

    @SerialName("external")
    EXTERNAL,
}
