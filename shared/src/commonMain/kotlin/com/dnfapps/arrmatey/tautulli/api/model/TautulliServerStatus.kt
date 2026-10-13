package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TautulliServerStatus(
    @SerialName("pms_identifier") val pmsIdentifier: String? = null,
    @SerialName("pms_name") val pmsName: String? = null,
    @SerialName("pms_version") val pmsVersion: String? = null,
    @SerialName("pms_url") val pmsUrl: String? = null,
    @SerialName("tautulli_version") val tautulliVersion: String? = null,
    @SerialName("tautulli_install_type") val tautulliInstallType: String? = null,
)
