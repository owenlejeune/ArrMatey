package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ArtistMember(
    val name: String? = null,
    val instrument: String? = null,
    val images: List<ArrImage> = emptyList(),
)
