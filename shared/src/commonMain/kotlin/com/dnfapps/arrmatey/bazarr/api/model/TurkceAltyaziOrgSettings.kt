package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TurkceAltyaziOrgSettings(
    val cookies: String,
    val user_agent: String
)
