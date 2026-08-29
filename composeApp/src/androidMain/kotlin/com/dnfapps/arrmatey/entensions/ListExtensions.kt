package com.dnfapps.arrmatey.entensions

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun <T : Any> List<T>.firstChunked(take: Int): List<List<T>> = listOf(subList(0, take), subList(take, size))

@OptIn(ExperimentalContracts::class)
inline fun <T : Any, R> List<T>.unlessEmpty(block: (List<T>) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return if (this.isEmpty()) null else block(this)
}
