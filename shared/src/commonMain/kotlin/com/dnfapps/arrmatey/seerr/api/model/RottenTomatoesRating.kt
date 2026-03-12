package com.dnfapps.arrmatey.seerr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.ImageResource
import kotlinx.serialization.Serializable

@Serializable
data class RottenTomatoesRating(
    val title: String,
    val url: String,
    val criticsRating: CriticsRating,
    val criticsScore: Int,
    val audienceRating: AudienceRating,
    val audienceScore: Int,
)

enum class CriticsRating(val icon: ImageResource) {
    Rotten(MR.images.rt_rotten),
    Fresh(MR.images.rt_fresh)
}

enum class AudienceRating(val icon: ImageResource) {
    Spilled(MR.images.rt_aud_rotten),
    Upright(MR.images.rt_aud_fresh)
}
