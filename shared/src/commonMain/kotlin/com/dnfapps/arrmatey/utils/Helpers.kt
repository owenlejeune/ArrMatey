package com.dnfapps.arrmatey.utils

infix fun <T> Boolean.thenGet(result: T): T? = if (this) result else null
