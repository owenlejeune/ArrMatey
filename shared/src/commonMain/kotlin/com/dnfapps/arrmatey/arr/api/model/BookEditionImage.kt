package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookEditionImage(
    val url: String,
    val coverType: CoverType,
    val extension: String
)