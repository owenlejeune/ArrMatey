package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName

enum class IndexerPrivacy {
    @SerialName("public")
    Public,

    @SerialName("private")
    Private,

    @SerialName("semiPrivate")
    SemiPrivate
}