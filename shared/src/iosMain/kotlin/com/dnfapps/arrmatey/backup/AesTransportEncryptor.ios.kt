@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.dnfapps.arrmatey.backup

import kotlinx.cinterop.*
import platform.CoreCrypto.*
import platform.Foundation.*
import platform.Security.*
import platform.posix.memcpy
import platform.zlib.*

@OptIn(ExperimentalForeignApi::class)
actual class AesTransportEncryptor : TransportEncryptor {
    private val saltLength = 16
    private val ivLength = 16
    private val iterations = 65536u
    private val keyLength = 32u // 256 bits
    private val defaultPassword = "ArrMatey_Default_Transport_Key"

    @OptIn(BetaInteropApi::class)
    actual override fun encrypt(
        data: String,
        password: String,
    ): String = memScoped {
        val effectivePassword = password.ifEmpty { defaultPassword }
        val salt = ByteArray(saltLength)
        val saltPtr = allocArray<UByteVar>(saltLength)
        if (SecRandomCopyBytes(kSecRandomDefault, saltLength.toULong(), saltPtr) != 0) return ""
        for (i in 0 until saltLength) salt[i] = saltPtr[i].toByte()

        val derivedKey = deriveKey(effectivePassword, salt) ?: return ""

        val iv = ByteArray(ivLength)
        val ivPtr = allocArray<UByteVar>(ivLength)
        if (SecRandomCopyBytes(kSecRandomDefault, ivLength.toULong(), ivPtr) != 0) return ""
        for (i in 0 until ivLength) iv[i] = ivPtr[i].toByte()

        val dataBytes = data.encodeToByteArray()
        val compressedBytes = gzipCompress(dataBytes) ?: return ""
        val encryptedBytes = crypt(kCCEncrypt, compressedBytes, derivedKey, iv) ?: return ""

        val combined = NSMutableData.create(capacity = (saltLength + ivLength + encryptedBytes.size).toULong())!!

        salt.usePinned { combined.appendBytes(it.addressOf(0), saltLength.toULong()) }
        iv.usePinned { combined.appendBytes(it.addressOf(0), ivLength.toULong()) }
        encryptedBytes.usePinned { combined.appendBytes(it.addressOf(0), encryptedBytes.size.toULong()) }

        combined.base64EncodedStringWithOptions(0u)
    }

    @OptIn(BetaInteropApi::class)
    actual override fun decrypt(
        encryptedData: String,
        password: String,
    ): String = memScoped {
        val effectivePassword = password.ifEmpty { defaultPassword }
        // options = 1u -> NSDataBase64DecodingIgnoreUnknownCharacters
        val data = NSData.create(base64EncodedString = encryptedData, options = 1u) ?: return ""
        if (data.length < (saltLength + ivLength).toULong()) return ""

        val salt = data.subdataWithRange(NSMakeRange(0u, saltLength.toULong())).toByteArray()
        val iv = data.subdataWithRange(NSMakeRange(saltLength.toULong(), ivLength.toULong())).toByteArray()
        val cipherTextRange =
            NSMakeRange(
                (saltLength + ivLength).toULong(),
                data.length - (saltLength + ivLength).toULong(),
            )
        val cipherText = data.subdataWithRange(cipherTextRange).toByteArray()

        val derivedKey = deriveKey(effectivePassword, salt) ?: return ""
        val decryptedBytes = crypt(kCCDecrypt, cipherText, derivedKey, iv) ?: return ""

        val finalBytes = if (isGzip(decryptedBytes)) {
            gzipDecompress(decryptedBytes) ?: decryptedBytes
        } else {
            decryptedBytes
        }

        finalBytes.decodeToString()
    }

    private fun isGzip(bytes: ByteArray): Boolean = bytes.size >= 2 && bytes[0] == 0x1F.toByte() && bytes[1] == 0x8B.toByte()

    private fun gzipCompress(data: ByteArray): ByteArray? = memScoped {
        if (data.isEmpty()) return ByteArray(0)

        val strm = alloc<z_stream>()
        strm.zalloc = null
        strm.zfree = null
        strm.opaque = null

        if (deflateInit2(strm.ptr, Z_DEFAULT_COMPRESSION, Z_DEFLATED, 31, 8, Z_DEFAULT_STRATEGY) != Z_OK) {
            return null
        }

        val maxCompressedSize = deflateBound(strm.ptr, data.size.toULong()).toInt()
        val outBuffer = ByteArray(maxCompressedSize)
        var resultBytes: ByteArray? = null

        data.usePinned { dataPinned ->
            outBuffer.usePinned { outPinned ->
                strm.next_in = dataPinned.addressOf(0).reinterpret()
                strm.avail_in = data.size.toUInt()
                strm.next_out = outPinned.addressOf(0).reinterpret()
                strm.avail_out = maxCompressedSize.toUInt()

                val res = deflate(strm.ptr, Z_FINISH)
                if (res == Z_STREAM_END) {
                    val compressedSize = strm.total_out.toInt()
                    resultBytes = outBuffer.copyOf(compressedSize)
                }
            }
        }

        deflateEnd(strm.ptr)
        resultBytes
    }

    @OptIn(BetaInteropApi::class)
    private fun gzipDecompress(data: ByteArray): ByteArray? = memScoped {
        if (data.isEmpty()) return ByteArray(0)

        val strm = alloc<z_stream>()
        strm.zalloc = null
        strm.zfree = null
        strm.opaque = null

        if (inflateInit2(strm.ptr, 47) != Z_OK) {
            return null
        }

        val bufferSize = 8192
        val outChunk = ByteArray(bufferSize)
        val outputData = NSMutableData.create(capacity = 8192u) ?: run {
            inflateEnd(strm.ptr)
            return null
        }

        var status = Z_OK
        data.usePinned { dataPinned ->
            strm.next_in = dataPinned.addressOf(0).reinterpret()
            strm.avail_in = data.size.toUInt()

            while (status == Z_OK) {
                outChunk.usePinned { outPinned ->
                    strm.next_out = outPinned.addressOf(0).reinterpret()
                    strm.avail_out = bufferSize.toUInt()

                    status = inflate(strm.ptr, Z_NO_FLUSH)

                    val bytesRead = bufferSize - strm.avail_out.toInt()
                    if (bytesRead > 0) {
                        outputData.appendBytes(outPinned.addressOf(0), bytesRead.toULong())
                    }
                }
            }
        }

        inflateEnd(strm.ptr)

        if (status == Z_STREAM_END) {
            outputData.toByteArray()
        } else {
            null
        }
    }

    @OptIn(BetaInteropApi::class)
    private fun deriveKey(
        password: String,
        salt: ByteArray,
    ): ByteArray? = memScoped {
        val derivedKeyPtr = allocArray<UByteVar>(keyLength.toInt())
        val passwordBytes = password.encodeToByteArray()

        val result =
            salt.usePinned { saltPinned ->
                CCKeyDerivationPBKDF(
                    kCCPBKDF2,
                    password,
                    passwordBytes.size.toULong(),
                    saltPinned.addressOf(0).reinterpret<UByteVar>(),
                    salt.size.toULong(),
                    kCCPRFHmacAlgSHA256,
                    iterations,
                    derivedKeyPtr,
                    keyLength.toULong(),
                )
            }

        if (result == kCCSuccess) {
            derivedKeyPtr.readBytes(keyLength.toInt())
        } else {
            null
        }
    }

    @OptIn(BetaInteropApi::class)
    private fun crypt(
        op: CCOperation,
        data: ByteArray,
        key: ByteArray,
        iv: ByteArray,
    ): ByteArray? = memScoped {
        val dataOutLength = data.size + kCCBlockSizeAES128.toInt()
        val dataOutPtr = allocArray<ByteVar>(dataOutLength)
        val movedBytes = alloc<ULongVar>()

        val status =
            key.usePinned { keyPinned ->
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
                            movedBytes.ptr,
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
