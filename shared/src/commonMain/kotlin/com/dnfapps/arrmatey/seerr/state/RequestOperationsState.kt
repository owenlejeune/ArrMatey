package com.dnfapps.arrmatey.seerr.state

import com.dnfapps.arrmatey.client.OperationStatus

data class RequestOperationsState(
    val approvalStates: Map<Long, OperationStatus> = emptyMap(),
    val cancelStates: Map<Long, OperationStatus> = emptyMap()
) {
    constructor(): this(emptyMap())
}