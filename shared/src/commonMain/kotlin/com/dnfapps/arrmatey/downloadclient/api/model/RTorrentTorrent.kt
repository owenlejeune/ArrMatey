package com.dnfapps.arrmatey.downloadclient.api.model

data class RTorrentTorrent(
    val hash: String,
    val name: String,
    val sizeBytes: Long,
    val completedBytes: Long,
    val leftBytes: Long,
    val downRate: Long,
    val upRate: Long,
    val uploadedTotal: Long,
    val ratioPerMille: Long,
    val isActive: Boolean,
    val isOpen: Boolean,
    val isComplete: Boolean,
    val hashing: Long,
    val message: String,
    val peersConnected: Long,
    val peersComplete: Long,
    val directory: String,
    val custom1: String,
    val priority: Int,
    val state: Int,
) {
    val progress: Double
        get() = if (sizeBytes > 0) (completedBytes.toDouble() / sizeBytes.toDouble()).coerceIn(0.0, 1.0) else 0.0

    val ratio: Double
        get() = ratioPerMille.toDouble() / 1000.0

    val remainingBytes: Long
        get() = if (leftBytes > 0) leftBytes else (sizeBytes - completedBytes).coerceAtLeast(0L)

    val eta: Long
        get() = if (downRate > 0 && remainingBytes > 0) remainingBytes / downRate else 0L

    val hasError: Boolean
        get() = message.isNotBlank()

    companion object {
        val FIELDS = listOf(
            "d.hash=",
            "d.name=",
            "d.size_bytes=",
            "d.completed_bytes=",
            "d.left_bytes=",
            "d.down.rate=",
            "d.up.rate=",
            "d.up.total=",
            "d.ratio=",
            "d.is_active=",
            "d.is_open=",
            "d.complete=",
            "d.hashing=",
            "d.message=",
            "d.peers_connected=",
            "d.peers_complete=",
            "d.directory=",
            "d.custom1=",
            "d.priority=",
            "d.state=",
        )

        fun fromRow(row: List<XmlRpcValue>): RTorrentTorrent {
            fun str(i: Int): String = row.getOrNull(i)?.asString().orEmpty()
            fun longAt(i: Int): Long = row.getOrNull(i)?.asLong() ?: 0L
            fun boolAt(i: Int): Boolean = row.getOrNull(i)?.asBoolean() ?: false
            fun intAt(i: Int): Int = longAt(i).toInt()

            return RTorrentTorrent(
                hash = str(0),
                name = str(1),
                sizeBytes = longAt(2),
                completedBytes = longAt(3),
                leftBytes = longAt(4),
                downRate = longAt(5),
                upRate = longAt(6),
                uploadedTotal = longAt(7),
                ratioPerMille = longAt(8),
                isActive = boolAt(9),
                isOpen = boolAt(10),
                isComplete = boolAt(11),
                hashing = longAt(12),
                message = str(13),
                peersConnected = longAt(14),
                peersComplete = longAt(15),
                directory = str(16),
                custom1 = str(17),
                priority = intAt(18),
                state = intAt(19),
            )
        }
    }
}
