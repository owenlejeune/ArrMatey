package com.dnfapps.arrmatey.seerr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.SerialName

enum class RequestType {
    @SerialName("movie")
    Movie,

    @SerialName("tv")
    Tv,

    @SerialName("person")
    Person,

    ;

    val associatedInstanceType: InstanceType?
        get() =
            when (this) {
                Movie -> InstanceType.Radarr
                Tv -> InstanceType.Sonarr
                Person -> null
            }
}
