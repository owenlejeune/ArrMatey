package com.dnfapps.arrmatey.extensions

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

fun List<String>.toJsonArray() =
    JsonArray(this.map { JsonPrimitive(it) })