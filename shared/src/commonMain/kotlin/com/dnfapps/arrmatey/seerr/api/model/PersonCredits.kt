package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class PersonCredits(
    val cast: List<DiscoverResult> = emptyList(),
    val crew: List<DiscoverResult> = emptyList(),
    val id: Long
) {
    val backdrops: List<String>
        get() = cast.mapNotNull { it.fullBackdropPath } + crew.mapNotNull { it.fullBackdropPath }
}
