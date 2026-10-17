package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.model.SeerrAuthType
import com.dnfapps.arrmatey.seerr.api.client.SeerrClientImpl
import com.dnfapps.arrmatey.seerr.api.model.QuickConnectCheckResponse
import com.dnfapps.arrmatey.seerr.api.model.QuickConnectInitiateResponse
import com.dnfapps.arrmatey.seerr.api.model.SeerrAuthResult
import com.dnfapps.networking.NetworkResult

class SeerrAuthUseCase(
    private val httpClientFactory: HttpClientFactory,
) {
    private fun createClient(url: String, headers: List<InstanceHeader>): SeerrClientImpl {
        val tempInstance =
            Instance(
                type = InstanceType.Seerr,
                label = "SeerrAuth",
                url = url.trimEnd('/'),
                apiKey = EncryptedString(""),
                headers = headers,
                seerrAuthType = SeerrAuthType.Local,
            )
        val httpClient = httpClientFactory.create(tempInstance)
        return SeerrClientImpl(tempInstance, httpClient)
    }

    suspend fun loginLocal(
        url: String,
        headers: List<InstanceHeader>,
        email: String,
        password: String,
    ): NetworkResult<SeerrAuthResult> = createClient(url, headers).loginLocal(email, password)

    suspend fun loginPlex(
        url: String,
        headers: List<InstanceHeader>,
        authToken: String,
    ): NetworkResult<SeerrAuthResult> = createClient(url, headers).loginPlex(authToken)

    suspend fun loginJellyfin(
        url: String,
        headers: List<InstanceHeader>,
        username: String,
        password: String,
    ): NetworkResult<SeerrAuthResult> = createClient(url, headers).loginJellyfin(username, password)

    suspend fun initiateJellyfinQuickConnect(
        url: String,
        headers: List<InstanceHeader>,
    ): NetworkResult<QuickConnectInitiateResponse> = createClient(url, headers).initiateJellyfinQuickConnect()

    suspend fun checkJellyfinQuickConnect(
        url: String,
        headers: List<InstanceHeader>,
        secret: String,
    ): NetworkResult<QuickConnectCheckResponse> = createClient(url, headers).checkJellyfinQuickConnect(secret)

    suspend fun authenticateJellyfinQuickConnect(
        url: String,
        headers: List<InstanceHeader>,
        secret: String,
    ): NetworkResult<SeerrAuthResult> = createClient(url, headers).authenticateJellyfinQuickConnect(secret)
}
