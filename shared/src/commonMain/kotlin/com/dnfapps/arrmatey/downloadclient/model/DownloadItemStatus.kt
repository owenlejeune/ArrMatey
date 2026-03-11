package com.dnfapps.arrmatey.downloadclient.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class DownloadItemStatus(val resource: StringResource) {
    Downloading(MR.strings.downloading),
    Paused(MR.strings.paused),
    Queued(MR.strings.queued),
    Completed(MR.strings.completed),
    Failed(MR.strings.failed),
    Seeding(MR.strings.seeding),
    Stalled(MR.strings.stalled)
}
