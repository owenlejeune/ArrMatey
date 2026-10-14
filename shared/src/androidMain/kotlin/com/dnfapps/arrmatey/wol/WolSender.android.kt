package com.dnfapps.arrmatey.wol

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

actual suspend fun sendWolPacket(macAddress: String, ipAddress: String?, port: Int): Boolean = withContext(Dispatchers.IO) {
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
            System.arraycopy(macBytes, 0, bytes, 6 + i * 6, 6)
        }

        val targetIp = if (!ipAddress.isNullOrBlank()) ipAddress else "255.255.255.255"
        val address = InetAddress.getByName(targetIp)

        DatagramSocket().use { socket ->
            socket.broadcast = true
            val packet = DatagramPacket(bytes, bytes.size, address, port)
            socket.send(packet)
        }
        true
    } catch (e: Exception) {
        false
    }
}
