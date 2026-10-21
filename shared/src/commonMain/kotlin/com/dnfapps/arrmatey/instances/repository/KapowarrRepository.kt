package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.KapowarrClient
import com.dnfapps.arrmatey.arr.api.model.WantedMediaItem
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.asSuccess
import dev.shivathapaa.logger.api.Logger
import io.ktor.client.HttpClient

class KapowarrRepository(
    instance: Instance,
    httpClient: HttpClient,
    logger: Logger,
    mokoStrings: MokoStrings,
) : ArrInstanceRepository(instance, httpClient, logger, mokoStrings) {

    val kapowarrClient: KapowarrClient = client as? KapowarrClient ?: KapowarrClient(instance, httpClient)

    override suspend fun refreshMissing(
        page: Int,
        pageSize: Int,
    ): NetworkResult<List<WantedMediaItem>> {
        val mediaList = libraryRepository.library.value?.asSuccess()?.data ?: emptyList()
        val missingMedia = mediaList.filter { it.isMissing }
        val wantedItems = missingMedia.map { WantedMediaItem(it, instance.id, instance.label, instance.type) }
        libraryRepository.updateMissingCache(wantedItems)
        return NetworkResult.Success(wantedItems)
    }

    override suspend fun refreshCutoff(
        page: Int,
        pageSize: Int,
    ): NetworkResult<List<WantedMediaItem>> {
        val empty = emptyList<WantedMediaItem>()
        libraryRepository.updateCutoffCache(empty)
        return NetworkResult.Success(empty)
    }
}
