package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ArrImage(
    val coverType: CoverType,
    val url: String? = null,
    val remoteUrl: String? = null,
) {
    fun rebuildWithLocalUrls(instanceUrl: String): ArrImage =
        if (remoteUrl?.startsWith("/") == true) {
            copy(remoteUrl = "${instanceUrl}$url")
        } else {
            this
        }
}
