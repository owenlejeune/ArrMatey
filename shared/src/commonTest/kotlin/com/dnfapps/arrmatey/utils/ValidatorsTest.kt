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

    @Test
    fun testIsValidUrlAcceptsUppercaseScheme() {
        assertTrue("HTTP://google.com".isValidUrl())
        assertTrue("HTTPS://google.com".isValidUrl())
    }

    @Test
    fun testIsValidUrlAcceptsBoundaryPorts() {
        assertTrue("http://localhost:1".isValidUrl())
        assertTrue("http://localhost:65535".isValidUrl())
    }

    @Test
    fun testIsValidUrlRejectsPortZero() {
        assertFalse("http://localhost:0".isValidUrl())
    }

    @Test
    fun testIsValidUrlAcceptsDeepPaths() {
        assertTrue("https://host.com/api/v1/resource?query=1&other=2".isValidUrl())
    }

    @Test
    fun testIsValidUrlRejectsWhitespace() {
        assertFalse("http://google .com".isValidUrl())
        assertFalse(" http://google.com".isValidUrl())
        assertFalse("http://google.com ".isValidUrl())
    }

    @Test
    fun testIsValidUrlRejectsMissingHost() {
        assertFalse("http:///path".isValidUrl())
    }

    @Test
    fun testIsValidUrlRejectsEmptyString() {
        assertFalse("".isValidUrl())
    }
}
