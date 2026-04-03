package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.SerialName

enum class AuthorMonitorType(val resource: StringResource) {
    @SerialName("unknown")
    Unknown(MR.strings.unknown),

    @SerialName("all")
    All(MR.strings.all_books),

    @SerialName("future")
    Future(MR.strings.future_books),

    @SerialName("missing")
    Missing(MR.strings.missing_books),

    @SerialName("existing")
    Existing(MR.strings.existing_books),

    @SerialName("first")
    FirstBook(MR.strings.first_book),

    @SerialName("latest")
    LatestBook(MR.strings.latest_book),

    @SerialName("new")
    New(MR.strings.new_books),

    @SerialName("none")
    None(MR.strings.none)
}