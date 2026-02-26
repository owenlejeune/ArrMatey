package com.dnfapps.arrmatey.features

import dev.icerock.moko.resources.FileResource

data class FeatureUpdate(
    val buildCode: Int,
    val title: String,
    val contentFile: FileResource
)