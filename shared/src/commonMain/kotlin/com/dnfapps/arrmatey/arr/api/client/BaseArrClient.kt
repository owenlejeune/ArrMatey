package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.CommandResponse
import com.dnfapps.arrmatey.arr.api.model.DownloadReleasePayload
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.QueuePage
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.safeDelete
import com.dnfapps.arrmatey.client.safeGet
import com.dnfapps.arrmatey.client.safePost
import com.dnfapps.arrmatey.client.safePut
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.koin.core.component.KoinComponent

abstract class BaseArrClient(
    protected val httpClient: HttpClient
): KoinComponent {
    protected abstract val instance: Instance

    protected val baseUrl: String
        get() = "${instance.url}/${instance.type.apiBase}"

    open suspend fun getQualityProfiles(): NetworkResult<List<QualityProfile>> =
        get("qualityprofile")

    open suspend fun getRootFolders(): NetworkResult<List<RootFolder>> =
        get("rootfolder")

    open suspend fun getTags(): NetworkResult<List<Tag>> =
        get("tag")

    open suspend fun command(payload: CommandPayload): NetworkResult<CommandResponse> =
        post("command", payload)

    open suspend fun fetchActivityTasks(
        page: Int,
        pageSize: Int
    ): NetworkResult<QueuePage> =
        get<QueuePage>("queue", mapOf(
            "page" to page,
            "pageSize" to pageSize,
            "includeMovie" to true,
            "includeSeries" to true,
            "includeEpisode" to true,
            "includeAlbum" to true,
            "includeArtist" to true
        )).map { it.setInstance(instance.id, instance.label) }

    open suspend fun downloadRelease(
        payload: DownloadReleasePayload
    ): NetworkResult<Any> =
        post("release", payload)

    open suspend fun deleteActivityTask(
        id: Int,
        removeFromClient: Boolean,
        blocklist: Boolean,
        skipRedownload: Boolean
    ): NetworkResult<Unit> =
        delete("queue/$id", mapOf(
            "removeFromClient" to removeFromClient,
            "blocklist" to blocklist,
            "skipRedownload" to skipRedownload
        ))

    /**
     * Helpers
     */

    protected suspend inline fun <reified T> get(
        endpoint: String,
        params: Map<String, Any> = emptyMap()
    ): NetworkResult<T> =
        httpClient.safeGet<T>("$baseUrl/$endpoint") {
            url {
                params.forEach { (key, value) ->
                    parameters.append(key, value.toString())
                }
            }
        }.rebuild()

    protected suspend inline fun <reified T, reified R> post(
        endpoint: String,
        body: T
    ): NetworkResult<R> =
        httpClient.safePost<R>("$baseUrl/$endpoint") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.rebuild()

    protected suspend inline fun <reified T, reified R> put(
        endpoint: String,
        body: T
    ): NetworkResult<R> =
        httpClient.safePut<R>("$baseUrl/$endpoint") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.rebuild()

    protected suspend inline fun <reified T, reified R> delete(
        endpoint: String,
        body: T,
        params: Map<String, Any> = emptyMap(),
    ): NetworkResult<R> =
        httpClient.safeDelete("$baseUrl/$endpoint") {
            contentType(ContentType.Application.Json)
            url {
                params.forEach { (key, value) ->
                    parameters.append(key, value.toString())
                }
            }
            setBody(body)
        }

    protected suspend inline fun <reified T> delete(
        endpoint: String,
        params: Map<String, Any> = emptyMap()
    ): NetworkResult<T> =
        httpClient.safeDelete("$baseUrl/$endpoint") {
            url {
                params.forEach { (key, value) ->
                    parameters.append(key, value.toString())
                }
            }
        }

    @Suppress("UNCHECKED_CAST")
    protected fun <T> NetworkResult<T>.rebuild(): NetworkResult<T> {
        return this.map { data ->
            when (data) {
                is HasArrImages<*> -> data.withLocalImages(instance.url) as T
                is List<*> -> data.map { item ->
                    if (item is HasArrImages<*>) item.withLocalImages(instance.url) else item
                } as T
                else -> data
            }
        }
    }
}