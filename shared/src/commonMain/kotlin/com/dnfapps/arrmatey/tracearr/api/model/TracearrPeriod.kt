package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TracearrPeriod(val value: String) {
    @SerialName("week")
    Week("week"),

    @SerialName("month")
    Month("month"),

    @SerialName("year")
    Year("year");

    override fun toString(): String = value
}
