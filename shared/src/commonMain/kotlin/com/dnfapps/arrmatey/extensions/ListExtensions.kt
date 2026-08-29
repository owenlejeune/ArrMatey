package com.dnfapps.arrmatey.extensions

import com.dnfapps.arrmatey.compose.utils.SortOrder

inline fun <T, R : Comparable<R>> Iterable<T>.orderedSortedBy(
    order: SortOrder,
    crossinline selector: (T) -> R?,
): List<T> =
    when (order) {
        SortOrder.Asc -> sortedBy(selector)
        SortOrder.Desc -> sortedByDescending(selector)
    }

fun <T> Iterable<T>.orderedSortedWith(
    order: SortOrder,
    comparator: Comparator<in T>,
): List<T> =
    when (order) {
        SortOrder.Asc -> sortedWith(comparator)
        SortOrder.Desc -> sortedWith(comparator.reversed())
    }
