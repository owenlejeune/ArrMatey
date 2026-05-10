package com.dnfapps.arrmatey.entensions

const val Bullet = " • "
const val ArrowUp = "↑"
const val ArrowDown = "↓"

fun StringBuilder.bullet(): StringBuilder = apply {
    append(Bullet)
}
fun <T: Appendable> T.bullet(): T = apply {
    append(Bullet)
}