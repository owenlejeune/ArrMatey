package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MetadataProfile(
    val id: Int,
    val name: String? = null,
    val profileType: Int? = null,
) {
    val isAudiobook: Boolean
        get() = profileType == 1

    val isEbook: Boolean
        get() = profileType == 2
}

fun List<MetadataProfile>.filterForAudiobook(): List<MetadataProfile> {
    val filtered = filter { it.isAudiobook || it.profileType == 0 || it.profileType == null }
    return if (filtered.isEmpty()) this else filtered
}

fun List<MetadataProfile>.filterForEbook(): List<MetadataProfile> {
    val filtered = filter { it.isEbook || it.profileType == 0 || it.profileType == null }
    return if (filtered.isEmpty()) this else filtered
}

fun List<MetadataProfile>.defaultForAudiobook(preferredId: Int? = null): MetadataProfile? = firstOrNull { it.id == preferredId }
    ?: filterForAudiobook().firstOrNull { it.isAudiobook }
    ?: filterForAudiobook().firstOrNull { it.name?.contains("audio", ignoreCase = true) == true }
    ?: filterForAudiobook().firstOrNull()

fun List<MetadataProfile>.defaultForEbook(preferredId: Int? = null): MetadataProfile? = firstOrNull { it.id == preferredId }
    ?: filterForEbook().firstOrNull { it.isEbook }
    ?: filterForEbook().firstOrNull { it.name?.contains("ebook", ignoreCase = true) == true }
    ?: filterForEbook().firstOrNull { it.name?.contains("audio", ignoreCase = true) != true }
    ?: filterForEbook().firstOrNull()
