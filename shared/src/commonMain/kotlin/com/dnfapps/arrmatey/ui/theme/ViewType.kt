package com.dnfapps.arrmatey.ui.theme

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

@Serializable
enum class ViewType(
    val resource: StringResource
) {
    Grid(MR.strings.grid_view),
    List(MR.strings.list_view)
}