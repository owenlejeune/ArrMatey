package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class SpokenLanguage(
    @JsonNames("englishName", "english_name")
    val englishName: String,
    val iso_639_1: String,
    val name: String
)