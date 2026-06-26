package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

/**
 * A subtitle language descriptor, used for missing subtitles and language tags.
 */
@Serializable
data class BazarrSubtitleLanguage(
    val name: String = "",
    val code2: String = "",
    val code3: String = "",
    val forced: Boolean = false,
    val hi: Boolean = false
)