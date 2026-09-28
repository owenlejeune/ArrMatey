package com.dnfapps.arrmatey.compose.utils

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.age
import com.dnfapps.arrmatey.shared.custom_score
import com.dnfapps.arrmatey.shared.file_size
import com.dnfapps.arrmatey.shared.quality
import com.dnfapps.arrmatey.shared.seeders
import com.dnfapps.arrmatey.shared.weight
import dev.icerock.moko.resources.StringResource

enum class ReleaseSortBy(
    val resource: StringResource,
) {
    Weight(MR.strings.weight),
    Age(MR.strings.age),
    Quality(MR.strings.quality),
    Seeders(MR.strings.seeders),
    FileSize(MR.strings.file_size),
    CustomScore(MR.strings.custom_score),
}
