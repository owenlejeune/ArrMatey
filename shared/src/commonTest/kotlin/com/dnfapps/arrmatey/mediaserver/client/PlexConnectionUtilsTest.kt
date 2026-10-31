package com.dnfapps.arrmatey.mediaserver.client

import com.dnfapps.arrmatey.mediaserver.model.PlexServerConnection
import com.dnfapps.arrmatey.mediaserver.model.PlexServerResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlexConnectionUtilsTest {

    @Test
    fun identifiesDockerBridgeIps() {
        assertTrue(PlexConnectionUtils.isDockerBridgeIp("172.17.0.1"))
        assertTrue(PlexConnectionUtils.isDockerBridgeIp("172.20.0.1"))
        assertTrue(PlexConnectionUtils.isDockerBridgeIp("172.31.255.255"))

        assertFalse(PlexConnectionUtils.isDockerBridgeIp("172.15.0.1"))
        assertFalse(PlexConnectionUtils.isDockerBridgeIp("172.32.0.1"))
        assertFalse(PlexConnectionUtils.isDockerBridgeIp("192.168.1.50"))
        assertFalse(PlexConnectionUtils.isDockerBridgeIp("10.0.0.1"))
    }

    @Test
    fun detectsDockerBridgeUrl() {
        assertTrue(PlexConnectionUtils.isDockerBridgeUrl("https://172-20-0-1.db639f66768546a9a6dc9ba7422e5f97.plex.direct:32400"))
        assertTrue(PlexConnectionUtils.isDockerBridgeUrl("http://172.20.0.1:32400"))
        assertFalse(PlexConnectionUtils.isDockerBridgeUrl("https://192-168-1-50.db639f66768546a9a6dc9ba7422e5f97.plex.direct:32400"))
        assertFalse(PlexConnectionUtils.isDockerBridgeUrl("http://192.168.1.50:32400"))
    }

    @Test
    fun extractsIpFromPlexDirectUrl() {
        assertEquals("172.20.0.1", PlexConnectionUtils.extractIpFromPlexDirect("https://172-20-0-1.db639f66768546a9a6dc9ba7422e5f97.plex.direct:32400"))
        assertEquals("192.168.1.100", PlexConnectionUtils.extractIpFromPlexDirect("https://192-168-1-100.db639f66768546a9a6dc9ba7422e5f97.plex.direct:32400"))
        assertEquals("http://192.168.1.100:32400", PlexConnectionUtils.toDirectHttpUrl("https://192-168-1-100.db639f66768546a9a6dc9ba7422e5f97.plex.direct:32400"))
    }

    @Test
    fun ranksLanHigherThanDockerBridge() {
        val dockerConn = PlexServerConnection(
            protocol = "https",
            address = "172.20.0.1",
            port = 32400,
            uri = "https://172-20-0-1.hash.plex.direct:32400",
            local = true,
        )
        val lanConn = PlexServerConnection(
            protocol = "https",
            address = "192.168.1.50",
            port = 32400,
            uri = "https://192-168-1-50.hash.plex.direct:32400",
            local = true,
        )
        val remoteConn = PlexServerConnection(
            protocol = "https",
            address = "203.0.113.1",
            port = 32400,
            uri = "https://203-0-113-1.hash.plex.direct:32400",
            local = false,
        )

        val server = PlexServerResource(
            name = "My Server",
            clientIdentifier = "srv123",
            connections = listOf(dockerConn, remoteConn, lanConn),
        )

        val best = PlexConnectionUtils.findBestConnection(server)
        assertEquals(lanConn.address, best?.address)

        val sorted = PlexConnectionUtils.sortConnections(server.connections)
        assertEquals(lanConn.address, sorted[0].address)
        assertEquals(remoteConn.address, sorted[1].address)
        assertEquals(dockerConn.address, sorted[2].address)
    }
}
