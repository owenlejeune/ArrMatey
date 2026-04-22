package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.arr.api.client.GenericClient
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType

class TestNewInstanceConnectionUseCase(
    private val client: GenericClient
) {
    suspend operator fun invoke(
        url: String,
        apiKey: String,
        type: InstanceType,
        headers: List<InstanceHeader> = emptyList(),
        basicAuthEnabled: Boolean = false
    ): Boolean =
        client.test(url.trim(), apiKey.trim(), type, headers, basicAuthEnabled)
}
