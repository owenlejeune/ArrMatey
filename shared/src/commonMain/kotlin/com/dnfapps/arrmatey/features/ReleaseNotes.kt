package com.dnfapps.arrmatey.features

import com.dnfapps.arrmatey.shared.MR

object ReleaseNotes {

    val updates = listOf(
        FeatureUpdate(
            buildCode = 1,
            version = "0.0.4",
            title = MR.strings.v1_title,
            androidContentFile = MR.files.release_0_0_4_txt
        ),
        FeatureUpdate(
            buildCode = 2,
            version = "0.1.0",
            title = MR.strings.v2_title,
            androidContentFile = MR.files.release_0_1_0_txt
        ),
        FeatureUpdate(
            buildCode = 3,
            version = "0.3.0",
            title = MR.strings.v3_title,
            androidContentFile = MR.files.release_0_3_0_txt
        ),
        FeatureUpdate(
            buildCode = 4,
            version = "0.4.0",
            title = MR.strings.v4_title,
            androidContentFile = MR.files.release_0_4_0_txt,
            iosContentFile = MR.files.release_0_4_0_ios_txt
        ),
        FeatureUpdate(
            buildCode = 5,
            version = "0.4.2",
            title = MR.strings.v5_title,
            androidContentFile = MR.files.release_0_4_2_txt
        ),
        FeatureUpdate(
            buildCode = 8,
            version = "0.5.0",
            title = MR.strings.v6_title,
            androidContentFile = MR.files.release_0_5_0_txt
        ),
        FeatureUpdate(
            buildCode = 9,
            version = "0.6.0",
            title = MR.strings.v7_title,
            androidContentFile = MR.files.release_0_6_0_txt
        ),
        FeatureUpdate(
            buildCode = 10,
            version = "0.7.0",
            title = MR.strings.v8_title,
            androidContentFile = MR.files.release_0_7_0_txt
        )
    ).sortedByDescending { it.buildCode }

    val latestUpdate = updates.maxBy { it.buildCode }

}