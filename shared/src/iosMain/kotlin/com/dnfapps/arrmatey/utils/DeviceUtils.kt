package com.dnfapps.arrmatey.utils

import platform.UIKit.UIScreen

actual val screenDensity: Float
    get() = UIScreen.mainScreen.scale.toFloat()
