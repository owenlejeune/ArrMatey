package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.CustomFilter
import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.arrmatey.arr.state.HttpErrorType
import com.dnfapps.arrmatey.compose.utils.FilterBy
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.extensions.orderedSortedWith
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.networking.NetworkResult
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlin.time.Instant

class GetLibraryUseCase(
    private val instanceManager: InstanceManager,
    private val preferencesStoreRepository: InstancePreferenceStoreRepository,
    private val applyCustomFilterItemUseCase: ApplyCustomFilterItemUseCase,
    private val logger: Logger,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun byType(
        instanceType: InstanceType,
        searchQuery: Flow<String>? = null,
    ): Flow<ArrLibrary> =
        instanceManager
            .getSelectedArrRepository(instanceType)
            .filterNotNull()
            .flatMapLatest {
                invoke(it.instance.id, searchQuery)
            }

    operator fun invoke(
        instanceId: Long,
        searchQuery: Flow<String>? = null,
    ): Flow<ArrLibrary> =
        flow {
            val repository = instanceManager.getArrRepository(instanceId)
            if (repository == null) {
                logger.error { "Instance not found: $instanceId" }
                emit(ArrLibrary.Error("Instance not found", HttpErrorType.Unexpected))
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

            val preferencesFlow =
                preferencesRepository
                    .observePreferences()
                    .distinctUntilChanged { old, new ->
                        old.sortBy == new.sortBy &&
                            old.sortOrder == new.sortOrder &&
                            old.filterBy == new.filterBy &&
                            old.customFilterId == new.customFilterId
                    }

            val queryFlow = searchQuery ?: flowOf("")

            combine(
                repository.library,
                preferencesFlow,
                repository.customFilters,
                queryFlow,
            ) { libraryResult, preferences, customFilters, query ->
                when (libraryResult) {
                    is NetworkResult.Loading -> ArrLibrary.Loading
                    is NetworkResult.Error -> ArrLibrary.Error(libraryResult.message ?: "")
                    is NetworkResult.Success<*> -> {
                        val sorted = applySorting(libraryResult.data as List<ArrMedia>, preferences)
                        val filtered = applyFiltering(sorted, preferences, customFilters)
                        val searched =
                            if (query.isNotBlank()) {
                                filtered.filter { it.title?.contains(query, ignoreCase = true) == true }
                            } else {
                                filtered
                            }
                        ArrLibrary.Success(searched, preferences)
                    }
                    null -> ArrLibrary.Initial
                }
            }.flowOn(Dispatchers.Default)
                .collect { emit(it) }
        }

    private fun applySorting(
        items: List<ArrMedia>,
        preferences: InstancePreferences,
    ): List<ArrMedia> {
        val comparator: Comparator<ArrMedia> =
            when (preferences.sortBy) {
                SortBy.Title -> compareBy { it.sortTitle }
                SortBy.Name -> compareBy { it.sortTitle }
                SortBy.TitleLastFirst -> compareBy { (it as? Author)?.sortNameLastFirst }
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
        preferences: InstancePreferences,
        customFilters: List<CustomFilter>,
    ): List<ArrMedia> {
        val filtered =
            when (preferences.filterBy) {
                FilterBy.All -> items
                FilterBy.Monitored -> items.filter { it.monitored }
                FilterBy.Unmonitored -> items.filterNot { it.monitored }
                FilterBy.Missing -> items.filter { it.isMissing }
                FilterBy.Wanted -> items.filter { it.isWanted }
                FilterBy.Downloaded -> items.filter { it.isDownloaded }
                FilterBy.EndedOnly -> items.filter { it.isEnded }
                FilterBy.ContinuingOnly -> items.filter { it.isContinuing }
            }

        val customFilter = customFilters.find { it.id == preferences.customFilterId }

        return if (customFilter != null) {
            items.filter { item ->
                customFilter.filters.all { filterItem ->
                    val itemValue: Any? =
                        when (filterItem.key) {
                            "monitored" -> item.monitored
                            "qualityProfile", "qualityProfileId" -> item.qualityProfileId
                            "status" -> item.status.name.lowercase()
                            "rootFolderPath" -> item.rootFolderPath
                            "path" -> item.path
                            "title" -> item.title
                            "year" -> item.year
                            "certification" -> item.certification
                            "added", "addedDate" -> item.added
                            "genres" -> item.genres
                            "tags" -> item.tags
                            "sizeOnDisk" -> item.fileSize
                            "runtime" -> item.runtime
                            "originalLanguage", "languages", "language" -> item.originalLanguage?.id
                            "monitoredStatus" -> {
                                when {
                                    !item.monitored -> "none"
                                    item.statusProgress >= 1.0f -> "all"
                                    else -> "partial"
                                }
                            }
                            "series", "seriesId" -> (item as? ArrSeries)?.id
                            "movie", "movieId" -> (item as? ArrMovie)?.id
                            "artist", "artistId" -> (item as? Arrtist)?.id
                            "author", "authorId" -> (item as? Author)?.id
                            "minimumAvailability" -> (item as? ArrMovie)?.minimumAvailability?.name?.lowercase()
                            "secondaryYear" -> (item as? ArrMovie)?.secondaryYear
                            "studio" -> (item as? ArrMovie)?.studio
                            "hasFile" -> (item as? ArrMovie)?.hasFile
                            "inCinemas" -> (item as? ArrMovie)?.inCinemas
                            "physicalRelease" -> (item as? ArrMovie)?.physicalRelease
                            "digitalRelease" -> (item as? ArrMovie)?.digitalRelease
                            "releaseDate" -> (item as? ArrMovie)?.releaseDate
                            "seriesType" -> (item as? ArrSeries)?.seriesType?.name?.lowercase()
                            "network" -> (item as? ArrSeries)?.network
                            "ended" -> (item as? ArrSeries)?.ended
                            "nextAiring" -> (item as? ArrSeries)?.nextAiring
                            "previousAiring" -> (item as? ArrSeries)?.previousAiring
                            "monitorNewItems" ->
                                when (item) {
                                    is ArrSeries -> item.monitorNewItems.name.lowercase()
                                    is Arrtist -> item.monitorNewItems.name.lowercase()
                                    is Author -> item.monitorNewItems.name.lowercase()
                                    else -> null
                                }
                            "artistType" -> (item as? Arrtist)?.artistType
                            "metadataProfileId" -> (item as? Arrtist)?.metadataProfileId ?: (item as? Author)?.metadataProfileId
                            "authors" -> (item as? Audiobook)?.authors
                            "narrators" -> (item as? Audiobook)?.narrators
                            "publishYear" -> (item as? Audiobook)?.publishYear
                            "publisher" -> (item as? Audiobook)?.publisher
                            "series" -> (item as? Audiobook)?.series
                            "asin" -> (item as? Audiobook)?.asin
                            "subtitle" -> (item as? Audiobook)?.subtitle
                            "edition" -> (item as? Audiobook)?.edition
                            "abridged" -> (item as? Audiobook)?.abridged
                            "explicit" -> (item as? Audiobook)?.explicit
                            else -> null
                        }
                    applyCustomFilterItemUseCase(itemValue, filterItem)
                }
            }
        } else {
            filtered
        }
    }
}
