package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BazarrRelease(
    @SerialName("dont_matches")
    val dontMatches: List<String>,
    @SerialName("forced")
    val forcedString: String,
    @SerialName("hearing_impaired")
    val hearingImpairedString: String,
    val language: String,
    val matches: List<String>,
    @SerialName("original_format")
    val originalFormatString: String,
    @SerialName("orig_score")
    val origScore: Int,
    val provider: String,
    @SerialName("release_info")
    val releaseInfo: List<String>,
    val score: Int,
    @SerialName("score_without_has")
    val scoreWithoutHash: Int,
    val subtitle: String,
    val uploader: String,
    val url: String
) {
    val forced: Boolean
        get() = forcedString == "True"

    val hearingImpaired: Boolean
        get() = hearingImpairedString == "True"

    val originalFormat: Boolean
        get() = originalFormatString == "True"

}
