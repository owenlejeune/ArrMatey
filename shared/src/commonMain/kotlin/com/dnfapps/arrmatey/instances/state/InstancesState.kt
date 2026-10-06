package com.dnfapps.arrmatey.instances.state

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType

data class InstancesState(
    val types: List<InstanceType>,
    val instances: List<Instance> = emptyList(),
    val selectedInstance: Instance? = null,
) {
    constructor(type: InstanceType, instances: List<Instance> = emptyList(), selectedInstance: Instance? = null) :
        this(listOf(type), instances, selectedInstance)

    val type: InstanceType
        get() = types.firstOrNull() ?: InstanceType.Sonarr
}
