package com.dnfapps.arrmatey.model

import kotlinx.serialization.Serializable

@Serializable
data class InfoItem(
    val key: String,
    val value: String,
    @Transient
    val onClick: (() -> Unit)? = null
)

fun Map<String, String>.toInfoList(): List<InfoItem> =
    map { (k, v) -> InfoItem(k, v) }