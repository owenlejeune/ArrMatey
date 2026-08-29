package com.dnfapps.arrmatey.entensions

const val BULLET = " • "
const val ARROW_UP = "↑"
const val ARROW_DOWN = "↓"

fun StringBuilder.bullet(): StringBuilder =
    apply {
        append(BULLET)
    }

fun <T : Appendable> T.bullet(): T =
    apply {
        append(BULLET)
    }
