package com.dnfapps.arrmatey.instances.model

import com.dnfapps.arrmatey.arr.api.model.CustomFilter
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag

data class InstanceData(
    val qualityProfiles: List<QualityProfile>,
    val rootFolders: List<RootFolder>,
    val tags: List<Tag>,
    val customFilters: List<CustomFilter>
)