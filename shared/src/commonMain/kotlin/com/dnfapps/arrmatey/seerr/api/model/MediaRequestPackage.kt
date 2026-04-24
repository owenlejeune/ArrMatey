package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaRequestPackage(
    val request: MediaRequest,
    val details: RequestMediaDetails?,
    val serviceDetails: ServiceDetails?
)