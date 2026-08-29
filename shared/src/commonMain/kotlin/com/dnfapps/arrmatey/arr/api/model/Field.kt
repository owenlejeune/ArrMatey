package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Field(
    val order: Int,
    val name: String,
    val label: String,
    val unit: String,
    val helpText: String,
    val helpTextWarning: String,
    val helpLink: String,
    val value: String,
    val type: String,
    val advanced: Boolean,
    val selectOptions: List<SelectOption>,
    val selectOptionsProviderAction: String,
    val section: String,
    val hidden: String,
    val privacy: String,
    val placeholder: String,
    val isFloat: Boolean,
)
