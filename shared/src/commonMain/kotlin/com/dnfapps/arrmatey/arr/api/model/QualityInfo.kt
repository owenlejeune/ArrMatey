package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class QualityInfo(
    val quality: Quality,
    val revision: Revision,
) {
    val qualityLabel: String
        get() {
            val name = quality.name
            val resolution = quality.resolution

            if (name.contains(resolution.toString()) || resolution == null) {
                return name
            }

            if (resolution > 0) {
                return "$name ${resolution}p"
            }

            return name
        }
}
