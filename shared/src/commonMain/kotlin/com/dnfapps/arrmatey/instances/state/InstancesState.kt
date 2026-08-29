package com.dnfapps.arrmatey.instances.state

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType

data class InstancesState(
    val type: InstanceType,
    val instances: List<Instance> = emptyList(),
    val selectedInstance: Instance? = null,
)
