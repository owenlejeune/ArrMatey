package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UpdateMechanism {
    @SerialName("builtIn")
    BUILT_IN,

    @SerialName("script")
    SCRIPT,

    @SerialName("external")
    EXTERNAL,

    @SerialName("apt")
    APT,

    @SerialName("docker")
    DOCKER,
}
