package com.dnfapps.arrmatey.ui.helpers

import android.content.Context
import coil3.ImageLoader
import coil3.intercept.Interceptor
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.dnfapps.arrmatey.instances.repository.InstanceManager

class ArrImageLoader(
    private val context: Context,
    private val instanceManager: InstanceManager
) {
    private val apiKeyInterceptor = Interceptor { chain ->
        val request = chain.request
        val url = request.data.toString()

        val repository = instanceManager.getAllRepositories().find { repository ->
            url.startsWith(repository.instance.url)
        }
        val instance = repository?.instance

        val newRequest = if (instance != null && !url.contains("apikey=")) {
            val separator = if (url.contains("?")) "&" else "?"
            val authenticatedUrl = "$url${separator}apikey=${instance.apiKey.value}"

            request.newBuilder()
                .data(authenticatedUrl)
                .httpHeaders(NetworkHeaders.Builder()
                    .set("X-Api-Key", instance.apiKey.value)
                    .set("Accept", "image/*")
                    .build())
                .build()
        } else {
            request
        }

        val result = chain
            .withRequest(newRequest)
            .proceed()
        result
    }

    val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(context)
            .components {
                add(apiKeyInterceptor)
            }
            .crossfade(true)
            .build()
    }
}