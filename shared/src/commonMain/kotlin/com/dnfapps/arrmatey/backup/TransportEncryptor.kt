package com.dnfapps.arrmatey.backup

interface TransportEncryptor {
    fun encrypt(
        data: String,
        password: String,
    ): String

    fun decrypt(
        encryptedData: String,
        password: String,
    ): String
}

expect class AesTransportEncryptor() : TransportEncryptor {
    override fun encrypt(
        data: String,
        password: String,
    ): String

    override fun decrypt(
        encryptedData: String,
        password: String,
    ): String
}
