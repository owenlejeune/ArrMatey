package com.dnfapps.arrmatey.discover.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import dev.icerock.moko.resources.ImageResource

sealed interface SearchFilter {
    val id: String
    val displayName: String
    val icon: ImageResource
    val color: Color
    fun matches(result: SearchResult): Boolean

    data class Instance(val type: InstanceType) : SearchFilter {
        override val id: String = "instance_${type.name}"
        override val displayName: String = type.name
        override val icon: ImageResource = type.tabIcon
        override val color: Color = type.associatedColor

        override fun matches(result: SearchResult): Boolean = when (result) {
            is SearchResult.ArrMediaResult -> result.instanceType == type
            is SearchResult.SeerrMediaResult -> type == InstanceType.Seerr
            is SearchResult.SeerrPersonResult -> type == InstanceType.Seerr
            is SearchResult.MediaServerResult -> false
        }
    }

    data class MediaServer(val serverType: MediaServerType) : SearchFilter {
        override val id: String = "mediaserver_${serverType.name}"
        override val displayName: String = serverType.name
        override val icon: ImageResource = serverType.tabIcon
        override val color: Color = serverType.brandColor

        override fun matches(result: SearchResult): Boolean = when (result) {
            is SearchResult.MediaServerResult -> result.serverType == serverType
            else -> false
        }
    }
}
