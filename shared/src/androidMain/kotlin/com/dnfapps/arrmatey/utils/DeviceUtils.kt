package com.dnfapps.arrmatey.utils

import android.content.res.Resources

actual val screenDensity: Float
    get() = Resources.getSystem().displayMetrics.density
