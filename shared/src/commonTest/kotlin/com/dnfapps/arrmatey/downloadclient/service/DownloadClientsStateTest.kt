package com.dnfapps.arrmatey.downloadclient.service

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DownloadClientsStateTest {
    @Test
    fun testInitialState() {
        val state = DownloadClientsState()
        assertEquals(0, state.downloadClients.size)
        assertNull(state.selectedDownloadClient)
    }

    @Test
    fun testStateWithClients() {
        val client =
            DownloadClient(
                id = 1,
                label = "Test",
                url = "http://test.com",
                type = DownloadClientType.QBittorrent,
                apiKey = EncryptedString(""),
                selected = true,
            )
        val state =
            DownloadClientsState(
                downloadClients = listOf(client),
                selectedDownloadClient = client,
            )
        assertEquals(1, state.downloadClients.size)
        assertEquals(client, state.selectedDownloadClient)
    }
}
