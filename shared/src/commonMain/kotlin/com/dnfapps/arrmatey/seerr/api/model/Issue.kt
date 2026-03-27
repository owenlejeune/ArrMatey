package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Issue(
    val id: Long,
    val issueType: Int,
    val status: Int,
    val problemSeason: Int = 0,
    val problemEpisode: Int = 0,
    @Contextual val createdAt: Instant? = null,
    @Contextual val updatedAt: Instant? = null,
    val media: RequestMedia? = null
    )

//"id": 1,
//"issueType": 1,
//"status": 1,
//"problemSeason": 0,
//"problemEpisode": 0,
//"createdAt": "2026-03-26T14:55:18.000Z",
//"updatedAt": "2026-03-26T14:55:18.000Z",
//"media": {



//issueType
//:
//4
//mediaId
//:
//2349
//message
//:
//"test other"
//problemEpisode
//:
//0
//problemSeason
//:
//0