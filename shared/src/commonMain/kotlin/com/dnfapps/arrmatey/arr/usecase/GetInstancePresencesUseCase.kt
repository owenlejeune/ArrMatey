package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.model.InstanceMediaPresence
import com.dnfapps.networking.NetworkResult

class GetInstancePresencesUseCase {
    suspend fun fetchMissingPresences(
        repositories: List<ArrInstanceRepository>,
        query: String?,
        resolvedTvdbLookupId: Long?,
        resolvedLookupId: Long?,
        existingPresences: Map<Long, ArrMedia?>,
    ): Map<Long, ArrMedia?> {
        if (repositories.isEmpty() || query.isNullOrBlank()) return existingPresences

        val missingRepos =
            repositories.filter { repo ->
                !existingPresences.containsKey(repo.instance.id)
            }
        if (missingRepos.isEmpty()) return existingPresences

        val updatedMap = existingPresences.toMutableMap()
        missingRepos.forEach { repo ->
            val lookupRes = repo.directLookup(query)
            val list = (lookupRes as? NetworkResult.Success)?.data ?: emptyList()
            val match =
                list.firstOrNull { media ->
                    when (media) {
                        is ArrSeries -> {
                            (resolvedTvdbLookupId != null && resolvedTvdbLookupId > 0 && media.tvdbId == resolvedTvdbLookupId) ||
                                (resolvedLookupId != null && resolvedLookupId > 0 && media.tmdbId == resolvedLookupId)
                        }
                        is ArrMovie -> {
                            resolvedLookupId != null && resolvedLookupId > 0 && media.tmdbId == resolvedLookupId
                        }
                        else -> false
                    }
                } ?: if (resolvedTvdbLookupId == null && resolvedLookupId == null) list.firstOrNull() else null
            updatedMap[repo.instance.id] = match
        }
        return updatedMap
    }

    fun buildPresencesList(
        repositories: List<ArrInstanceRepository>,
        activeRepoId: Long?,
        activeArrMedia: ArrMedia?,
        presencesMap: Map<Long, ArrMedia?>,
    ): List<InstanceMediaPresence> =
        buildPresencesListFromInstances(
            instances = repositories.map { it.instance },
            activeRepoId = activeRepoId,
            activeArrMedia = activeArrMedia,
            presencesMap = presencesMap,
        )

    fun buildPresencesListFromInstances(
        instances: List<Instance>,
        activeRepoId: Long?,
        activeArrMedia: ArrMedia?,
        presencesMap: Map<Long, ArrMedia?>,
    ): List<InstanceMediaPresence> {
        val hasActiveArrId = activeArrMedia?.let { it.id != null && it.id != 0L } ?: false
        return instances.map { instance ->
            val media =
                if (instance.id == activeRepoId && hasActiveArrId) {
                    activeArrMedia
                } else {
                    presencesMap[instance.id]
                }
            InstanceMediaPresence(
                instance = instance,
                arrMedia = media,
            )
        }
    }
}
