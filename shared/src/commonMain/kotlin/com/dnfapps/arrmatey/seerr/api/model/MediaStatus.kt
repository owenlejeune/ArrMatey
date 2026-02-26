package com.dnfapps.arrmatey.seerr.api.model

enum class MediaStatus(val value: Int) {
    Unknown(1),
    Pending(2),
    Processing(3),
    PartiallyAvailable(4),
    Available(5),
    Deleted(7);

    companion object {
        fun fromValue(value: Int) = entries.firstOrNull { it.value == value } ?: Unknown
    }
}