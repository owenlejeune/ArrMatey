package com.dnfapps.arrmatey.features

import com.dnfapps.arrmatey.shared.MR

object ReleaseNotes {

    val updates = listOf(
        FeatureUpdate(
            buildCode = 1,
            title = "Announcements",
            contentFile = MR.files.release_0_0_4_txt
        )
    )

    val latestUpdate = updates.maxBy { it.buildCode }

}