package com.dnfapps.arrmatey.features

import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.StringResource

data class FeatureUpdate(
    val buildCode: Int,
    val version: String,
    val title: StringResource,
    val androidContentFile: FileResource,
    val iosContentFile: FileResource = androidContentFile,
)
