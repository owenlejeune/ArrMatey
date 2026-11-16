package com.dnfapps.arrmatey.ui.helpers

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.intercept.Interceptor
import coil3.memory.MemoryCache
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.crossfade
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager

class ArrImageLoader(
    private val context: Context,
    private val instanceManager: InstanceManager,
    private val mediaServerManager: MediaServerManager,
) {
    private val apiKeyInterceptor = Interceptor { chain ->
        val request = chain.request
        val originalUrl = request.data.toString()

        val resolved = resolveImageRequest(originalUrl)
        if (resolved != null) {
            val newRequest = request.newBuilder()
                .data(resolved.url)
                .httpHeaders(resolved.headers)
                .build()
            return@Interceptor chain.withRequest(newRequest).proceed()
        }

        chain.proceed()
    }

    private data class ResolvedImageData(
        val url: String,
        val headers: NetworkHeaders,
    )

    private fun resolveImageRequest(originalUrl: String): ResolvedImageData? {
        return resolveInstanceRequest(originalUrl) ?: resolveMediaServerRequest(originalUrl)
    }

    private fun resolveInstanceRequest(originalUrl: String): ResolvedImageData? {
        val (instance, matchedPrefix) = findMatch(
            url = originalUrl,
            items = instanceManager.getAllRepositories().map { it.instance },
            getUrl = { it.url },
            getEffectiveUrl = { it.getEffectiveBaseUrl() },
            getLocalEndpoint = { it.localNetworkEndpoint },
        ) ?: return null

        val effectiveBase = instance.getEffectiveBaseUrl().trimEnd('/')
        val rewrittenUrl = rewriteUrl(originalUrl, matchedPrefix, effectiveBase)
        val headersBuilder = createHeadersBuilder(instance.headers)
        val apiKey = instance.apiKey.value

        val authenticatedUrl = when (instance.type) {
            InstanceType.Tracearr -> {
                headersBuilder["Authorization"] = "Bearer $apiKey"
                rewrittenUrl
            }
            InstanceType.Kapowarr -> appendQueryParamIfMissing(rewrittenUrl, "api_key", apiKey, "apikey")
            InstanceType.Tautulli -> appendQueryParamIfMissing(rewrittenUrl, "apikey", apiKey, "api_key")
            else -> {
                headersBuilder["X-Api-Key"] = apiKey
                appendQueryParamIfMissing(rewrittenUrl, "apikey", apiKey)
            }
        }

        return ResolvedImageData(authenticatedUrl, headersBuilder.build())
    }

    private fun resolveMediaServerRequest(originalUrl: String): ResolvedImageData? {
        val (integration, matchedPrefix) = findMatch(
            url = originalUrl,
            items = mediaServerManager.getAllIntegrations(),
            getUrl = { it.url },
            getEffectiveUrl = { it.getEffectiveBaseUrl() },
            getLocalEndpoint = { it.localNetworkEndpoint },
        ) ?: return null

        val effectiveBase = integration.getEffectiveBaseUrl().trimEnd('/')
        val rewrittenUrl = rewriteUrl(originalUrl, matchedPrefix, effectiveBase)
        val headersBuilder = createHeadersBuilder(integration.headers)
        val token = integration.token.value

        val authenticatedUrl = when (integration.serverType) {
            MediaServerType.Jellyfin, MediaServerType.Emby -> {
                if (token.isNotBlank()) {
                    headersBuilder["X-Emby-Token"] = token
                    headersBuilder["X-MediaBrowser-Token"] = token
                    appendQueryParamIfMissing(rewrittenUrl, "api_key", token, "apikey")
                } else {
                    rewrittenUrl
                }
            }
            MediaServerType.Plex -> {
                if (token.isNotBlank()) {
                    headersBuilder["X-Plex-Token"] = token
                    appendQueryParamIfMissing(rewrittenUrl, "X-Plex-Token", token)
                } else {
                    rewrittenUrl
                }
            }
        }

        return ResolvedImageData(authenticatedUrl, headersBuilder.build())
    }

    private fun <T> findMatch(
        url: String,
        items: List<T>,
        getUrl: (T) -> String,
        getEffectiveUrl: (T) -> String,
        getLocalEndpoint: (T) -> String?,
    ): Pair<T, String>? {
        for (item in items) {
            val candidateBases = listOfNotNull(
                getEffectiveUrl(item).trimEnd('/'),
                getUrl(item).trimEnd('/'),
                getLocalEndpoint(item)?.takeIf { it.isNotBlank() }?.trimEnd('/'),
            ).distinct()

            for (base in candidateBases) {
                if (matchesBaseUrl(url, base)) {
                    return item to base
                }
            }
        }
        return null
    }

    private fun matchesBaseUrl(url: String, base: String): Boolean {
        if (base.isEmpty() || !url.startsWith(base, ignoreCase = true)) return false
        val remainingIndex = base.length
        return remainingIndex == url.length || url[remainingIndex] == '/' || url[remainingIndex] == '?'
    }

    private fun rewriteUrl(url: String, matchedPrefix: String, effectiveBase: String): String {
        return if (matchedPrefix != effectiveBase) {
            effectiveBase + url.substring(matchedPrefix.length)
        } else {
            url
        }
    }

    private fun createHeadersBuilder(headers: List<InstanceHeader>): NetworkHeaders.Builder {
        val builder = NetworkHeaders.Builder().set("Accept", "image/*")
        headers.forEach { header ->
            if (header.key.isNotBlank() && header.value.isNotBlank()) {
                builder[header.key] = header.value
            }
        }
        return builder
    }

    private fun appendQueryParamIfMissing(
        url: String,
        paramName: String,
        paramValue: String,
        alternativeParamName: String? = null,
    ): String {
        val hasParam = url.contains("$paramName=") ||
            (alternativeParamName != null && url.contains("$alternativeParamName="))
        if (hasParam) return url
        val separator = if (url.contains("?")) "&" else "?"
        return "$url$separator$paramName=$paramValue"
    }

    val imageLoader: ImageLoader by lazy {
        ImageLoader
            .Builder(context)
            .components {
                add(apiKeyInterceptor)
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(150L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
