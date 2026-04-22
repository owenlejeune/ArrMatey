package com.dnfapps.arrmatey.features

import com.dnfapps.arrmatey.shared.MR

object ReleaseNotes {

    val updates = listOf(
        FeatureUpdate(
            buildCode = 1,
            title = "Announcements",
            contentFile = MR.files.release_0_0_4_txt
        ),
        FeatureUpdate(
            buildCode = 2,
            title = "We're out of alpha!",
            contentFile = MR.files.release_0_1_0_txt
        ),
        FeatureUpdate(
            buildCode = 3,
            title = "New in 0.3.0",
            contentFile = MR.files.release_0_3_0_txt
        )
    )

    val latestUpdate = updates.maxBy { it.buildCode }

}