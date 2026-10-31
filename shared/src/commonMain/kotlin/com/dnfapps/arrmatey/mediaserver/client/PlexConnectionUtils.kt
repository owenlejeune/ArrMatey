package com.dnfapps.arrmatey.mediaserver.client

import com.dnfapps.arrmatey.mediaserver.model.PlexServerConnection
import com.dnfapps.arrmatey.mediaserver.model.PlexServerResource

object PlexConnectionUtils {
    /**
     * Checks if an IP address is in the Docker bridge range (172.16.0.0 - 172.31.255.255).
     * Docker default bridge is 172.17.x.x, user-defined networks are 172.18.x.x - 172.31.x.x.
     * These IPs are internal to the Docker host and unroutable from devices outside the host.
     */
    fun isDockerBridgeIp(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size == 4 && parts[0] == "172") {
            val second = parts[1].toIntOrNull() ?: return false
            return second in 16..31
        }
        return false
    }

    /**
     * Checks if a URL contains a Docker bridge IP (either as direct IP or within a .plex.direct hostname).
     */
    fun isDockerBridgeUrl(url: String): Boolean {
        val directIp = extractIpFromPlexDirect(url)
        if (directIp != null && isDockerBridgeIp(directIp)) return true
        val host = url.removePrefix("http://").removePrefix("https://").substringBefore(":").substringBefore("/")
        return isDockerBridgeIp(host)
    }

    /**
     * Ranks a Plex connection for reachability from client devices.
     * - Local LAN connection not on Docker (192.168.x.x, 10.x.x.x): highest (100)
     * - Remote / public connection not on Docker: second (75)
     * - Local Docker bridge connection: low (25)
     * - Others: lowest (10)
     */
    fun rankConnection(conn: PlexServerConnection): Int {
        val isDocker = isDockerBridgeIp(conn.address)
        return when {
            conn.local && !isDocker -> 100
            !conn.local && !isDocker -> 75
            conn.local && isDocker -> 25
            else -> 10
        }
    }

    /**
     * Sorts connections with best (most likely reachable) connections first.
     */
    fun sortConnections(connections: List<PlexServerConnection>): List<PlexServerConnection> = connections.sortedWith(
        compareByDescending<PlexServerConnection> { rankConnection(it) }
            .thenBy { if (it.protocol == "https") 0 else 1 },
    )

    /**
     * Finds the best candidate connection for a Plex server resource.
     */
    fun findBestConnection(server: PlexServerResource): PlexServerConnection? = sortConnections(server.connections).firstOrNull()

    /**
     * If a host is in the format "<a-b-c-d>.<hash>.plex.direct", extracts the IP "a.b.c.d".
     */
    fun extractIpFromPlexDirect(hostOrUrl: String): String? {
        val clean = hostOrUrl.removePrefix("http://").removePrefix("https://").substringBefore(":").substringBefore("/")
        if (!clean.endsWith(".plex.direct")) return null
        val prefix = clean.substringBefore(".")
        val parts = prefix.split("-")
        if (parts.size == 4 && parts.all { it.toIntOrNull() in 0..255 }) {
            return parts.joinToString(".")
        }
        return null
    }

    /**
     * If a URL uses a *.plex.direct hostname, converts it to direct HTTP (e.g. http://192.168.1.50:32400)
     * to bypass DNS rebinding protection.
     */
    fun toDirectHttpUrl(url: String): String? {
        val ip = extractIpFromPlexDirect(url) ?: return null
        val port = url.substringAfter(":", "").substringBefore("/").toIntOrNull() ?: 32400
        return "http://$ip:$port"
    }
}
