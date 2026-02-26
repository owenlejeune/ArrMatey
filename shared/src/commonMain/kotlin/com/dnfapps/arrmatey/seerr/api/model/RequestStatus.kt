package com.dnfapps.arrmatey.seerr.api.model

enum class RequestStatus(val value: Int) {
    Pending(1),
    Approved(2),
    Declined(3);

    companion object {
        fun fromValue(value: Int) =
            entries.firstOrNull { it.value == value } ?: Pending
    }
}