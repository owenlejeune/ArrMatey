package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class BookshelfBlocklistItem(
    override val id: Long,
    override val sourceTitle: String? = null,
    override val quality: QualityInfo,
    @Contextual override val date: Instant,
    override val protocol: ReleaseProtocol? = null,
    override val indexer: String? = null,
    override val message: String? = null,
    override val customFormats: List<CustomFormat> = emptyList(),
    override val customFormatScore: Int? = 0,
    override val data: Map<String, String?> = emptyMap(),
    override val instanceId: Long? = null,
    override val instanceName: String? = null,
    override val instanceType: InstanceType? = null,
    val authorId: Long = 0,
    val bookId: Long = 0,
    val author: Author? = null,
    val book: Book? = null,
) : BlocklistItem {
    override val languages: List<Language>
        get() = emptyList()

    override val displayTitle: String?
        get() =
            when {
                book != null -> book.title
                else -> super.displayTitle
            }
}
