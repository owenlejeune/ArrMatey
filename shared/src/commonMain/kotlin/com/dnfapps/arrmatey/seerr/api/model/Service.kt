package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val id: Long,
    val name: String,
    val isDefault: Boolean,
    val activeDirectory: String,
    val activeProfileId: Int,
    val activeTags: List<Int> = emptyList(),
    val is4k: Boolean = false,
)


//{
//    "id": 0,
//    "name": "Radarr",
//    "is4k": false,
//    "isDefault": true,
//    "activeDirectory": "/movies",
//    "activeProfileId": 8,
//    "activeTags": []
//}