package com.dnfapps.arrmatey.compose.utils

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

@Serializable
enum class BookMediaFilterBy(
    val resource: StringResource,
) {
    All(MR.strings.all),
    EBook(MR.strings.ebooks),
    Audiobook(MR.strings.audiobooks),
}
