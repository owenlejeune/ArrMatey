package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.compose.utils.FilterBy
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.extensions.orderedSortedWith
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.time.Instant

class GetLibraryUseCase(
    private val instanceManager: InstanceManager,
    private val preferencesStoreRepository: InstancePreferenceStoreRepository
) {
    operator fun invoke(instanceId: Long): Flow<ArrLibrary> = flow {
        val repository = instanceManager.getArrRepository(instanceId)
        if (repository == null) {
            emit(ArrLibrary.Error("Instance not found", ErrorType.Unexpected))
            return@flow
        }
        val preferencesRepository = preferencesStoreRepository.getInstancePreferences(instanceId)


        if (repository.library.value == null) {
            emit(ArrLibrary.Loading)
            coroutineScope {
                launch {
                        repository.refreshLibrary()
                }
            }
        }

        combine(
            repository.library,
            preferencesRepository.observePreferences()
        ) { libraryResult, preferences ->
            when (libraryResult) {
                is NetworkResult.Loading -> ArrLibrary.Loading
                is NetworkResult.Error -> ArrLibrary.Error(libraryResult.message ?: "")
                is NetworkResult.Success -> {
                    val sorted = applySorting(libraryResult.data, preferences)
                    val filtered = applyFiltering(sorted, preferences)
                    ArrLibrary.Success(filtered, preferences)
                }
                null -> ArrLibrary.Initial
            }
        }.collect { emit(it) }
    }

    private fun applySorting(
        items: List<ArrMedia>,
        preferences: InstancePreferences
    ): List<ArrMedia> {
        val comparator: Comparator<ArrMedia> = when (preferences.sortBy) {
            SortBy.Title -> compareBy { it.sortTitle }
            SortBy.Year -> compareBy { it.year }
            SortBy.Added -> compareBy { it.added }
            SortBy.Rating -> compareBy { it.ratingScore() }
            SortBy.FileSize -> compareBy { it.fileSize }
            SortBy.NextAiring -> compareBy { (it as? ArrSeries)?.nextAiring ?: Instant.DISTANT_FUTURE }
            SortBy.PreviousAiring -> compareBy { (it as? ArrSeries)?.previousAiring ?: Instant.DISTANT_PAST }
            SortBy.Grabbed -> compareBy { (it as? ArrMovie)?.grabbed ?: Instant.DISTANT_PAST }
            SortBy.DigitalRelease -> compareBy { (it as? ArrMovie)?.digitalRelease ?: Instant.DISTANT_PAST }
            else -> compareBy { it.sortTitle } // should never happen for library sorting
        }

        return items.orderedSortedWith(preferences.sortOrder, comparator)
    }

    private fun applyFiltering(
        items: List<ArrMedia>,
        preferences: InstancePreferences
    ): List<ArrMedia> = when (preferences.filterBy) {
        FilterBy.All -> items
        FilterBy.Monitored -> items.filter { it.monitored }
        FilterBy.Unmonitored -> items.filterNot { it.monitored }
        FilterBy.Missing -> items.filter { it.isMissing }
        FilterBy.Wanted -> items.filter { it.isWanted }
        FilterBy.Downloaded -> items.filter { it.isDownloaded }
        FilterBy.EndedOnly -> items.filter { it.isEnded }
        FilterBy.ContinuingOnly -> items.filter { it.isContinuing }
    }
}
