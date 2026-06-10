package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType

sealed interface InstanceTypeIdentifiable {
    val instanceType: InstanceType
        get() = when(this) {
            is ArrSeries,
            is MockMedia.Sonarr,
            is MockMedia.Default -> InstanceType.Sonarr
            is ArrMovie,
            is MockMedia.Radarr -> InstanceType.Radarr
            is Arrtist,
            is ArrAlbum,
            is MockMedia.Lidarr -> InstanceType.Lidarr
            is Author,
            is MockMedia.Readarr -> InstanceType.Booksehelf
            is Audiobook,
            is SearchAudiobook,
            is MockMedia.Listenarr -> InstanceType.Listenarr
        }
}