package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class NapiProjektSettings(
    val only_authors: Boolean,
    val only_real_names: Boolean
)
