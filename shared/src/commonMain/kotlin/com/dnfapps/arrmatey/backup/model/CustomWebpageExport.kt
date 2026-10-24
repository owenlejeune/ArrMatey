package com.dnfapps.arrmatey.backup.model

import com.dnfapps.arrmatey.instances.model.InstanceHeader
import kotlinx.serialization.Serializable

@Serializable
data class CustomWebpageExport(
    /** Source database ID, used only to remap tab preferences during restore. */
    val id: Long? = null,
    val name: String,
    val url: String,
    val headers: List<InstanceHeader> = emptyList(),
    val allowSelfSignedCerts: Boolean = false,
)
