package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A single subtitle result returned by a manual provider search
 * (`/api/providers/episodes` or `/api/providers/movies`).
 *
 * [forced]/[hearingImpaired]/[originalFormat] are returned by Bazarr as the strings
 * "True"/"False". [subtitle] is an opaque token (a pickled subtitle reference) that must
 * be echoed back verbatim when downloading this result.
 */
@Serializable
data class ProviderSubtitle(
    val provider: String = "",
    val language: String = "",
    val forced: String = "False",
    @SerialName("hearing_impaired") val hearingImpaired: String = "False",
    @SerialName("original_format") val originalFormat: String = "False",
    val score: Int = 0,
    @SerialName("orig_score") val origScore: Int? = null,
    @SerialName("score_without_hash") val scoreWithoutHash: Int? = null,
    @SerialName("release_info") val releaseInfo: List<String> = emptyList(),
    val matches: List<String> = emptyList(),
    @SerialName("dont_matches") val dontMatches: List<String> = emptyList(),
    val uploader: String? = null,
    val url: String? = null,
    val subtitle: String = ""
) {
    val isForced: Boolean get() = forced.equals("true", ignoreCase = true)
    val isHearingImpaired: Boolean get() = hearingImpaired.equals("true", ignoreCase = true)
}

/** Status of a configured subtitle provider, from `/api/providers`. */
@Serializable
data class ProviderStatus(
    val name: String = "",
    val status: String? = null,
    val retry: String? = null
)

@Serializable
data class ProvidersResponse(
    val data: List<ProviderStatus> = emptyList()
)

/** Manual provider search results are wrapped in a `data` envelope by Bazarr. */
@Serializable
data class ProviderSubtitlesResponse(
    val data: List<ProviderSubtitle> = emptyList()
)
