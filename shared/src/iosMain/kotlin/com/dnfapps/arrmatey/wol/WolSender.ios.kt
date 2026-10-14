package com.dnfapps.arrmatey.wol

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.darwin.inet_addr
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual suspend fun sendWolPacket(macAddress: String, ipAddress: String?, port: Int): Boolean = withContext(Dispatchers.Default) {
    try {
        val cleanMac = macAddress.replace(":", "").replace("-", "").replace(" ", "")
        if (cleanMac.length != 12) return@withContext false
        val macBytes = ByteArray(6)
        for (i in 0..5) {
            macBytes[i] = cleanMac.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }

        val bytes = ByteArray(6 + 16 * 6)
        for (i in 0..5) {
            bytes[i] = 0xFF.toByte()
        }
        for (i in 0 until 16) {
            for (j in 0..5) {
                bytes[6 + i * 6 + j] = macBytes[j]
            }
        }

        val sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
        if (sock < 0) return@withContext false

        memScoped {
            val broadcastEnable = alloc<IntVar>()
            broadcastEnable.value = 1
            setsockopt(sock, SOL_SOCKET, SO_BROADCAST, broadcastEnable.ptr, sizeOf<IntVar>().toUInt())

            val addr = alloc<sockaddr_in>()
            memset(addr.ptr, 0, sizeOf<sockaddr_in>().toULong())
            addr.sin_family = AF_INET.toUByte()
            val swappedPort = ((port shl 8) or (port ushr 8)).toUShort()
            addr.sin_port = swappedPort

            val targetIp = if (!ipAddress.isNullOrBlank()) ipAddress else "255.255.255.255"
            addr.sin_addr.s_addr = inet_addr(targetIp)

            val sent = bytes.usePinned { pinned ->
                sendto(
                    sock,
                    pinned.addressOf(0),
                    bytes.size.toULong(),
                    0,
                    addr.ptr.reinterpret(),
                    sizeOf<sockaddr_in>().toUInt(),
                )
            }

            close(sock)
            sent >= 0
        }
    } catch (_: Exception) {
        false
    }
}
