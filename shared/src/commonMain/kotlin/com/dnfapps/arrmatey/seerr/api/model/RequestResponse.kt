package com.dnfapps.arrmatey.seerr.api.model

import com.dnfapps.arrmatey.client.paging.PageInfo
import kotlinx.serialization.Serializable

@Serializable
data class RequestResponse(
    val pageInfo: PageInfo,
    val results: List<MediaRequest>,
    val serviceErrors: ServiceErrors? = null,
)
