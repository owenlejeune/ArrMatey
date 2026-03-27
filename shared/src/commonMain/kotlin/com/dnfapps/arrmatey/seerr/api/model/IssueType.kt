package com.dnfapps.arrmatey.seerr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class IssueType(
    val value: Int,
    val label: StringResource
) {
    Video(1, MR.strings.video),
    Audio(2, MR.strings.audio),
    Subtitle(3, MR.strings.subtitle),
    Other(4, MR.strings.other);

    companion object {
        fun fromValue(value: Int): IssueType =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalStateException("Unknown issue type: $value")
    }
}