package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.Serializable

@Serializable
data class ArrImage(
    val coverType: CoverType,
    val url: String? = null,
    val remoteUrl: String? = null,
) {
    fun rebuildWithLocalUrls(instance: Instance): ArrImage {
        val localUrl = url?.takeIf { it.startsWith("/") }
        val publicUrl = remoteUrl?.takeIf { it.startsWith("http", ignoreCase = true) }
        val apiUrl = localUrl?.let { mediaCoverApiUrl(instance, it) }
        // Arrs only append ?lastWrite= / ?h= once the cover file exists on the server.
        val coverDownloaded = localUrl?.contains('?') == true
        return when {
            apiUrl != null && (publicUrl == null || coverDownloaded) -> copy(remoteUrl = apiUrl)
            publicUrl != null -> this
            localUrl != null -> copy(remoteUrl = joinInstanceUrl(instance.url, localUrl))
            remoteUrl?.startsWith("/") == true -> copy(remoteUrl = joinInstanceUrl(instance.url, remoteUrl))
            else -> this
        }
    }
}

private val mediaCoverPath = Regex("""/MediaCover/(?:(Albums|Books)/)?(\d+)/([^/?]+)(?:\?(.*))?$""", RegexOption.IGNORE_CASE)

// /MediaCover/ only accepts a UI login session; the API mediacover route accepts the API key header.
private fun mediaCoverApiUrl(
    instance: Instance,
    localUrl: String,
): String? {
    val (subFolder, id, file, query) = mediaCoverPath.find(localUrl)?.destructured ?: return null
    val entity =
        when (instance.type) {
            InstanceType.Sonarr, InstanceType.Radarr -> if (subFolder.isEmpty()) "" else return null
            InstanceType.Lidarr ->
                when {
                    subFolder.isEmpty() -> "artist/"
                    subFolder.equals("Albums", ignoreCase = true) -> "album/"
                    else -> return null
                }
            InstanceType.Bookshelf, InstanceType.Chaptarr ->
                when {
                    subFolder.isEmpty() -> "author/"
                    subFolder.equals("Books", ignoreCase = true) -> "book/"
                    else -> return null
                }
            else -> return null
        }
    val path = "${instance.url.trimEnd('/')}/${instance.type.apiBase}/mediacover/$entity$id/$file"
    return if (query.isEmpty()) path else "$path?$query"
}

// Local paths from Arr/Bazarr already include the server's URL base (e.g. /sonarr/MediaCover/...).
internal fun joinInstanceUrl(
    instanceUrl: String,
    path: String,
): String {
    val base = instanceUrl.trimEnd('/')
    val basePath = base.substringAfter("://").substringAfter('/', "").let { if (it.isEmpty()) "" else "/$it" }
    val pathIncludesBase =
        basePath.isNotEmpty() &&
            (path.equals(basePath, ignoreCase = true) || path.startsWith("$basePath/", ignoreCase = true))
    return if (pathIncludesBase) base.dropLast(basePath.length) + path else base + path
}
