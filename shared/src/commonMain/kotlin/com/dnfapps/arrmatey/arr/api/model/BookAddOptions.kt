package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookAddOptions(
//    val addType: AddType,
    val searchForNewBook: Boolean = false
)