package com.dnfapps.arrmatey.backup

import kotlinx.cinterop.*
import platform.CoreCrypto.*
import platform.Foundation.*
import platform.Security.*
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual class AesTransportEncryptor : TransportEncryptor {
    private val saltLength = 16
    private val ivLength = 16
    private val iterations = 65536u
    private val keyLength = 32u // 256 bits

    @OptIn(BetaInteropApi::class)
    override fun encrypt(data: String, password: String): String = memScoped {
        val salt = ByteArray(saltLength)
        val saltPtr = allocArray<UByteVar>(saltLength)
        if (SecRandomCopyBytes(kSecRandomDefault, saltLength.toULong(), saltPtr) != 0) return ""
        for (i in 0 until saltLength) salt[i] = saltPtr[i].toByte()
        
        val derivedKey = deriveKey(password, salt) ?: return ""
        
        val iv = ByteArray(ivLength)
        val ivPtr = allocArray<UByteVar>(ivLength)
        if (SecRandomCopyBytes(kSecRandomDefault, ivLength.toULong(), ivPtr) != 0) return ""
        for (i in 0 until ivLength) iv[i] = ivPtr[i].toByte()
        
        val dataBytes = data.encodeToByteArray()
        val encryptedBytes = crypt(kCCEncrypt, dataBytes, derivedKey, iv) ?: return ""
        
        val combined = NSMutableData.create(capacity = (saltLength + ivLength + encryptedBytes.size).toULong())!!
        
        salt.usePinned { combined.appendBytes(it.addressOf(0), saltLength.toULong()) }
        iv.usePinned { combined.appendBytes(it.addressOf(0), ivLength.toULong()) }
        encryptedBytes.usePinned { combined.appendBytes(it.addressOf(0), encryptedBytes.size.toULong()) }
        
        combined.base64EncodedStringWithOptions(0u)
    }

    @OptIn(BetaInteropApi::class)
    override fun decrypt(encryptedData: String, password: String): String = memScoped {
        val data = NSData.create(base64EncodedString = encryptedData, options = 1u) ?: return "" // 1u = NSDataBase64DecodingIgnoreUnknownCharacters
        if (data.length < (saltLength + ivLength).toULong()) return ""
        
        val salt = data.subdataWithRange(NSMakeRange(0u, saltLength.toULong())).toByteArray()
        val iv = data.subdataWithRange(NSMakeRange(saltLength.toULong(), ivLength.toULong())).toByteArray()
        val cipherText = data.subdataWithRange(NSMakeRange((saltLength + ivLength).toULong(), data.length - (saltLength + ivLength).toULong())).toByteArray()
        
        val derivedKey = deriveKey(password, salt) ?: return ""
        val decryptedBytes = crypt(kCCDecrypt, cipherText, derivedKey, iv) ?: return ""
        
        decryptedBytes.decodeToString()
    }

    @OptIn(BetaInteropApi::class)
    private fun deriveKey(password: String, salt: ByteArray): ByteArray? = memScoped {
        val derivedKeyPtr = allocArray<UByteVar>(keyLength.toInt())
        val passwordBytes = password.encodeToByteArray()
        
        val result = salt.usePinned { saltPinned ->
            CCKeyDerivationPBKDF(
                kCCPBKDF2,
                password,
                passwordBytes.size.toULong(),
                saltPinned.addressOf(0).reinterpret<UByteVar>(),
                salt.size.toULong(),
                kCCPRFHmacAlgSHA256,
                iterations,
                derivedKeyPtr,
                keyLength.toULong()
            )
        }
        
        if (result == kCCSuccess) {
            derivedKeyPtr.readBytes(keyLength.toInt())
        } else null
    }

    @OptIn(BetaInteropApi::class)
    private fun crypt(op: CCOperation, data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray? = memScoped {
        val dataOutLength = data.size + kCCBlockSizeAES128.toInt()
        val dataOutPtr = allocArray<ByteVar>(dataOutLength)
        val movedBytes = alloc<ULongVar>()
        
        val status = key.usePinned { keyPinned ->
            iv.usePinned { ivPinned ->
                data.usePinned { dataPinned ->
                    CCCrypt(
                        op,
                        kCCAlgorithmAES,
                        kCCOptionPKCS7Padding,
                        keyPinned.addressOf(0),
                        keyLength.toULong(),
                        ivPinned.addressOf(0),
                        dataPinned.addressOf(0),
                        data.size.toULong(),
                        dataOutPtr,
                        dataOutLength.toULong(),
                        movedBytes.ptr
                    )
                }
            }
        }
        
        if (status == kCCSuccess) {
            dataOutPtr.readBytes(movedBytes.value.toInt())
        } else {
            null
        }
    }

    private fun NSData.toByteArray(): ByteArray = ByteArray(this.length.toInt()).apply {
        if (isNotEmpty()) {
            val src = this@toByteArray.bytes
            this.usePinned { pinned ->
                memcpy(pinned.addressOf(0), src, this@toByteArray.length)
            }
        }
    }
}
