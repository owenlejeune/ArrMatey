package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.instances.model.PlexPinResponse
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeGet
import com.dnfapps.networking.safePost
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import kotlin.random.Random

class PlexAuthUseCase(
    httpClientFactory: HttpClientFactory,
) {
    private val httpClient: HttpClient = httpClientFactory.createGeneric()

    fun generateClientIdentifier(): String {
        val chars = "0123456789abcdef"
        val sb = StringBuilder()
        repeat(32) { i ->
            if (i == 8 || i == 12 || i == 16 || i == 20) sb.append('-')
            if (i == 12) {
                sb.append('4')
            } else if (i == 16) {
                sb.append(chars[Random.nextInt(8, 12)])
            } else {
                sb.append(chars[Random.nextInt(16)])
            }
        }
        return sb.toString()
    }

    suspend fun createPin(clientIdentifier: String): NetworkResult<PlexPinResponse> = httpClient.safePost<PlexPinResponse>("https://plex.tv/api/v2/pins?strong=true") {
        header("Accept", "application/json")
        header("X-Plex-Product", "ArrMatey")
        header("X-Plex-Client-Identifier", clientIdentifier)
    }

    suspend fun checkPin(pinId: Long, pinCode: String, clientIdentifier: String): NetworkResult<PlexPinResponse> = httpClient.safeGet<PlexPinResponse>("https://plex.tv/api/v2/pins/$pinId?code=$pinCode") {
        header("Accept", "application/json")
        header("X-Plex-Client-Identifier", clientIdentifier)
    }

    fun getAuthUrl(clientIdentifier: String, code: String): String = "https://app.plex.tv/auth/#!?clientID=$clientIdentifier&code=$code&context%5Bdevice%5D%5Bproduct%5D=ArrMatey"
}
