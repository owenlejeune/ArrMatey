package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.instances.model.Instance

data class InstanceMediaPresence(
    val instance: Instance,
    val arrMedia: ArrMedia? = null,
    val isPresent: Boolean = arrMedia?.let { it.id != null && it.id != 0L } ?: false
)
