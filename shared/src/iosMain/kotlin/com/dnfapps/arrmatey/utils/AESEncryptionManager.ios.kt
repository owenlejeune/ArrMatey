package com.dnfapps.arrmatey.utils

import kotlinx.cinterop.*
import platform.CoreCrypto.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

@OptIn(ExperimentalForeignApi::class)
class AESEncryptionManager : EncryptionManager {
    private val keyAccount = "com.dnfapps.arrmatey.encryption.key"

    @OptIn(BetaInteropApi::class)
    private fun getOrGenerateKey(): NSData? = memScoped {
        val query = NSDictionary.dictionaryWithObjectsAndKeys(
            kSecClassGenericPassword.toAny(), kSecClass.toAny(),
            (keyAccount as Any as NSString), kSecAttrAccount.toAny(),
            kCFBooleanTrue.toAny(), kSecReturnData.toAny(),
            kSecMatchLimitOne.toAny(), kSecMatchLimit.toAny(),
            null
        )

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query as Any? as CFDictionaryRef?, result.ptr)

        if (status == errSecSuccess) {
            return CFBridgingRelease(result.value) as? NSData
        }

        // Generate new key if not found
        val newKey = NSMutableData.create(length = 32u) ?: return null
        val statusRandom = SecRandomCopyBytes(kSecRandomDefault, 32u, newKey.mutableBytes)
        if (statusRandom != errSecSuccess) return null

        val addQuery = NSDictionary.dictionaryWithObjectsAndKeys(
            kSecClassGenericPassword.toAny(), kSecClass.toAny(),
            (keyAccount as Any as NSString), kSecAttrAccount.toAny(),
            newKey, kSecValueData.toAny(),
            kSecAttrAccessibleAfterFirstUnlock.toAny(), kSecAttrAccessible.toAny(),
            null
        )

        SecItemAdd(addQuery as Any? as CFDictionaryRef?, null)
        return newKey
    }

    @OptIn(BetaInteropApi::class)
    override fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return plainText
        return try {
            val nsString = plainText as Any as NSString
            val data = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: return plainText
            val encryptedData = crypt(kCCEncrypt, data) ?: return plainText
            encryptedData.base64EncodedStringWithOptions(0u)
        } catch (e: Exception) {
            plainText
        }
    }

    @OptIn(BetaInteropApi::class)
    override fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return encryptedText
        return try {
            val data = NSData.create(base64EncodedString = encryptedText, options = 0u) ?: return encryptedText
            val decryptedData = crypt(kCCDecrypt, data) ?: return encryptedText
            val nsString = NSString.create(data = decryptedData, encoding = NSUTF8StringEncoding)
            nsString?.toString() ?: encryptedText
        } catch (e: Exception) {
            encryptedText
        }
    }

    @OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
    private fun crypt(op: CCOperation, data: NSData): NSData? = memScoped {
        val keyData = getOrGenerateKey() ?: return null
        
        val dataOut = NSMutableData.create(length = (data.length + kCCBlockSizeAES128.toULong())) ?: return null
        val movedBytes = alloc<ULongVar>()
        
        val status = CCCrypt(
            op,
            kCCAlgorithmAES,
            kCCOptionPKCS7Padding,
            keyData.bytes,
            kCCKeySizeAES256.toULong(),
            null,
            data.bytes,
            data.length,
            dataOut.mutableBytes,
            dataOut.length,
            movedBytes.ptr
        )
        
        return if (status == kCCSuccess) {
            dataOut.setLength(movedBytes.value)
            dataOut
        } else {
            null
        }
    }

    @OptIn(BetaInteropApi::class)
    private fun CPointer<*>?.toAny(): Any = this?.let { interpretObjCPointer(it.rawValue) } ?: NSNull()
}
