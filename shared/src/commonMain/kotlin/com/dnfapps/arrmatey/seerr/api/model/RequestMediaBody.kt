package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestMediaBody(
    val mediaType: RequestType,
    val mediaId: Long,
    val is4k: Boolean = false,
    val serverId: Long? = null,
    val profileId: Long? = null,
    val rootFolder: String? = null,
    val languageProfileId: Long? = null,
    val seasons: List<Int>? = null,
    val userId: Long? = null
)
