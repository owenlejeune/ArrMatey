package com.dnfapps.arrmatey.tautulli.api.model

enum class TautulliStatsType(val value: String) {
    Plays("plays"),
    Duration("duration"),
    ;

    companion object {
        fun fromString(value: String?): TautulliStatsType = when (value?.lowercase()?.trim()) {
            "duration" -> Duration
            else -> Plays
        }
    }
}
