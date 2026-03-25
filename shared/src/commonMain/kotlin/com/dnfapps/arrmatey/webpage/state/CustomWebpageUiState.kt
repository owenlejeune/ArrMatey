package com.dnfapps.arrmatey.webpage.state

import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.instances.model.InstanceHeader

data class CustomWebpageUiState(
    val id: Long = 0,
    val name: String = "",
    val url: String = "",
    val headers: List<InstanceHeader> = emptyList(),
    val isEditing: Boolean = false,
    val error: String? = null,
    val saveButtonEnabled: Boolean = false,
    val saveResult: InsertResult? = null,
    val endpointError: Boolean = false
) {
    constructor(): this(0) // empty ios constructor
}