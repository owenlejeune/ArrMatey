package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.monitoring_scheme_all
import com.dnfapps.arrmatey.shared.monitoring_scheme_missing
import com.dnfapps.arrmatey.shared.monitoring_scheme_none
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MonitoringScheme(val value: String, val resource: StringResource) {
    @SerialName("all")
    All("all", MR.strings.monitoring_scheme_all),

    @SerialName("missing")
    Missing("missing", MR.strings.monitoring_scheme_missing),

    @SerialName("none")
    None("none", MR.strings.monitoring_scheme_none),
}
