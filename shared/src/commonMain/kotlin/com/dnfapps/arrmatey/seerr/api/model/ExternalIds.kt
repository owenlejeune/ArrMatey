package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ExternalIds(
    val facebookId: String? = null,
    val freebaseId: String? = null,
    val freebaseMid: String? = null,
    val imdbId: String? = null,
    val instagramId: String? = null,
    val tvdbId: Long? = null,
    val tvrageId: Long? = null,
    val twitterId: String? = null
)