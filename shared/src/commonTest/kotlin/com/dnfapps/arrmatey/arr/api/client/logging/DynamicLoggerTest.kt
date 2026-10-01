package com.dnfapps.arrmatey.arr.api.client.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DynamicLoggerTest {

    @Test
    fun testSanitizeLogMessageRemovesSABnzbdApiKeyQueryParameter() {
        val input = "REQUEST: http://192.168.1.100:8080/api?apikey=1234567890abcdef&mode=queue"
        val sanitized = sanitizeLogMessage(input)

        assertFalse(sanitized.contains("1234567890abcdef"))
        assertEquals("REQUEST: http://192.168.1.100:8080/api?apikey=REDACTED&mode=queue", sanitized)
    }

    @Test
    fun testSanitizeLogMessageRemovesApiKeyAtEnd() {
        val input = "REQUEST: http://192.168.1.100:8080/api?mode=queue&apikey=mySecretKey123"
        val sanitized = sanitizeLogMessage(input)

        assertFalse(sanitized.contains("mySecretKey123"))
        assertEquals("REQUEST: http://192.168.1.100:8080/api?mode=queue&apikey=REDACTED", sanitized)
    }

    @Test
    fun testSanitizeLogMessageCaseInsensitiveQueryParam() {
        val input = "REQUEST: http://example.com/api?APIKEY=secretKey&TOKEN=tokenValue"
        val sanitized = sanitizeLogMessage(input)

        assertFalse(sanitized.contains("secretKey"))
        assertFalse(sanitized.contains("tokenValue"))
        assertEquals("REQUEST: http://example.com/api?APIKEY=REDACTED&TOKEN=REDACTED", sanitized)
    }

    @Test
    fun testSanitizeLogMessageHeaders() {
        val input = """
            METHOD: GET
            COMMON HEADERS
            -> Accept: application/json
            -> X-Api-Key: super-secret-api-key
            -> Authorization: Bearer secret-jwt-token
            <- Set-Cookie: session_id=abc123456; Path=/
        """.trimIndent()

        val sanitized = sanitizeLogMessage(input)

        assertFalse(sanitized.contains("super-secret-api-key"))
        assertFalse(sanitized.contains("secret-jwt-token"))
        assertFalse(sanitized.contains("session_id=abc123456"))

        assertTrue(sanitized.contains("-> X-Api-Key: REDACTED"))
        assertTrue(sanitized.contains("-> Authorization: REDACTED"))
        assertTrue(sanitized.contains("<- Set-Cookie: REDACTED"))
        assertTrue(sanitized.contains("-> Accept: application/json"))
    }

    @Test
    fun testSanitizeLogMessageUrlCredentials() {
        val input = "REQUEST: http://admin:secretpass@192.168.1.50:8080/api?mode=queue"
        val sanitized = sanitizeLogMessage(input)

        assertFalse(sanitized.contains("secretpass"))
        assertTrue(sanitized.contains("http://admin:REDACTED@192.168.1.50:8080/api?mode=queue"))
    }

    @Test
    fun testSanitizeLogMessageJsonBody() {
        val input = """{"status": "ok", "apikey": "123456", "token": "secret_token"}"""
        val sanitized = sanitizeLogMessage(input)

        assertFalse(sanitized.contains("123456"))
        assertFalse(sanitized.contains("secret_token"))
        assertEquals("""{"status": "ok", "apikey": "REDACTED", "token": "REDACTED"}""", sanitized)
    }

    @Test
    fun testSanitizeLogMessageLeavesNonSensitiveDataIntact() {
        val input = "REQUEST: http://192.168.1.100:8080/api?mode=queue&output=json"
        val sanitized = sanitizeLogMessage(input)

        assertEquals(input, sanitized)
    }
}
