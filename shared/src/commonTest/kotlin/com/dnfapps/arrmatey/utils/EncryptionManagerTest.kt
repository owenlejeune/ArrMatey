package com.dnfapps.arrmatey.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EncryptionManagerTest {
    private val manager: EncryptionManager = SimpleEncryptionManager()

    @Test
    fun testEncryptDecryptRoundTrip() {
        val plain = "hello world"
        val encrypted = manager.encrypt(plain)
        assertNotEquals(plain, encrypted)
        assertEquals(plain, manager.decrypt(encrypted))
    }

    @Test
    fun testEncryptEmptyStringReturnsEmpty() {
        assertEquals("", manager.encrypt(""))
        assertEquals("", manager.decrypt(""))
    }

    @Test
    fun testEncryptDecryptRoundTripNonAscii() {
        val plain = "héllo wörld — 日本語 🚀"
        val encrypted = manager.encrypt(plain)
        assertEquals(plain, manager.decrypt(encrypted))
    }

    @Test
    fun testEncryptDecryptRoundTripLongString() {
        val plain = "a".repeat(4096) + "-boundary-" + "b".repeat(4096)
        val encrypted = manager.encrypt(plain)
        assertEquals(plain, manager.decrypt(encrypted))
    }

    @Test
    fun testEncryptDecryptRoundTripApiKeyShape() {
        val plain = "sk_live_1234567890abcdefABCDEF"
        assertEquals(plain, manager.decrypt(manager.encrypt(plain)))
    }

    @Test
    fun testDecryptInvalidBase64FallsBackSilently() {
        // Documents current fallback behavior: invalid Base64 returns the input unchanged.
        val garbage = "!!!not-base64!!!"
        assertEquals(garbage, manager.decrypt(garbage))
    }

    @Test
    fun testEncryptProducesDeterministicOutput() {
        val plain = "same input"
        assertEquals(manager.encrypt(plain), manager.encrypt(plain))
    }
}
