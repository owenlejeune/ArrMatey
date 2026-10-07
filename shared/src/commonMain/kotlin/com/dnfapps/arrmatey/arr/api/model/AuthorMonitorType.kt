package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.all_books
import com.dnfapps.arrmatey.shared.existing_books
import com.dnfapps.arrmatey.shared.first_book
import com.dnfapps.arrmatey.shared.future_books
import com.dnfapps.arrmatey.shared.latest_book
import com.dnfapps.arrmatey.shared.missing_books
import com.dnfapps.arrmatey.shared.new_books
import com.dnfapps.arrmatey.shared.none
import com.dnfapps.arrmatey.shared.unknown
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.SerialName

enum class AuthorMonitorType(
    val resource: StringResource,
) {
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
    None(MR.strings.none),
    ;

    companion object {
        val chaptarrOptions: List<AuthorMonitorType> = listOf(
            None,
            All,
            Future,
            Missing,
            Existing,
            FirstBook,
            LatestBook,
        )

        fun fromChaptarrIndex(index: Int?): AuthorMonitorType = index?.let { chaptarrOptions.getOrNull(it) } ?: None

        fun toChaptarrIndex(type: AuthorMonitorType): Int = chaptarrOptions.indexOf(type).takeIf { it >= 0 } ?: 0
    }
}
