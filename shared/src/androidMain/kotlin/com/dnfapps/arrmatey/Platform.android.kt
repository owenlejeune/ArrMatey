package com.dnfapps.arrmatey

actual fun isDebug(): Boolean =
    runCatching {
        Class.forName("com.dnfapps.arrmatey.BuildConfig").getField("DEBUG").getBoolean(null)
    }.getOrDefault(false)
