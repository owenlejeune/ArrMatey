package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ArrImage(
    val coverType: CoverType,
    val url: String? = null,
    val remoteUrl: String? = null,
) {
    fun rebuildWithLocalUrls(instanceUrl: String): ArrImage {
        val cleanInstanceUrl = instanceUrl.trimEnd('/')
        return when {
            url?.startsWith("/") == true -> copy(remoteUrl = "$cleanInstanceUrl$url")
            remoteUrl?.startsWith("/") == true -> copy(remoteUrl = "$cleanInstanceUrl$remoteUrl")
            else -> this
        }
    }
}
