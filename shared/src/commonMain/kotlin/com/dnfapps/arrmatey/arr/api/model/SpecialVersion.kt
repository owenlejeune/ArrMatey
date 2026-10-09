package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.special_version_auto
import com.dnfapps.arrmatey.shared.special_version_hardcover
import com.dnfapps.arrmatey.shared.special_version_normal
import com.dnfapps.arrmatey.shared.special_version_omnibus
import com.dnfapps.arrmatey.shared.special_version_oneshot
import com.dnfapps.arrmatey.shared.special_version_tpb
import com.dnfapps.arrmatey.shared.special_version_volume_as_issue
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SpecialVersion(val value: String, val resource: StringResource) {
    @SerialName("auto")
    Automatic("auto", MR.strings.special_version_auto),

    @SerialName("normal")
    NormalVolume("normal", MR.strings.special_version_normal),

    @SerialName("tpb")
    TradePaperback("tpb", MR.strings.special_version_tpb),

    @SerialName("oneshot")
    OneShot("oneshot", MR.strings.special_version_oneshot),

    @SerialName("hardcover")
    Hardcover("hardcover", MR.strings.special_version_hardcover),

    @SerialName("omnibus")
    Omnibus("omnibus", MR.strings.special_version_omnibus),

    @SerialName("volume_as_issue")
    VolumeAsIssue("volume_as_issue", MR.strings.special_version_volume_as_issue),
}
