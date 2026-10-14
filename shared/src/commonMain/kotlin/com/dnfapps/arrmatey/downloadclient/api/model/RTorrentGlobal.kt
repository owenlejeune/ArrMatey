package com.dnfapps.arrmatey.downloadclient.api.model

data class RTorrentGlobal(
    val version: String = "",
    val downRate: Long = 0L,
    val upRate: Long = 0L,
    val downLimit: Long = 0L,
    val upLimit: Long = 0L,
    val listenPort: Long = 0L,
) {
    val downLimited: Boolean
        get() = downLimit > 0

    val upLimited: Boolean
        get() = upLimit > 0

    companion object {
        val COMMANDS = listOf(
            "system.client_version",
            "throttle.global_down.rate",
            "throttle.global_up.rate",
            "throttle.global_down.max_rate",
            "throttle.global_up.max_rate",
            "network.listen.port",
        )

        fun fromRows(rows: List<XmlRpcValue>): RTorrentGlobal {
            fun at(i: Int): XmlRpcValue? {
                if (i >= rows.size) return null
                val v = rows[i]
                return if (v is XmlRpcValue.ArrayValue && v.values.isNotEmpty()) v.values.first() else v
            }

            return RTorrentGlobal(
                version = at(0)?.asString().orEmpty(),
                downRate = at(1)?.asLong() ?: 0L,
                upRate = at(2)?.asLong() ?: 0L,
                downLimit = at(3)?.asLong() ?: 0L,
                upLimit = at(4)?.asLong() ?: 0L,
                listenPort = at(5)?.asLong() ?: 0L,
            )
        }
    }
}
