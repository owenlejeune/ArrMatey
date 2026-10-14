package com.dnfapps.arrmatey.wol

expect suspend fun sendWolPacket(macAddress: String, ipAddress: String?, port: Int): Boolean
