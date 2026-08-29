package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DatabaseType {
    @SerialName("sqLite")
    SQLITE,

    @SerialName("postgreSQL")
    POSTGRESQL,
}
