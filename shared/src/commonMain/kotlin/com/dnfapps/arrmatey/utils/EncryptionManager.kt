package com.dnfapps.arrmatey.utils

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface EncryptionManager {
    fun encrypt(plainText: String): String
    fun decrypt(encryptedText: String): String
}

class SimpleEncryptionManager : EncryptionManager {
    private val key = "ArrMateySecretKey".encodeToByteArray()

    @OptIn(ExperimentalEncodingApi::class)
    override fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return plainText
        val bytes = plainText.encodeToByteArray()
        val encrypted = ByteArray(bytes.size)
        for (i in bytes.indices) {
            encrypted[i] = (bytes[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return Base64.encode(encrypted)
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return encryptedText
        return try {
            val bytes = Base64.decode(encryptedText)
            val decrypted = ByteArray(bytes.size)
            for (i in bytes.indices) {
                decrypted[i] = (bytes[i].toInt() xor key[i % key.size].toInt()).toByte()
            }
            decrypted.decodeToString()
        } catch (e: Exception) {
            encryptedText
        }
    }
}
