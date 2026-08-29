package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExtraFile(
    val id: Int,
    val movieId: Int,
    val movieFileId: Int,
    val relativePath: String,
    val extension: String,
    val languageTags: List<String>,
    val title: String? = null,
    val type: ExtraFileType,
)

enum class ExtraFileType {
    @SerialName("subtitle")
    Subtitle,

    @SerialName("metadata")
    Metadata,

    @SerialName("other")
    Other,
}
