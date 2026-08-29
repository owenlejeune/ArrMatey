package com.dnfapps.arrmatey.entensions

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <R> String.unlessEmpty(block: (String) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return if (this.isEmpty()) null else block(this)
}

fun String.takeUnlessEmpty(): String? = this.takeUnless { it.isEmpty() }
