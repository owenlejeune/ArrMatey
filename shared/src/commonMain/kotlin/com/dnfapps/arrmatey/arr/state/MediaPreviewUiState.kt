package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.client.OperationStatus

data class MediaPreviewUiState(
    val qualityProfiles: List<QualityProfile> = emptyList(),
    val rootFolders: List<RootFolder> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val addItemStatus: OperationStatus = OperationStatus.Idle,
    val lastAddedItemId: Long? = null,
    val relativePath: String = ""
) {
    constructor(): this(emptyList())
}