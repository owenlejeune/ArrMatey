package com.dnfapps.arrmatey.downloadclient.api.model

data class RTorrentFile(
    val path: String = "",
    val sizeBytes: Long = 0L,
    val completedChunks: Long = 0L,
    val sizeChunks: Long = 0L,
    val priority: Int = 1,
) {
    val progress: Double
        get() = if (sizeChunks <= 0L) 0.0 else (completedChunks.toDouble() / sizeChunks.toDouble()).coerceIn(0.0, 1.0)

    val displayName: String
        get() = if (path.contains('/')) path.substringAfterLast('/') else path

    val priorityLabel: String
        get() = when (priority) {
            0 -> "Skip"
            2 -> "High"
            else -> "Normal"
        }

    companion object {
        val FIELDS = listOf(
            "f.path=",
            "f.size_bytes=",
            "f.completed_chunks=",
            "f.size_chunks=",
            "f.priority=",
        )

        fun fromRow(row: List<XmlRpcValue>): RTorrentFile {
            fun str(i: Int): String = row.getOrNull(i)?.asString().orEmpty()
            fun longAt(i: Int): Long = row.getOrNull(i)?.asLong() ?: 0L

            return RTorrentFile(
                path = str(0),
                sizeBytes = longAt(1),
                completedChunks = longAt(2),
                sizeChunks = longAt(3),
                priority = longAt(4).toInt(),
            )
        }
    }
}

data class RTorrentTracker(
    val url: String = "",
    val group: Long = 0L,
    val isEnabled: Boolean = false,
) {
    companion object {
        val FIELDS = listOf(
            "t.url=",
            "t.group=",
            "t.is_enabled=",
        )

        fun fromRow(row: List<XmlRpcValue>): RTorrentTracker {
            fun str(i: Int): String = row.getOrNull(i)?.asString().orEmpty()
            fun longAt(i: Int): Long = row.getOrNull(i)?.asLong() ?: 0L

            return RTorrentTracker(
                url = str(0),
                group = longAt(1),
                isEnabled = longAt(2) != 0L,
            )
        }
    }
}

data class RTorrentPeer(
    val address: String = "",
    val client: String = "",
    val downRate: Long = 0L,
    val upRate: Long = 0L,
    val completedPercent: Long = 0L,
    val isEncrypted: Boolean = false,
) {
    val progress: Double
        get() = (completedPercent.toDouble() / 100.0).coerceIn(0.0, 1.0)

    companion object {
        val FIELDS = listOf(
            "p.address=",
            "p.client_version=",
            "p.down_rate=",
            "p.up_rate=",
            "p.completed_percent=",
            "p.is_encrypted=",
        )

        fun fromRow(row: List<XmlRpcValue>): RTorrentPeer {
            fun str(i: Int): String = row.getOrNull(i)?.asString().orEmpty()
            fun longAt(i: Int): Long = row.getOrNull(i)?.asLong() ?: 0L

            return RTorrentPeer(
                address = str(0),
                client = str(1),
                downRate = longAt(2),
                upRate = longAt(3),
                completedPercent = longAt(4),
                isEncrypted = longAt(5) != 0L,
            )
        }
    }
}

data class RTorrentDetail(
    val files: List<RTorrentFile> = emptyList(),
    val trackers: List<RTorrentTracker> = emptyList(),
    val peers: List<RTorrentPeer> = emptyList(),
)
