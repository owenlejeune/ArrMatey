package com.dnfapps.arrmatey.wol.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wol_devices")
data class WolDevice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val macAddress: String,
    val ipAddress: String? = null,
    val port: Int = 9,
)
