package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class BookshelfHistoryItem(
    override val id: Long,
    override val eventType: HistoryEventType,
    @Contextual override val date: Instant,
    override val sourceTitle: String? = null,
    override val quality: QualityInfo,
    override val customFormats: List<CustomFormat> = emptyList(),
    override val customFormatScore: Int? = 0,
    override val data: Map<String, String?> = emptyMap(),

    val authorId: Long = 0,
    val bookId: Long = 0,
    val author: Author? = null,
    val book: Book? = null
): HistoryItem {
    override val languages: List<Language>
        get() = emptyList()

    override val displayTitle: String?
        get() = when {
            book != null -> book.title
            else -> super.displayTitle
        }
}