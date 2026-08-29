package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class QualityProfile(
    val id: Int,
    val name: String? = null,
    val upgradeAllowed: Boolean = false,
    val cutoff: Int,
    val minFormatScore: Int,
    val cutoffFormatScore: Int,
    val minUpgradeFormatScore: Int? = null,
    val formatItems: List<FormatItem>,
)
