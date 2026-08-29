package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class UnmappedFolder(
    val name: String,
    val path: String,
    val relativePath: String,
)
