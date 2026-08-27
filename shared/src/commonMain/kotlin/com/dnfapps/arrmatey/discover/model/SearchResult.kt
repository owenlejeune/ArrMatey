package com.dnfapps.arrmatey.discover.model

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import kotlinx.serialization.Serializable

@Serializable
sealed interface SearchResult {
    val id: String

    @Serializable
    data class ArrMediaResult(val media: ArrMedia) : SearchResult {
        override val id: String = "arr_${media.guid}"
    }

    @Serializable
    data class SeerrMediaResult(val result: DiscoverResult) : SearchResult {
        override val id: String = "seerr_media_${result.id}"
    }

    @Serializable
    data class SeerrPersonResult(val result: DiscoverResult) : SearchResult {
        override val id: String = "seerr_person_${result.id}"
    }
}
