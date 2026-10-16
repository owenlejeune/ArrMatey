package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.import_mode_copy
import com.dnfapps.arrmatey.shared.import_mode_move
import dev.icerock.moko.resources.StringResource

enum class ManualImportMode(val value: String, val label: StringResource) {
    Move("move", MR.strings.import_mode_move),
    Copy("copy", MR.strings.import_mode_copy),
    ;

    companion object {
        fun fromValue(value: String): ManualImportMode = entries.find { it.value == value } ?: Move
    }
}
