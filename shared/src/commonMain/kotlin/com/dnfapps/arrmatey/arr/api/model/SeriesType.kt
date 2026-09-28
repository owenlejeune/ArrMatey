package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.anime
import com.dnfapps.arrmatey.shared.daily
import com.dnfapps.arrmatey.shared.standard
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.SerialName

enum class SeriesType(
    val resource: StringResource,
) {
    @SerialName("standard")
    Standard(MR.strings.standard),

    @SerialName("daily")
    Daily(MR.strings.daily),

    @SerialName("anime")
    Anime(MR.strings.anime),
}
