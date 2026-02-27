package com.dnfapps.arrmatey.entensions

fun <T: Any> List<T>.firstChunked(take: Int): List<List<T>> =
    listOf(subList(0, take), subList(take, size))