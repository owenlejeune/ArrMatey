package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.CustomFormat
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.QualityInfo
import com.dnfapps.arrmatey.arr.api.model.ReleaseProtocol
import com.dnfapps.arrmatey.compose.utils.ReleaseFilterBy
import com.dnfapps.arrmatey.compose.utils.ReleaseSortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder

data class InteractiveSearchUiState(
    val sortBy: ReleaseSortBy = ReleaseSortBy.Weight,
    val sortOrder: SortOrder = SortOrder.Desc,
    val filterBy: ReleaseFilterBy = ReleaseFilterBy.Any,
    val language: Language? = null,
    val indexer: String? = null,
    val protocol: ReleaseProtocol? = null,
    val quality: QualityInfo? = null,
    val customFormat: CustomFormat? = null,
    val customFilterId: Long? = null
) {
    companion object {
        fun empty(filterBy: ReleaseFilterBy) = InteractiveSearchUiState(
            filterBy = filterBy
        )
    }
}