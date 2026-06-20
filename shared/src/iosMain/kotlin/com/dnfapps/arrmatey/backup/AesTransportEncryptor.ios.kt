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
        SecRandomCopyBytes(kSecRandomDefault, saltLength.toULong(), salt.toCValues().ptr)
        
        val derivedKey = deriveKey(password, salt) ?: return ""
        
        val iv = ByteArray(ivLength)
        SecRandomCopyBytes(kSecRandomDefault, ivLength.toULong(), iv.toCValues().ptr)
        
        val dataBytes = data.encodeToByteArray()
        val encryptedBytes = crypt(kCCEncrypt, dataBytes, derivedKey, iv) ?: return ""
        
        val combined = NSMutableData.create(length = (saltLength + ivLength + encryptedBytes.size).toULong())!!
        combined.appendBytes(salt.toCValues().ptr, saltLength.toULong())
        combined.appendBytes(iv.toCValues().ptr, ivLength.toULong())
        combined.appendBytes(encryptedBytes.toCValues().ptr, encryptedBytes.size.toULong())
        
        combined.base64EncodedStringWithOptions(0u)
    }

    @OptIn(BetaInteropApi::class)
    override fun decrypt(encryptedData: String, password: String): String = memScoped {
        val data = NSData.create(base64EncodedString = encryptedData, options = 0u) ?: return ""
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
        val derivedKey = ByteArray(keyLength.toInt())
        val result = CCKeyDerivationPBKDF(
            kCCPBKDF2,
            password, password.length.toULong(),
            salt.toCValues().ptr.reinterpret<UByteVar>(), salt.size.toULong(),
            kCCPRFHmacAlgSHA256,
            iterations,
            derivedKey.toCValues().ptr.reinterpret<UByteVar>(), keyLength.toULong()
        )
        if (result == kCCSuccess) derivedKey else null
    }

    @OptIn(BetaInteropApi::class)
    private fun crypt(op: CCOperation, data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray? = memScoped {
        val dataOutLength = data.size + kCCBlockSizeAES128.toInt()
        val dataOut = ByteArray(dataOutLength)
        val movedBytes = alloc<ULongVar>()
        
        val status = CCCrypt(
            op,
            kCCAlgorithmAES,
            kCCOptionPKCS7Padding,
            key.toCValues().ptr,
            keyLength.toULong(),
            iv.toCValues().ptr,
            data.toCValues().ptr,
            data.size.toULong(),
            dataOut.toCValues().ptr,
            dataOutLength.toULong(),
            movedBytes.ptr
        )
        
        if (status == kCCSuccess) {
            dataOut.sliceArray(0 until movedBytes.value.toInt())
        } else {
            null
        }
    }

    private fun NSData.toByteArray(): ByteArray {
        val length = this.length.toInt()
        val bytes = ByteArray(length)
        if (length > 0) {
            bytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), this.bytes, this.length.toULong())
            }
        }
        return bytes
    }
}
