package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.SerialName

enum class RequestType {
    @SerialName("movie")
    Movie,

    @SerialName("tv")
    Tv,

    @SerialName("person")
    Person
}