package com.dnfapps.arrmatey.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidatorsTest {

    @Test
    fun testIsValidUrl() {
        assertTrue("http://google.com".isValidUrl())
        assertTrue("https://google.com".isValidUrl())
        assertTrue("http://localhost:8080".isValidUrl())
        assertTrue("http://192.168.1.1:8989/sonarr".isValidUrl())
        assertTrue("http://user:pass@host.com".isValidUrl())
        
        assertFalse("google.com".isValidUrl())
        assertFalse("ftp://google.com".isValidUrl())
        assertFalse("http://".isValidUrl())
        assertFalse("http://localhost:70000".isValidUrl()) // Port too high
    }
}
