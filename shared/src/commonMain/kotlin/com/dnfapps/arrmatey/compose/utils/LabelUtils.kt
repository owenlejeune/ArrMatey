package com.dnfapps.arrmatey.compose.utils

import com.dnfapps.arrmatey.arr.api.model.Language

fun List<Language>.singleLanguageLabel(): String =
    when (this.size) {
        0 -> "Unknown"
        1 -> first().name ?: "Unknown"
        else -> "Multilingual"
    }
