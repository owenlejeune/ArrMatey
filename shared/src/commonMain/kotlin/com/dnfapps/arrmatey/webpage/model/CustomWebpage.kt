package com.dnfapps.arrmatey.webpage.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dnfapps.arrmatey.instances.model.InstanceHeader

@Entity(tableName = "custom_webpages")
data class CustomWebpage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val headers: List<InstanceHeader> = emptyList()
)