package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.KapowarrClient
import com.dnfapps.arrmatey.instances.model.Instance
import dev.shivathapaa.logger.api.Logger
import io.ktor.client.HttpClient

class KapowarrRepository(
    instance: Instance,
    httpClient: HttpClient,
    logger: Logger,
) : ArrInstanceRepository(instance, httpClient, logger) {

    val kapowarrClient: KapowarrClient = client as? KapowarrClient ?: KapowarrClient(instance, httpClient)
}
