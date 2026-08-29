package com.dnfapps.arrmatey.seerr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.ImageResource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RottenTomatoesRating(
    val title: String,
    val url: String,
    val criticsRating: CriticsRating? = null,
    val criticsScore: Int? = null,
    val audienceRating: AudienceRating? = null,
    val audienceScore: Int? = null,
)

@Serializable
enum class CriticsRating(
    val icon: ImageResource,
) {
    @SerialName("Rotten")
    Rotten(MR.images.rt_rotten),

    @SerialName("Fresh")
    Fresh(MR.images.rt_fresh),

    @SerialName("Certified Fresh")
    CertifiedFresh(MR.images.rt_fresh),
}

@Serializable
enum class AudienceRating(
    val icon: ImageResource,
) {
    @SerialName("Spilled")
    Spilled(MR.images.rt_aud_rotten),

    @SerialName("Upright")
    Upright(MR.images.rt_aud_fresh),
}
