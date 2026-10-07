package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class QualityProfileType {
    @SerialName("audiobook")
    Audiobook,

    @SerialName("ebook")
    Ebook,

    @SerialName("none")
    None,
}

@Serializable
data class QualityProfile(
    val id: Int,
    val name: String? = null,
    val upgradeAllowed: Boolean = false,
    val cutoff: Int = 0,
    val minFormatScore: Int = 0,
    val cutoffFormatScore: Int = 0,
    val minUpgradeFormatScore: Int? = null,
    val formatItems: List<FormatItem> = emptyList(),
    val profileType: QualityProfileType? = null,
) {
    val isAudiobook: Boolean
        get() = profileType == QualityProfileType.Audiobook

    val isEbook: Boolean
        get() = profileType == QualityProfileType.Ebook
}

fun List<QualityProfile>.filterForAudiobook(): List<QualityProfile> {
    val filtered = filter { it.isAudiobook || (!it.isEbook && it.profileType == null) }
    return if (filtered.isEmpty()) this else filtered
}

fun List<QualityProfile>.filterForEbook(): List<QualityProfile> {
    val filtered = filter { it.isEbook || (!it.isAudiobook && it.profileType == null) }
    return if (filtered.isEmpty()) this else filtered
}

fun List<QualityProfile>.defaultForAudiobook(preferredId: Int? = null): QualityProfile? = firstOrNull { it.id == preferredId }
    ?: filterForAudiobook().firstOrNull { it.isAudiobook }
    ?: filterForAudiobook().firstOrNull()

fun List<QualityProfile>.defaultForEbook(preferredId: Int? = null): QualityProfile? = firstOrNull { it.id == preferredId }
    ?: filterForEbook().firstOrNull { it.isEbook }
    ?: filterForEbook().firstOrNull()
