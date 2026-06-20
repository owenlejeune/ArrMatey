package com.dnfapps.arrmatey.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AESEncryptionManager : EncryptionManager {
    private val algorithm = "AES/GCM/NoPadding"
    private val keyStoreAlias = "ArrMateyEncryptionKey"
    private val tagLength = 128
    private val ivLength = 12

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existingKey = keyStore.getEntry(keyStoreAlias, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) return existingKey.secretKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                keyStoreAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }

    override fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return plainText
        return try {
            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    override fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return encryptedText
        return try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
            if (combined.size < ivLength) return encryptedText

            val iv = combined.sliceArray(0 until ivLength)
            val cipherText = combined.sliceArray(ivLength until combined.size)
            
            val cipher = Cipher.getInstance(algorithm)
            val gcmParameterSpec = GCMParameterSpec(tagLength, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), gcmParameterSpec)
            
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            // Return original if decryption fails (e.g. if it was plain text)
            encryptedText
        }
    }
}
