package com.dnfapps.arrmatey

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

// class IOSPlatform: Platform {
//    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
// }
//
// actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(ExperimentalNativeApi::class)
actual fun isDebug() = Platform.isDebugBinary
