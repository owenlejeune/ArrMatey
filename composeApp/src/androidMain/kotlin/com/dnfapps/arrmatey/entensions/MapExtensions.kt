package com.dnfapps.arrmatey.entensions

inline fun <K, V> Map<out K, V>.forEachIndexed(action: (Int, Map.Entry<K, V>) -> Unit) {
    var i = 0
    for (element in this) {
        action(i++, element)
    }
}
