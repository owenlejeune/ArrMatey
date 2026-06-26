package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A subtitle file that is already present for a media item.
 * [path] is null for embedded subtitles that have no external file.
 */
@Serializable
data class BazarrSubtitle(
    val path: String? = null,
    val name: String = "",
    val code2: String = "",
    val code3: String = "",
    val forced: Boolean = false,
    val hi: Boolean = false,
    @SerialName("file_size") val fileSize: Long = 0
) {
    /** Whether this subtitle is an external (downloadable / deletable) file rather than embedded. */
    val isExternal: Boolean get() = !path.isNullOrBlank()

    val isEmbedded: Boolean get() = path.isNullOrBlank() && fileSize == 0L
}
