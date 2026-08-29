package com.dnfapps.arrmatey.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetworkUtilsTest {
    private class FakeNetworkUtils(
        private val currentSsid: String?,
        private val wifiConnected: Boolean = true,
    ) : NetworkUtils {
        override fun getCurrentWifiSsid(): String? = currentSsid

        override fun isConnectedToWifi(): Boolean = wifiConnected
    }

    @Test
    fun testIsConnectedToSsidExactMatch() {
        val utils = FakeNetworkUtils(currentSsid = "HomeNetwork")
        assertTrue(utils.isConnectedToSsid("HomeNetwork"))
    }

    @Test
    fun testIsConnectedToSsidIgnoresCase() {
        val utils = FakeNetworkUtils(currentSsid = "HomeNetwork")
        assertTrue(utils.isConnectedToSsid("homenetwork"))
        assertTrue(utils.isConnectedToSsid("HOMENETWORK"))
    }

    @Test
    fun testIsConnectedToSsidReturnsFalseWhenDifferent() {
        val utils = FakeNetworkUtils(currentSsid = "HomeNetwork")
        assertFalse(utils.isConnectedToSsid("Guest"))
    }

    @Test
    fun testIsConnectedToSsidReturnsFalseWhenNoSsid() {
        val utils = FakeNetworkUtils(currentSsid = null)
        assertFalse(utils.isConnectedToSsid("HomeNetwork"))
    }
}
