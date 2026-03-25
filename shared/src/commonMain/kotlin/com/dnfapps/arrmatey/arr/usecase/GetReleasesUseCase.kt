package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrRelease
import com.dnfapps.arrmatey.arr.api.model.CustomFormat
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.QualityInfo
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.api.model.ReleaseProtocol
import com.dnfapps.arrmatey.arr.state.ReleaseLibrary
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class GetReleasesUseCase(
    private val instanceManager: InstanceManager
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(type: InstanceType): Flow<ReleaseLibrary> =
        instanceManager.getSelectedArrRepository(type)
            .filterNotNull()
            .flatMapLatest { repository ->
                repository.releases.map { result ->
                    when (result) {
                        null -> ReleaseLibrary.Initial
                        is NetworkResult.Loading -> ReleaseLibrary.Loading
                        is NetworkResult.Error ->
                            ReleaseLibrary.Error(
                                message = result.message ?: "",
                                type = result.errorType
                            )

                        is NetworkResult.Success ->
                            ReleaseLibrary.Success(
                                items = result.data,
                                filterLanguages = parseLanguages(result.data),
                                filterIndexers = parseIndexers(result.data),
                                filterProtocols = parseProtocols(result.data),
                                filterQualities = parseQualities(result.data),
                                filterCustomFormats = parseCustomFormats(result.data)
                            )

                    }
                }
            }

    private fun parseLanguages(items: List<ArrRelease>): Set<Language> {
        return items
            .flatMap { it.languages.filter { l -> l.name != null } }
            .sortedBy { it.name }
            .toSet()
    }

    private fun parseProtocols(items: List<ArrRelease>): Set<ReleaseProtocol> {
        return items
            .map { it.protocol }
            .sortedBy { it.name }
            .toSet()
    }

    private fun parseQualities(items: List<ArrRelease>): Set<QualityInfo> {
        return items
            .mapNotNull { it.quality }
            .sortedBy { it.quality.resolution }
            .toSet()
    }

    private fun parseIndexers(items: List<ArrRelease>): Set<String> {
        return items
            .map { it.indexerLabel }
            .sorted()
            .toSet()
    }

    private fun parseCustomFormats(items: List<ArrRelease>): Set<CustomFormat> {
        return items
            .flatMap { it.customFormats }
            .sortedBy { it.name }
            .toSet()
    }

    suspend fun fetch(type: InstanceType, params: ReleaseParams) {
        instanceManager.getSelectedArrRepository(type)
            .firstOrNull()
            ?.getReleases(params)
    }

    suspend fun clear(type: InstanceType) {
        instanceManager.getSelectedArrRepository(type)
            .firstOrNull()
            ?.clearReleases()
    }
}