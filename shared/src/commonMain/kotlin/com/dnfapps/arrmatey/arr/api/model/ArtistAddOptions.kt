package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
class ArtistAddOptions(
    val monitor: ArtistMonitorType,
    val searchForMissingAlbums: Boolean = false,
)
