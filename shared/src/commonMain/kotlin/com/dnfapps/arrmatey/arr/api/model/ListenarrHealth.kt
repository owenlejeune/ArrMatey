package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ListenarrHealth(
    val status: String,
    val version: String,
    val uptime: String,
    val downloadClients: ListenarrHealthComponent,
    val externalApis: ListenarrHealthComponent,
) {
    fun toArrHealthItems(): List<ArrHealth> {
        val items = mutableListOf<ArrHealth>()

        if (downloadClients.status != "healthy") {
            val count = downloadClients.total - downloadClients.connected
            items.add(
                ArrHealth(
                    type = ArrHealthType.Error,
                    source = "Download Clients",
                    message = "$count of ${downloadClients.total} download clients are disconnected",
                ),
            )
        }

        if (externalApis.status != "healthy") {
            val count = externalApis.total - externalApis.connected
            items.add(
                ArrHealth(
                    type = ArrHealthType.Warning,
                    source = "External APIs",
                    message = "$count of ${externalApis.total} external APIs are disconnected",
                ),
            )
        }

        return items
    }
}

@Serializable
data class ListenarrHealthComponent(
    val status: String,
    val connected: Int,
    val total: Int,
)
