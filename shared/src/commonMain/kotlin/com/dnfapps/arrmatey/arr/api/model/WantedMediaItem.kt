package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class WantedMediaItem(
    val media: ArrMedia? = null,
    val album: ArrAlbum? = null,
    val book: Book? = null,
    val instanceId: Long?,
    val instanceName: String?,
    val instanceType: InstanceType?,
) {
    constructor(media: ArrMedia, instanceId: Long?, instanceName: String?, instanceType: InstanceType?) : this(
        media = media,
        album = null,
        book = null,
        instanceId = instanceId,
        instanceName = instanceName,
        instanceType = instanceType,
    )

    constructor(album: ArrAlbum, instanceId: Long?, instanceName: String?, instanceType: InstanceType?) : this(
        media = null,
        album = album,
        book = null,
        instanceId = instanceId,
        instanceName = instanceName,
        instanceType = instanceType,
    )

    constructor(book: Book, instanceId: Long?, instanceName: String?, instanceType: InstanceType?) : this(
        media = null,
        album = null,
        book = book,
        instanceId = instanceId,
        instanceName = instanceName,
        instanceType = instanceType,
    )

    val id: Long? get() = media?.id ?: album?.id ?: book?.id
    val title: String? get() = media?.title ?: album?.title ?: book?.title
    val year: Int? get() = media?.year ?: try {
        album?.releaseDate?.toLocalDateTime(TimeZone.UTC)?.year
    } catch (_: Exception) {
        null
    } ?: try {
        book?.releaseDate?.toLocalDateTime(TimeZone.UTC)?.year
    } catch (_: Exception) {
        null
    }
    val images: List<ArrImage> get() = media?.images ?: album?.images ?: book?.images ?: emptyList()
    val type: InstanceType? get() = instanceType
}
