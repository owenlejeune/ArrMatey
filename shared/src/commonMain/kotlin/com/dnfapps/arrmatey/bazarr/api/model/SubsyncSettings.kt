package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SubsyncSettings(
    val checker: SubsyncCheckerSettings,
    val debug: Boolean,
    val force_audio: Boolean,
    val gss: Boolean,
    val max_offset_seconds: Int,
    val no_fix_framerate: Boolean,
    val subsync_movie_threshold: Int,
    val subsync_threshold: Int,
    val use_subsync: Boolean,
    val use_subsync_movie_threshold: Boolean,
    val use_subsync_threshold: Boolean
)
