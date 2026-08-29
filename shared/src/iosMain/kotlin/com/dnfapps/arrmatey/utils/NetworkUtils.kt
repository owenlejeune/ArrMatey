package com.dnfapps.arrmatey.utils

// @OptIn(ExperimentalForeignApi::class)
// class IOSNetworkUtils : NetworkUtils {
//
//    override fun getCurrentWifiSsid(): String? {
//        // Get supported interfaces
//        val interfacesRef = CNCopySupportedInterfaces() ?: return null
//
//        val count = CFArrayGetCount(interfacesRef)
//
//        if (count == 0L) {
//            CFRelease(interfacesRef)
//            return null
//        }
//
//        var ssid: String? = null
//
//        for (i in 0 until count) {
//            val interfacePtr = CFArrayGetValueAtIndex(interfacesRef, i) ?: continue
//
//            // Interpret as CFStringRef
//            val interfaceCFString = interpretCPointer<ByteVar>(interfacePtr.rawValue)
//                ?.let { CFStringCreateWithCString(null, it.toString(), kCFStringEncodingUTF8) }
//                ?: continue
//
//            // Get current network info
//            val networkInfoRef = CNCopyCurrentNetworkInfo(interfaceCFString) ?: continue
//
//            // Get SSID value from dictionary
//            val ssidValue = CFDictionaryGetValue(networkInfoRef, kCNNetworkInfoKeySSID)
//
//            if (ssidValue != null) {
//                // Convert CFString to Kotlin String
//                memScoped {
//                    val buffer = allocArray<ByteVar>(256)
//                    val cfString = interpretCPointer<CFStringRefVar>(ssidValue.rawValue)?.pointed?.value
//
//                    if (cfString != null && CFStringGetCString(cfString, buffer, 256, kCFStringEncodingUTF8)) {
//                        ssid = buffer.toKString()
//                    }
//                }
//            }
//
//            CFRelease(networkInfoRef)
//            CFRelease(interfaceCFString)
//
//            if (ssid != null) break
//        }
//
//        CFRelease(interfacesRef)
//        return ssid
//    }
//
//    override fun isConnectedToWifi(): Boolean {
//        // Simple check: if we can get SSID, we're connected
//        return getCurrentWifiSsid() != null
//    }
// }

// @OptIn(ExperimentalForeignApi::class)
// class IOSNetworkUtils : NetworkUtils {
//
//    override fun getCurrentWifiSsid(): String? {
//        return memScoped {
//            // Get supported interfaces
//            print("****1")
//            val interfacesRef = CNCopySupportedInterfaces() ?: return null
//
//            print("****2")
//            val interfaces = CFBridgingRelease(interfacesRef) as? List<*> ?: return null
//
//            print("****3")
//            for (iface in interfaces) {
//                val interfaceName = iface as? String ?: continue
//
//                // Get network info
//                val networkInfoRef = CNCopyCurrentNetworkInfo(
//                    interfaceName as CFStringRef
//                ) ?: continue
//
//                val networkInfo = CFBridgingRelease(networkInfoRef) as? Map<*, *>
//
//                // Get SSID
//                val ssid = networkInfo?.get(kCNNetworkInfoKeySSID as String) as? String
//
//                print("GOT SSID $ssid")
//
//                if (ssid != null) {
//                    return ssid
//                }
//            }
//
//            print("****4")
//            return null
//        }
//    }
//
//    override fun isConnectedToWifi(): Boolean {
//        return getCurrentWifiSsid() != null
//    }
// }

// @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
// class IOSNetworkUtils : NetworkUtils {
//
//    override fun getCurrentWifiSsid(): String? {
//        // Get supported interfaces
//        val interfacesPtr = CNCopySupportedInterfaces() ?: return null
//
//        // Convert to NSArray
//        val interfaces = CFBridgingRelease(interfacesPtr) as? NSArray ?: return null
//
//        val count = interfaces.count
//
//        for (i in 0UL until count) {
//            val interfaceName = interfaces.objectAtIndex(i) as? NSString ?: continue
//
//            // Get network info for this interface
//            val networkInfoPtr = CNCopyCurrentNetworkInfo(interfaceName as CFStringRef) ?: continue
//
//            // Convert to NSDictionary
//            val networkInfo = CFBridgingRelease(networkInfoPtr) as? NSDictionary ?: continue
//
//            // Get SSID
//            val ssidKey = kCNNetworkInfoKeySSID as NSString
//            val ssid = networkInfo.objectForKey(ssidKey) as? NSString
//
//            if (ssid != null) {
//                return ssid.toString()
//            }
//        }
//
//        return null
//    }
//
//    override fun isConnectedToWifi(): Boolean {
//        return getCurrentWifiSsid() != null
//    }
// }

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreFoundation.CFStringRef
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSArray
import platform.Foundation.NSDictionary
import platform.Foundation.NSString
import platform.SystemConfiguration.CNCopyCurrentNetworkInfo
import platform.SystemConfiguration.CNCopySupportedInterfaces
import platform.SystemConfiguration.kCNNetworkInfoKeySSID

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IOSNetworkUtils : NetworkUtils {
    override fun getCurrentWifiSsid(): String? {
        // Get supported interfaces
        val interfacesPtr = CNCopySupportedInterfaces() ?: return null

        // Convert to NSArray
        val interfaces = CFBridgingRelease(interfacesPtr) as? NSArray ?: return null

        val count = interfaces.count

        for (i in 0UL until count) {
            val interfaceName = interfaces.objectAtIndex(i) as? NSString ?: continue

            // Get network info for this interface
            val networkInfoPtr = CNCopyCurrentNetworkInfo(interfaceName as CFStringRef) ?: continue

            // Convert to NSDictionary
            val networkInfo = CFBridgingRelease(networkInfoPtr) as? NSDictionary ?: continue

            // Get SSID
            val ssidKey = kCNNetworkInfoKeySSID as NSString
            val ssid = networkInfo.objectForKey(ssidKey) as? NSString

            if (ssid != null) {
                return ssid.toString()
            }
        }

        return null
    }

    override fun isConnectedToWifi(): Boolean = getCurrentWifiSsid() != null
}

private var networkUtilsInstance: NetworkUtils? = null

fun initializeNetworkUtils() {
    networkUtilsInstance = IOSNetworkUtils()
}

actual fun getNetworkUtils(): NetworkUtils = networkUtilsInstance ?: IOSNetworkUtils().also { networkUtilsInstance = it }
