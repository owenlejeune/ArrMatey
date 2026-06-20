package com.dnfapps.arrmatey.backup

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual class AesTransportEncryptor : TransportEncryptor {
    private val algorithm = "AES/CBC/PKCS7Padding"
    private val pbkdf2Algorithm = "PBKDF2WithHmacSHA256"
    private val saltLength = 16
    private val ivLength = 16
    private val iterations = 65536
    private val keyLength = 256

    override fun encrypt(data: String, password: String): String {
        val salt = ByteArray(saltLength)
        SecureRandom().nextBytes(salt)

        val keySpec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
        val secretKeyFactory = SecretKeyFactory.getInstance(pbkdf2Algorithm)
        val keyBytes = secretKeyFactory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val iv = ByteArray(ivLength)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val cipherText = cipher.doFinal(data.encodeToByteArray())

        val combined = ByteArray(salt.size + iv.size + cipherText.size)
        System.arraycopy(salt, 0, combined, 0, salt.size)
        System.arraycopy(iv, 0, combined, salt.size, iv.size)
        System.arraycopy(cipherText, 0, combined, salt.size + iv.size, cipherText.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    override fun decrypt(encryptedData: String, password: String): String {
        return try {
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
            val salt = combined.sliceArray(0 until saltLength)
            val iv = combined.sliceArray(saltLength until saltLength + ivLength)
            val cipherText = combined.sliceArray(saltLength + ivLength until combined.size)

            val keySpec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
            val secretKeyFactory = SecretKeyFactory.getInstance(pbkdf2Algorithm)
            val keyBytes = secretKeyFactory.generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val ivSpec = IvParameterSpec(iv)
            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }
}
