package com.dnfapps.arrmatey.downloadclient.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dnfapps.arrmatey.instances.model.InstanceHeader

@Entity(
    tableName = "download_clients",
    indices = [
        Index(value = ["url"], unique = true),
        Index(value = ["label"], unique = true)
    ]
)
data class DownloadClient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: DownloadClientType,
    val label: String,
    val url: String,
    val username: String = "",
    val password: String = "",
    val apiKey: String = "",
    val enabled: Boolean = true,
    val selected: Boolean = false,
    val headers: List<InstanceHeader> = emptyList()
)
