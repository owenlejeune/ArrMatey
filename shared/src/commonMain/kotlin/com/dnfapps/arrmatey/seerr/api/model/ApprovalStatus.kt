package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.SerialName

enum class ApprovalStatus {
    @SerialName("approve")
    Approve,

    @SerialName("decline")
    Decline
}