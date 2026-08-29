package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.instances.model.Instance

data class AddSheetUiState(
    val targetInstance: Instance? = null,
    val qualityProfiles: List<QualityProfile> = emptyList(),
    val rootFolders: List<RootFolder> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val availableInstances: List<Instance> = emptyList(),
) {
    constructor() : this(null) // empty ios constructor
}
