package com.dnfapps.arrmatey.seerr.api.model

import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import kotlinx.serialization.Serializable

@Serializable
data class ServiceDetails(
    val server: Service,
    val profiles: List<QualityProfile>,
    val rootFolders: List<RootFolder>,
    val tags: List<Tag>
)