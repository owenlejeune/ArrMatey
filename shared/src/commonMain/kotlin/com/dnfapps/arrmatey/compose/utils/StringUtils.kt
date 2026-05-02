package com.dnfapps.arrmatey.compose.utils

fun String.breakable() = replace(".", ".\u200B")
    .replace(" ", " \u200B")